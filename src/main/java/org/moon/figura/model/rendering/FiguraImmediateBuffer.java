package org.moon.figura.model.rendering;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.model.PartCustomization;
import org.moon.figura.model.rendering.texture.FiguraTextureSet;
import org.moon.figura.model.rendering.texture.RenderTypes;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class FiguraImmediateBuffer {

    private final FiguraTextureSet textureSet;
    private final PartCustomization.PartCustomizationStack customizationStack;
    public final FloatBuffer positions, uvs, normals;

    private static final FiguraVec4 pos = FiguraVec4.of();
    private static final FiguraVec3 normal = FiguraVec3.of();
    private static final FiguraVec3 uv = FiguraVec3.of(0, 0, 1);

    private FiguraImmediateBuffer(FloatArrayList posList, FloatArrayList uvList, FloatArrayList normalList, FiguraTextureSet textureSet, PartCustomization.PartCustomizationStack customizationStack) {
        positions = BufferUtils.createFloatBuffer(posList.size());
        positions.put(posList.toArray(new float[0]));
        uvs = BufferUtils.createFloatBuffer(uvList.size());
        uvs.put(uvList.toArray(new float[0]));
        normals = BufferUtils.createFloatBuffer(normalList.size());
        normals.put(normalList.toArray(new float[0]));
        this.textureSet = textureSet;
        this.customizationStack = customizationStack;
    }

    public void clean() {
        textureSet.clean();
    }

    public void uploadTexIfNeeded() {
        textureSet.uploadIfNeeded();
    }

    public void markBuffers() {
        positions.mark();
        uvs.mark();
        normals.mark();
    }

    public void resetBuffers() {
        positions.reset();
        uvs.reset();
        normals.reset();
    }

    public void clearBuffers() {
        positions.clear();
        uvs.clear();
        normals.clear();
    }

    /**
     * Advances the buffers without drawing those vertices.
     * @param faceCount The number of faces to skip
     */
    public void advanceBuffers(int faceCount) {
        positions.position(positions.position() + faceCount * 12);
        uvs.position(uvs.position() + faceCount * 8);
        normals.position(normals.position() + faceCount * 12);
    }

    public void pushVertices(ImmediateAvatarRenderer renderer, int faceCount, int[] remainingComplexity) {
        //Handle cases that we can quickly
        if (faceCount == 0)
            return;

        PartCustomization customization = customizationStack.peek();
        if (!customization.render) {
            advanceBuffers(faceCount);
            //Refund complexity for invisible parts
            remainingComplexity[0] += faceCount;
            return;
        }

        VertexData primary = getTexture(renderer, customization, textureSet, true);
        VertexData secondary = getTexture(renderer, customization, textureSet, false);

        if (primary.renderType == null && secondary.renderType == null) {
            advanceBuffers(faceCount);
            remainingComplexity[0] += faceCount;
            return;
        }

        if (primary.renderType != null) {
            if (secondary.renderType != null)
                markBuffers();
            pushToBuffer(faceCount, primary);
        }
        if (secondary.renderType != null) {
            if (primary.renderType != null)
                resetBuffers();
            pushToBuffer(faceCount, secondary);
        }
    }

    private VertexData getTexture(ImmediateAvatarRenderer renderer, PartCustomization customization, FiguraTextureSet textureSet, boolean primary) {
        RenderTypes types = primary ? customization.getPrimaryRenderType() : customization.getSecondaryRenderType();
        Pair<FiguraTextureSet.OverrideType, Object> texture = primary ? customization.primaryTexture : customization.secondaryTexture;
        VertexData ret = new VertexData();

        if (types == RenderTypes.NONE)
            return ret;

        //get texture
        ResourceLocation id = textureSet.getOverrideTexture(renderer.avatar.owner, texture);

        //color
        ret.color = primary ? customization.color : customization.color2;

        //primary
        ret.primary = primary;

        //get render type
        if (id != null) {
            if (renderer.translucent) {
                ret.renderType = RenderType.itemEntityTranslucentCull(id);
                return ret;
            }
            if (renderer.glowing) {
                ret.renderType = RenderType.outline(id);
                return ret;
            }
        }

        if (types == null)
            return ret;

        if (renderer.offsetRenderLayers && !primary && types.isOffset())
            ret.vertexOffset = -0.005f;

        //Switch to cutout with fullbright if the iris emissive fix is enabled
        if (renderer.doIrisEmissiveFix && types == RenderTypes.EMISSIVE) {
            ret.fullBright = true;
            ret.renderType = RenderTypes.TRANSLUCENT_CULL.get(id);
        } else {
            ret.renderType = types.get(id);
        }

        return ret;
    }

    private void pushToBuffer(int faceCount, VertexData vertexData) {
        FloatArrayList buffer = ImmediateAvatarRenderer.VERTEX_BUFFER.getBufferFor(vertexData.renderType, vertexData.primary);
        PartCustomization customization = customizationStack.peek();

        FiguraVec3 uvFixer = FiguraVec3.of();
        uvFixer.set(textureSet.getWidth(), textureSet.getHeight(), 1); //Dividing by this makes uv 0 to 1

        double overlay = customization.overlay;
        double light = vertexData.fullBright ? LightTexture.FULL_BRIGHT : customization.light;

        for (int i = 0; i < faceCount * 4; i++) {
            pos.set(positions.get(), positions.get(), positions.get(), 1);
            pos.transform(customization.positionMatrix);
            pos.add(pos.normalized().scale(vertexData.vertexOffset));
            normal.set(normals.get(), normals.get(), normals.get());
            normal.transform(customization.normalMatrix);
            uv.set(uvs.get(), uvs.get(), 1);
            uv.divide(uvFixer);
            uv.transform(customization.uvMatrix);

            buffer.add((float) pos.x);
            buffer.add((float) pos.y);
            buffer.add((float) pos.z);

            buffer.add((float) vertexData.color.x);
            buffer.add((float) vertexData.color.y);
            buffer.add((float) vertexData.color.z);
            buffer.add((float) customization.alpha);

            buffer.add((float) uv.x);
            buffer.add((float) uv.y);

            buffer.add((float) overlay);
            buffer.add((float) light);

            buffer.add((float) normal.x);
            buffer.add((float) normal.y);
            buffer.add((float) normal.z);
        }
    }

    public static class VertexData {
        public RenderType renderType;
        public boolean fullBright;
        public float vertexOffset;
        public FiguraVec3 color;
        public boolean primary;
    }

    public static class Builder {
        private final List<Vertex> vertices = new ArrayList<>();

        public static class Vertex {
            public float x, y, z;
            public float u, v;
            public float nx, ny, nz;
        }

        public void vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
            Vertex vx = new Vertex();
            vx.x = x; vx.y = y; vx.z = z;
            vx.u = u; vx.v = v;
            vx.nx = nx; vx.ny = ny; vx.nz = nz;
            vertices.add(vx);
        }

        public FiguraImmediateBuffer build(FiguraTextureSet textureSet, PartCustomization.PartCustomizationStack customizationStack) {
            int size = vertices.size();
            FloatArrayList positions = new FloatArrayList(size * 3);
            FloatArrayList uvs = new FloatArrayList(size * 2);
            FloatArrayList normals = new FloatArrayList(size * 3);
            for (Vertex vertex : vertices) {
                positions.add(vertex.x);
                positions.add(vertex.y);
                positions.add(vertex.z);
                uvs.add(vertex.u);
                uvs.add(vertex.v);
                normals.add(vertex.nx);
                normals.add(vertex.ny);
                normals.add(vertex.nz);
            }
            return new FiguraImmediateBuffer(positions, uvs, normals, textureSet, customizationStack);
        }
    }
}
