package josscoder.jkit.data;

import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import josscoder.jkit.JKitPlugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Kit {

    private final String id;
    private final String image;
    private final String name;
    private final String permission;
    private final int cooldown;
    private final Item helmet;
    private final Item chestPlate;
    private final Item leggings;
    private final Item boots;
    private final Map<Integer, Item> itemList = new HashMap<>();

    public Kit(String id, String name, String permission, int cooldown, PlayerInventory playerInventory) {
        this.id = id;
        this.image = "textures/blocks/barrier.png";
        this.name = name;
        this.permission = permission;
        this.cooldown = cooldown;
        this.helmet = playerInventory.getHelmet();
        this.chestPlate = playerInventory.getChestplate();
        this.leggings = playerInventory.getLeggings();
        this.boots = playerInventory.getBoots();
        this.itemList.putAll(playerInventory.getContents());
    }

    public Kit(String id, ConfigSection mainSection) {
        this.id = id;
        this.image = mainSection.getString("image", "textures/blocks/barrier.png");
        this.name = mainSection.getString("name", "No name");
        this.permission = mainSection.getString("permission", "kit.permission");
        this.cooldown = mainSection.getInt("cooldown");
        this.helmet = Item.get(mainSection.getInt("helmet"));
        this.chestPlate = Item.get(mainSection.getInt("chestPlate"));
        this.leggings = Item.get(mainSection.getInt("leggings"));
        this.boots = Item.get(mainSection.getInt("boots"));

        ConfigSection itemsSection = mainSection.getSection("items");
        itemsSection.getSections().getKeys(false).forEach(key -> {
            ConfigSection currentItemSection = itemsSection.getSection(key);
            String[] itemData = currentItemSection.getString("data").split(":");

            Item item = Item.get(Integer.parseInt(itemData[0]),
                    itemData.length >= 2 ? Integer.parseInt(itemData[1]) : 0,
                    itemData.length == 3 ? Integer.parseInt(itemData[2]) : 1
            );

            if (currentItemSection.exists("customName")) {
                item.setCustomName(currentItemSection.getString("customName"));
            }

            List<String> enchantmentsList = currentItemSection.getStringList("enchantments");
            if (!enchantmentsList.isEmpty()) {
                enchantmentsList.forEach(enchantment -> {
                    String[] split = enchantment.split(":");
                    if (enchantment.length() > 0 && split.length > 0) {
                        Enchantment enchantment1 = Enchantment.getEnchantment(Integer.parseInt(split[0]))
                                .setLevel(split.length == 2 ? Integer.parseInt(split[1]) : 1, false);
                        item.addEnchantment(enchantment1);
                    }
                });
            }

            itemList.put(Integer.valueOf(key), item);
        });
    }

    private String getNBTId() {
        return id + "_nbt_cooldown";
    }

    public int getTimeLeft(Player player) {
        return player.namedTag.getInt(getNBTId());
    }

    public boolean hasCooldown(Player player) {
        String nbtId = getNBTId();

        int seconds = JKitPlugin.getInstance().getHelper().getCurrentSeconds();

        return player.namedTag.contains(nbtId) && player.namedTag.getInt(nbtId) > seconds;
    }

    public boolean isAvailable(Player player) {
        return !hasCooldown(player) || player.isOp() || player.hasPermission(permission);
    }

    public void give(Player player) {
        PlayerInventory inventory = player.getInventory();

        inventory.clearAll();

        inventory.setHelmet(helmet);
        inventory.setChestplate(chestPlate);
        inventory.setLeggings(leggings);
        inventory.setBoots(boots);

        itemList.forEach(inventory::setItem);

        if (!player.isOp()) {
            player.namedTag.putInt(getNBTId(), JKitPlugin.getInstance().getHelper().getCurrentSeconds() + cooldown);
        }
    }

    public String getName() {
        return TextFormat.colorize(name);
    }
}
