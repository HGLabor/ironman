package gg.norisk.heroes.ironman

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager

object IronManManager: ModInitializer, DedicatedServerModInitializer, ClientModInitializer {
    val modId = "ironman"
    fun String.toId() = Identifier(modId, this)
    val logger = LogManager.getLogger("modId")

    override fun onInitialize() {
        logger.info("Starting IronMan...")
        // Common initialization
    }

    override fun onInitializeClient() {
        logger.info("Starting Client IronMan...")
        // Client initialization
    }

    override fun onInitializeServer() {
        logger.info("Starting Server IronMan...")
        // Dedicated server initialization
    }
}
