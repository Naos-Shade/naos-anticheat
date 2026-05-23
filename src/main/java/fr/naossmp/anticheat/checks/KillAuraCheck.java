package fr.naossmp.anticheat.checks;

import fr.naossmp.anticheat.NaosAnticheat;
import fr.naossmp.anticheat.ViolationManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class KillAuraCheck {

    private final NaosAnticheat plugin;
    // Timestamps des attaques par joueur (pour CPS)
    private final Map<UUID, List<Long>> attackTimes = new HashMap<>();
    // Dernières entités frappées + timestamp (pour multi-target)
    private final Map<UUID, Map<UUID, Long>> recentTargets = new HashMap<>();

    public KillAuraCheck(NaosAnticheat plugin) {
        this.plugin = plugin;
    }

    public void check(Player attacker, Entity victim) {
        UUID aid = attacker.getUniqueId();
        long now = System.currentTimeMillis();
        double reachLimit = plugin.getConfig().getDouble("killa-reach-limit", 4.5);
        int cpsLimit = plugin.getConfig().getInt("killa-cps-limit", 20);

        // Vérification portée
        double dist = attacker.getLocation().distance(victim.getLocation());
        if (dist > reachLimit) {
            plugin.getViolationManager().flag(attacker, ViolationManager.CheckType.KILLAURA,
                    String.format("reach=%.2f max=%.2f", dist, reachLimit));
            return;
        }

        // Suivi CPS (fenêtre 1 seconde)
        attackTimes.computeIfAbsent(aid, k -> new ArrayList<>());
        List<Long> times = attackTimes.get(aid);
        times.removeIf(t -> now - t > 1000);
        times.add(now);
        if (times.size() > cpsLimit) {
            plugin.getViolationManager().flag(attacker, ViolationManager.CheckType.KILLAURA,
                    "cps=" + times.size());
        }

        // Multi-target : 3 entités différentes en < 200ms
        recentTargets.computeIfAbsent(aid, k -> new HashMap<>());
        Map<UUID, Long> targets = recentTargets.get(aid);
        targets.entrySet().removeIf(e -> now - e.getValue() > 200);
        targets.put(victim.getUniqueId(), now);
        if (targets.size() >= 3) {
            plugin.getViolationManager().flag(attacker, ViolationManager.CheckType.KILLAURA,
                    "multi-target=" + targets.size());
            targets.clear();
        }
    }

    public void remove(UUID id) {
        attackTimes.remove(id);
        recentTargets.remove(id);
    }
}
