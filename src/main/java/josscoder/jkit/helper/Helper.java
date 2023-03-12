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
                registerKit(new Kit(key, mainSection.getSection(key)))
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
        mainSection.set("helmet", itemToString(kit.getHelmet()));
        mainSection.set("chestPlate", itemToString(kit.getChestPlate()));
        mainSection.set("leggings", itemToString(kit.getLeggings()));
        mainSection.set("boots", itemToString(kit.getBoots()));

        ConfigSection itemsSection = new ConfigSection();
        kit.getItemList().forEach((index, item) -> {
            ConfigSection currentItemSection = new ConfigSection();
            currentItemSection.set("data", itemToString(item));
            currentItemSection.set("enchantments", enchantmentsToStringList(item.getEnchantments()));

            itemsSection.set(String.valueOf(index), currentItemSection);
        });

        mainSection.set("items", itemsSection);

        this.mainSection.set(kit.getId(), mainSection);
        config.save();
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
