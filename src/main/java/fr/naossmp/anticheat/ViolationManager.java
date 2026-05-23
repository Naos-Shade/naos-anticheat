package fr.naossmp.anticheat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ViolationManager {

    public enum CheckType {
        SPEED, FLY, KILLAURA, XRAY
    }

    private final NaosAnticheat plugin;
    private final Map<UUID, Map<CheckType, Integer>> violations = new HashMap<>();

    public ViolationManager(NaosAnticheat plugin) {
        this.plugin = plugin;
    }

    public void flag(Player player, CheckType type, String detail) {
        UUID id = player.getUniqueId();
        violations.computeIfAbsent(id, k -> new HashMap<>());
        int count = violations.get(id).merge(type, 1, Integer::sum);

        plugin.getLogger().warning("[AC] " + player.getName() + " | " + type + " | " + detail + " | vl=" + count);

        int limit = plugin.getConfig().getInt("violations-before-kick", 5);
        if (count >= limit) {
            violations.get(id).put(type, 0);
            plugin.getServer().getScheduler().runTask(plugin, () ->
                player.kickPlayer("§cAnticheat — " + type.name().toLowerCase() + " détecté")
            );
        }

        if (plugin.getConfig().getBoolean("alerts-to-ops", true)) {
            String msg = "§7[AC] §e" + player.getName() + " §7(" + type + ") vl=" + count + " — " + detail;
            plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> p.isOp() || p.hasPermission("naosanticheat.admin"))
                .forEach(p -> p.sendMessage(msg));
        }
    }

    public void reset(UUID id) {
        violations.remove(id);
    }

    public Map<CheckType, Integer> getViolations(UUID id) {
        return violations.getOrDefault(id, new HashMap<>());
    }

    public void decayAll() {
        for (Map<CheckType, Integer> map : violations.values()) {
            map.replaceAll((type, count) -> Math.max(0, count - 1));
        }
    }
}
