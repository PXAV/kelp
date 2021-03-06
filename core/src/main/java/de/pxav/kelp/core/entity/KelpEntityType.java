package de.pxav.kelp.core.entity;

import de.pxav.kelp.core.entity.type.*;
import de.pxav.kelp.core.entity.type.general.MinecartEntity;
import de.pxav.kelp.core.player.KelpPlayer;
import de.pxav.kelp.core.version.KelpVersion;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * A class description goes here.
 *
 * @author pxav
 */
public enum  KelpEntityType {

  DROPPED_ITEM(KelpVersion.MC_1_8_0, DroppedItemEntity.class),
  EXPERIENCE_ORB(KelpVersion.MC_1_8_0, ExperienceOrbEntity.class),
  AREA_EFFECT_CLOUD(KelpVersion.MC_1_9_0, AreaEffectCloudEntity.class),
  ELDER_GUARDIAN(KelpVersion.MC_1_8_0, ElderGuardianEntity.class) {
    public boolean isHostile() { return true; }
    public boolean isAquatic() { return true; }
  },
  WITHER_SKELETON(KelpVersion.MC_1_8_0, WitherSkeletonEntity.class) {
    public boolean isUndead() { return true; }
    public boolean isHostile() { return true; }
  },
  STRAY_SKELETON(KelpVersion.MC_1_10_0, StraySkeletonEntity.class) {
    public boolean isUndead() { return true; }
    public boolean isHostile() { return true; }
  },
  CHICKEN_EGG(KelpVersion.MC_1_8_0, ThrownChickenEggEntity.class),
  LEASH_HITCH(KelpVersion.MC_1_8_0, LeashHitchEntity.class),
  PAINTING(KelpVersion.MC_1_8_0, PaintingEntity.class),
  ARROW(KelpVersion.MC_1_8_0, ArrowEntity.class),
  SNOWBALL(KelpVersion.MC_1_8_0, ThrownSnowballEntity.class),
  FIREBALL(KelpVersion.MC_1_8_0, ThrownFireballEntity.class),
  SMALL_FIREBALL(KelpVersion.MC_1_8_0, SmallFireballEntity.class),
  ENDER_PEARL(KelpVersion.MC_1_8_0, ThrownEnderPearlEntity.class),
  ENDER_SIGNAL(KelpVersion.MC_1_8_0, EnderSignalEntity.class),
  SPLASH_POTION(KelpVersion.MC_1_8_0, ThrownPotionEntity.class),
  THROWN_EXP_BOTTLE(KelpVersion.MC_1_8_0, ThrownExperienceBottleEntity.class),
  ITEM_FRAME(KelpVersion.MC_1_8_0, ItemFrameEntity.class),
  WITHER_SKULL(KelpVersion.MC_1_8_0, ThrownWitherSkullEntity.class),
  PRIMED_TNT(KelpVersion.MC_1_8_0, PrimedTntEntity.class),
  FALLING_BLOCK(KelpVersion.MC_1_8_0, FallingBlockEntity.class),
  FIREWORK(KelpVersion.MC_1_8_0, FireworkEntity.class),
  HUSK(KelpVersion.MC_1_10_0, HuskEntity.class) {
    public boolean isUndead() { return true; }
    public boolean isHostile() { return true; }
  },
  SPECTRAL_ARROW(KelpVersion.MC_1_9_0, SpectralArrowEntity.class),
  SHULKER_BULLET(KelpVersion.MC_1_9_0, ShulkerBulletEntity.class),
  DRAGON_FIREBALL(KelpVersion.MC_1_9_0, DragonFireballEntity.class),
  ZOMBIE_VILLAGER(KelpVersion.MC_1_8_0, ZombieVillagerEntity.class) {
    public boolean isUndead() { return true; }
    public boolean isHostile() { return true; }
  },
  SKELETON_HORSE(KelpVersion.MC_1_8_0, SkeletonHorseEntity.class) {
    public boolean isUndead() { return true; }
  },
  ZOMBIE_HORSE(KelpVersion.MC_1_8_0, ZombieHorse.class) {
    public boolean isUndead() { return true; }
  },
  ARMOR_STAND(KelpVersion.MC_1_8_0, ArmorStandEntity.class),
  DONKEY(KelpVersion.MC_1_8_0, DonkeyEntity.class),
  MULE(KelpVersion.MC_1_8_0, MuleEntity.class),
  EVOKER_FANGS(KelpVersion.MC_1_11_0, EvokerFangEntity.class),
  EVOKER(KelpVersion.MC_1_11_0, EvokerEntity.class) {
    public boolean isHostile() { return true; }
  },
  VEX(KelpVersion.MC_1_11_0, VexEntity.class) {
    public boolean isHostile() { return true; }
  },
  VINDICATOR(KelpVersion.MC_1_11_0, VindicatorEntity.class) {
    public boolean isHostile() { return true; }
  },
  ILLUSIONER(KelpVersion.MC_1_12_0, IllusionerEntity.class),
  MINECART_COMMAND(KelpVersion.MC_1_8_0, CommandMinecartEntity.class),
  BOAT(KelpVersion.MC_1_8_0, BoatEntity.class),
  MINECART(KelpVersion.MC_1_8_0, RideableMinecart.class),
  MINECART_CHEST(KelpVersion.MC_1_8_0, StorageMinecart.class),
  MINECART_FURNACE(KelpVersion.MC_1_8_0, PoweredMinecart.class),
  MINECART_TNT(KelpVersion.MC_1_8_0, ExplosiveMinecart.class),
  MINECART_HOPPER(KelpVersion.MC_1_8_0, HopperMinecart.class),
  MINECART_MOB_SPAWNER(KelpVersion.MC_1_8_0, SpawnerMinecart.class),
  CREEPER(KelpVersion.MC_1_8_0, CreeperEntity.class) {
    public boolean isHostile() { return true; }
  },
  SKELETON(KelpVersion.MC_1_8_0, SkeletonEntity.class) {
    public boolean isUndead() { return true; }
    public boolean isHostile() { return true; }
  },
  SPIDER(KelpVersion.MC_1_8_0, SpiderEntity.class),
  ZOMBIE_GIANT(KelpVersion.MC_1_8_0, ZombieGiant.class) {
    public boolean isUndead() { return true; }
  },
  ZOMBIE(KelpVersion.MC_1_8_0, ZombieEntity.class) {
    public boolean isUndead() { return true; }
    public boolean isHostile() { return true; }
  },
  SLIME(KelpVersion.MC_1_8_0, SlimeEntity.class) {
    public boolean isHostile() { return true; }
  },
  GHAST(KelpVersion.MC_1_8_0, GhastEntity.class) {
    public boolean isHostile() { return true; }
  },
  PIG_ZOMBIE(KelpVersion.MC_1_8_0, PigZombie.class) {
    public boolean isUndead() { return true; }
  },
  ENDERMAN(KelpVersion.MC_1_8_0, EndermanEntity.class),
  CAVE_SPIDER(KelpVersion.MC_1_8_0, CaveSpiderEntity.class),
  SILVERFISH(KelpVersion.MC_1_8_0, SilverfishEntity.class) {
    public boolean isHostile() { return true; }
  },
  BLAZE(KelpVersion.MC_1_8_0, BlazeEntity.class) {
    public boolean isHostile() { return true; }
  },
  MAGMA_CUBE(KelpVersion.MC_1_8_0, MagmaCubeEntity.class) {
    public boolean isHostile() { return true; }
  },
  ENDER_DRAGON(KelpVersion.MC_1_8_0, EnderDragonEntity.class) {
    public boolean isBoss() { return true; }
  },
  WITHER(KelpVersion.MC_1_8_0, WitherEntity.class) {
    public boolean isUndead() { return true; }
    public boolean isBoss() { return true; }
  },
  BAT(KelpVersion.MC_1_8_0, BatEntity.class),
  WITCH(KelpVersion.MC_1_8_0, WitchEntity.class) {
    public boolean isHostile() { return true; }
  },
  ENDERMITE(KelpVersion.MC_1_8_0, EndermiteEntity.class) {
    public boolean isHostile() { return true; }
  },
  GUARDIAN(KelpVersion.MC_1_8_0, GuardianEntity.class) {
    public boolean isHostile() { return true; }
    public boolean isAquatic() { return true; }
  },
  SHULKER(KelpVersion.MC_1_9_0, ShulkerEntity.class) {
    public boolean isHostile() { return true; }
  },
  PIG(KelpVersion.MC_1_8_0, PigEntity.class),
  SHEEP(KelpVersion.MC_1_8_0, SheepEntity.class),
  COW(KelpVersion.MC_1_8_0, CowEntity.class),
  CHICKEN(KelpVersion.MC_1_8_0, ChickenEntity.class),
  SQUID(KelpVersion.MC_1_8_0, SquidEntity.class) {
    public boolean isAquatic() { return true; }
  },
  WOLF(KelpVersion.MC_1_8_0, WolfEntity.class),
  MUSHROOM_COW(KelpVersion.MC_1_8_0, MushroomCowEntity.class),
  SNOWMAN(KelpVersion.MC_1_8_0, SnowmanEntity.class),
  OCELOT(KelpVersion.MC_1_8_0, OcelotEntity.class),
  IRON_GOLEM(KelpVersion.MC_1_8_0, IronGolemEntity.class),
  HORSE(KelpVersion.MC_1_8_0, HorseEntity.class),
  RABBIT(KelpVersion.MC_1_8_0, RabbitEntity.class),
  KILLER_BUNNY(KelpVersion.MC_1_8_0, KillerBunnyEntity.class),
  POLAR_BEAR(KelpVersion.MC_1_10_0, PolarBear.class),
  LLAMA(KelpVersion.MC_1_11_0, LlamaEntity.class),
  LLAMA_SPIT(KelpVersion.MC_1_11_0, LlamaSpitEntity.class),
  PARROT(KelpVersion.MC_1_12_0, ParrotEntity.class),
  VILLAGER(KelpVersion.MC_1_8_0, VillagerEntity.class),
  ENDER_CRYSTAL(KelpVersion.MC_1_8_0, EnderCrystalEntity.class),
  TURTLE(KelpVersion.MC_1_13_0, TurtleEntity.class) {
    public boolean isAquatic() { return true; }
  },
  PHANTOM(KelpVersion.MC_1_13_0, PhantomEntity.class) {
    public boolean isHostile() { return true; }
  },
  TRIDENT(KelpVersion.MC_1_13_0, ThrownTridentEntity.class),
  COD(KelpVersion.MC_1_13_0, CodEntity.class) {
    public boolean isAquatic() { return true; }
  },
  SALMON(KelpVersion.MC_1_13_0, SalmonEntity.class) {
    public boolean isAquatic() { return true; }
  },
  PUFFERFISH(KelpVersion.MC_1_13_0, PufferfishEntity.class) {
    public boolean isAquatic() { return true; }
  },
  TROPICAL_FISH(KelpVersion.MC_1_13_0, TropicalFishEntity.class) {
    public boolean isAquatic() { return true; }
  },
  DROWNED(KelpVersion.MC_1_13_0, DrownedEntity.class) {
    public boolean isUndead() { return true; }
    public boolean isHostile() { return true; }
  },
  DOLPHIN(KelpVersion.MC_1_13_0, DolphinEntity.class) {
    public boolean isAquatic() { return true; }
  },
  CAT(KelpVersion.MC_1_8_0, CatEntity.class),
  PANDA(KelpVersion.MC_1_14_0, PandaEntity.class),
  PILLAGER(KelpVersion.MC_1_14_0, PillagerEntity.class) {
    public boolean isHostile() { return true; }
  },
  RAVAGER(KelpVersion.MC_1_14_0, RavagerEntity.class) {
    public boolean isHostile() { return true; }
  },
  TRADER_LLAMA(KelpVersion.MC_1_14_0, TraderLlamaEntity.class),
  WANDERING_TRADER(KelpVersion.MC_1_14_0, WanderingTraderEntity.class),
  FOX(KelpVersion.MC_1_14_0, FoxEntity.class),
  FISHING_HOOK(KelpVersion.MC_1_8_0, FishHookEntity.class),
  LIGHTNING(KelpVersion.MC_1_8_0, LightningEntity.class),
  PLAYER(KelpVersion.MC_1_8_0, KelpPlayer.class),

