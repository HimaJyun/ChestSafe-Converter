package jp.jyn.chestsafe_converter.converter;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Flag;
import com.griefcraft.model.Permission;
import com.griefcraft.model.Protection;
import jp.jyn.chestsafe.config.MainConfig;
import jp.jyn.chestsafe_converter.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class LwcConverter extends Converter {
    private final LWC lwc;
    private final Map<Material, MainConfig.ProtectionConfig> protectable;

    private final Queue<List<Protection>> queue;
    private int checked = 0, converted = 0;

    public LwcConverter(CommandSender sender, int speed) {
        super(sender);
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LWC");
        if (plugin == null || !plugin.isEnabled()) {
            throw new IllegalStateException("LWC is not Enabled");
        }

        this.lwc = LWC.getInstance();
        this.protectable = Main.getInstance().reflectionGetConfig().getMainConfig().protectable;
        this.queue = divide(lwc.getPhysicalDatabase().loadProtections(), speed);

        msg("Ready: size=" + lwc.getPhysicalDatabase().getProtectionCount());
    }

    @Override
    public void run() {
        for (Protection protection : queue.remove()) {
            checked += 1;
            Block block = protection.getBlock();
            if (block == null) {
                msg("Unknown block: id=" + protection.getId() + ", world=" + protection.getWorld() + ", x=" + protection.getX() + ", y=" + protection.getY() + ", z=" + protection.getZ());
                continue;
            }

            MainConfig.ProtectionConfig config = protectable.get(block.getType());
            if (config == null) {
                msg("Not protectable: id=" + protection.getId() + ", block=" + protection.getBlock().getType());
                continue;
            }

            jp.jyn.chestsafe.protection.Protection p = jp.jyn.chestsafe.protection.Protection.newProtection();
            UUID uuid = str2UUID(protection.getOwner());
            if (uuid == null) {
                msg("Unknown owner: id=" + protection.getId() + ", owner=" + protection.getOwner());
                continue;
            }
            p.setOwner(uuid);

            switch (protection.getType()) {
                case PUBLIC:
                    p.setType(jp.jyn.chestsafe.protection.Protection.Type.PUBLIC);
                    break;
                case PRIVATE:
                    p.setType(jp.jyn.chestsafe.protection.Protection.Type.PRIVATE);
                    break;
                default:
                    msg("Unsupported type: id=" + protection.getId() + ", type=" + protection.getType());
                    p.setType(jp.jyn.chestsafe.protection.Protection.Type.PRIVATE);
            }

            if (protection.hasFlag(Flag.Type.ALLOWEXPLOSIONS)) {
                if (!config.flag.get(jp.jyn.chestsafe.protection.Protection.Flag.EXPLOSION)) {
                    p.setFlag(jp.jyn.chestsafe.protection.Protection.Flag.EXPLOSION, true);
                }
            }

            if (protection.hasFlag(Flag.Type.HOPPER)) {
                boolean hopper = !Boolean.parseBoolean(lwc.resolveProtectionConfiguration(protection.getBlock(), "denyHopper"));
                if (hopper != config.flag.get(jp.jyn.chestsafe.protection.Protection.Flag.HOPPER)) {
                    p.setFlag(jp.jyn.chestsafe.protection.Protection.Flag.HOPPER, hopper);
                }
            }

            if (protection.hasFlag(Flag.Type.REDSTONE)) {
                boolean redstone = !lwc.getConfiguration().getBoolean("protections.denyRedstone", false);
                if (redstone != config.flag.get(jp.jyn.chestsafe.protection.Protection.Flag.REDSTONE)) {
                    p.setFlag(jp.jyn.chestsafe.protection.Protection.Flag.REDSTONE, redstone);
                }
            }

            for (Permission permission : protection.getPermissions()) {
                if (permission.getType() != Permission.Type.PLAYER) {
                    msg("Unsupported permission: id=" + protection.getId() + ", type=" + permission.getType());
                    continue;
                }

                UUID member = str2UUID(permission.getName());
                if (member == null) {
                    msg("Unknown member: id=" + protection.getId() + ", member=" + permission.getName());
                    continue;
                }

                p.addMember(member);
            }

            if (set(p, protection.getBlock())) {
                converted += 1;
            }
        }

        msg("Progress: checked=" + checked + ", converted=" + converted + ", error=" + (checked - converted));
        if (queue.isEmpty()) {
            this.cancel();
            msg("Completed!!");
        }
    }

    private UUID str2UUID(String string) {
        try {
            return UUID.fromString(string);
        } catch (IllegalArgumentException ignore) { }
        return null;
    }
}
