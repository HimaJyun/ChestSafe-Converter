package jp.jyn.chestsafe_converter;

import jp.jyn.chestsafe.ChestSafe;
import jp.jyn.chestsafe.config.ConfigLoader;
import jp.jyn.chestsafe_converter.config.ChestSafeConfig;
import jp.jyn.chestsafe_converter.config.InteractiveConfig;
import jp.jyn.chestsafe_converter.config.LwcConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Main extends JavaPlugin {
    private static Main instance;
    private final Map<CommandSender, InteractiveConfig> convert = new HashMap<>();

    private ChestSafe chestSafe;
    private ConfigLoader chestSafeConfig;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chestsafe.convert")) {
            sender.sendMessage("You don't have permission!");
            return true;
        }

        if (args.length != 0 && args[0].equalsIgnoreCase("reset")) {
            sender.sendMessage("It was reset.");
            convert.remove(sender);
            return true;
        }

        InteractiveConfig config = convert.get(sender);
        if (config != null) {
            if (!config.next(new ArrayDeque<>(Arrays.asList(args)))) {
                convert.remove(sender);
            }
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Be sure to get a backup of the database before starting work!");
            sender.sendMessage("Please select source type: LWC/ChestSafe");
            sender.sendMessage("Example: /" + command.getName() + " chestsafe");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "lwc":
                config = new LwcConfig(sender);
                break;
            case "chestsafe":
                config = new ChestSafeConfig(sender);
                break;
            default:
                sender.sendMessage("Unknown source type: " + args[0]);
                return true;
        }

        if (config.check()) {
            convert.put(sender, config);
        }
        sender.sendMessage("To start over from the beginning, use /" + command.getName() + " reset.");
        return true;
    }

    public static Main getInstance() {
        return Objects.requireNonNull(instance);
    }

    public ChestSafe getChestSafe() {
        if (chestSafe != null) {
            return chestSafe;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("ChestSafe");
        if (plugin != null && plugin.isEnabled()) {
            this.chestSafe = (ChestSafe) plugin;
        }
        return chestSafe;
    }

    public ConfigLoader reflectionGetConfig() {
        if (chestSafeConfig != null) {
            return chestSafeConfig;
        }

        try {
            Field field = ChestSafe.class.getDeclaredField("config");
            field.setAccessible(true);
            chestSafeConfig = (ConfigLoader) field.get(getChestSafe());
            return chestSafeConfig;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
