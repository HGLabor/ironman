package gg.norisk.heroes.ironman.abilities

import gg.norisk.heroes.ironman.registry.EntityRegistry
import net.minecraft.server.network.ServerPlayerEntity
import net.silkmc.silk.core.entity.directionVector

object SentryModeAbility {
    fun initClient() {

    }

    fun initServer() {

    }

    fun spawnSentry(playerEntity: ServerPlayerEntity) {
        val sentry = EntityRegistry.SENTRY.create(playerEntity.world) ?: return
        val directionVector = playerEntity.directionVector.normalize().multiply(1.0)
        sentry.setPosition(playerEntity.pos.add(directionVector.x, 0.0, directionVector.z))
        sentry.owner = playerEntity.uuid
        playerEntity.world.spawnEntity(sentry)
    }
}