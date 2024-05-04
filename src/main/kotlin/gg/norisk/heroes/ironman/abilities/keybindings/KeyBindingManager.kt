package gg.norisk.heroes.ironman.abilities.keybindings

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW

object KeyBindingManager {
    val firstAbilityKey = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.ironman.fly",
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.ironman.categories.abilities"
        )
    )

    fun init() {}
}
