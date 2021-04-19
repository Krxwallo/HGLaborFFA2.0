package de.hglabor.plugins.ffa.commands;

import de.hglabor.plugins.ffa.player.FFAPlayer;
import de.hglabor.plugins.ffa.player.PlayerList;
import de.hglabor.plugins.kitapi.kit.kits.SoulstealerKit;
import de.hglabor.utils.noriskutils.command.AbstractCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuicideCommand extends AbstractCommand {
    public SuicideCommand() {
        super("suicide");
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (super.onCommand(commandSender, command, s, strings)) {
            Player player = (Player) commandSender;
            FFAPlayer ffaPlayer = PlayerList.getInstance().getPlayer(player);

            if (ffaPlayer.isInArena()) {
                player.setHealth(0);
                for (Object kit : ffaPlayer.getKits()) {
                    if (kit instanceof SoulstealerKit) {
                        // Kill player again so he exits "soulstealer mode"
                        player.setHealth(0);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
