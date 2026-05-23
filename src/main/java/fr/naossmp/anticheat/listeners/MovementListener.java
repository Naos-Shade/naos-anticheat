package fr.naossmp.anticheat.listeners;

import fr.naossmp.anticheat.NaosAnticheat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {

    private final NaosAnticheat plugin;

    public MovementListener(NaosAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) return;

        var player = event.getPlayer();
        plugin.getSpeedCheck().check(player, event.getFrom(), event.getTo());
        plugin.getFlyCheck().check(player, event.getTo());
    }
}
