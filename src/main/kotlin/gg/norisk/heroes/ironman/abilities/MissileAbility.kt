package gg.norisk.heroes.ironman.abilities

import gg.norisk.heroes.events.MouseEvents.mouseClickEvent
import gg.norisk.heroes.ironman.IronManManager.toId
import gg.norisk.heroes.ironman.abilities.FlyAbility.fourthAbilityTogglePacket
import gg.norisk.heroes.ironman.abilities.FlyAbility.mouseScrollPacket
import gg.norisk.heroes.ironman.abilities.RepulsorBlastAbility.rightClickPacket
import gg.norisk.heroes.ironman.player.*
import gg.norisk.heroes.ironman.player.projectile.MissileEntity
import gg.norisk.heroes.ironman.registry.EntityRegistry
import gg.norisk.heroes.ironman.registry.SoundRegistry
import gg.norisk.heroes.serialization.UUIDSerializer
import gg.norisk.heroes.utils.Animation
import gg.norisk.heroes.utils.RaycastUtils
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.model.ModelPart
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderDispatcher
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Style
import net.minecraft.util.math.Box
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.server.players
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.core.text.literal
import net.silkmc.silk.core.text.literalText
import net.silkmc.silk.network.packet.s2cPacket
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.*
import java.util.function.Predicate
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object MissileAbility {
    val missileTargetPacket =
        s2cPacket<Map<@Serializable(with = UUIDSerializer::class) UUID, Int>>("missile-target-packet".toId())
    var animation: Animation? = null

    fun initServer() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            command("missile") {
                runs {
                    shootMissile(this.source.playerOrThrow)
                }
            }
        }
        ServerTickEvents.END_SERVER_TICK.register(::onEndServerTick)
        fourthAbilityTogglePacket.receiveOnServer { packet, context ->
            val player = context.player
            val ironman = player as IronManPlayer
            player.isMissileSelecting = packet
            ironman.missileTargets.clear()
            missileTargetPacket.send(ironman.missileTargets, player)
        }
        mouseClickEvent.listen {
            val player = MinecraftClient.getInstance().player ?: return@listen
            val ironman = player as IronManPlayer
            if (MinecraftClient.getInstance().options.useKey.matchesMouse(it.key.code)) {
                if (it.pressed && player.isMissileSelecting && ironman.missileTargets.isEmpty()) {
                    player.world.playSoundFromEntity(
                        player,
                        SoundRegistry.MISSILE_EMPTY,
                        SoundCategory.PLAYERS,
                        1f,
                        1f
                    )
                }
            }
        }
        mouseScrollPacket.receiveOnServer { packet, context ->
            val player = context.player
            val ironman = player as IronManPlayer
            val currentTarget = player.currentMissileTarget.getOrNull() ?: return@receiveOnServer
            val currentValue = ironman.missileTargets[currentTarget] ?: 1
            val updatedValue = if (packet > 0) {
                6.coerceAtMost(currentValue + 1)
            } else {
                1.coerceAtLeast(currentValue - 1)
            }
            ironman.missileTargets.replace(currentTarget, updatedValue)
            missileTargetPacket.send(ironman.missileTargets, player)
        }
        rightClickPacket.receiveOnServer { packet, context ->
            val player = context.player
            shootMissile(player)
        }
    }

    fun initClient() {
        missileTargetPacket.receiveOnClient { packet, context ->
            val player = context.client.player ?: return@receiveOnClient
            val ironman = player as IronManPlayer
            ironman.missileTargets.clear()
            packet.forEach { (uuid, value) ->
                ironman.missileTargets[uuid] = value
            }
            if (packet.isNotEmpty()) {
                mcCoroutineTask(sync = true) {
                    player.world.playSoundFromEntity(
                        player,
                        SoundRegistry.MISSILE_TARGET_FOUND,
                        SoundCategory.PLAYERS,
                        1f,
                        1f
                    )
                }
            }
        }
    }

    private fun onEndServerTick(server: MinecraftServer) {
        server.players
            .filter { it.isMissileSelecting }
            .forEach {
                val target = raycastMissileTarget(it)
                val ironman = it as IronManPlayer
                it.currentMissileTarget = Optional.ofNullable(target?.uuid)
                if (target != null) {
                    if (!ironman.missileTargets.containsKey(target.uuid)) {
                        ironman.missileTargets[target.uuid] = 1
                        missileTargetPacket.send(ironman.missileTargets, it)
                    }
                }
            }
    }

    private fun raycastMissileTarget(player: ServerPlayerEntity): LivingEntity? {
        val world = player.serverWorld
        val distance = 128
        val result = player.raycast(distance.toDouble(), 0f, false)
        val predicate: Predicate<Entity> = Predicate { it is LivingEntity && it !is MissileEntity }
        var target = player.world.getOtherEntities(
            player,
            Box.from(result.pos).expand(5.0), predicate
        ).randomOrNull()

        if (target == null) {
            target = RaycastUtils.raycastEntity(player, distance, predicate).getOrNull()
        }

        return target as? LivingEntity?
    }

    private fun shootMissile(player: ServerPlayerEntity) {
        val world = player.serverWorld
        if (player.isMissileSelecting) {
            val ironman = player as IronManPlayer
            var delay = 0
            for ((uuid, value) in ironman.missileTargets) {
                repeat(value) {
                    val missile = MissileEntity(EntityRegistry.MISSILE, player.world)
                    missile.owner = player
                    missile.targetGoal = world.getEntity(uuid) as? LivingEntity? ?: return@repeat
                    missile.setPosition(
                        player.pos.add(
                            Random.nextDouble(-5.0, 5.0),
                            Random.nextDouble(2.0, 5.0),
                            Random.nextDouble(-5.0, 5.0)
                        )
                    )
                    mcCoroutineTask(delay = delay.ticks) {
                        player.serverWorld.spawnEntity(missile)
                        world.playSound(
                            null,
                            player.x,
                            player.y,
                            player.z, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT,
                            0.8f,
                            Random.nextDouble(0.9, 1.2).toFloat()
                        )
                    }
                    delay += Random.nextInt(0, 5)
                }
            }
            if (ironman.missileTargets.isNotEmpty()) {
                player.isMissileSelecting = false
            }
            ironman.missileTargets.clear()
            missileTargetPacket.send(ironman.missileTargets, player)
        }
    }

    fun handleTrackedDataSet(livingEntity: LivingEntity, trackedData: TrackedData<*>) {
        val player = livingEntity as? PlayerEntity ?: return
        if (missileSelector == trackedData) {
            if (player.isMissileSelecting) {
                animation = Animation(0f, 0.4f, 1.seconds.toJavaDuration(), Animation.Easing.EXPO_OUT)
                if (player.world.isClient) {
                    player.world.playSoundFromEntity(
                        player,
                        SoundRegistry.REPULSOR_CHARGE,
                        SoundCategory.PLAYERS,
                        2f,
                        1f
                    )
                }
            } else {
                if (player.world.isClient) {
                    player.world.playSoundFromEntity(
                        player,
                        SoundRegistry.MISSILE_EMPTY,
                        SoundCategory.PLAYERS,
                        1f,
                        1f
                    )
                }
            }
        }
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
            if (playerEntity.isMissileSelecting) {
                matrixStack.translate(0f, get(), 0f)
            }
        }
    }

    fun <T : Entity> renderTargetNameTag(
        entity: T,
        tickDelta: Float,
        g: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        dispatcher: EntityRenderDispatcher,
        textRenderer: TextRenderer
    ) {
        val ironman = MinecraftClient.getInstance().player as? IronManPlayer? ?: return
        if (!ironman.missileTargets.containsKey(entity.uuid)) {
            return
        }
        val amount = ironman.missileTargets.getValue(entity.uuid)
        val text = if (amount == 1) {
            "▼".literal
        } else {
            literalText {
                repeat(amount) {
                    text(" ▼ ")
                }
            }
        }
        text.setStyle(Style.EMPTY.withBold(true).withColor(0xae0c00))
        val bl = !entity.isSneaky
        val f = entity.nameLabelHeight
        val j = if ("deadmau5" == text.getString()) -10 else 0
        matrixStack.push()
        matrixStack.translate(0.0f, f, 0.0f)
        matrixStack.multiply(dispatcher.getRotation())
        matrixStack.scale(-0.025f, -0.025f, 0.025f)
        matrixStack.scale(5f, 5f, 5f)
        matrixStack.translate(0f, -f * 2, 0f)
        val matrix4f = matrixStack.peek().positionMatrix
        val g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.0f)
        val k = (g * 255.0f).toInt() shl 24
        val h: Float = (-textRenderer.getWidth(text) / 2).toFloat()
        textRenderer.draw(
            text,
            h,
            j.toFloat(),
            553648127,
            false,
            matrix4f,
            vertexConsumerProvider,
            if (bl) TextRenderer.TextLayerType.SEE_THROUGH else TextRenderer.TextLayerType.NORMAL,
            k,
            i
        )
        if (bl) {
            textRenderer.draw(
                text,
                h,
                j.toFloat(),
                -1,
                false,
                matrix4f,
                vertexConsumerProvider,
                TextRenderer.TextLayerType.NORMAL,
                0,
                i
            )
        }

        matrixStack.pop()
    }

    fun handleHotBarScrolling(info: CallbackInfo) {
        val player = MinecraftClient.getInstance().player ?: return
        if (player.isMissileSelecting && player.currentMissileTarget.isPresent) {
            info.cancel()
        }
    }
}