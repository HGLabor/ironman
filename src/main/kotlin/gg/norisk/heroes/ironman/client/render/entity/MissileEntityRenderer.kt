package gg.norisk.heroes.ironman.client.render.entity

import gg.norisk.heroes.ironman.player.projectile.MissileEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.RotationAxis
import kotlin.random.Random

class MissileEntityRenderer(
    context: EntityRendererFactory.Context
) : EntityRenderer<MissileEntity>(context) {
    val itemRenderer = context.itemRenderer

    override fun render(
        entity: MissileEntity,
        f: Float,
        g: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        matrixStack.push()
        val scale = 5f
        matrixStack.scale(scale, scale, scale)
        matrixStack.multiply(dispatcher.rotation)
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f))
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.age * Math.PI.toFloat() * 2f))
        this.itemRenderer.renderItem(
            entity.stack,
            ModelTransformationMode.GROUND,
            i,
            OverlayTexture.DEFAULT_UV,
            matrixStack,
            vertexConsumerProvider,
            entity.world,
            entity.id
        )
        matrixStack.pop()
        super.render(entity, f, g, matrixStack, vertexConsumerProvider, i)
    }

    override fun getTexture(entity: MissileEntity): Identifier {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE
    }
}