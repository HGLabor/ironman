package gg.norisk.heroes.ironman.client.render.entity

import gg.norisk.heroes.ironman.player.projectile.BlastProjectile
import gg.norisk.heroes.utils.RaycastUtils
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.LlamaSpitEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.random.Random

@Environment(EnvType.CLIENT)
class BlastProjectileRenderer(context: EntityRendererFactory.Context) : EntityRenderer<BlastProjectile>(context) {
    override fun render(
        blastProjectile: BlastProjectile,
        f: Float,
        g: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        val k = 3847130
        val n: Int = (k and 0xFF0000) shr 16
        val o: Int = (k and 0xFF00) shr 8
        val p: Int = (k and 0xFF) shr 0
        val color = floatArrayOf(n.toFloat() / 255.0f, o.toFloat() / 255.0f, p.toFloat() / 255.0f)
        matrixStack.push()
        //matrixStack.translate(-0.5f,0f,0f)
        val acosLerpY = acos(blastProjectile.shootDirectionVector.y).toFloat()
        val atan2LerpXZ = atan2(
            blastProjectile.shootDirectionVector.z,
            blastProjectile.shootDirectionVector.x
        ).toFloat() //TODO notice
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((1.5707964f - atan2LerpXZ) * 57.295776f))
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(acosLerpY * 57.295776f))

        renderBeam(
            matrixStack,
            vertexConsumerProvider,
            f,
            Random.nextLong(),
            0,
            5,
            color
        )
        matrixStack.pop()
        super.render(blastProjectile, f, g, matrixStack, vertexConsumerProvider, i)
    }

    override fun getTexture(llamaSpitEntity: BlastProjectile): Identifier {
        return TEXTURE
    }

    companion object {

        private fun renderBeam(
            matrixStack: MatrixStack,
            vertexConsumerProvider: VertexConsumerProvider,
            f: Float,
            l: Long,
            i: Int,
            j: Int,
            fs: FloatArray
        ) {
            BeaconBlockEntityRenderer.renderBeam(
                matrixStack,
                vertexConsumerProvider,
                BeaconBlockEntityRenderer.BEAM_TEXTURE,
                f,
                1.0f,
                l,
                i,
                j,
                fs,
                0.2f,
                0.25f
            )
        }

        private fun lerpPosition(entity: Entity, yOffset: Double, delta: Float): Vec3d {
            return Vec3d(entity.x, entity.y + yOffset, entity.z)
        }

        fun renderBlastRepulsor(
            player: PlayerEntity,
            delta: Float,
            matrixStack: MatrixStack,
            vertexConsumerProvider: VertexConsumerProvider,
            firstPerson: Boolean
        ) {
            val raycast = player.raycast(256.0, 0.0f, true)
            val targetPos = raycast.pos
            val progress = 0.9f + sin((player.age.toFloat() / 5.0f).toDouble()).toFloat() / 10.0f
            val eyeHeight = player.getEyeHeight(player.pose)
            matrixStack.push()
            if (firstPerson) {
                matrixStack.translate(0.0, eyeHeight.toDouble(), 0.0)
            } else {
                matrixStack.translate(0.0, 1.25, 0.0)
            }
            val lerpPlayerPos = lerpPosition(player, eyeHeight.toDouble(), delta)
            var lerpPos = targetPos.subtract(lerpPlayerPos)
            var distance: Double = lerpPos.length()
            val found = RaycastUtils.raycastEntity(player, delta, 64.0f)
            if (found != null) {
                distance = found.pos.subtract(lerpPlayerPos).length()
            }
            lerpPos = lerpPos.normalize()
            val acosLerpY = acos(lerpPos.y).toFloat()
            val atan2LerpXZ = atan2(lerpPos.z, lerpPos.x).toFloat() //TODO notice
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((1.5707964f - atan2LerpXZ) * 57.295776f))
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(acosLerpY * 57.295776f))

            val k = 15961002
            val n: Int = (k and 0xFF0000) shr 16
            val o: Int = (k and 0xFF00) shr 8
            val p: Int = (k and 0xFF) shr 0
            val color = floatArrayOf(n.toFloat() / 255.0f, o.toFloat() / 255.0f, p.toFloat() / 255.0f)
            renderBeam(
                matrixStack,
                vertexConsumerProvider,
                delta,
                player.age.toLong(),
                0,
                3,
                color
            )

            matrixStack.pop()
        }

        private val TEXTURE = Identifier("textures/entity/llama/spit.png")
    }
}
