package phonon.nodes.commands

/**
 * Commands to toggle neutrality
 */

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import phonon.nodes.Message
import phonon.nodes.Nodes
import phonon.nodes.objects.Nation
import phonon.nodes.objects.Town
import phonon.nodes.war.*

/**
 * @command /neutral
 * Toggles the neutrality of a nation
 */
public class NeutralCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): Boolean {

        // no args, print help
        if ( args.size != 0 ) {
            printHelp(sender)
            return true
        }

        if ( !(sender is Player) ) {
            return true
        }

        val player: Player = sender
        val resident = Nodes.getResident(player)
        if ( resident == null ) {
            return true
        }

        val town = resident.town
        if ( town == null ) {
            return true
        }

        val nation = town.nation
        if ( nation !== null && town !== nation.capital ) {
            Message.error(player, "Only the nation's capital town can toggle neutrality")
            return true
        }

        if ( resident !== town.leader && !town.officers.contains(resident) ) {
            Message.error(player, "Only the leader and officers can toggle neutrality")
            return true
        }

        return true
    }

    private fun printHelp(sender: CommandSender) {
        Message.print(sender, "[Nodes] Neutral commands:")
        Message.print(sender, "/neutral${ChatColor.WHITE}: Toggles neutrality of a nation")
        return
    }

    // toggles the neutrality of a nation
    private fun toggleNeutrality(player: Player, town: Town, townNation: Nation?) {
        if (townNation != null) {
            townNation?.toggleNeutrality()
            Message.print(player, if (townNation.getNeutrality()) "Your nation is now neutral." else "Your nation is no longer neutral")
            Message.error(player, "Nation is null")
        }
    }

}
