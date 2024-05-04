package gg.norisk.heroes.ironman.client.sound

import gg.norisk.heroes.ironman.player.isIronManFlying
import gg.norisk.heroes.ironman.registry.SoundRegistry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.MathHelper

@Environment(EnvType.CLIENT)
class FlyingSoundInstance(private val player: ClientPlayerEntity) :
    MovingSoundInstance(SoundRegistry.JETPACK, SoundCategory.PLAYERS, SoundInstance.createRandom()) {
    private var tickCount = 0

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 0.1f
    }

    override fun tick() {
        ++this.tickCount
        if (!player.isRemoved && (this.tickCount <= 20 || player.isIronManFlying)) {
            this.x = player.x.toFloat().toDouble()
            this.y = player.y.toFloat().toDouble()
            this.z = player.z.toFloat().toDouble()
            val f = player.velocity.lengthSquared().toFloat()
            if (f.toDouble() >= 1.0E-7) {
                this.volume = MathHelper.clamp(f / 4.0f, 0.02f, 5.0f)
            }

            if (this.tickCount < 20) {
                this.volume = 0.0f
            } else if (this.tickCount < 40) {
                this.volume *= (this.tickCount - 20).toFloat() / 20.0f
            }

            if (this.volume > 0.8f) {
                this.pitch = 1.0f + (this.volume - 0.8f)
            } else {
                this.pitch = 1.0f
            }
            this.volume *= 3f
        } else {
            this.setDone()
        }
    }
}
