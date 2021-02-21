package de.hglabor.plugins.ffa;

import de.hglabor.plugins.ffa.commands.ReloadMapCommand;
import de.hglabor.plugins.ffa.commands.SuicideCommand;
import de.hglabor.plugins.ffa.config.FFAConfig;
import de.hglabor.plugins.ffa.gamemechanics.SoupHealing;
import de.hglabor.plugins.ffa.gamemechanics.Tracker;
import de.hglabor.plugins.ffa.kit.KitItemListener;
import de.hglabor.plugins.ffa.kit.KitSelectorFFA;
import de.hglabor.plugins.ffa.listener.FFADeathListener;
import de.hglabor.plugins.ffa.listener.FFAJoinListener;
import de.hglabor.plugins.ffa.listener.FFAQuitListener;
import de.hglabor.plugins.ffa.listener.TestListener;
import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.ffa.util.ScoreboardManager;
import de.hglabor.plugins.ffa.world.ArenaManager;
import de.hglabor.plugins.ffa.world.ArenaSettings;
import de.hglabor.plugins.kitapi.KitApi;
import de.hglabor.plugins.kitapi.kit.events.KitEventHandlerImpl;
import de.hglabor.plugins.kitapi.listener.LastHitDetection;
import de.hglabor.utils.localization.Localization;
import de.hglabor.utils.noriskutils.feast.FeastListener;
import de.hglabor.utils.noriskutils.listener.DamageNerf;
import de.hglabor.utils.noriskutils.listener.DurabilityFix;
import de.hglabor.utils.noriskutils.listener.OldKnockback;
import de.hglabor.utils.noriskutils.listener.RemoveHitCooldown;
import de.hglabor.utils.noriskutils.scoreboard.ScoreboardFactory;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class FFA extends JavaPlugin {
    private static FFA plugin;
    private static ArenaManager arenaManager;
    private static FFARunnable ffaRunnable;

    public static FFARunnable getFFARunnable() {
        return ffaRunnable;
    }

    public static ArenaManager getArenaManager() {
        return arenaManager;
    }

    public static FFA getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        this.loadLocalizationFiles();
        KitApi.getInstance().register(PlayerList.getInstance(), KitSelectorFFA.getInstance(), this);
        FFAConfig.load();
        World world = Bukkit.getWorld("world");
        arenaManager = new ArenaManager(world, FFAConfig.getInteger("ffa.size"));
        ffaRunnable = new FFARunnable(world, FFAConfig.getInteger("ffa.duration"));
        ffaRunnable.runTaskTimer(this, 0, 20);
        ScoreboardManager scoreboardManager = new ScoreboardManager();
        scoreboardManager.runTaskTimer(this, 0, 20);

        this.registerListeners();
        this.registerCommands();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            FFAPlayer player = PlayerList.getInstance().getPlayer(onlinePlayer);
            onlinePlayer.setMetadata("oldKnockback", new FixedMetadataValue(FFA.getPlugin(), ""));
            PlayerList.getInstance().add(player);
            ScoreboardFactory.create(player);
            Bukkit.getOnlinePlayers().forEach(newPlayer -> {
                ScoreboardFactory.addPlayerToNoCollision(newPlayer, player);
            });
            arenaManager.prepareKitSelection(onlinePlayer);
        }
    }

    @Override
    public void onDisable() {
    }

    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(KitSelectorFFA.getInstance(), this);
        pluginManager.registerEvents(new ArenaSettings(), this);
        pluginManager.registerEvents(new KitEventHandlerImpl(), this);
        pluginManager.registerEvents(new KitItemListener(), this);
        pluginManager.registerEvents(new FFAJoinListener(), this);
        pluginManager.registerEvents(new FFAQuitListener(), this);
        pluginManager.registerEvents(new FFADeathListener(), this);
        //mechanics
        pluginManager.registerEvents(new SoupHealing(), this);
        pluginManager.registerEvents(new Tracker(), this);
        pluginManager.registerEvents(new DamageNerf(FFAConfig.getDouble("damage.sword.nerf"),FFAConfig.getDouble("damage.other.nerf")), this);
        pluginManager.registerEvents(new DurabilityFix(), this);
        pluginManager.registerEvents(new FeastListener(), this);
        pluginManager.registerEvents(new RemoveHitCooldown(), this);
        pluginManager.registerEvents(new LastHitDetection(), this);
        pluginManager.registerEvents(new OldKnockback(this), this);
        pluginManager.registerEvents(new TestListener(), this);
    }

    private void loadLocalizationFiles() {
        try {
            Localization.INSTANCE.loadLanguageFiles(Paths.get(FFA.getPlugin().getDataFolder() + "/lang"), "§");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        this.getCommand("suicide").setExecutor(new SuicideCommand());
        this.getCommand("reloadmap").setExecutor(new ReloadMapCommand());
    }
}