  BEE(KelpVersion.MC_1_15_0, BeeEntity.class),
  ZOGLIN(KelpVersion.MC_1_16_0, ZoglinEntity.class),
  PIGLIN(KelpVersion.MC_1_16_0, PiglinEntity.class),
  PIGLIN_BRUTE(KelpVersion.MC_1_16_0, PiglinBruteEntity.class),
  LARGE_FIREBALL(KelpVersion.MC_1_8_0, LargeFireball.class),
  HOGLIN(KelpVersion.MC_1_8_0, HoglinEntity.class),
  STRIDER(KelpVersion.MC_1_16_0, StriderEntity.class),

  UNKNOWN(KelpVersion.MC_1_8_0, null);

  private KelpVersion since;
  private Class<? extends KelpEntity<?>> entityClass;

  KelpEntityType(KelpVersion since, Class<? extends KelpEntity<?>> entityClass) {
    this.since = since;
    this.entityClass = entityClass;
  }

  public KelpVersion since() {
    return since;
  }

  public Class<?> getEntityClass() {
    return entityClass;
  }

  public static boolean isLivingEntity(KelpEntityType entityType) {
    return entityType == KelpEntityType.ZOMBIE;
  }

  public boolean isUndead() {
    return false;
  }

  public boolean isAquatic() {
    return false;
  }

