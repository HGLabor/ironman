package gg.norisk.heroes.events

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.silkmc.silk.core.event.Cancellable
import net.silkmc.silk.core.event.Event
import net.silkmc.silk.core.event.EventScopeProperty

object RenderEvents {
    open class EntityRendererEvent(
        val entity: Entity,
        val f: Float,
        val g: Float,
        val matrixStack: MatrixStack,
        val vertexConsumerProvider: VertexConsumerProvider,
        val light: Int
    ) : Cancellable {
        override val isCancelled: EventScopeProperty<Boolean> = EventScopeProperty(false)
    }

    val entityRendererEvent = Event.onlySync<EntityRendererEvent>()
}