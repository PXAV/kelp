package de.pxav.kelp.core.inventory.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.pxav.kelp.core.KelpPlugin;
import de.pxav.kelp.core.inventory.listener.KelpClickEvent;
import de.pxav.kelp.core.inventory.listener.KelpListenerRepository;
import de.pxav.kelp.core.inventory.material.KelpMaterial;
import de.pxav.kelp.core.inventory.version.ItemVersionTemplate;
import de.pxav.kelp.core.player.KelpPlayer;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * The {@code KelpItem} can be compared with the {@code ItemStack} of the
 * bukkit library. It represents an item including its metadata. Unlike in
 * the bukkit library, no extra {@code ItemMeta} is needed to control things
 * like the display name or lore. It is all handled via one class.
 *
 * @author pxav
 */
public class KelpItem {

  private ItemVersionTemplate itemVersionTemplate;
  private ItemTagVersionTemplate itemTagVersionTemplate;
  private KelpListenerRepository listenerRepository;

  public KelpItem(ItemVersionTemplate itemVersionTemplate,
                  ItemTagVersionTemplate itemTagVersionTemplate,
                  KelpListenerRepository listenerRepository) {
    this.itemVersionTemplate = itemVersionTemplate;
    this.itemTagVersionTemplate = itemTagVersionTemplate;
    this.listenerRepository = listenerRepository;
  }

  public static KelpItem create() {
    return new KelpItem(
      KelpPlugin.getInjector().getInstance(ItemVersionTemplate.class),
      KelpPlugin.getInjector().getInstance(ItemTagVersionTemplate.class),
      KelpPlugin.getInjector().getInstance(KelpListenerRepository.class)
    );
  }

  public static KelpItem from(ItemStack itemStack) {
    return KelpPlugin.getInjector().getInstance(ItemVersionTemplate.class).fromItemStack(itemStack);
  }

  // the material of the item. If none is set, stone will be used
  private KelpMaterial material = KelpMaterial.STONE;

  // the amount of the item (stack), 1 by default
  private int amount = 1;

  // the slot inside the inventory where it is stored.
  // This attribute is ignored by some inventory widgets as they
  // have their own item sorting methods.
  private int slot;

  // the display name of the item
  private String displayName;

  // the item description - aka. the item lore.
  // Those are some lines of text below the display name.
  private List<String> itemDescription = Lists.newArrayList();

  private Collection<ItemFlag> itemFlags = Lists.newArrayList();
  private Map<Enchantment, Integer> enchantments = Maps.newHashMap();

  private boolean glowing = false;
  private boolean unbreakable = false;

  private ConcurrentMap<String, Object> nbtTags = Maps.newConcurrentMap();
  private Collection<String> tagsToRemove = Lists.newArrayList();