  // TODO IMPLEMENT!!!!
  public boolean isArthropod() {
    return false;
  }

  public boolean isBoss() {
    return false;
  }

  public boolean isHostile() {
    return false;
  }

  public static Collection<KelpEntityType> aboveVersion(KelpVersion version) {
    Collection<KelpEntityType> output = new ArrayList<>();

    if (version == KelpVersion.highestVersion()) {
      return output;
    }

    for (KelpEntityType entityType : KelpEntityType.values()) {
      if (entityType.since() == version) continue;
      if (KelpVersion.higherVersion(version,  entityType.since()) == entityType.since()) {
        output.add(entityType);
      }
    }

    return output;
  }

  public static Collection<KelpEntityType> belowVersion(KelpVersion version) {
    Collection<KelpEntityType> output = new ArrayList<>();

    if (version == KelpVersion.lowestVersion()) {
      return output;
    }

    for (KelpEntityType entityType : KelpEntityType.values()) {
      if (entityType.since() == version) continue;
      if (KelpVersion.lowerVersion(version,  entityType.since()) == entityType.since()) {
        output.add(entityType);
      }
    }

    return output;
  }

  public static Collection<KelpEntityType> matchesVersion(KelpVersion version) {
    Collection<KelpEntityType> output = new ArrayList<>();

    for (KelpEntityType entityType : KelpEntityType.values()) {
      if (entityType.since() == version) {
        output.add(entityType);
      }
    }

    return output;
  }

  public static Collection<KelpEntityType> includedIn(KelpVersion version) {
    Collection<KelpEntityType> output = new ArrayList<>(belowVersion(version));
    output.addAll(matchesVersion(version));
    return output;
  }

}
