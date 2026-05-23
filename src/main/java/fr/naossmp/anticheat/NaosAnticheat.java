package fr.naossmp.anticheat;

import fr.naossmp.anticheat.checks.XRayCheck;
import fr.naossmp.anticheat.listeners.DeathListener;
import fr.naossmp.anticheat.listeners.MiningListener;
import org.bukkit.plugin.java.JavaPlugin;

public class NaosAnticheat extends JavaPlugin {

    private DeathTracker deathTracker;
    private XRayCheck xrayCheck;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        deathTracker = new DeathTracker();
        xrayCheck = new XRayCheck(this);

        var pm = getServer().getPluginManager();
        pm.registerEvents(new MiningListener(this), this);
        pm.registerEvents(new DeathListener(this), this);

        getLogger().info("NaosAnticheat activé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("NaosAnticheat désactivé.");
    }

    public DeathTracker getDeathTracker() { return deathTracker; }
    public XRayCheck getXRayCheck() { return xrayCheck; }
}
