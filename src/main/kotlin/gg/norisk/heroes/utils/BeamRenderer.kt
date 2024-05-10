package gg.norisk.heroes.utils

import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import org.joml.Matrix3f
import org.joml.Matrix4f

object BeamRenderer {
    fun renderBeam(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        f: Float,
        l: Long,
        i: Float,
        j: Float,
        fs: FloatArray
    ) {
        renderBeam(
            matrixStack,
            vertexConsumerProvider,
            BeaconBlockEntityRenderer.BEAM_TEXTURE,
            f,
            1.0f,
            l,
            i,
            j,
            fs,
            0.2f,
            0.25f
        )
    }

    fun renderBeam(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        identifier: Identifier?,
        f: Float,
        g: Float,
        l: Long,
        i: Float,
        j: Float,
        fs: FloatArray,
        h: Float,
        k: Float
    ) {
        val m = i + j
        matrixStack.push()
        matrixStack.translate(0.5, 0.0, 0.5)
        val n = Math.floorMod(l, 40).toFloat() + f
        val o = if (j < 0) n else -n
        val p = MathHelper.fractionalPart(o * 0.2f - MathHelper.floor(o * 0.1f).toFloat())
        val q = fs[0]
        val r = fs[1]
        val s = fs[2]
        matrixStack.push()
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n * 2.25f - 45.0f))
        var t = 0.0f
        var w = 0.0f
        var x = -h
        val y = 0.0f
        val z = 0.0f
        val aa = -h
        var ab = 0.0f
        var ac = 1.0f
        var ad = -1.0f + p
        var ae = j.toFloat() * g * (0.5f / h) + ad
        renderBeamLayer(
            matrixStack,
            vertexConsumerProvider.getBuffer(RenderLayer.getBeaconBeam(identifier, false)),
            q,
            r,
            s,
            1.0f,
            i,
            m,
            0.0f,
            h,
            h,
            0.0f,
            x,
            0.0f,
            0.0f,
            aa,
            0.0f,
            1.0f,
            ae,
            ad
        )
        matrixStack.pop()
        t = -k
        val u = -k
        w = -k
        x = -k
        ab = 0.0f
        ac = 1.0f
        ad = -1.0f + p
        ae = j.toFloat() * g + ad
        renderBeamLayer(
            matrixStack,
            vertexConsumerProvider.getBuffer(RenderLayer.getBeaconBeam(identifier, true)),
            q,
            r,
            s,
            0.125f,
            i,
            m,
            t,
            u,
            k,
            w,
            x,
            k,
            k,
            k,
            0.0f,
            1.0f,
            ae,
            ad
        )
        matrixStack.pop()
    }

    private fun renderBeamLayer(
        matrixStack: MatrixStack,
        vertexConsumer: VertexConsumer,
        f: Float,
        g: Float,
        h: Float,
        i: Float,
        j: Float,
        k: Float,
        l: Float,
        m: Float,
        n: Float,
        o: Float,
        p: Float,
        q: Float,
        r: Float,
        s: Float,
        t: Float,
        u: Float,
        v: Float,
        w: Float
    ) {
        val entry = matrixStack.peek()
        val matrix4f = entry.positionMatrix
        val matrix3f = entry.normalMatrix
        renderBeamFace(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, l, m, n, o, t, u, v, w)
        renderBeamFace(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, r, s, p, q, t, u, v, w)
        renderBeamFace(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, n, o, r, s, t, u, v, w)
        renderBeamFace(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, p, q, l, m, t, u, v, w)
    }

    private fun renderBeamFace(
        matrix4f: Matrix4f,
        matrix3f: Matrix3f,
        vertexConsumer: VertexConsumer,
        f: Float,
        g: Float,
        h: Float,
        i: Float,
        j: Float,
        k: Float,
        l: Float,
        m: Float,
        n: Float,
        o: Float,
        p: Float,
        q: Float,
        r: Float,
        s: Float
    ) {
        renderBeamVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, k, l, m, q, r)
        renderBeamVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, l, m, q, s)
        renderBeamVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, n, o, p, s)
        renderBeamVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, k, n, o, p, r)
    }

    private fun renderBeamVertex(
        matrix4f: Matrix4f,
        matrix3f: Matrix3f,
        vertexConsumer: VertexConsumer,
        f: Float,
        g: Float,
        h: Float,
        i: Float,
        j: Float,
        k: Float,
        l: Float,
        m: Float,
        n: Float
    ) {
        vertexConsumer.vertex(matrix4f, k, j, l)
            .color(f, g, h, i)
            .texture(m, n)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(15728880)
            .normal(matrix3f, 0.0f, 1.0f, 0.0f)
            .next()
    }
}