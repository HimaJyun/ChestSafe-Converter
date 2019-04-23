package jp.jyn.chestsafe_converter.config;

import jp.jyn.chestsafe_converter.Main;
import jp.jyn.chestsafe_converter.converter.ChestSafeV1Converter;
import jp.jyn.chestsafe_converter.converter.Converter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;

public class ChestSafeConfig implements InteractiveConfig {
    private enum Type {MYSQL, SQLITE}

    private final CommandSender sender;

    private Type type = null;

    private Path sqliteFile = null;
    private String mysqlHost = null;
    private String mysqlName = null;
    private String mysqlUser = null;
    private String mysqlPass = null;
    private Connection connection = null;

    private Converter converter = null;

    public ChestSafeConfig(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean next(Queue<String> args) {
        if (type == null) {
            return selectDB(args);
        }

        if (type == Type.SQLITE) {
            if (sqliteFile == null) {
                return sqliteFile(args) && checkVersion();
            }
        } else if (type == Type.MYSQL) {
            if (mysqlHost == null) {
                return mysqlHost(args);
            }
            if (mysqlName == null) {
                return mysqlName(args);
            }
            if (mysqlUser == null) {
                return mysqlUser(args);
            }
            if (mysqlPass == null) {
                return mysqlPass(args) && checkVersion();
            }
        }
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

    @Override
    public boolean check() {
        sender.sendMessage("Checking required requirements...");
        Plugin plugin = Main.getInstance().getChestSafe();
        if (plugin == null) {
            sender.sendMessage("Error: ChestSafe is not Available");
            return false;
        }

        sender.sendMessage("OK: ChestSafe available.");
        sender.sendMessage("");
        sender.sendMessage("Please select the type of database from which you are importing.(sqlite/mysql)");
        return true;
    }

    private boolean selectDB(Queue<String> args) {
        String db = args.poll();
        if (db == null) {
            sender.sendMessage("Automatically acquires from config file.");
            String tmp = getConfig("database.type");
            if (tmp == null) {
                sender.sendMessage("Automatic acquisition failed. Please set it manually.");
                return true;
            }
            if (tmp.equalsIgnoreCase("sqlite")) {
                db = "mysql";
            } else if (tmp.equalsIgnoreCase("mysql")) {
                db = "sqlite";
            } else {
                db = "unknown";
            }
        }

        if (db.equalsIgnoreCase("sqlite")) {
            type = Type.SQLITE;
            sender.sendMessage("SQLite has been selected.");
            sender.sendMessage("");
            sender.sendMessage("Please specify the location of the database file.");
        } else if (db.equalsIgnoreCase("mysql")) {
            type = Type.MYSQL;
            sender.sendMessage("MySQL has been selected.");
            sender.sendMessage("");
            sender.sendMessage("Please specify the host of MySQL.");
        } else {
            sender.sendMessage("It is a database not supported.");
        }
        return true;
    }

    private boolean sqliteFile(Queue<String> args) {
        String file = args.poll();
        if (file == null) {
            sender.sendMessage("Automatically acquires from config file.");
            file = getConfig("database.sqlite.file");
            if (file == null) {
                sender.sendMessage("Automatic acquisition failed. Please set it manually.");
                return true;
            }
        }

        Plugin plugin = Main.getInstance().getChestSafe();
        if (plugin != null) {
            sqliteFile = Paths.get(plugin.getDataFolder().getAbsolutePath(), file);
        } else {
            sqliteFile = Paths.get(file);
        }

        sender.sendMessage("Checking the existence of the file: " + sqliteFile.toAbsolutePath());
        if (!Files.exists(sqliteFile)) {
            sender.sendMessage("File does not exist.");
            return true;
        }
        sender.sendMessage("The file exists.");
        sender.sendMessage("");
        sender.sendMessage("Testing the database...");
        return true;
    }

    private boolean mysqlHost(Queue<String> args) {
        mysqlHost = args.poll();
        if (mysqlHost == null) {
            sender.sendMessage("Automatically acquires from config file.");
            mysqlHost = getConfig("database.mysql.host");
            if (mysqlHost == null) {
                sender.sendMessage("Automatic acquisition failed. Please set it manually.");
                return true;
            }
        }

        sender.sendMessage("Host: " + mysqlHost);
        sender.sendMessage("");
        sender.sendMessage("Please specify the database name of MySQL.");
        return true;
    }

    private boolean mysqlName(Queue<String> args) {
        mysqlName = args.poll();
        if (mysqlName == null) {
            sender.sendMessage("Automatically acquires from config file.");
            mysqlName = getConfig("database.mysql.name");
            if (mysqlName == null) {
                sender.sendMessage("Automatic acquisition failed. Please set it manually.");
                return true;
            }
        }

        sender.sendMessage("Name: " + mysqlName);
        sender.sendMessage("");
        sender.sendMessage("Please specify the username of MySQL.");
        return true;
    }

    private boolean mysqlUser(Queue<String> args) {
        mysqlUser = args.poll();
        if (mysqlUser == null) {
            sender.sendMessage("Automatically acquires from config file.");
            mysqlUser = getConfig("database.mysql.username");
            if (mysqlUser == null) {
                sender.sendMessage("Automatic acquisition failed. Please set it manually.");
                return true;
            }
        }

        sender.sendMessage("User: " + mysqlUser);
        sender.sendMessage("");
        sender.sendMessage("Please specify the password of MySQL.");
        return true;
    }

    private boolean mysqlPass(Queue<String> args) {
        mysqlPass = args.poll();
        if (mysqlPass == null) {
            sender.sendMessage("Automatically acquires from config file.");
            mysqlPass = getConfig("database.mysql.password");
            if (mysqlPass == null) {
                sender.sendMessage("Automatic acquisition failed. Please set it manually.");
                return true;
            }
        }

        sender.sendMessage("Pass: " + mysqlPass);
        sender.sendMessage("");
        sender.sendMessage("Testing the database...");
        return true;
    }

    private boolean convertSpeed(Queue<String> args) {
        String speed = args.poll();
        if (speed == null) {
            speed = "100";
        }
        try {
            converter = new ChestSafeV1Converter(sender, connection, Integer.parseInt(speed));
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
        sender.sendMessage("Be sure to back up the database of the conversion source and the conversion destination before running!");
        sender.sendMessage("Conversion failure or database corruption can not be restored!!");
    }

    private String getConfig(String key) {
        Plugin plugin = Main.getInstance().getChestSafe();
        if (plugin != null) {
            return plugin.getConfig().getString(key);
        }
        return null;
    }

    private boolean checkVersion() {
        try {
            if (type == Type.MYSQL) {
                connection = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + "/" + mysqlName, mysqlUser, mysqlPass);
            } else if (type == Type.SQLITE) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile.toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (connection == null) {
            sender.sendMessage("Database connection failed.");
            return false;
        }

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT `value` FROM `meta` WHERE `key`='dbversion'")) {
            if (!result.next()) {
                sender.sendMessage("Database confirmation failed.");
            }

            String version = result.getString(1);
            if (!version.equals("1")) {
                sender.sendMessage("Unsupported version.");
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        sender.sendMessage("Successfully confirmed Database.");
        sender.sendMessage("");
        sender.sendMessage("Please enter convert speed.(default: 100)");
        return true;
    }
}
