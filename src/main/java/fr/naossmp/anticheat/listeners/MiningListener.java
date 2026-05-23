package fr.naossmp.anticheat.listeners;

import fr.naossmp.anticheat.NaosAnticheat;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningListener implements Listener {

    private final NaosAnticheat plugin;

    public MiningListener(NaosAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        plugin.getXRayCheck().check(event.getPlayer(), event.getBlock().getType());
    }
}
