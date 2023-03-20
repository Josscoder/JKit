package josscoder.jkit.helper;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import josscoder.jkit.data.Kit;
import lombok.Getter;

import java.time.Duration;
import java.util.*;

public class Helper {

    private final Config config;
    private final ConfigSection mainSection;

    @Getter
    private final Map<String, Kit> kitList = new HashMap<>();

    public Helper(Config config) {
        this.config = config;
        this.mainSection = config.getSection("kits");

        loadKits();
    }

    public void loadKits() {
        mainSection.getSections().getKeys(false).forEach(key ->
                registerKit(new Kit(key, mainSection.getSection(key), this))
        );
    }

    public void reloadKits() {
        kitList.clear();
        loadKits();
    }

    public void registerKit(Kit kit) {
        registerKit(kit, false);
    }

    public void registerKit(Kit kit, boolean serialize) {
        kitList.put(kit.getId(), kit);

        if (!serialize) {
            return;
        }

        ConfigSection mainSection = new ConfigSection();
        mainSection.set("name", kit.getName());
        mainSection.set("permission", kit.getPermission());
        mainSection.set("cooldown", kit.getCooldown());

        ConfigSection helmet = new ConfigSection();
        writeItemDataIntoSection(kit.getHelmet(), helmet);
        mainSection.set("helmet", helmet);

        ConfigSection chestPlate = new ConfigSection();
        writeItemDataIntoSection(kit.getChestPlate(), chestPlate);
        mainSection.set("chestPlate", chestPlate);

        ConfigSection leggings = new ConfigSection();
        writeItemDataIntoSection(kit.getLeggings(), leggings);
        mainSection.set("leggings", leggings);

        ConfigSection boots = new ConfigSection();
        writeItemDataIntoSection(kit.getBoots(), boots);
        mainSection.set("boots", boots);

        ConfigSection itemsSection = new ConfigSection();
        kit.getItemList().forEach((index, item) -> {
            ConfigSection currentItemSection = new ConfigSection();
            writeItemDataIntoSection(item, currentItemSection);
            itemsSection.set(String.valueOf(index), currentItemSection);
        });

        mainSection.set("items", itemsSection);

        this.mainSection.set(kit.getId(), mainSection);
        config.save();
    }

    public void writeItemDataIntoSection(Item item, ConfigSection section) {
        if (item.hasCustomName()) {
            section.set("customName", item.getCustomName());
        }
        section.set("data", itemToString(item));
        section.set("enchantments", enchantmentsToStringList(item.getEnchantments()));
    }

    public Item readItemDataFromSection(ConfigSection section) {
        String[] itemData = section.getString("data").split(":");

        Item item = Item.get(Integer.parseInt(itemData[0]),
                itemData.length >= 2 ? Integer.parseInt(itemData[1]) : 0,
                itemData.length == 3 ? Integer.parseInt(itemData[2]) : 1
        );

        if (section.exists("customName")) {
            item.setCustomName(section.getString("customName"));
        }

        List<String> enchantmentsList = section.getStringList("enchantments");
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

        return item;
    }

    public String itemToString(Item item) {
        return String.format("%s:%s:%s", item.getId(), item.getDamage(), item.getCount());
    }

    public List<String> enchantmentsToStringList(Enchantment[] enchantments) {
        List<String> enchantmentStringList = new ArrayList<>();

        Arrays.stream(enchantments).forEach(enchantment ->
                enchantmentStringList.add(String.format("%s:%s", enchantment.getId(), enchantment.getLevel()))
        );

        return enchantmentStringList;
    }

    public void unregisterKit(String id) {
        unregisterKit(id, true);
    }

    public void unregisterKit(String id, boolean serialize) {
        kitList.remove(id);

        if (!serialize) {
            return;
        }

        mainSection.remove(id);
        config.save();
    }

    public Kit getKit(String id) {
        return kitList.get(id);
    }

    public boolean kitExists(String id) {
        return getKit(id) != null;
    }

    public int getCurrentSeconds() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" day");
            if (days > 1) {
                sb.append("s");
            }
            sb.append(" ");
        }
        sb.append(hours).append(":")
                .append(String.format("%02d", minutes)).append(":")
                .append(String.format("%02d", seconds));
        return sb.toString();
    }
}
