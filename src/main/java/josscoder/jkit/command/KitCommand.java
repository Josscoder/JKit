package josscoder.jkit.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.denzelcode.form.element.Button;
import com.denzelcode.form.window.SimpleWindowForm;
import josscoder.jkit.JKitPlugin;
import josscoder.jkit.data.Kit;
import josscoder.jkit.helper.Helper;

public class KitCommand extends Command {

    public KitCommand() {
        super("kit", "Give a kit", TextFormat.RED + "/kit help");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            selectKit(player);
            return false;
        }

        Helper helper = JKitPlugin.getInstance().getHelper();

        String child = args[0];

        switch (child.toLowerCase()) {
            case "help":
                player.sendMessage(TextFormat.colorize("&l&9Kit Commands"));
                player.sendMessage(TextFormat.WHITE + "/kit help");
                if (player.isOp()) {
                    player.sendMessage(TextFormat.WHITE + "/kit add <id> <display name> <permission> <cooldown>");
                    player.sendMessage(TextFormat.WHITE + "/kit remove <id>");
                }
                break;
            case "add":
                if (args.length < 5 || !player.isOp()) {
                    return false;
                }

                String id = args[1];
                String displayName = args[2];
                String permission = args[3];

                int cooldown;

                try {
                    cooldown = Integer.parseInt(args[4]);
                } catch (Exception e) {
                    player.sendMessage(TextFormat.RED + "Kit cooldown should be int");
                    return false;
                }

                helper.registerKit(new Kit(id, displayName, permission, cooldown, player.getInventory()), true);
                player.sendMessage(TextFormat.GREEN + String.format("Kit %s saved successfully!", id));

                break;
            case "remove":
                if (args.length < 2 || !player.isOp()) {
                    return false;
                }

                String idToRemove = args[1];

                if (!helper.kitExists(idToRemove)) {
                    player.sendMessage(TextFormat.RED + "That kit does not exist!");
                    return false;
                }

                helper.unregisterKit(idToRemove);
                player.sendMessage(TextFormat.RED + String.format("You removed kit %s", idToRemove));
                break;
        }

        return false;
    }


    private void selectKit(Player player) {
        SimpleWindowForm windowForm = new SimpleWindowForm("Majestic Kits");

        Helper helper = JKitPlugin.getInstance().getHelper();

        helper.getKitList().values().forEach(kit -> {
            String timeLeft = kit.getTimeLeftString(player);
            String state = (kit.isAvailable(player) ? TextFormat.DARK_GREEN + "Available" : TextFormat.DARK_RED + (kit.hasCooldown(player) ? timeLeft : "No available"));

            windowForm.addButton(kit.getId(), String.format(
                    "%s\n%s",
                    kit.getName(),
                    state
            ), kit.getImage());
        });

        windowForm.addHandler(event -> {
            if (event.wasClosed()) {
                return;
            }

            Button button = event.getButton();
            if (button == null) {
                return;
            }

            String buttonName = button.getName();

            Kit kit = helper.getKit(buttonName);
            if (kit == null) {
                return;
            }

            if (!player.hasPermission(kit.getPermission())) {
                player.sendMessage(TextFormat.RED + "You do not have permission to equip this kit!");

                return;
            }

            if (kit.hasCooldown(player) && !player.isOp()) {
                String timeLeft = kit.getTimeLeftString(player);
                player.sendMessage(TextFormat.RED + String.format("You have to wait %s to equip this kit again!", timeLeft));

                return;
            }

            kit.give(player);
            player.sendMessage(TextFormat.colorize(String.format("&aYou have equipped the kit %s&a!", kit.getName())));
        });
        windowForm.sendTo(player);
    }
}
