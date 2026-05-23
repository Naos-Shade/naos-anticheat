package fr.naossmp.anticheat.checks;

import fr.naossmp.anticheat.NaosAnticheat;
import fr.naossmp.anticheat.ViolationManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpeedCheck {

    // Max vitesse horizontale sprint sans potion ≈ 0.281 blocs/tick
    private static final double BASE_MAX = 0.281;

    private final NaosAnticheat plugin;
    private final Map<UUID, Integer> consecutiveFlags = new HashMap<>();

    public SpeedCheck(NaosAnticheat plugin) {
        this.plugin = plugin;
    }

    public void check(Player player, Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        double limit = BASE_MAX * plugin.getConfig().getDouble("speed-multiplier", 1.5);

        // Ajustement selon la potion de vitesse
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int lvl = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1;
            limit += lvl * 0.1;
        }

        // Ignorer les téléportations
        if (dist > 20) {
            consecutiveFlags.remove(player.getUniqueId());
            return;
        }

        if (dist > limit) {
            int count = consecutiveFlags.merge(player.getUniqueId(), 1, Integer::sum);
            if (count >= 5) {
                consecutiveFlags.put(player.getUniqueId(), 0);
                plugin.getViolationManager().flag(player, ViolationManager.CheckType.SPEED,
                        String.format("dist=%.3f max=%.3f", dist, limit));
            }
        } else {
            consecutiveFlags.put(player.getUniqueId(), 0);
        }
    }

    public void remove(UUID id) {
        consecutiveFlags.remove(id);
    }
}
