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
    val secondAbilityKey = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.ironman.repulsor",
            GLFW.GLFW_KEY_C,
            "key.ironman.categories.abilities"
        )
    )
    val thirdAbilityKey = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.ironman.suit",
            GLFW.GLFW_KEY_G,
            "key.ironman.categories.abilities"
        )
    )

    fun init() {}
}
