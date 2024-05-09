package gg.norisk.heroes.events

import net.minecraft.client.util.InputUtil
import net.silkmc.silk.core.event.Event

object MouseEvents {
    open class MouseClickEvent(val key: InputUtil.Key, val pressed: Boolean)

    val mouseClickEvent = Event.onlySync<MouseClickEvent>()
}