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

        matrixStack.push()
        this.contextModel.setArmAngle(Arm.RIGHT, matrixStack)
        renderFire(matrixStack, livingEntity, vertexConsumerProvider, isRightSide = true)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.setArmAngle(Arm.LEFT, matrixStack)
        renderFire(matrixStack, livingEntity, vertexConsumerProvider)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.leftLeg.rotate(matrixStack)
        renderFire(matrixStack, livingEntity, vertexConsumerProvider)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.rightLeg.rotate(matrixStack)
        renderFire(matrixStack, livingEntity, vertexConsumerProvider, isRightSide = true)
        matrixStack.pop()


        matrixStack.push()
        this.contextModel.setArmAngle(Arm.RIGHT, matrixStack)
        renderFire(matrixStack, livingEntity, vertexConsumerProvider, isRightSide = true, mirror = true)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.setArmAngle(Arm.LEFT, matrixStack)
        renderFire(matrixStack, livingEntity, vertexConsumerProvider, mirror = true)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.leftLeg.rotate(matrixStack)
        renderFire(matrixStack, livingEntity, vertexConsumerProvider, mirror = true)
        matrixStack.pop()
        matrixStack.push()
        this.contextModel.rightLeg.rotate(matrixStack)
        renderFire(matrixStack, livingEntity, vertexConsumerProvider, isRightSide = true, mirror = true)
        matrixStack.pop()
    }

    //Gescaled wird immer from origin aus, es sieht so aus als wäher dieser hier in der Mitte vom Player oder so also musst du zuerst das feuer dahin transformen,
    // dann scalen und dann wieder zurück transformen
    //
    //Oder du renderst das Feuer gleich beim origin, also so, dass der obere Teil vom Feuer bei 0|0|0 ist und scalest es dann
    //und transformst es dann zu der richtigen position

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
        matrixStack: MatrixStack,
        livingEntity: T,
        vertexConsumerProvider: VertexConsumerProvider,
        isRightSide: Boolean = false,
        mirror: Boolean = false
    ) {
        val speed = livingEntity.velocity.horizontalLengthSquared().toFloat() * 1.2f
        matrixStack.push()

        val i = 0
        val h: Float = 0f
        val identifier: Identifier = "textures/particle/fire_$currentFrame.png".toId()
        val positionMatrix = matrixStack.peek().positionMatrix
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        vertexConsumerProvider.getBuffer(RenderLayer.getGui())

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)
        val r = 1f
        val g = 1f
        val b = 1f
        val scale = 1f

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f))
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-180.0f))
        if (mirror) {
           // matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f))
        }

        matrixStack.scale(scale, scale, scale)
        //matrixStack.translate(5f + if (!isRightSide) -1f else 0f, -8.5f, 0f)
        //matrixStack.translate(3.5f + if (!isRightSide) 1f else 0f, 0f, 0f)
        //matrixStack.translate(5f / 1 / -scale, 8.65f / 1 / -scale, 0f)
        // matrixStack.translate(-5f * scale, -8.65f * scale, 0f)
        //matrixStack.scale(1f + speed / 2, 1f + speed, 1f + speed / 2)

        //TODO scalable
        //TODO Fire
        buffer.vertex(positionMatrix, h, i.toFloat(), 0f).color(r, g, b, 1f).texture(0f, 0f).next()
        buffer.vertex(positionMatrix, h, (i + 8).toFloat(), 0f).color(r, g, b, 1f).texture(0f, 1f).next()
        buffer.vertex(positionMatrix, h + 8, (i + 8).toFloat(), 0f).color(r, g, b, 1f).texture(1f, 1f).next()
        buffer.vertex(positionMatrix, h + 8, i.toFloat(), 0f).color(r, g, b, 1f).texture(1f, 0f).next()

        RenderSystem.setShader { GameRenderer.getPositionColorTexProgram() }
        RenderSystem.setShaderTexture(0, identifier)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)


        RenderSystem.disableCull()
        RenderSystem.depthFunc(GL11.GL_ALWAYS)
        tessellator.draw()
        RenderSystem.depthFunc(GL11.GL_LEQUAL)
        RenderSystem.enableCull()

        matrixStack.pop()
    }
}
