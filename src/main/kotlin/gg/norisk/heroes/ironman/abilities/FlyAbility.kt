package gg.norisk.heroes.ironman.abilities

import gg.norisk.heroes.events.KeyEvents
import gg.norisk.heroes.ironman.IronManManager.toId
import gg.norisk.heroes.ironman.abilities.keybindings.KeyBindingManager
import gg.norisk.heroes.ironman.client.sound.FlyingSoundInstance
import gg.norisk.heroes.ironman.player.IronManPlayer
import gg.norisk.heroes.ironman.player.flyTracker
import gg.norisk.heroes.ironman.player.isIronMan
import gg.norisk.heroes.ironman.player.isIronManFlying
import gg.norisk.heroes.ironman.registry.SoundRegistry
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.silkmc.silk.core.entity.directionVector
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.network.packet.c2sPacket

object FlyAbility {
    val firstAbilityTogglePacket = c2sPacket<Boolean>("first-ability-toggle".toId())
    val secondAbilityTogglePacket = c2sPacket<Boolean>("second-ability-toggle".toId())
    val thirdAbilityTogglePacket = c2sPacket<Boolean>("third-ability-toggle".toId())

    fun initClient() {
        KeyEvents.keyEvent.listen { event ->
            if (event.matchesKeyBinding(KeyBindingManager.firstAbilityKey)) {
                if (event.isClicked()) {
                    firstAbilityTogglePacket.send(true)
                }
            } else if (event.matchesKeyBinding(KeyBindingManager.secondAbilityKey)) {
                if (event.isClicked()) {
                    secondAbilityTogglePacket.send(true)
                }
            } else if (event.matchesKeyBinding(KeyBindingManager.thirdAbilityKey)) {
                if (event.isClicked()) {
                    thirdAbilityTogglePacket.send(true)
                }
            }
        }
    }

    fun initServer() {
        firstAbilityTogglePacket.receiveOnServer { packet, context ->
            val player = context.player
            if (!player.isIronMan) return@receiveOnServer
            player.isIronManFlying = !player.isIronManFlying
            if (player.isIronManFlying) {
                val world = player.world as ServerWorld
                player.addStatusEffect(StatusEffectInstance(StatusEffects.LEVITATION, 40, 5, false, false))
                world.playSoundFromEntity(
                    null,
                    player,
                    SoundRegistry.FLY_START_STOUND,
                    SoundCategory.PLAYERS,
                    2f,
                    1f
                )
                mcCoroutineTask(delay = 10.ticks) {
                    player.abilities.allowFlying = true
                    player.abilities.flying = true
                    player.sendAbilitiesUpdate()
                    player.modifyVelocity(player.directionVector.normalize().multiply(5.0))
                }
                player.abilities.allowFlying = true
                player.abilities.flying = true
                player.sendAbilitiesUpdate()
            }
        }
        secondAbilityTogglePacket.receiveOnServer { packet, context ->
            val player = context.player
            player.sendAbilitiesUpdate()
        }
    }

    fun onLanding(entity: Entity) {
        val player = entity as? PlayerEntity ?: return
        if (!player.world.isClient && player.isIronManFlying && !player.hasStatusEffect(StatusEffects.LEVITATION)) {
            player.isIronManFlying = false
            player.abilities.allowFlying = false
            player.abilities.flying = false
            player.sendAbilitiesUpdate()
        }
    }

    fun handleTrackedDataSet(livingEntity: LivingEntity, trackedData: TrackedData<*>) {
        val player = livingEntity as? PlayerEntity ?: return
        if (flyTracker == trackedData) {
            if (player.isIronManFlying) {
                (player as IronManPlayer).startFlightTimestamp = System.currentTimeMillis()
            }
            if (player.world.isClient) {
                if (player.isIronManFlying && player is ClientPlayerEntity) {
                    MinecraftClient.getInstance().soundManager.play(FlyingSoundInstance(player))
                }
            }
        }
    }
}
