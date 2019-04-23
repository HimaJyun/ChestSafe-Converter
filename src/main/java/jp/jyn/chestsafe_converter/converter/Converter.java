package jp.jyn.chestsafe_converter.converter;

import jp.jyn.chestsafe.protection.Protection;
import jp.jyn.chestsafe.protection.ProtectionRepository;
import jp.jyn.chestsafe_converter.Main;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

public abstract class Converter extends BukkitRunnable {
    private final CommandSender[] senders;
    private final ProtectionRepository repository;

    protected Converter(CommandSender sender) {
        if (sender instanceof Player) {
            senders = new CommandSender[]{sender, Bukkit.getConsoleSender()};
        } else {
            senders = new CommandSender[]{sender};
        }

        repository = Main.getInstance().getChestSafe().getRepository();
    }

    public abstract void run();

    protected boolean set(Protection protection, Block block) {
        ProtectionRepository.Result result = repository.set(protection, block);
        switch (result) {
            case ALREADY_PROTECTED:
                msg("Already protected: location=" + block.getLocation());
                return false;
            case NOT_PROTECTABLE:
                msg("Not protectable: block=" + block.getType() + ", location=" + block.getLocation());
                return false;
        }
        return true;
    }

    protected <T> Queue<List<T>> divide(Collection<T> origin, int size) {
        if (origin == null) {
            return new ArrayDeque<>(0);
        }
        if (origin.size() <= size) {
            Queue<List<T>> queue = new ArrayDeque<>(1);
            queue.add(new ArrayList<>(origin));
            return queue;
        }

        Queue<List<T>> result = new ArrayDeque<>(Math.max(origin.size() / size, 1));
        List<T> tmp = new ArrayList<>(size);
        for (T t : origin) {
            tmp.add(t);

            if (tmp.size() >= size) {
                result.add(tmp);
                tmp = new ArrayList<>(size);
            }
        }
        if (!tmp.isEmpty()) {
            result.add(tmp);
        }
        return result;
    }

    protected void msg(String msg) {
        for (CommandSender sender : senders) {
            sender.sendMessage(msg);
        }
    }
}
