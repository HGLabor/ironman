package gg.norisk.heroes.ironman.abilities

import gg.norisk.heroes.events.MouseEvents
import gg.norisk.heroes.events.MouseEvents.mouseClickEvent
import gg.norisk.heroes.ironman.IronManManager.toId
import gg.norisk.heroes.ironman.abilities.FlyAbility.secondAbilityTogglePacket
import gg.norisk.heroes.ironman.client.render.CameraShaker
import gg.norisk.heroes.ironman.client.render.entity.BlastProjectileRenderer
import gg.norisk.heroes.ironman.player.IronManPlayer
import gg.norisk.heroes.ironman.player.isRepulsorCharging
import gg.norisk.heroes.ironman.player.repulsorChargeTracker
import gg.norisk.heroes.ironman.registry.EntityRegistry
import gg.norisk.heroes.ironman.registry.SoundRegistry
import gg.norisk.heroes.utils.Animation
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.option.Perspective
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Arm
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.core.entity.directionVector
import net.silkmc.silk.network.packet.c2sPacket
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object RepulsorBlastAbility {
    val rightClickPacket = c2sPacket<Unit>("right-click-packet".toId())
    fun initClient() {
        /*entityRendererEvent.listen { event ->
            val player = MinecraftClient.getInstance().player ?: return@listen
            if (event.entity is PlayerEntity) {
                BlastProjectileRenderer.renderBlastRepulsor(
                    player,
                    MinecraftClient.getInstance().tickDelta,
                    event.matrixStack,
                    event.vertexConsumerProvider,
                    false
                )
            }
        }*/
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

    fun ClientPlayerEntity.getLightningPos(f: Float): Vec3d {
        val client = MinecraftClient.getInstance()
        if (client.options.perspective.isFirstPerson) {
            val g: Float = MathHelper.lerp(f * 0.5f, this.yaw, this.prevYaw) * (Math.PI.toFloat() / 180)
            val h: Float = MathHelper.lerp(f * 0.5f, this.pitch, this.prevPitch) * (Math.PI.toFloat() / 180)
            val vec3d = Vec3d(0.39 * ((if (this.mainArm == Arm.RIGHT) 8 else -8) * 0.4), 0.0, 0.0)
            return vec3d.rotateX(-h).rotateY(-g).add(this.getCameraPosVec(f))
        }
        return getLeashPos(f)
    }

    fun onEnd(context: WorldRenderContext) {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return
        val matrices = context.matrixStack()
        val camera = context.camera()
        val tickDelta = context.tickDelta().toDouble()
        if (client.options.perspective === Perspective.FIRST_PERSON) {
            val vec3d =
                player.getLightningPos(tickDelta.toFloat()).add(player.directionVector.normalize().multiply(-5.0))
            val cameraX = vec3d.x
            val cameraY = vec3d.y
            val cameraZ = vec3d.z
            val lerpX = MathHelper.lerp(tickDelta, player.lastRenderX, player.x)
            val lerpY = MathHelper.lerp(tickDelta, player.lastRenderY, player.y)
            val lerpZ = MathHelper.lerp(tickDelta, player.lastRenderZ, player.z)
            val x = lerpX - cameraX
            val y = lerpY - cameraY
            val z = lerpZ - cameraZ
            val off = client.entityRenderDispatcher.getRenderer(player).getPositionOffset(player, tickDelta.toFloat())
            val offX = x + off.x
            val offY = y + off.y
            val offZ = z + off.z
            matrices.push()
            matrices.translate(offX, offY, offZ)
            matrices.translate(0.0, -0.05, 0.0)
            BlastProjectileRenderer.renderBlastRepulsor(
                player,
                tickDelta.toFloat(),
                matrices,
                client.bufferBuilders.effectVertexConsumers,
                true
            )
            matrices.pop()
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