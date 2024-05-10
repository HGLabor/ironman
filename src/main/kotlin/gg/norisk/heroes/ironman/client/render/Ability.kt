package gg.norisk.heroes.ironman.client.render

import gg.norisk.heroes.ironman.abilities.keybindings.KeyBindingManager
import gg.norisk.heroes.ironman.player.isIronMan
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.core.text.literalText
import net.silkmc.silk.core.world.pos.Pos2i

data class Ability(
    val text: (PlayerEntity) -> Text,
    val shouldRender: ((AbstractClientPlayerEntity) -> Boolean) = { true },
    val description: ((AbstractClientPlayerEntity) -> String),
)

val transformAbility = Ability({
    literalText {
        text(KeyBindingManager.thirdAbilityKey.boundKeyLocalizedText) {
            color = 0xD20103
        }
    }
}, { !it.isIronMan }, { "Transform" })

val sentryModeAbility = Ability({
    literalText {
        text(MinecraftClient.getInstance().options.sneakKey.boundKeyLocalizedText) {
            color = 0xD20103

        }
        if (MinecraftClient.getInstance().options.sneakKey.isPressed) {
            text(" + ") { }
            text(KeyBindingManager.thirdAbilityKey.boundKeyLocalizedText) { }
        }
    }
}, { it.isIronMan }, { "Sentry Mode" })

val energyBeamAbility = Ability({
    literalText {
        text(KeyBindingManager.secondAbilityKey.boundKeyLocalizedText) {
            color = 0xD20103
        }
        if (KeyBindingManager.secondAbilityKey.isPressed) {
            text(" + ") { }
            text(MinecraftClient.getInstance().options.attackKey.boundKeyLocalizedText) { }
        }
    }
}, { it.isIronMan }, { "Unibeam" })


val flyBoostAbility = Ability({
    literalText {
        text(KeyBindingManager.firstAbilityKey.boundKeyLocalizedText) {
            color = 0xD20103
        }
    }
}, { it.isIronMan }, { "Boost" })

val repulsorBlastAbility = Ability({
    literalText {
        text(KeyBindingManager.secondAbilityKey.boundKeyLocalizedText) {
            color = 0xD20103
        }
        if (KeyBindingManager.secondAbilityKey.isPressed) {
            text(" + ") { }
            text(MinecraftClient.getInstance().options.useKey.boundKeyLocalizedText) { }
        }
    }
}, { it.isIronMan }, { "Repulsor Blast" })

val missileAbility = Ability({
    literalText {
        text(KeyBindingManager.fourthAbilityKey.boundKeyLocalizedText) {
            color = 0xD20103
        }
        if (KeyBindingManager.fourthAbilityKey.isPressed) {
            text(" + ") { }
            text(MinecraftClient.getInstance().options.useKey.boundKeyLocalizedText) { }
            text(" (At Entity, Scroll) ") {  }
        }
    }
}, { it.isIronMan }, { "Guided Missile" })

object AbilityRenderer : HudRenderCallback {
    val abilities = mutableSetOf<Ability>()

    init {
        reload()
    }

    fun reload() {
        abilities.clear()
        abilities.add(transformAbility)
        abilities.add(sentryModeAbility)
        abilities.add(flyBoostAbility)
        abilities.add(missileAbility)
        abilities.add(energyBeamAbility)
        abilities.add(repulsorBlastAbility)
    }

    fun init() {
        HudRenderCallback.EVENT.register(this)
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            clientCommand("reloadabilitiyhud") {
                runs {
                    reload()
                }
            }
        }
    }

    override fun onHudRender(drawContext: DrawContext, tickDelta: Float) {
        if (MinecraftClient.getInstance().options.hudHidden) return
        val player = MinecraftClient.getInstance().player ?: return
        val offset = 2
        drawContext.matrices.push()
        val scale = 0.8f
        drawContext.matrices.scale(scale, scale, scale)
        abilities.filter { it.shouldRender(player) }.forEachIndexed { index, ability ->
            val text = literalText {
                /*if (ability.hold) {
                    text("Hold ") { color = 0x47CD45 }
                }*/
                text(ability.text.invoke(player)) { }
                text(" - ") { color = 0x919191 }
                text(ability.description.invoke(player))
            }
            val pos = Pos2i(5, 5 + (text.height + offset * 2) * index)
            drawContext.fill(
                RenderLayer.getGuiOverlay(),
                pos.x - offset,
                pos.z - offset,
                pos.x + text.width + offset,
                pos.z + text.height + offset,
                -1873784752
            )
            drawContext.drawText(
                MinecraftClient.getInstance().textRenderer,
                text,
                pos.x,
                pos.z,
                14737632,
                true
            )
        }
        drawContext.matrices.pop()
    }

    val Text.width
        get() = MinecraftClient.getInstance().textRenderer.getWidth(this)

    val Text.height
        get() = MinecraftClient.getInstance().textRenderer.fontHeight
}