  /**
   * Sets the material type of the item.
   *
   * @param material The material type you want to set.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem material(KelpMaterial material) {
    this.material = material;
    return this;
  }

  /**
   * Sets the amount of items to be stacked.
   *
   * @param amount The item amount you want to set.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem amount(int amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Sets the slot of the item, where it should be located
   * in the parent inventory.
   *
   * @param slot The slot location you want to set.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem slot(int slot) {
    this.slot = slot;
    return this;
  }

  /**
   * Sets the display name of the item.
   *
   * @param displayName The display name you want to set.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * Adds a new string tag to the item.
   *
   * @param key     The key of the tag. This key is important
   *                to be able to access the value later.
   * @param value   The value you want to assign to the given key.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem addTag(String key, Object value) {
    this.nbtTags.put(key, value);
    return this;
  }

  /**
   * Removes the tag with the given key from the item.
   *
   * @param key The key of the item tag you want to remove.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem removeTag(String key) {
    this.tagsToRemove.add(key);
    this.nbtTags.remove(key);
    return this;
  }

  /**
   * Sets the description/lore of the item. This method overwrites
   * the description which has been set before and replaces
   * it with the given list.
   *
   * @param description The new description of the item.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem itemDescription(List<String> description) {
    this.itemDescription = description;
    return this;
  }

  /**
   * Sets the description/lore of the item. This method overwrites
   * the description which has been set before and replaces
   * it with the given string.
   *
   * Using {@code \n} won't create a new line in all versions,
   * so it is recommended to use for example {@link #itemDescription(List)}
   * if you want to set multiple lines.
   *
   * @param description The new description of the item.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem itemDescription(String description) {
    this.itemDescription = Collections.singletonList(description);
    return this;
  }

  /**
   * Sets the description/lore of the item. This method overwrites
   * the description which has been set before and replaces
   * it with the given array.
   *
   * @param description The new description of the item.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem itemDescription(String... description) {
    this.itemDescription = Lists.newArrayList(description);
    return this;
  }

  /**
   * Adds a new line to the item's description. This method does not
   * override the old description, but appends a new line to it.
   *
   * @param description The new description line you want to append.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem addItemDescription(String description) {
    this.itemDescription.add(description);
    return this;
  }

  /**
   * Adds new lines to the item's description. This method does not
   * override the old description, but appends new lines to it.
   *
   * @param description The new description lines you want to append.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem addItemDescription(List<String> description) {
    this.itemDescription.addAll(description);
    return this;
  }

  /**
   * Adds new lines to the item's description. This method does not
   * override the old description, but appends new lines to it.
   *
   * @param description The new description lines you want to append.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem addItemDescription(String... description) {
    this.itemDescription.addAll(Arrays.asList(description));
    return this;
  }

  /**
   * Cancels interactions when a player clicks on the item. This
   * avoids that items can be taken out of inventories for example.
   * This flag is given to items by default, so if you have not removed
   * it manually before {@link #allowInteractions()}, there is no need to use this method.
   *
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem cancelInteractions() {
    this.addTag("interactionCancelled", true);
    return this;
  }

  /**
   * Makes item interactions allowed again. If you click on an item
   * as if you want to take it out of the inventory, this interaction
   * is canceled by default and the item remains in the inventory.
   *
   * By executing this method, you can remove this cancellation flag.
   *
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem allowInteractions() {
    this.removeTag("interactionCancelled");
    return this;
  }

  /**
   * Adds a new listener to to the item. The listener is fired when the
   * given player clicks on the item.
   *
   * @param player    The player who should be able to click on the item. This is also used to
   *                  clear the listener cache of a specific player if
   *                  they quit the inventory or server.
   * @param listener  The listener interface containing the code you want
   *                  to execute when the item is clicked.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem addListener(KelpPlayer player, Consumer<KelpClickEvent> listener) {
    return addListener(player.getUUID(), listener);
  }

  /**
   * Adds a global listener, which does not depend on any player.
   *
   * @param listener The listener.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem addGlobalListener(Consumer<KelpClickEvent> listener) {
    return addListener(KelpListenerRepository.GLOBAL_LISTENER_ID, listener);
  }

  /**
   * Adds a new listener to to the item. The listener is fired when the
   * given player clicks on the item.
   *
   * @param player    The {@link UUID} of the player who should be
   *                  able to click on the item. This is also used to
   *                  clear the listener cache of a specific player if
   *                  they quit the inventory or server.
   * @param listener  The listener interface containing the code you want
   *                  to execute when the item is clicked.
   * @return Instance of the current {@code KelpItem} object.
   */
  public KelpItem addListener(UUID player, Consumer<KelpClickEvent> listener) {
    String listenerId = listenerRepository.registerListener(player, listener);
    this.addTag("listener-" + ThreadLocalRandom.current().nextInt(1, 1000), listenerId);
    return this;
  }

