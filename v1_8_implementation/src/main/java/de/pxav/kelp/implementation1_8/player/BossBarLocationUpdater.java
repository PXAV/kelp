package de.pxav.kelp.implementation1_8.player;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.pxav.kelp.core.scheduler.type.SchedulerFactory;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class handles the boss bar updating service. In 1.8, boss bars
 * are officially not supported by the spigot API so they have to be created
 * manually by spawning bosses. A wither is spawned far enough to make the smoke
 * particles invisible, but close enough to see the boss bar. If the player
 * changes their location or direction in which they look, the location has to
 * be changed as well.
 *
 * @author pxav
 */
@Singleton
public class BossBarLocationUpdater {

  private ConcurrentMap<UUID, Integer> bossBarEntities = Maps.newConcurrentMap();
  private ConcurrentMap<UUID, Float> bossBarHealth = Maps.newConcurrentMap();
  private ConcurrentMap<UUID, String> bossBarMessages = Maps.newConcurrentMap();

  private SchedulerFactory schedulerFactory;

  @Inject
  public BossBarLocationUpdater(SchedulerFactory schedulerFactory) {
    this.schedulerFactory = schedulerFactory;
  }

  /**
   * Despawns the boss entity for the given player and removes it from the list
   * in order to save performance.
   *
   * @param player The player to remove from the cache.
   */
  public void remove(UUID player) {
    if (bossBarEntities.containsKey(player)) {
      // send the destroy packet to despawn the entity on the client side
      CraftPlayer craftPlayer = (CraftPlayer) Bukkit.getPlayer(player);
      PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(bossBarEntities.get(player));
      craftPlayer.getHandle().playerConnection.sendPacket(destroyPacket);
    }

    this.bossBarEntities.remove(player);
    this.bossBarMessages.remove(player);
    this.bossBarHealth.remove(player);
  }

  /**
   * Adds the given player and its boss bar entity to the cache.
   * This does not spawn a new boss bar entity for the player.
   *
   * @param player      The player whose entity should be added.
   * @param entityId    The minecraft internal id of the entity (retrievable
   *                    with {@link Entity#getId()})
   * @param message     The boss bar message (display name of the entity. )
   */
  public void add(UUID player, int entityId, float health, String message) {
    this.bossBarEntities.put(player, entityId);
    this.bossBarMessages.put(player, message);
    this.bossBarHealth.put(player, health);
  }

  /**
   * Sets the health of the player's boss bar entity with the next
   * update performed by the move listener.
   *
   * @param player The player whose entity health you want to set.
   * @param health The actual health to display (min 0f, max 300f)
   */
  public void setHealth(UUID player, float health) {
    this.bossBarHealth.put(player, health);
  }

  /**
   * The boss bar is updated every time the player moves. A scheduler is
   * not the best solution here as it is not teleport-safe. If a player teleports to the
   * location where the wither is located they will see the smoke effects. If the player
   * looks into the ground, the wither might disappear.
   *
   * So the move event seems to be the best solution available as it immediately
   * updates when the player moves, although there might be some performance issues.
   *
   * @param event The event to listen for.
   */
  @EventHandler
  public void handlePlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    if (!bossBarEntities.containsKey(player.getUniqueId())) {
      return;
    }

    CraftPlayer craftPlayer = (CraftPlayer) player;
    Vector direction = craftPlayer.getLocation().getDirection();
    Location location = craftPlayer.getLocation().add(direction.multiply(40));
    String message = this.bossBarMessages.getOrDefault(player.getUniqueId(), "Custom Boss Bar Message");
    final float health = this.bossBarHealth.get(player.getUniqueId());

    EntityWither entityWither = new EntityWither(craftPlayer.getHandle().getWorld());
    entityWither.setInvisible(true);
    entityWither.setCustomName(message);
    entityWither.setCustomNameVisible(false);
    entityWither.setHealth(health);

    // check all blocks between the player and the boss bar entity. If there are too
    // many blocks, minecraft automatically hides the boss bar again, so the boss bar
    // entity has to be moved nearer to the player in those cases to still be visible.
    BlockIterator blockIterator = new BlockIterator(craftPlayer.getLocation(), craftPlayer.getEyeHeight(),30);
    while (blockIterator.hasNext()) {
      Block block = blockIterator.next();

      // ignore all blocks of type air as they do not affect the behaviour
      if (block.getType() == Material.AIR) {
        continue;
      }

      // if there is a block in between, move the entity close enough to be still visible.
      location = block.getLocation().add(craftPlayer.getLocation().getDirection().multiply(20));
      break;
    }

    entityWither.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);

    // spawn the entity to the player and add it to the cache
    PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(entityWither);

    this.remove(player.getUniqueId());
    this.add(player.getUniqueId(), entityWither.getId(), health, message);

    craftPlayer.getHandle().playerConnection.sendPacket(spawnPacket);
  }

  /**
   * Despawns the boss bar entity and removes the player from the list
   * when they quit to save server performance.
   *
   * @param event The event to listen for.
   */
  @EventHandler
  public void handlePlayerQuit(PlayerQuitEvent event) {
    this.remove(event.getPlayer().getUniqueId());
  }

}
