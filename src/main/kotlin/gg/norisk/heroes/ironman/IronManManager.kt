package gg.norisk.heroes.ironman

import gg.norisk.heroes.ironman.abilities.FlyAbility
import gg.norisk.heroes.ironman.abilities.MissileAbility
import gg.norisk.heroes.ironman.abilities.RepulsorBlastAbility
import gg.norisk.heroes.ironman.abilities.TransformAbility
import gg.norisk.heroes.ironman.abilities.keybindings.KeyBindingManager
import gg.norisk.heroes.ironman.client.render.CameraShaker
import gg.norisk.heroes.ironman.client.render.entity.BlastProjectileRenderer
import gg.norisk.heroes.ironman.client.render.entity.MissileEntityRenderer
import gg.norisk.heroes.ironman.player.projectile.BlastProjectile
import gg.norisk.heroes.ironman.registry.EntityRegistry
import gg.norisk.heroes.ironman.registry.ItemRegistry
import gg.norisk.heroes.ironman.registry.SoundRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.FlyingItemEntityRenderer
import net.minecraft.client.render.entity.PigEntityRenderer
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
        RepulsorBlastAbility.initServer()
        SoundRegistry.init()
        ItemRegistry.init()
        EntityRegistry.init()
        BlastProjectile.debug()
        MissileAbility.initServer()
    }

    override fun onInitializeClient() {
        logger.info("Starting Client IronMan...")
        // Client initialization
        KeyBindingManager.init()
        FlyAbility.initClient()
        CameraShaker.initClient()
        MissileAbility.initClient()
        RepulsorBlastAbility.initClient()
        EntityRendererRegistry.register(EntityRegistry.BLAST, ::BlastProjectileRenderer)
        EntityRendererRegistry.register(EntityRegistry.MISSILE, ::MissileEntityRenderer)
    }

    override fun onInitializeServer() {
        logger.info("Starting Server IronMan...")
        // Dedicated server initialization
    }
}
