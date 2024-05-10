package gg.norisk.heroes.ironman.client.render.entity

import gg.norisk.heroes.ironman.IronManManager
import gg.norisk.heroes.ironman.player.projectile.SentryEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.entity.BipedEntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class SentryEntityRenderer(context: EntityRendererFactory.Context) :
    BipedEntityRenderer<SentryEntity, PlayerEntityModel<SentryEntity>>(
        context, PlayerEntityModel<SentryEntity>(
            context.getPart(EntityModelLayers.PLAYER_SLIM),
            true
        ),
        0.5f
    ) {

    override fun getTexture(entity: SentryEntity): Identifier {
        return IronManManager.skin
    }
}