  /**
   * Converts all the given data about the item into a bukkit
   * {@link ItemStack} which can be put into "real" bukkit inventories.
   *
   * @return The {@link ItemStack} equivalent to the data of the kelp item.
   */
  public ItemStack getItemStack() {
    ItemStack itemStack = itemVersionTemplate.newItemStack(this.material);
    itemStack.setAmount(this.amount);

    // set the display name
    if (this.displayName != null) {
      itemStack = itemVersionTemplate.setDisplayName(itemStack, displayName);
    }

    if (this.itemDescription != null && !this.itemDescription.isEmpty()) {
      itemVersionTemplate.setLore(itemStack, itemDescription);
    }

    // make the item unbreakable if needed.
    if (this.unbreakable) {
      itemStack = itemVersionTemplate.makeUnbreakable(itemStack);
    }

    // add a flag to cancel interactions by default, if nothing else has been defined
    if (!this.nbtTags.containsKey("interactionCancelled") && !tagsToRemove.contains("interactionCancelled")) {
      this.cancelInteractions();
    }

    // add string tags
    for (Map.Entry<String, Object> tagEntry : this.nbtTags.entrySet()) {
      Object value = tagEntry.getValue();
      itemStack = itemTagVersionTemplate.tagItem(itemStack, tagEntry.getKey(), value);
    }

    // remove tags, which should be removed
    for (String currentTag : this.tagsToRemove) {
      itemStack = itemTagVersionTemplate.removeTag(itemStack, currentTag);
    }

    return itemStack;
  }

  /**
   * Gets the slot, where the item should be placed
   * in the parent-inventory.
   *
   * @return The item's slot.
   */
  public int getSlot() {
    return slot;
  }

  /**
   * Gets the material type of the item.
   *
   * @return The item's material.
   */
  public KelpMaterial getMaterial() {
    return material;
  }

  /**
   * Gets the display name of the item.
   *
   * @return The item's display name.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Gets the entire item description.
   *
   * @return A list of all lines containing the item description.
   */
  public List<String> getItemDescription() {
    return itemDescription;
  }

  /**
   * Gets only a specific line of the item description.
   *
   * @param line The number of the line you want to get.
   * @return The requested line of the item description.
   */
  public String getItemDescriptionAt(int line) {
    return itemDescription.get(line);
  }

  /**
   * Gets the amount of items to be stacked.
   *
   * @return The item amount.
   */
  public int getAmount() {
    return amount;
  }

  /**
   * Checks whether this item has a specific tag.
   * Tags can be assigned using {@link #addTag(String, Object)}.
   *
   * You can get the value for a tag using one of the get methods
   * like {@link #getIntTag(String)}.
   *
   * @param key The key of the tag to check.
   * @return {@code true} if the item has the desired tag.
   */
  public boolean hasTagKey(String key) {
    return nbtTags.containsKey(key);
  }

  /**
   * Gets the value for the tag associated with the given value.
   * This method does not cast the value to a specific type, but
   * returns a generic object.
   *
   * You can retrieve specific values using {@link #getIntTag(String)}
   * for example.
   *
   * @param key The key of the tag you want to get the value of.
   * @return The value associated with the given key.
   */
  public Object getRawTag(String key) {
    return nbtTags.get(key);
  }

  /**
   * Gets the value for the tag associated with the given value.
   * This method only returns values of type integer and you should
   * be sure that the tag you are retrieving is an integer.
   * Otherwise, class cast exceptions might occur.
   *
   * If you want to retrieve any datatype, use {@link #getRawTag(String)}
   * instead.
   *
   * @param key The key of the tag to get the value of.
   * @return The integer value associated with this tag.
   */
  public int getIntTag(String key) {
    return (int) nbtTags.get(key);
  }

  /**
   * Gets the value for the tag associated with the given value.
   * This method only returns values of type string and you should
   * be sure that the tag you are retrieving is a string.
   * Otherwise, class cast exceptions might occur.
   *
   * If you want to retrieve any datatype, use {@link #getRawTag(String)}
   * instead.
   *
   * @param key The key of the tag to get the value of.
   * @return The string value associated with this tag.
   */
  public String getStringTag(String key) {
    return (String) nbtTags.get(key);
  }

