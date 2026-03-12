package id.tntchallenge;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class TNTChallengePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("TNTChallenge plugin aktif!");
        getCommand("spawntnt").setExecutor(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("TNTChallenge plugin nonaktif.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /spawntnt <jumlah> [nama_player]
        // Kalau nama player tidak disebutkan, spawn ke semua player online
        if (!command.getName().equalsIgnoreCase("spawntnt")) return false;

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /spawntnt <jumlah> [nama_player]");
            return true;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(args[0]);
            if (jumlah < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage("§cJumlah TNT harus angka positif!");
            return true;
        }

        int maxTNT = getConfig().getInt("max-tnt-per-donate", 50);
        if (jumlah > maxTNT) {
            jumlah = maxTNT;
            sender.sendMessage("§eJumlah TNT dibatasi ke " + maxTNT);
        }

        if (args.length >= 2) {
            // Spawn ke player tertentu
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer " + args[1] + " tidak ditemukan / offline!");
                return true;
            }
            spawnTNTUnderPlayer(target, jumlah);
            sender.sendMessage("§aSukses spawn " + jumlah + " TNT di bawah " + target.getName());
        } else {
            // Spawn ke semua player online
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            if (players.isEmpty()) {
                sender.sendMessage("§cTidak ada player online!");
                return true;
            }
            for (Player p : players) {
                spawnTNTUnderPlayer(p, jumlah);
            }
            sender.sendMessage("§aSukses spawn " + jumlah + " TNT ke semua player online!");
        }

        return true;
    }

    private void spawnTNTUnderPlayer(Player player, int jumlah) {
        Location loc = player.getLocation();
        int fuseDelay = getConfig().getInt("fuse-ticks", 60); // default 3 detik

        for (int i = 0; i < jumlah; i++) {
            // Spawn TNT 1 block di bawah kaki player
            Location spawnLoc = loc.clone().subtract(0, 1, 0);

            // Random sedikit supaya tidak tumpuk persis
            double offsetX = (Math.random() - 0.5) * 2;
            double offsetZ = (Math.random() - 0.5) * 2;
            spawnLoc.add(offsetX, 0, offsetZ);

            TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(spawnLoc, EntityType.PRIMED_TNT);
            tnt.setFuseTicks(fuseDelay);
        }

        // Kirim pesan ke player
        String msg = getConfig().getString("message-player",
                "§c💣 DONATE MASUK! §e{jumlah} TNT §cspawn di bawahmu!");
        msg = msg.replace("{jumlah}", String.valueOf(jumlah));
        player.sendMessage(msg);
    }
}

