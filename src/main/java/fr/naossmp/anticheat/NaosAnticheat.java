package fr.naossmp.anticheat;

import fr.naossmp.anticheat.checks.FlyCheck;
import fr.naossmp.anticheat.checks.KillAuraCheck;
import fr.naossmp.anticheat.checks.SpeedCheck;
import fr.naossmp.anticheat.checks.XRayCheck;
import fr.naossmp.anticheat.listeners.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class NaosAnticheat extends JavaPlugin {

    private ViolationManager violationManager;
    private DeathTracker deathTracker;
    private SpeedCheck speedCheck;
    private FlyCheck flyCheck;
    private KillAuraCheck killAuraCheck;
    private XRayCheck xrayCheck;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        violationManager = new ViolationManager(this);
        deathTracker = new DeathTracker();
        speedCheck = new SpeedCheck(this);
        flyCheck = new FlyCheck(this);
        killAuraCheck = new KillAuraCheck(this);
        xrayCheck = new XRayCheck(this);

        var pm = getServer().getPluginManager();
        pm.registerEvents(new MovementListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new MiningListener(this), this);
        pm.registerEvents(new DeathListener(this), this);

        // Decay des violations
        long decayTicks = getConfig().getLong("decay-seconds", 30) * 20L;
        getServer().getScheduler().runTaskTimerAsynchronously(this, violationManager::decayAll, decayTicks, decayTicks);

        getLogger().info("NaosAnticheat activé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("NaosAnticheat désactivé.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("ac")) return true;
        if (!sender.hasPermission("naosanticheat.admin")) {
            sender.sendMessage("§cPas la permission.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§eUsage : /ac <violations|reset> <joueur>");
            return true;
        }
        Player target = getServer().getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("§cJoueur introuvable ou hors ligne.");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "violations" -> {
                Map<ViolationManager.CheckType, Integer> vl = violationManager.getViolations(target.getUniqueId());
                sender.sendMessage("§eViolations de §f" + target.getName() + "§e : " + vl);
            }
            case "reset" -> {
                violationManager.reset(target.getUniqueId());
                sender.sendMessage("§aViolations de §f" + target.getName() + " §aréinitialisées.");
            }
            default -> sender.sendMessage("§eUsage : /ac <violations|reset> <joueur>");
        }
        return true;
    }

    public ViolationManager getViolationManager() { return violationManager; }
    public DeathTracker getDeathTracker() { return deathTracker; }
    public SpeedCheck getSpeedCheck() { return speedCheck; }
    public FlyCheck getFlyCheck() { return flyCheck; }
    public KillAuraCheck getKillAuraCheck() { return killAuraCheck; }
    public XRayCheck getXRayCheck() { return xrayCheck; }
}
