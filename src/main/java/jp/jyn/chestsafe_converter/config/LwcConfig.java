package jp.jyn.chestsafe_converter.config;

import jp.jyn.chestsafe_converter.Main;
import jp.jyn.chestsafe_converter.converter.Converter;
import jp.jyn.chestsafe_converter.converter.LwcConverter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Queue;

public class LwcConfig implements InteractiveConfig {
    private final CommandSender sender;

    private Converter converter = null;

    public LwcConfig(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean next(Queue<String> args) {
        if (converter == null) {
            return convertSpeed(args);
        }

        String confirm = args.poll();
        if (confirm != null && confirm.equalsIgnoreCase("confirm")) {
            converter.runTaskTimer(Main.getInstance(), 0, 20);
            return false;
        } else {
            confirmMsg();
            return true;
        }
    }

    public boolean check() {
        sender.sendMessage("Checking required requirements...");
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LWC");
        if (plugin == null || !plugin.isEnabled()) {
            sender.sendMessage("Error: LWC is not Available");
            return false;
        }
        sender.sendMessage("OK: LWC available.");

        plugin = Main.getInstance().getChestSafe();
        if (plugin == null) {
            sender.sendMessage("Error: ChestSafe is not Available");
            return false;
        }

        sender.sendMessage("OK: ChestSafe available.");
        sender.sendMessage("");
        sender.sendMessage("Please enter convert speed.(default: 100)");
        return true;
    }

    private boolean convertSpeed(Queue<String> args) {
        String speed = args.poll();
        if (speed == null) {
            speed = "100";
        }
        try {
            converter = new LwcConverter(sender, Integer.parseInt(speed));
        } catch (NumberFormatException e) {
            sender.sendMessage("Unable to parse numbers!!");
            return true;
        }
        sender.sendMessage("Set speed: " + speed);
        sender.sendMessage("");
        confirmMsg();
        return true;
    }

    private void confirmMsg() {
        sender.sendMessage("You can start work by running \"confirm\"");
        sender.sendMessage("Be sure to back up the LWC and ChestSafe databases before running!");
        sender.sendMessage("Conversion failure or database corruption can not be restored!!");
    }
}
