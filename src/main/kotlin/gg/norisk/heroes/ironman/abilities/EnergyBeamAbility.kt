package gg.norisk.heroes.ironman.abilities

import gg.norisk.heroes.events.MouseEvents
import gg.norisk.heroes.events.MouseEvents.mouseClickEvent
import gg.norisk.heroes.events.RenderEvents.entityRendererEvent
import gg.norisk.heroes.ironman.IronManManager.toId
import gg.norisk.heroes.ironman.client.render.entity.BlastProjectileRenderer
import gg.norisk.heroes.ironman.client.sound.BeamSoundInstance
import gg.norisk.heroes.ironman.player.IronManPlayer
import gg.norisk.heroes.ironman.player.holdingLeftClick
import gg.norisk.heroes.ironman.player.isHoldingLeftClick
import gg.norisk.heroes.ironman.player.isRepulsorCharging
import gg.norisk.heroes.utils.Animation
import gg.norisk.heroes.utils.RaycastUtils
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.option.Perspective
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Arm
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.entity.directionVector
import net.silkmc.silk.core.server.players
import net.silkmc.silk.network.packet.c2sPacket
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object EnergyBeamAbility {
    val leftClickPacket = c2sPacket<Boolean>("left-click-packet".toId())

    fun initClient() {
        mouseClickEvent.listen {
            handleMouseClickEvent(it)
        }
        entityRendererEvent.listen { event ->
            val player = MinecraftClient.getInstance().player ?: return@listen
            val beamAnimation = (player as IronManPlayer).beamAnimation ?: return@listen
            if (event.entity is PlayerEntity && event.entity.isHoldingLeftClick && event.entity.isRepulsorCharging) {
                BlastProjectileRenderer.renderEnergyBeam(
                    player,
                    event.f,
                    event.matrixStack,
                    event.vertexConsumerProvider,
                    false,
                    beamAnimation.get().toDouble()
                )

            }
        }
        WorldRenderEvents.END.register(::onEnd)
    }

    fun initServer() {
        leftClickPacket.receiveOnServer { packet, context ->
            val player = context.player
            player.isHoldingLeftClick = packet
        }
        ServerTickEvents.END_SERVER_TICK.register {
            handleServerTickEvent(it)
        }
    }

    private fun handleServerTickEvent(server: MinecraftServer) {
        for (player in server.players.filter { it.isHoldingLeftClick && it.isRepulsorCharging }) {
            val world = player.serverWorld
            val animation = (player as IronManPlayer).beamAnimation ?: continue
            if (animation.isDone) {
                player.isHoldingLeftClick = false
                player.isRepulsorCharging = false
            } else {
                val hitResult = player.raycast(animation.get().toDouble(), 0f, true)
                val entityRaycast = RaycastUtils.raycastEntity(player,animation.get().toInt())
                if (hitResult is BlockHitResult) {
                    if (world.testBlockState(hitResult.blockPos) { !it.isAir }) {
                        world.breakBlock(hitResult.blockPos, false, player)
                    }
                }

                if (entityRaycast.isPresent) {
                    entityRaycast.get().setOnFireFor(2)
                    entityRaycast.get().damage(player.damageSources.playerAttack(player),0.2f)
                }
            }
        }
    }

    private fun handleMouseClickEvent(mouseClickEvent: MouseEvents.MouseClickEvent) {
        if (MinecraftClient.getInstance().options.attackKey.matchesMouse(mouseClickEvent.key.code)) {
            leftClickPacket.send(mouseClickEvent.pressed)
        }
    }

    fun handleTrackedDataSet(livingEntity: LivingEntity, trackedData: TrackedData<*>) {
        val player = livingEntity as? PlayerEntity ?: return
        val ironman = player as IronManPlayer
        if (holdingLeftClick == trackedData) {
            if (player.isHoldingLeftClick && player.isRepulsorCharging) {
                ironman.beamAnimation = Animation(0f, 18f, 7.seconds.toJavaDuration(), Animation.Easing.ELASTIC_OUT)
                if (player.world.isClient) {
                    println("Hier Rein")
                    MinecraftClient.getInstance().soundManager.play(BeamSoundInstance(player as ClientPlayerEntity))
                }
            }
        }
    }

    fun ClientPlayerEntity.getLightningPos(f: Float): Vec3d {
        val client = MinecraftClient.getInstance()
        if (client.options.perspective.isFirstPerson) {
            val g: Float = MathHelper.lerp(f * 0.5f, this.yaw, this.prevYaw) * (Math.PI.toFloat() / 180)
            val h: Float = MathHelper.lerp(f * 0.5f, this.pitch, this.prevPitch) * (Math.PI.toFloat() / 180)
            val vec3d = Vec3d(0.39 * ((if (this.mainArm == Arm.RIGHT) 3 else -3) * 0.4), 0.0, 0.0)
            return vec3d.rotateX(-h).rotateY(-g).add(this.getCameraPosVec(f))
        }
        return getLeashPos(f)
    }

    fun onEnd(context: WorldRenderContext) {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return
        if (!(player.isHoldingLeftClick && player.isRepulsorCharging)) return
        val beamAnimation = (player as IronManPlayer).beamAnimation ?: return
        val matrices = context.matrixStack()
        val camera = context.camera()
        val tickDelta = context.tickDelta().toDouble()
        if (client.options.perspective === Perspective.FIRST_PERSON) {
            val vec3d =
                player.getLightningPos(tickDelta.toFloat()).add(player.directionVector.normalize().multiply(-0.0))
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

            BlastProjectileRenderer.renderEnergyBeam(
                player,
                tickDelta.toFloat(),
                matrices,
                client.bufferBuilders.effectVertexConsumers,
                true,
                beamAnimation.get().toDouble()
            )
            matrices.pop()
        }
    }
}