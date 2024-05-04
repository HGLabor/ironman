package gg.norisk.heroes.events

import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.silkmc.silk.core.annotations.ExperimentalSilkApi
import net.silkmc.silk.core.event.Event

object KeyEvents {
    open class KeyEvent(val key: Int, val scanCode: Int, val action: Int, val client: MinecraftClient) {
        override fun toString(): String {
            return "KeyEvent(key=$key, scanCode=$scanCode, action=$action)"
        }

        fun isReleased(): Boolean = action == 0
        fun isClicked(): Boolean = action == 1
        fun isHold(): Boolean = action == 2

        fun matchesKeyBinding(keyBinding: KeyBinding): Boolean {
            return keyBinding.matchesKey(key, scanCode)
        }
    }

    @OptIn(ExperimentalSilkApi::class)
    val keyEvent = Event.onlySync<KeyEvent>()
}
