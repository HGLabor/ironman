package gg.norisk.heroes.ironman.abilities

import gg.norisk.heroes.ironman.abilities.FlyAbility.thirdAbilityTogglePacket
import gg.norisk.heroes.ironman.player.IronManPlayer
import gg.norisk.heroes.ironman.player.ironManTracker
import gg.norisk.heroes.ironman.player.isIronMan
import gg.norisk.heroes.ironman.registry.SoundRegistry
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory

object TransformAbility {
    fun initClient() {
    }

    fun initServer() {
        thirdAbilityTogglePacket.receiveOnServer { packet, context ->
            val player = context.player
            player.isIronMan = !player.isIronMan
        }
    }

    fun handleTrackedDataSet(livingEntity: LivingEntity, trackedData: TrackedData<*>) {
        val player = livingEntity as? PlayerEntity ?: return
        val world = player.world
        if (ironManTracker == trackedData) {
            (player as IronManPlayer).transformTimestamp = System.currentTimeMillis()
            player.playSuitUpSound()
        }
    }

    private fun PlayerEntity.playSuitUpSound() {
        if (!world.isClient) {
            world.playSoundFromEntity(
                null,
                this,
                SoundRegistry.SUIT_UP,
                SoundCategory.PLAYERS,
                2f,
                1f
            )
        }
    }
}