  /**
   * Gets the value for the tag associated with the given value.
   * This method only returns values of type boolean and you should
   * be sure that the tag you are retrieving is a boolean.
   * Otherwise, class cast exceptions might occur.
   *
   * If you want to retrieve any datatype, use {@link #getRawTag(String)}
   * instead.
   *
   * @param key The key of the tag to get the value of.
   * @return The boolean value associated with this tag.
   */
  public boolean getBooleanTag(String key) {
    return (boolean) nbtTags.get(key);
  }

  /**
   * Gets the value for the tag associated with the given value.
   * This method only returns values of type double and you should
   * be sure that the tag you are retrieving is a double.
   * Otherwise, class cast exceptions might occur.
   *
   * If you want to retrieve any datatype, use {@link #getRawTag(String)}
   * instead.
   *
   * @param key The key of the tag to get the value of.
   * @return The double value associated with this tag.
   */
  public double getDoubleTag(String key) {
    return (double) nbtTags.get(key);
  }

  /**
   * Gets the value for the tag associated with the given value.
   * This method only returns values of type float and you should
   * be sure that the tag you are retrieving is a float.
   * Otherwise, class cast exceptions might occur.
   *
   * If you want to retrieve any datatype, use {@link #getRawTag(String)}
   * instead.
   *
   * @param key The key of the tag to get the value of.
   * @return The double value associated with this tag.
   */
  public float getFloatTag(String key) {
    return (float) nbtTags.get(key);
  }

  /**
   * Gets the value for the tag associated with the given value.
   * This method only returns values of type long and you should
   * be sure that the tag you are retrieving is a long.
   * Otherwise, class cast exceptions might occur.
   *
   * If you want to retrieve any datatype, use {@link #getRawTag(String)}
   * instead.
   *
   * @param key The key of the tag to get the value of.
   * @return The long value associated with this tag.
   */
  public long getLongTag(String key) {
    return (long) nbtTags.get(key);
  }

  /**
   * Gets the value for the tag associated with the given value.
   * This method only returns values of type short and you should
   * be sure that the tag you are retrieving is a short.
   * Otherwise, class cast exceptions might occur.
   *
   * If you want to retrieve any datatype, use {@link #getRawTag(String)}
   * instead.
   *
   * @param key The key of the tag to get the value of.
   * @return The short value associated with this tag.
   */
  public short getShortTag(String key) {
    return (short) nbtTags.get(key);
  }

  /**
   * Gets the value for the tag associated with the given value.
   * This method only returns values of type byte and you should
   * be sure that the tag you are retrieving is a byte.
   * Otherwise, class cast exceptions might occur.
   *
   * If you want to retrieve any datatype, use {@link #getRawTag(String)}
   * instead.
   *
   * @param key The key of the tag to get the value of.
   * @return The byte value associated with this tag.
   */
  public byte getByteTag(String key) {
    return (byte) nbtTags.get(key);
  }

  /**
   * Gets the value for the tag associated with the given value.
   * This method only returns values of type int array and you should
   * be sure that the tag you are retrieving is an integer array.
   * Otherwise, class cast exceptions might occur.
   *
   * If you want to retrieve any datatype, use {@link #getRawTag(String)}
   * instead.
   *
   * @param key The key of the tag to get the value of.
   * @return The integer array associated with this tag.
   */
  public int[] getIntegerArrayTag(String key) {
    return (int[]) nbtTags.get(key);
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof KelpItem)) {
      return false;
    }

    KelpItem item = (KelpItem) object;
    return displayName.equalsIgnoreCase(item.getDisplayName())
      && material == item.getMaterial()
      && itemDescription.equals(item.getItemDescription())
      && slot == item.getSlot()
      && unbreakable == item.unbreakable
      && nbtTags.equals(item.nbtTags);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
      .append(this.displayName)
      .append(this.itemDescription)
      .append(this.slot)
      .append(this.material)
      .append(this.nbtTags)
      .append(this.glowing)
      .append(this.unbreakable)
      .append(this.enchantments)
      .append(this.itemFlags)
      .toHashCode();
  }

}
