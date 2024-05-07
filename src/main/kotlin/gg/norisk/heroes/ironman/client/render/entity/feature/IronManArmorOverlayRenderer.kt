package gg.norisk.heroes.ironman.client.render.entity.feature

import gg.norisk.heroes.ironman.IronManManager
import gg.norisk.heroes.ironman.player.IronManPlayer
import gg.norisk.heroes.ironman.player.isIronMan
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.MathHelper

@Environment(EnvType.CLIENT)
class IronManArmorOverlayRenderer<T : LivingEntity, M : BipedEntityModel<T>, A : BipedEntityModel<T>>(
    featureRendererContext: FeatureRendererContext<T, M>,
) : FeatureRenderer<T, M>(featureRendererContext) {
    var currentScale = 0.4f

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
        if (player.isIronMan) {
            currentScale = MathHelper.lerp(MinecraftClient.getInstance().tickDelta * 0.05f, currentScale, 1.05f)
        } else {
            currentScale = MathHelper.lerp(MinecraftClient.getInstance().tickDelta * 0.05f, currentScale, 0.4f)
        }

        if (player.shouldRenderIronManSkin()) {
            return
        }

        if (!player.isIronMan && (player as IronManPlayer).transformTimestamp + 250L < System.currentTimeMillis()) {
            return
        }

        matrixStack.push()
        matrixStack.scale(currentScale, currentScale, currentScale)
        this.contextModel.render(
            matrixStack,
            vertexConsumerProvider.getBuffer(this.contextModel.getLayer(IronManManager.skin)),
            i,
            OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f
        )
        matrixStack.pop()
    }

    companion object {
        fun PlayerEntity.shouldRenderIronManSkin(): Boolean {
            if ((this as IronManPlayer).transformTimestamp + 1000L < System.currentTimeMillis() && isIronMan) {
                return true
            }
            return false
        }
    }
}
