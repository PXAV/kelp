package de.pxav.kelp.core.entity.type;

import de.pxav.kelp.core.KelpPlugin;
import de.pxav.kelp.core.entity.KelpEntity;
import de.pxav.kelp.core.entity.KelpEntityType;
import de.pxav.kelp.core.entity.type.general.AbstractHorseEntity;
import de.pxav.kelp.core.entity.util.HorseColor;
import de.pxav.kelp.core.entity.util.HorseStyle;
import de.pxav.kelp.core.entity.version.EntityTypeVersionTemplate;
import de.pxav.kelp.core.inventory.type.HorseInventory;
import de.pxav.kelp.core.world.KelpLocation;
import org.bukkit.entity.Entity;

public interface HorseEntity extends AbstractHorseEntity<HorseEntity> {

  /**
   * Creates a new entity of this type at the given location.
   *
   * While this creates a new instance, it won't actually spawn the entity.
   * You can first do some modifications on it and then call the
   * {@link KelpEntity#spawn()} method.
   *
   * If you don't want to create a new entity, but just a new kelp
   * entity instance based of an existing bukkit entity, you can use
   * {@link #from(Entity)} instead.
   *
   * @param location The location, where the entity should be spawned later.
   * @return A new instance of a sheep entity.
   */
  static HorseEntity create(KelpLocation location) {
    return (HorseEntity) KelpPlugin.getInjector().getInstance(EntityTypeVersionTemplate.class)
      .newKelpEntity(getEntityType(), location.getBukkitLocation());
  }

  /**
   * Takes a bukkit entity and converts it to a kelp entity of the same type.
   *
   * This can be used if you are for example handling an event that returns a bukkit entity,
   * but you want to use a kelp entity for your operations. You can also use
   * the more general method provided by {@link de.pxav.kelp.core.entity.KelpEntity the
   * kelp entity base class}: {@link de.pxav.kelp.core.entity.KelpEntity#from(Entity)},
   * but this way you don't have to cast your entity to the specific type
   * manually.
   *
   * @param entity The entity you want to convert.
   * @return The kelp instance of the given bukkit entity.
   */
  static HorseEntity from(Entity entity) {
    return (HorseEntity) KelpPlugin.getInjector().getInstance(EntityTypeVersionTemplate.class)
      .getKelpEntity(entity);
  }

  static KelpEntityType getEntityType() {
    return KelpEntityType.HORSE;
  }

  @Override
  default KelpEntityType getType() {
    return getEntityType();
  }

  HorseInventory getInventory();

  HorseColor getHorseColor();

  HorseStyle getHorseStyle();

  HorseEntity setHorseColor(HorseColor color);

  HorseEntity setHorseStyle(HorseStyle style);

}
