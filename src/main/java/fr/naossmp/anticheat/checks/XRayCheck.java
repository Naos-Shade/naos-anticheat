package fr.naossmp.anticheat.checks;

import fr.naossmp.anticheat.NaosAnticheat;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class XRayCheck {

    private static final Set<Material> ORES = EnumSet.of(
        Material.DIAMOND_ORE,
        Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE,
        Material.DEEPSLATE_EMERALD_ORE,
        Material.ANCIENT_DEBRIS,
        Material.GOLD_ORE,
        Material.DEEPSLATE_GOLD_ORE,
        Material.IRON_ORE,
        Material.DEEPSLATE_IRON_ORE
    );

    private final NaosAnticheat plugin;
    private final Map<UUID, int[]> stats = new HashMap<>(); // [0]=total, [1]=ores

    public XRayCheck(NaosAnticheat plugin) {
        this.plugin = plugin;
    }

    public void check(Player player, Material broken) {
        UUID id = player.getUniqueId();
        stats.computeIfAbsent(id, k -> new int[]{0, 0});
        int[] s = stats.get(id);

        s[0]++;
        if (ORES.contains(broken)) s[1]++;

        int minBlocks = plugin.getConfig().getInt("xray-min-blocks", 50);
        double oreRatio = plugin.getConfig().getDouble("xray-ore-ratio", 0.6);

        if (s[0] >= minBlocks) {
            double ratio = (double) s[1] / s[0];
            if (ratio >= oreRatio) {
                String msg = "§7[AC] §6⚠ XRay suspect: §f" + player.getName()
                        + " §7— " + s[1] + "/" + s[0] + " minerais (" + String.format("%.0f%%", ratio * 100) + ")";
                plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> p.isOp() || p.hasPermission("naosanticheat.admin"))
                    .forEach(p -> p.sendMessage(msg));
                plugin.getLogger().warning("[AC] XRay suspect: " + player.getName()
                        + " ratio=" + String.format("%.2f", ratio));
                s[0] = 0;
                s[1] = 0;
            } else {
                // Réinitialiser le compteur pour la prochaine fenêtre
                s[0] = 0;
                s[1] = 0;
            }
        }
    }

    public void remove(UUID id) {
        stats.remove(id);
    }
}
