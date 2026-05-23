package fr.naossmp.anticheat;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class DeathTracker {

    private static final long H24 = 24 * 60 * 60 * 1000L;

    // killerUUID → victimUUID → liste de timestamps
    private final Map<UUID, Map<UUID, List<Long>>> killMap = new HashMap<>();

    public void recordKill(Player killer, Player victim) {
        UUID kId = killer.getUniqueId();
        UUID vId = victim.getUniqueId();
        long now = System.currentTimeMillis();

        killMap.computeIfAbsent(kId, k -> new HashMap<>())
               .computeIfAbsent(vId, k -> new ArrayList<>());

        List<Long> times = killMap.get(kId).get(vId);
        times.removeIf(t -> now - t > H24);
        times.add(now);

        int count = times.size();

        if (count <= 3) {
            killer.sendMessage("§e⚠ [" + count + "/3] Tu as tué §f" + victim.getName()
                    + " §e" + count + " fois aujourd'hui.");
            if (count == 3) {
                killer.sendMessage("§c⚠ Dernier avertissement — prochain kill = ban permanent !");
            }
        } else {
            String reason = "§cHarcèlement — a tué " + victim.getName() + " 4 fois en 24h";
            Bukkit.getBanList(BanList.Type.NAME).addBan(killer.getName(), reason, null, "NaosAnticheat");
            killer.kickPlayer(reason);
            Bukkit.broadcastMessage("§c[AC] §f" + killer.getName() + " §ca été banni pour harcèlement.");
        }
    }
}
