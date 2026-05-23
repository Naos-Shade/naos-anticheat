package fr.naossmp.anticheat.checks;

import fr.naossmp.anticheat.NaosAnticheat;
import fr.naossmp.anticheat.ViolationManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyCheck {

    private final NaosAnticheat plugin;
    private final Map<UUID, Integer> airTicks = new HashMap<>();

    public FlyCheck(NaosAnticheat plugin) {
        this.plugin = plugin;
    }

    public void check(Player player, Location to) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            airTicks.remove(player.getUniqueId());
            return;
        }
        if (player.getAllowFlight() || player.isFlying()) {
            airTicks.remove(player.getUniqueId());
            return;
        }
        // Elytra
        if (player.isGliding()) {
            airTicks.remove(player.getUniqueId());
            return;
        }
        // Slow falling
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            airTicks.remove(player.getUniqueId());
            return;
        }
        // Levitation
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) {
            airTicks.remove(player.getUniqueId());
            return;
        }
        // Dans l'eau ou la lave
        if (player.isInWater() || player.isInLava()) {
            airTicks.remove(player.getUniqueId());
            return;
        }

        if (!player.isOnGround()) {
            int ticks = airTicks.merge(player.getUniqueId(), 1, Integer::sum);
            int threshold = plugin.getConfig().getInt("fly-ticks-threshold", 40);
            if (ticks >= threshold && ticks % threshold == 0) {
                plugin.getViolationManager().flag(player, ViolationManager.CheckType.FLY,
                        "airTicks=" + ticks);
            }
        } else {
            airTicks.put(player.getUniqueId(), 0);
        }
    }

    public void remove(UUID id) {
        airTicks.remove(id);
    }
}
