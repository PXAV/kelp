package de.pxav.kelp.implementation1_8.npc;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.pxav.kelp.core.KelpPlugin;
import de.pxav.kelp.core.npc.KelpNpc;
import de.pxav.kelp.core.npc.KelpNpcMeta;
import de.pxav.kelp.core.npc.version.NpcVersionTemplate;
import de.pxav.kelp.core.reflect.ReflectionUtil;
import de.pxav.kelp.core.version.Versioned;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A class description goes here.
 *
 * @author pxav
 */
@Versioned
public class VersionedNpc extends NpcVersionTemplate {

  private ReflectionUtil reflectionUtil;

  @Inject
  public VersionedNpc(ReflectionUtil reflectionUtil) {
    this.reflectionUtil = reflectionUtil;
  }

  @Override
  public KelpNpcMeta spawnNpc(KelpNpc npc, Player player) {
    PlayerConnection playerConnection = ((CraftPlayer)player).getHandle().playerConnection;
    WorldServer nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
    PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn();
    int entityId = ThreadLocalRandom.current().nextInt(10_000) + 2_000;

    // set random custom name if null
    if (npc.getCustomName() == null || npc.getCustomName().equalsIgnoreCase("")) {
      npc.customName("n" + ThreadLocalRandom.current().nextInt(10_000));
    }

    GameProfile gameProfile = new GameProfile(npc.getUuid(), npc.getCustomName());

    if (npc.getSkinTexture() != null && npc.getSkinSignature() != null) {
      gameProfile.getProperties().put("textures", new Property("textures", npc.getSkinTexture(), npc.getSkinSignature()));
    }

    Bukkit.broadcastMessage("1");
    List<String> currentTitles = npc.getCurrentTitles();
    Collections.reverse(currentTitles);
    Collection<Integer> armorStandIds = Lists.newArrayList();
    Bukkit.broadcastMessage("2");
    for (int i = 0; i < currentTitles.size(); i++) {
      double height = npc.getTitleHeights(i);
      Bukkit.broadcastMessage("3");
      Bukkit.broadcastMessage("4");

      EntityArmorStand armorStand = new EntityArmorStand(nmsWorld,
        npc.getSpawnLocation().getX(),
        npc.getSpawnLocation().clone().add(0, height, 0).getY(),
        npc.getSpawnLocation().getZ());
      Bukkit.broadcastMessage("5");
      armorStand.setInvisible(true);
      armorStand.setBasePlate(false);
      armorStand.setGravity(false);
      armorStand.setCustomNameVisible(true);
      Bukkit.broadcastMessage("6");
      armorStand.setCustomName(currentTitles.get(i));
      Bukkit.broadcastMessage("7");
      armorStandIds.add(armorStand.getId());
      Bukkit.broadcastMessage("9");

      playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
      Bukkit.broadcastMessage("10");
    }

    Bukkit.broadcastMessage("11");

    KelpNpcMeta npcMeta = new KelpNpcMeta(entityId, gameProfile, npc.getCustomName(), armorStandIds);

    reflectionUtil.setValue(spawnPacket, "a", entityId);
    reflectionUtil.setValue(spawnPacket, "b", gameProfile.getId());
    reflectionUtil.setValue(spawnPacket, "c", MathHelper.floor(npc.getSpawnLocation().getX() * 32.0D));
    reflectionUtil.setValue(spawnPacket, "d", MathHelper.floor(npc.getSpawnLocation().getY() * 32.0D));
    reflectionUtil.setValue(spawnPacket, "e", MathHelper.floor(npc.getSpawnLocation().getZ() * 32.0D));
    reflectionUtil.setValue(spawnPacket, "f", (byte) ((int) (npc.getSpawnLocation().getYaw() * 256.0F / 360.0F)));
    reflectionUtil.setValue(spawnPacket, "g", (byte) ((int) (npc.getSpawnLocation().getPitch() * 256.0F / 360.0F)));
    Bukkit.broadcastMessage("13");
    if (npc.getItemInHand() != null) {
      reflectionUtil.setValue(spawnPacket, "h", npc.getItemInHand().getItemStack().getType().getId());
    }Bukkit.broadcastMessage("14");

    DataWatcher dataWatcher = new DataWatcher(null);
    this.applyToDataWatcher(dataWatcher, npc);
    Bukkit.broadcastMessage("15");
    reflectionUtil.setValue(spawnPacket, "i", dataWatcher);
    Bukkit.broadcastMessage("16");
    addToTab(npcMeta, player);
    Bukkit.broadcastMessage("17");
    playerConnection.sendPacket(spawnPacket);
    Bukkit.broadcastMessage("18");

    Bukkit.getScheduler().runTaskLater(KelpPlugin.getPlugin(KelpPlugin.class), () -> {
      removeFromTab(npcMeta, player);
      Bukkit.broadcastMessage("20");
    }, 30L);
    Bukkit.broadcastMessage("19");
    return npcMeta;
  }

