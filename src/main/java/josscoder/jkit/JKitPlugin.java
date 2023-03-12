package josscoder.jkit;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.FormAPI;
import josscoder.jkit.command.KitCommand;
import josscoder.jkit.helper.Helper;
import lombok.Getter;

public class JKitPlugin extends PluginBase {

    @Getter
    private static JKitPlugin instance;

    @Getter
    private Helper helper;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (FormAPI.mainThread == null) { //Hack to avoid bug when you are already using this api
            FormAPI.init(this);
        }

        helper = new Helper(getConfig());

        getServer().getCommandMap().register("kit", new KitCommand());

        getLogger().info(TextFormat.DARK_GREEN + "JKit has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info(TextFormat.DARK_RED + "JKit has been disabled!");
    }
}