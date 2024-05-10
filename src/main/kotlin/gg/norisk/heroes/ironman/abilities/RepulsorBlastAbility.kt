package gg.norisk.heroes.ironman.abilities

import gg.norisk.heroes.events.MouseEvents
import gg.norisk.heroes.events.MouseEvents.mouseClickEvent
import gg.norisk.heroes.ironman.IronManManager.toId
import gg.norisk.heroes.ironman.abilities.FlyAbility.secondAbilityTogglePacket
import gg.norisk.heroes.ironman.client.render.CameraShaker
import gg.norisk.heroes.ironman.player.IronManPlayer
import gg.norisk.heroes.ironman.player.isRepulsorCharging
import gg.norisk.heroes.ironman.player.repulsorChargeTracker
import gg.norisk.heroes.ironman.registry.EntityRegistry
import gg.norisk.heroes.ironman.registry.SoundRegistry
import gg.norisk.heroes.utils.Animation
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.silkmc.silk.network.packet.c2sPacket
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object RepulsorBlastAbility {
    val rightClickPacket = c2sPacket<Unit>("right-click-packet".toId())
    fun initClient() {
        mouseClickEvent.listen {
            handleRepsulsorBlastShoot(it)
        }
    }

    private fun handleRepsulsorBlastShoot(mouseClickEvent: MouseEvents.MouseClickEvent) {
        if (MinecraftClient.getInstance().options.useKey.matchesMouse(mouseClickEvent.key.code)) {
            if (mouseClickEvent.pressed) {
                rightClickPacket.send(Unit)
            }
        }
    }

    fun initServer() {
        secondAbilityTogglePacket.receiveOnServer { active, context ->
            val player = context.player
            player.isRepulsorCharging = active
        }
        rightClickPacket.receiveOnServer { packet, context ->
            val player = context.player
            shootRepulsorBlast(player)
        }
    }

    private fun shootRepulsorBlast(player: ServerPlayerEntity) {
        if (player.isRepulsorCharging) {
            CameraShaker.cameraShakePacket.send(CameraShaker.BoomShake(0.3, 0.1, 0.4), player)
            val world = player.serverWorld
            val blastProjectile = EntityRegistry.BLAST.create(world)
            blastProjectile?.shootFrom(player)
        }
    }

    var animation: Animation? = null

    fun handleTrackedDataSet(livingEntity: LivingEntity, trackedData: TrackedData<*>) {
        val player = livingEntity as? PlayerEntity ?: return
        if (repulsorChargeTracker == trackedData) {
            if (player.isRepulsorCharging) {
                (player as IronManPlayer).repulsorTimestamp = System.currentTimeMillis()
                animation = Animation(0f, 360f, 1.seconds.toJavaDuration(), Animation.Easing.EXPO_OUT)
                if (player.world.isClient) {
                    player.world.playSoundFromEntity(
                        player,
                        SoundRegistry.REPULSOR_CHARGE,
                        SoundCategory.PLAYERS,
                        2f,
                        1f
                    )
                }
            }
        }
    }

    fun rotateHand(matrixStack: MatrixStack, camera: Camera, f: Float) {
    }

    fun renderHand(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        light: Int,
        playerEntity: AbstractClientPlayerEntity,
        modelPart: ModelPart,
        modelPart2: ModelPart
    ) {
        animation?.apply {
            if (!isDone) {
                modelPart.yaw = get()
                modelPart2.yaw = get()
            }
        }
    }
}