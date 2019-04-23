package jp.jyn.chestsafe_converter.converter;

import jp.jyn.chestsafe.protection.Protection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class ChestSafeV1Converter extends Converter {
    private final Connection connection;
    private final PreparedStatement protection;
    private final PreparedStatement member;
    private final PreparedStatement flag;

    private final Map<Integer, String> worlds = new HashMap<>();
    private final Map<Integer, UUID> users = new HashMap<>();

    private final Queue<List<Map.Entry<Integer, RawLocation>>> queue;
    private int checked = 0, converted = 0;

    public ChestSafeV1Converter(CommandSender sender, Connection connection,int speed) {
        super(sender);
        this.connection = connection;
        try {
            protection = connection.prepareStatement("SELECT `owner`,`type`,`has_member`,`has_flag` FROM `protection_info` WHERE `id`=?");
            member = connection.prepareStatement("SELECT `member` FROM `protection_member` WHERE `id`=?");
            flag = connection.prepareStatement("SELECT `flag`,`value` FROM `protection_flag` WHERE `id`=?");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        loadWorld();
        loadUser();

        Map<Integer, RawLocation> ids = getIds();
        this.queue = divide(ids.entrySet(), speed);

        msg("Ready: size=" + ids.size());
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        try {
            flag.close();
            member.close();
            protection.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            super.cancel();
        }
    }

    @Override
    public void run() {
        for (Map.Entry<Integer, RawLocation> entry : queue.remove()) {
            checked += 1;
            RawLocation location = entry.getValue();
            if (location.world == null) {
                msg("Unknown world: id=" + entry.getKey());
                continue;
            }

            World world = Bukkit.getWorld(location.world);
            if (world == null) {
                msg("Unknown world: name=" + location.world);
                continue;
            }
            Location loc = new Location(world, location.x, location.y, location.z);
            Block block = loc.getBlock();

            if (set(getProtection(entry.getKey()), block)) {
                converted += 1;
            }
        }

        msg("Progress: checked=" + checked + ", converted=" + converted + ", error=" + (checked - converted));
        if (queue.isEmpty()) {
            this.cancel();
            msg("Completed!!");
        }
    }

    private Protection getProtection(int id) {
        Protection p = Protection.newProtection();

        boolean hasMember, hasFlag;

        try {
            protection.setInt(1, id);
            try (ResultSet result = protection.executeQuery()) {
                if (!result.next()) {
                    msg("Protection not found: id=" + id);
                    return null;
                }
                UUID uuid = users.get(result.getInt("owner"));
                if (uuid == null) {
                    msg("User not found: id=" + id);
                    return null;
                }
                p.setOwner(uuid).setType(Protection.Type.valueOf(result.getInt("type")));
                hasMember = result.getBoolean("has_member");
                hasFlag = result.getBoolean("has_flag");
            }

            if (hasMember) {
                member.setInt(1, id);
                try (ResultSet result = member.executeQuery()) {
                    while (result.next()) {
                        UUID uuid = users.get(result.getInt("member"));
                        if (uuid == null) {
                            msg("Member not found: id=" + id);
                            continue;
                        }
                        p.addMember(uuid);
                    }
                }
            }

            if (hasFlag) {
                flag.setInt(1, id);
                try (ResultSet result = flag.executeQuery()) {
                    while (result.next()) {
                        p.setFlag(Protection.Flag.valueOf(result.getInt("flag")), result.getBoolean("value"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return p;
    }

    private Map<Integer, RawLocation> getIds() {
        Map<Integer, RawLocation> result = new HashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT `id`,`world`,`x`,`y`,`z` FROM `id_protection`")) {
            while (resultSet.next()) {
                result.put(
                    resultSet.getInt("id"),
                    new RawLocation(
                        worlds.get(resultSet.getInt("world")),
                        resultSet.getInt("x"),
                        resultSet.getInt("y"),
                        resultSet.getInt("z")
                    )
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private void loadWorld() {
        try (PreparedStatement statement = connection.prepareStatement("SELECT `id`,`name` FROM `id_world`");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                worlds.put(result.getInt("id"), result.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadUser() {
        try (PreparedStatement statement = connection.prepareStatement("SELECT `id`,`uuid` FROM `id_user`");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                users.put(result.getInt("id"), byteToUUID(result.getBytes("uuid")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private UUID byteToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long most = bb.getLong();
        long least = bb.getLong();
        return new UUID(most, least);
    }

    private static class RawLocation {
        private final String world;
        private final int x, y, z;

        private RawLocation(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
