package gg.norisk.heroes.ironman.client.render.entity.feature

import com.mojang.blaze3d.systems.RenderSystem
import gg.norisk.heroes.ironman.IronManManager.toId
import gg.norisk.heroes.ironman.player.isIronManFlying
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.model.BakedModelManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Arm
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import org.lwjgl.opengl.GL11

@Environment(EnvType.CLIENT)
class FlightParticleRenderer<T : LivingEntity, M : BipedEntityModel<T>, A : BipedEntityModel<T>>(
    featureRendererContext: FeatureRendererContext<T, M>,
    private val innerModel: A,
    private val outerModel: A,
    bakedModelManager: BakedModelManager
) : FeatureRenderer<T, M>(featureRendererContext) {

    override fun render(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        livingEntity: T,
        f: Float,
        g: Float,
        h: Float,
        j: Float,
        k: Float,
        l: Float
    ) {
        val player = livingEntity as? PlayerEntity ?: return
        if (!player.isIronManFlying) return
        val speed = player.velocity.horizontalLengthSquared().toFloat()
        update(speed)
        vertexConsumerProvider.getBuffer(RenderLayer.getGui())

        matrixStack.push()
        this.contextModel.setArmAngle(Arm.RIGHT, matrixStack)
        renderFire(matrixStack, livingEntity, isRightSide = true)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.setArmAngle(Arm.LEFT, matrixStack)
        renderFire(matrixStack, livingEntity)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.leftLeg.rotate(matrixStack)
        renderFire(matrixStack, livingEntity, feet = true)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.rightLeg.rotate(matrixStack)
        renderFire(matrixStack, livingEntity, isRightSide = true, feet = true)
        matrixStack.pop()


        matrixStack.push()
        this.contextModel.setArmAngle(Arm.RIGHT, matrixStack)
        renderFire(matrixStack, livingEntity, isRightSide = true, mirror = true)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.setArmAngle(Arm.LEFT, matrixStack)
        renderFire(matrixStack, livingEntity, mirror = true)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.leftLeg.rotate(matrixStack)
        renderFire(matrixStack, livingEntity, mirror = true, feet = true)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.rightLeg.rotate(matrixStack)
        renderFire(matrixStack, livingEntity, isRightSide = true, mirror = true, feet = true)
        matrixStack.pop()
    }

    private var currentFrame = 0
    private var nextUpdate: Long = 0
    fun update(speed: Float) {
        val speedBoost = (100L * speed).toLong()
        //println("Boost: $speedBoost")
        if (System.currentTimeMillis() + speedBoost > nextUpdate) {
            nextUpdate = System.currentTimeMillis() + 125
            currentFrame++
            if (currentFrame >= 4) {
                currentFrame = 0
            }
        }
    }


    fun renderFire(
        matrices: MatrixStack,
        livingEntity: T,
        isRightSide: Boolean = false,
        mirror: Boolean = false,
        feet: Boolean = false
    ) {
        val speed = livingEntity.velocity.horizontalLengthSquared().toFloat() * 2f

        val identifier: Identifier = "textures/particle/fire_$currentFrame.png".toId()

        if (mirror) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f))
        }

        var right = if (feet) -0.01f else -0.075f
        if (!isRightSide) right *= -1f

        renderTopCenteredQuad(matrices, identifier, right, 0.6f, 0.5f, 0.3f + 0.75f * speed)
    }

    fun renderTopCenteredQuad(
        matrices: MatrixStack,
        identifier: Identifier,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)

        val positionMatrix = matrices.peek().positionMatrix
        buffer.vertex(positionMatrix, x - width * 0.5f, y, 0f).texture(0f, 0f).next()
        buffer.vertex(positionMatrix, x - width * 0.5f, y + height, 0f).texture(0f, 1f).next()
        buffer.vertex(positionMatrix, x + width * 0.5f, y + height, 0f).texture(1f, 1f).next()
        buffer.vertex(positionMatrix, x + width * 0.5f, y, 0f).texture(1f, 0f).next()

        RenderSystem.setShader { GameRenderer.getPositionTexProgram() }
        RenderSystem.setShaderTexture(0, identifier)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

        RenderSystem.depthFunc(GL11.GL_ALWAYS)
        RenderSystem.disableCull()
        tessellator.draw()
        RenderSystem.enableCull()
        RenderSystem.depthFunc(GL11.GL_LEQUAL)
    }
}
