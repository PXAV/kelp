package de.pxav.kelp.core.event.listener;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.pxav.kelp.core.logger.KelpLogger;
import de.pxav.kelp.core.logger.LogLevel;
import de.pxav.kelp.core.player.KelpPlayer;
import de.pxav.kelp.core.reflect.MethodCriterion;
import de.pxav.kelp.core.reflect.MethodFinder;
import de.pxav.kelp.core.reflect.TypeFinder;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * A class description goes here.
 *
 * @author pxav
 */
@Singleton
public class KelpEventRepository {

  private TypeFinder typeFinder;
  private MethodFinder methodFinder;
  private JavaPlugin javaPlugin;
  private Injector injector;
  private KelpLogger logger;

  private Map<UUID, KelpListener<?>> kelpListeners;
  private Map<UUID, Integer> timesCalled;

  @Inject
  public KelpEventRepository(TypeFinder typeFinder, MethodFinder methodFinder, JavaPlugin javaPlugin, Injector injector, KelpLogger logger) {
    this.typeFinder = typeFinder;
    this.methodFinder = methodFinder;
    this.javaPlugin = javaPlugin;
    this.injector = injector;
    this.logger = logger;

    this.kelpListeners = Maps.newHashMap();
    this.timesCalled = Maps.newHashMap();
  }

  public void detectSubscriptions(String... packageNames) {
    methodFinder.filter(packageNames, MethodCriterion.annotatedWith(Subscribes.class)).forEach(current -> {

      Subscribes annotation = current.getAnnotation(Subscribes.class);
      for (Class<? extends Event> eventType : annotation.value()) {
        Bukkit.getPluginManager()
          .registerEvent(
            (eventType),
            new Listener() {},
            EventPriority.NORMAL,
            (listener, event) -> {
              try {
                if (current.getParameters().length == 0) {
                  current.invoke(injector.getInstance(current.getDeclaringClass()));
                } else if (current.getParameters().length == 1 && current.getParameterTypes()[0].isAssignableFrom(KelpPlayer.class)) {
                  // invoke with event player as parameter
                }

              } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
              }
            },
            javaPlugin,
            false);
      }

      logger.log(LogLevel.DEBUG, "Successfully registered event @subscription for '" + current.getName() + "'");
    });
  }

  UUID addListener(KelpListener<?> kelpListener) {
    UUID uuid = UUID.randomUUID();

    Listener listenerInstance = new Listener() {};
    kelpListener.setBukkitListener(listenerInstance);

    Bukkit.getPluginManager()
      .registerEvent(
        (kelpListener.getEventClass()),
        listenerInstance,
        EventPriority.NORMAL,
        (listener, event) -> {

          boolean expire = kelpListener.testConditions(event, ConditionalExpiryTestStage.BEFORE_HANDLER);

          if (expire) {
            removeListener(uuid);
            return;
          }

          kelpListener.triggerHandler(event);

          expire = kelpListener.testConditions(event, ConditionalExpiryTestStage.AFTER_HANDLER);

          if (expire) {
            removeListener(uuid);
            return;
          }

          int timesCalled = this.timesCalled.getOrDefault(uuid, 0);
          if ((timesCalled + 1) >= kelpListener.getMaxExecutions() && kelpListener.getMaxExecutions() != -1) {
            removeListener(uuid);
            return;
          }

          this.timesCalled.put(uuid, timesCalled + 1);

        },
        javaPlugin,
        false);

    this.kelpListeners.put(uuid, kelpListener);
    return uuid;
  }

  public void removeListener(UUID listenerId) {
    if (!this.kelpListeners.containsKey(listenerId)) {
      logger.log(LogLevel.ERROR, "Cannot remove non-existing listener (id: " + listenerId + ")");
      return;
    }

    KelpListener kelpListener = this.kelpListeners.get(listenerId);
    try {
        Method method = kelpListener.getEventClass().getMethod("getHandlerList");
        HandlerList handlerList = (HandlerList) method.invoke(null);
        handlerList.unregister(kelpListener.getBukkitListener());
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
    this.kelpListeners.remove(listenerId);
  }

}
