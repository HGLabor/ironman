package gg.norisk.heroes.ironman

import gg.norisk.heroes.ironman.abilities.FlyAbility
import gg.norisk.heroes.ironman.abilities.TransformAbility
import gg.norisk.heroes.ironman.abilities.keybindings.KeyBindingManager
import gg.norisk.heroes.ironman.registry.ItemRegistry
import gg.norisk.heroes.ironman.registry.SoundRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

object IronManManager : ModInitializer, DedicatedServerModInitializer, ClientModInitializer {
    val modId = "ironman"
    fun String.toId() = Identifier(modId, this)
    val logger = LogManager.getLogger("modId")
    val skin = "textures/ironman_skin.png".toId()

    override fun onInitialize() {
        logger.info("Starting IronMan...")
        // Common initialization
        FlyAbility.initServer()
        TransformAbility.initServer()
        SoundRegistry.init()
        ItemRegistry.init()
    }

    override fun onInitializeClient() {
        logger.info("Starting Client IronMan...")
        // Client initialization
        KeyBindingManager.init()
        FlyAbility.initClient()
    }

    override fun onInitializeServer() {
        logger.info("Starting Server IronMan...")
        // Dedicated server initialization
    }
}
