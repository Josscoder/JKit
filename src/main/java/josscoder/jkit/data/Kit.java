package josscoder.jkit.data;

import cn.nukkit.Player;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import josscoder.jkit.JKitPlugin;
import josscoder.jkit.helper.Helper;
import lombok.Getter;

import java.time.Duration;
import java.util.HashMap;
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

    public Kit(String id, ConfigSection mainSection, Helper helper) {
        this.id = id;
        this.image = mainSection.getString("image", "textures/blocks/barrier.png");
        this.name = mainSection.getString("name", "No name");
        this.permission = mainSection.getString("permission", "kit.permission");
        this.cooldown = mainSection.getInt("cooldown");

        this.helmet = helper.readItemDataFromSection(mainSection.getSection("helmet"));
        this.chestPlate = helper.readItemDataFromSection(mainSection.getSection("chestPlate"));
        this.leggings = helper.readItemDataFromSection(mainSection.getSection("leggings"));
        this.boots = helper.readItemDataFromSection(mainSection.getSection("boots"));

        ConfigSection itemsSection = mainSection.getSection("items");
        itemsSection.getSections().getKeys(false).forEach(key -> {
            ConfigSection currentItemSection = itemsSection.getSection(key);
            Item item = helper.readItemDataFromSection(currentItemSection);
            itemList.put(Integer.parseInt(key), item);
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

    public String getTimeLeftString(Player player) {
        Helper helper = JKitPlugin.getInstance().getHelper();

        int secondsLeft = getTimeLeft(player) - helper.getCurrentSeconds();
        Duration duration = Duration.ofSeconds(secondsLeft);
        return helper.formatDuration(duration);
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