  @Override
  public void deSpawn(KelpNpc npc, Player player) {
    npc.getNpcMeta().getArmorStandEntityIds().forEach(current -> {
      PacketPlayOutEntityDestroy armorStandDestroyPacket = new PacketPlayOutEntityDestroy(current);
      ((CraftPlayer)player).getHandle().playerConnection.sendPacket(armorStandDestroyPacket);
    });

    PacketPlayOutEntityDestroy npcDestroyPacket = new PacketPlayOutEntityDestroy(npc.getEntityId());
    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(npcDestroyPacket);
  }

  @Override
  public void moveRelativeDistance(KelpNpc npc, Player player, double x, double y, double z, float absoluteYaw, float absolutePitch) {

    npc.getNpcMeta().getArmorStandEntityIds().forEach(armorStand -> {
      PacketPlayOutEntity.PacketPlayOutRelEntityMove movePacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
        armorStand,
        (byte) MathHelper.floor(x * 32.0D),
        (byte) MathHelper.floor(y * 32.0D),
        (byte) MathHelper.floor(z * 32.0D),
        true
      );
      ((CraftPlayer)player).getHandle().playerConnection.sendPacket(movePacket);
    });

    PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook moveLookPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
      npc.getEntityId(),
      (byte) MathHelper.floor(x * 32.0D),
      (byte) MathHelper.floor(y * 32.0D),
      (byte) MathHelper.floor(z * 32.0D),
      (byte) ((int) (absoluteYaw * 256.0F / 360.0F)),
      (byte) ((int) (absolutePitch * 256.0F / 360.0F)),
      true
    );

    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(moveLookPacket);
  }

  @Override
  public void teleport(KelpNpc npc, Location location) {
    CraftPlayer craftPlayer = (CraftPlayer) npc.getPlayer().getBukkitPlayer();
    PlayerConnection playerConnection = craftPlayer.getHandle().playerConnection;

    PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport();

    reflectionUtil.setValue(teleportPacket, "a", npc.getEntityId());
    reflectionUtil.setValue(teleportPacket, "b", MathHelper.floor(location.getX() * 32.0D));
    reflectionUtil.setValue(teleportPacket, "c", MathHelper.floor(location.getY() * 32.0D));
    reflectionUtil.setValue(teleportPacket, "d", MathHelper.floor(location.getZ() * 32.0D));
    reflectionUtil.setValue(teleportPacket, "e", (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
    reflectionUtil.setValue(teleportPacket, "f", (byte) ((int) (location.getPitch() * 256.0F / 360.0F)));

    PacketPlayOutEntityHeadRotation headRotationPacket = new PacketPlayOutEntityHeadRotation();
    reflectionUtil.setValue(headRotationPacket, "a", npc.getEntityId());
    reflectionUtil.setValue(headRotationPacket, "b", (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));

    playerConnection.sendPacket(teleportPacket);
    playerConnection.sendPacket(headRotationPacket);

    int index = 0;
    for (Integer entityId : npc.getNpcMeta().getArmorStandEntityIds()) {
      double height = npc.getTitleHeights(index);
      PacketPlayOutEntityTeleport teleportArmorStandPacket = new PacketPlayOutEntityTeleport();
      reflectionUtil.setValue(teleportArmorStandPacket, "a", entityId);
      reflectionUtil.setValue(teleportArmorStandPacket, "b", MathHelper.floor(location.getX() * 32.0D));

      reflectionUtil.setValue(teleportArmorStandPacket, "c", MathHelper.floor((location.getY() + height) * 32.0D));

      reflectionUtil.setValue(teleportArmorStandPacket, "d", MathHelper.floor(location.getZ() * 32.0D));
      reflectionUtil.setValue(teleportArmorStandPacket, "e", (byte) 0);
      reflectionUtil.setValue(teleportArmorStandPacket, "f", (byte) 0);
      playerConnection.sendPacket(teleportArmorStandPacket);
      index++;
    }
  }

  @Override
  public void updateCustomName(KelpNpc npc) {
    CraftPlayer craftPlayer = (CraftPlayer) npc.getPlayer().getBukkitPlayer();
    PlayerConnection playerConnection = craftPlayer.getHandle().playerConnection;

    ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), craftPlayer.getName());

    if (npc.isCustomNameShown()) {
      team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS);
    } else {
      team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
    }

    Collection<String> toHide = Lists.newArrayList(npc.getCustomName());

    playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, 1));
    playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
    playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(team, toHide,3));
  }

  @Override
  public void updateTitleLines(KelpNpc npc) {
    CraftPlayer player = (CraftPlayer) npc.getPlayer().getBukkitPlayer();
    WorldServer nmsWorld = ((CraftWorld) npc.getCurrentLocation().getWorld()).getHandle();

    npc.getNpcMeta().getArmorStandEntityIds().forEach(current -> {
      PacketPlayOutEntityDestroy armorStandDestroyPacket = new PacketPlayOutEntityDestroy(current);
      player.getHandle().playerConnection.sendPacket(armorStandDestroyPacket);
    });

    List<String> currentTitles = npc.getCurrentTitles();
    Collections.reverse(currentTitles);
    Collection<Integer> armorStandIds = Lists.newArrayList();
    Bukkit.broadcastMessage("2");
    for (int i = 0; i < currentTitles.size(); i++) {
      double height = npc.getTitleHeights(i);
      Bukkit.broadcastMessage("3");
      Bukkit.broadcastMessage("4");

      EntityArmorStand armorStand = new EntityArmorStand(nmsWorld,
        npc.getCurrentLocation().getX(),
        npc.getCurrentLocation().clone().add(0, height, 0).getY(),
        npc.getCurrentLocation().getZ());
      Bukkit.broadcastMessage("5");
      armorStand.setInvisible(true);
      armorStand.setBasePlate(false);
      armorStand.setGravity(false);
      armorStand.setCustomNameVisible(true);
      Bukkit.broadcastMessage("6");
      armorStand.setCustomName(currentTitles.get(i));
      Bukkit.broadcastMessage("7");
      armorStandIds.add(armorStand.getId());
      Bukkit.broadcastMessage("9");

      player.getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
      Bukkit.broadcastMessage("10");
    }
    npc.setArmorStandEntityIds(armorStandIds);
  }

  @Override
  public void refreshMetadata(KelpNpc npc, Player player) {
    DataWatcher dataWatcher = new DataWatcher(null);

    applyToDataWatcher(dataWatcher, npc);
    PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(npc.getEntityId(), dataWatcher, true);
    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(metaPacket);
  }

  private void addToTab(KelpNpcMeta npcMeta, Player player) {
    Bukkit.broadcastMessage("meta " + npcMeta);
    Bukkit.broadcastMessage("o " + npcMeta.getOverHeadDisplayName());
    Bukkit.broadcastMessage("o1 " + CraftChatMessage.fromString(npcMeta.getOverHeadDisplayName())[0]);

    PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo();
    PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData = infoPacket.new PlayerInfoData(
      npcMeta.getGameProfile(),
      1,
      WorldSettings.EnumGamemode.NOT_SET,
      CraftChatMessage.fromString(npcMeta.getOverHeadDisplayName())[0]);
    Bukkit.broadcastMessage("16.1");

    List<PacketPlayOutPlayerInfo.PlayerInfoData> players = new ArrayList<>();
    players.add(playerInfoData);
    Bukkit.broadcastMessage("16.2");

    reflectionUtil.setValue(infoPacket, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
    reflectionUtil.setValue(infoPacket, "b", players);
    Bukkit.broadcastMessage("16.3");

    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(infoPacket);
    Bukkit.broadcastMessage("16.4");
  }

  private void removeFromTab(KelpNpcMeta npc, Player player) {
    PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo();
    PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData = infoPacket.new PlayerInfoData(npc.getGameProfile(), 1, WorldSettings.EnumGamemode.NOT_SET, CraftChatMessage.fromString(npc.getOverHeadDisplayName())[0]);
    List<PacketPlayOutPlayerInfo.PlayerInfoData> players = new ArrayList<>();
    players.add(playerInfoData);

    reflectionUtil.setValue(infoPacket, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
    reflectionUtil.setValue(infoPacket, "b", players);

    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(infoPacket);
  }

  private DataWatcher applyToDataWatcher(DataWatcher dataWatcher, KelpNpc kelpNpc) {

    if (kelpNpc.hasBurningEffect()) {
      dataWatcher.a(0, (byte) 0x01);
    }

    if (kelpNpc.isSneaking()) {
      dataWatcher.a(0, (byte) 0x02);
    }

    if (kelpNpc.isSprinting()) {
      dataWatcher.a(0, (byte) 0x08);
    }

    if (kelpNpc.isInvisible()) {
      dataWatcher.a(0, (byte) 0x20);
    }

    if (!kelpNpc.isSneaking() && !kelpNpc.hasBurningEffect() && !kelpNpc.isInvisible() && !kelpNpc.isSprinting()) {
      dataWatcher.a(0, (byte) 0);
    }

    if (kelpNpc.isCustomNameShown()) {
      dataWatcher.a(11, (byte) 2);
    } else {
      dataWatcher.a(11, (byte) 0);
    }

    return dataWatcher;
  }

}
