package josscoder.jkit;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import lombok.Getter;

public class JKitPlugin extends PluginBase {

    @Getter
    private static JKitPlugin instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        getLogger().info(TextFormat.DARK_GREEN + "JKit has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info(TextFormat.DARK_RED + "JKit has been disabled!");
    }
}