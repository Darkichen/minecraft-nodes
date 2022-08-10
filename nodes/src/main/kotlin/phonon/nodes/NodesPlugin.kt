/*
 * Implement bukkit plugin interface
 */

package phonon.nodes

import com.earth2me.essentials.Essentials
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin
import phonon.nodes.commands.*
import phonon.nodes.listeners.*
import phonon.nodes.objects.Nametag
import phonon.nodes.tasks.*
import phonon.nodes.utils.loadLongFromFile
import java.io.File

public class NodesPlugin : JavaPlugin() {
    
    override fun onEnable() {
        
        // measure load time
        val timeStart = System.currentTimeMillis()

        val logger = this.getLogger()
        val pluginManager = this.getServer().getPluginManager()

        // initialize nodes
        Nodes.initialize(this)

        // ===================================
        // save hooks to external plugins
        // ===================================
        // essentials
        val essentials = pluginManager.getPlugin("Essentials")
        if ( essentials !== null ) {
            Nodes.hookEssentials(essentials as Essentials)
            logger.info("Using Essentials v${essentials.getDescription().getVersion()}")
        }
        
        // dynmap hook, just flag that dynmap exists
        val dynmap = pluginManager.getPlugin("dynmap")
        if ( dynmap !== null ) {
            Nodes.hookDynmap()
            logger.info("Using Dynmap v${dynmap.getDescription().getVersion()}")
        }

        // protocol lib, may be needed?
        val protocolLib = pluginManager.getPlugin("ProtocolLib")
        if ( protocolLib !== null && Config.useNametags ) {
            Nodes.hookProtocolLib()
            logger.info("Using ProtocolLib v${protocolLib.getDescription().getVersion()}")
        }

        // ===================================
        // load config
        // ===================================
        Nodes.reloadConfig()

        Nodes.war.initialize(Config.flagMaterials)

        // register listeners
        pluginManager.registerEvents(NodesBlockGrowListener(), this)
        pluginManager.registerEvents(NodesChatListener(), this)
        pluginManager.registerEvents(NodesChestProtectionListener(), this)
        pluginManager.registerEvents(NodesChestProtectionDestroyListener(), this)
        pluginManager.registerEvents(NodesDiplomacyAllianceListener(), this)
        pluginManager.registerEvents(NodesDiplomacyTruceExpiredListener(), this)
        pluginManager.registerEvents(NodesEntityBreedListener(), this)
        pluginManager.registerEvents(NodesGuiListener(), this)
        pluginManager.registerEvents(NodesIncomeInventoryListener(), this)
        pluginManager.registerEvents(NodesWorldListener(), this)
        pluginManager.registerEvents(NodesPlayerAFKKickListener(), this)
        pluginManager.registerEvents(NodesPlayerJoinQuitListener(), this)
        pluginManager.registerEvents(NodesPlayerMoveListener(), this)
        pluginManager.registerEvents(NodesSheepShearListener(), this)
        pluginManager.registerEvents(NodesNametagListener(), this)

        // register commands
        this.getCommand("town")?.setExecutor(TownCommand())
        this.getCommand("nation")?.setExecutor(NationCommand())
        this.getCommand("nodes")?.setExecutor(NodesCommand())
        this.getCommand("nodesadmin")?.setExecutor(NodesAdminCommand())
        this.getCommand("ally")?.setExecutor(AllyCommand())
        this.getCommand("unally")?.setExecutor(UnallyCommand())
        this.getCommand("war")?.setExecutor(WarCommand())
        this.getCommand("peace")?.setExecutor(PeaceCommand())
        this.getCommand("truce")?.setExecutor(TruceCommand())
        this.getCommand("globalchat")?.setExecutor(GlobalChatCommand())
        this.getCommand("townchat")?.setExecutor(TownChatCommand())
        this.getCommand("nationchat")?.setExecutor(NationChatCommand())
        this.getCommand("allychat")?.setExecutor(AllyChatCommand())
        this.getCommand("player")?.setExecutor(PlayerCommand())
        this.getCommand("territory")?.setExecutor(TerritoryCommand())
        this.getCommand("toggleneutral")?.setExecutor(NeutralCommand())
        
        // override command aliases tab complete if they exist
        this.getCommand("t")?.setTabCompleter(this.getCommand("town")?.getExecutor() as TabCompleter)
        this.getCommand("n")?.setTabCompleter(this.getCommand("nation")?.getExecutor() as TabCompleter)
        this.getCommand("nd")?.setTabCompleter(this.getCommand("nodes")?.getExecutor() as TabCompleter)
        this.getCommand("nda")?.setTabCompleter(this.getCommand("nodesadmin")?.getExecutor() as TabCompleter)
        this.getCommand("gc")?.setTabCompleter(this.getCommand("globalchat")?.getExecutor() as TabCompleter)
        this.getCommand("p")?.setTabCompleter(this.getCommand("player")?.getExecutor() as TabCompleter)
        
        // load world
        val pluginPath = Config.pathPlugin
        logger.info("Loading world from: ${pluginPath}")
        if ( Nodes.loadWorld(pluginPath) == true ) { // successful load
            // print number of resource nodes and territories loaded
            logger.info("- Resource Nodes: ${Nodes.getResourceNodeCount()}")
            logger.info("- Territories: ${Nodes.getTerritoryCount()}")
            logger.info("- Residents: ${Nodes.getResidentCount()}")
            logger.info("- Towns: ${Nodes.getTownCount()}")
            logger.info("- Nations: ${Nodes.getNationCount()}")
        }

        // load current income tick
        val currTime = System.currentTimeMillis()
        Nodes.lastBackupTime = loadLongFromFile(Config.pathLastBackupTime) ?: currTime
        Nodes.lastIncomeTime = loadLongFromFile(Config.pathLastIncomeTime) ?: currTime

        // run background schedulers/tasks
        CopyClaimsConfigToDynmap.run(this)
        Nodes.reloadManagers()

        // initialize all players online
        Nodes.initializeOnlinePlayers()

        // set final initialized flag
        Nodes.initialized = true
        Nodes.enabled = true

        // print load time
        val timeEnd = System.currentTimeMillis()
        val timeLoad = timeEnd - timeStart
        logger.info("Enabled in ${timeLoad}ms")

        // print success message
        logger.info("now this is epic")
    }

    override fun onDisable() {
        logger.info("wtf i hate xeth now")

        Nodes.enabled = false

        // world cleanup
        Nodes.cleanup()
        
        // final save of world
        // -> only save when world was properly initialized,
        //    to avoid saving junk empty data when plugin fails load
        if ( Nodes.initialized ) {
            Nodes.saveWorldSync()
        }
    }
}
