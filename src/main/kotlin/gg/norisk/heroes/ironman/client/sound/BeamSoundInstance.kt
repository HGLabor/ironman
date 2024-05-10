package gg.norisk.heroes.ironman.client.sound

import gg.norisk.heroes.ironman.player.IronManPlayer
import gg.norisk.heroes.ironman.player.isHoldingLeftClick
import gg.norisk.heroes.ironman.player.isRepulsorCharging
import gg.norisk.heroes.ironman.registry.SoundRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.MathHelper

@Environment(EnvType.CLIENT)
class BeamSoundInstance(private val player: ClientPlayerEntity) :
    MovingSoundInstance(SoundRegistry.ENERGY_BEAM, SoundCategory.PLAYERS, SoundInstance.createRandom()) {
    private var tickCount = 0

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 0.1f
    }

    override fun tick() {
        val animation = (player as IronManPlayer).beamAnimation
        if (animation?.isDone == true) {
            this.setDone()
        }
        ++this.tickCount
        if (!player.isRemoved && (this.tickCount <= 20 || (player.isRepulsorCharging && player.isHoldingLeftClick))) {
            this.x = player.x.toFloat().toDouble()
            this.y = player.y.toFloat().toDouble()
            this.z = player.z.toFloat().toDouble()
            this.volume = 0.8f
        } else {
            this.setDone()
        }
    }
}
