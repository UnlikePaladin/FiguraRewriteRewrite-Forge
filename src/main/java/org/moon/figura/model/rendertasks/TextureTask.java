package org.moon.figura.model.rendertasks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.luaj.vm2.LuaError;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.vector.FiguraVec2;
import org.moon.figura.math.vector.FiguraVec3;
import org.moon.figura.math.vector.FiguraVec4;
import org.moon.figura.model.PartCustomization;
import org.moon.figura.model.rendering.texture.FiguraTexture;
import org.moon.figura.model.rendering.texture.RenderTypes;
import org.moon.figura.utils.LuaUtils;

@LuaWhitelist
@LuaTypeDoc(
        name = "TextureTask",
        value = "texture_task"
)
public class TextureTask extends RenderTask {

    private ResourceLocation texture;
    private int textureW = -1, textureH = -1;
    private int width, height;
    private int regionW, regionH;
    private float u = 0f, v = 0f;
    private int r = 0xFF, g = 0xFF, b = 0xFF, a = 0xFF;
    private RenderTypes renderType = RenderTypes.TRANSLUCENT;

    public TextureTask(String name) {
        super(name);
    }

    @Override
    public boolean render(PartCustomization.Stack stack, MultiBufferSource buffer, int light, int overlay) {
        if (!enabled || texture == null || renderType == RenderTypes.NONE)
            return false;

        this.pushOntoStack(stack); //push
        PoseStack poseStack = stack.peek().copyIntoGlobalPoseStack();
        poseStack.scale(-1, -1, 1);

        //prepare variables
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        int newLight = this.light != null ? this.light : light;
        int newOverlay = this.overlay != null ? this.overlay : overlay;
        float u2 = u + regionW / (float) textureW;
        float v2 = v + regionH / (float) textureH;

        //setup texture render
        VertexConsumer consumer = buffer.getBuffer(renderType.get(texture));

        //create vertices
        consumer.vertex(pose, 0f, height, 0f).color(r, g, b, a).uv(u, v2).overlayCoords(newOverlay).uv2(newLight).normal(normal, 0f, 0f, -1f).endVertex();
        consumer.vertex(pose, width, height, 0f).color(r, g, b, a).uv(u2, v2).overlayCoords(newOverlay).uv2(newLight).normal(normal, 0f, 0f, -1f).endVertex();
        consumer.vertex(pose, width, 0f, 0f).color(r, g, b, a).uv(u2, v).overlayCoords(newOverlay).uv2(newLight).normal(normal, 0f, 0f, -1f).endVertex();
        consumer.vertex(pose, 0f, 0f, 0f).color(r, g, b, a).uv(u, v).overlayCoords(newOverlay).uv2(newLight).normal(normal, 0f, 0f, -1f).endVertex();

        stack.pop(); //pop
        return true;
    }

    @Override
    public int getComplexity() {
        return 1; //1 face, 1 complexity
    }


    // -- lua -- //


    @LuaWhitelist
    @LuaMethodDoc("texture_task.get_texture")
    public String getTexture() {
        return texture == null ? null : texture.toString();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {String.class, Integer.class, Integer.class},
                            argumentNames = {"textureLocation", "width", "height"}
                    ),
                    @LuaMethodOverload(
                            argumentTypes = FiguraTexture.class,
                            argumentNames = "texture"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {FiguraTexture.class, Integer.class, Integer.class},
                            argumentNames = {"texture", "width", "height"}
                    )
            },
            aliases = "texture",
            value = "texture_task.set_texture"
    )
    public TextureTask setTexture(Object texture, Integer width, Integer height) {
        if (texture == null) {
            this.texture = null;
            return this;
        }

        if (texture instanceof String s) {
            try {
                this.texture = new ResourceLocation(s);
            } catch (Exception e) {
                throw new LuaError(e.getMessage());
            }
            if (width == null || height == null)
                throw new LuaError("Texture dimensions cannot be null");
        } else if (texture instanceof FiguraTexture tex) {
            this.texture = tex.getLocation();
            if (width == null || height == null) {
                width = tex.getWidth();
                height = tex.getHeight();
            }
        } else {
            throw new LuaError("Illegal argument to setTexture(): " + texture.getClass().getSimpleName());
        }

        if (width <= 0 || height <= 0)
            throw new LuaError("Invalid texture size: " + width + "x" + height);

        this.textureW = this.regionW = this.width = width;
        this.textureH = this.regionH = this.height = height;
        return this;
    }

    @LuaWhitelist
    public TextureTask texture(Object texture, Integer width, Integer height) {
        return setTexture(texture, width, height);
    }

    @LuaWhitelist
    @LuaMethodDoc("texture_task.get_dimensions")
    public FiguraVec2 getDimensions() {
        return FiguraVec2.of(textureW, textureH);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "dimensions"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"width", "height"}
                    )
            },
            aliases = "dimensions",
            value = "texture_task.set_dimensions"
    )
    public TextureTask setDimensions(Object w, Double h) {
        FiguraVec2 vec = LuaUtils.parseVec2("setDimensions", w, h);
        if (vec.x <= 0 || vec.y <= 0)
            throw new LuaError("Invalid dimensions: " + vec.x + "x" + vec.y);
        this.textureW = (int) Math.round(vec.x);
        this.textureH = (int) Math.round(vec.y);
        return this;
    }

    @LuaWhitelist
    public TextureTask dimensions(Object w, Double h) {
        return setDimensions(w, h);
    }

    @LuaWhitelist
    @LuaMethodDoc("texture_task.get_size")
    public FiguraVec2 getSize() {
        return FiguraVec2.of(width, height);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "size"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"width", "height"}
                    )
            },
            aliases = "size",
            value = "texture_task.set_size"
    )
    public TextureTask setSize(Object w, Double h) {
        FiguraVec2 vec = LuaUtils.parseVec2("setSize", w, h);
        this.width = (int) Math.round(vec.x);
        this.height = (int) Math.round(vec.y);
        return this;
    }

    @LuaWhitelist
    public TextureTask size(Object w, Double h) {
        return setSize(w, h);
    }

    @LuaWhitelist
    @LuaMethodDoc("texture_task.get_region")
    public FiguraVec2 getRegion() {
        return FiguraVec2.of(regionW, regionH);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "region"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Integer.class, Integer.class},
                            argumentNames = {"width", "height"}
                    )
            },
            aliases = "region",
            value = "texture_task.set_region"
    )
    public TextureTask setRegion(Object w, Double h) {
        FiguraVec2 vec = LuaUtils.parseVec2("setRegion", w, h);
        this.regionW = (int) Math.round(vec.x);
        this.regionH = (int) Math.round(vec.y);
        return this;
    }

    @LuaWhitelist
    public TextureTask region(Object w, Double h) {
        return setRegion(w, h);
    }

    @LuaWhitelist
    @LuaMethodDoc("texture_task.get_uv")
    public FiguraVec2 getUV() {
        return FiguraVec2.of(u, v);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "uv"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"u", "v"}
                    )
            },
            aliases = "uv",
            value = "texture_task.set_uv"
    )
    public TextureTask setUV(Object u, Double v) {
        FiguraVec2 vec = LuaUtils.parseVec2("setUV", u, v);
        this.u = (float) vec.x;
        this.v = (float) vec.y;
        return this;
    }

    @LuaWhitelist
    public TextureTask uv(Object u, Double v) {
        return setUV(u, v);
    }

    @LuaWhitelist
    @LuaMethodDoc("texture_task.get_uv_pixels")
    public FiguraVec2 getUVPixels() {
        if (this.textureW == -1 || this.textureH == -1)
            throw new LuaError("Cannot call getUVPixels before defining the texture dimensions!");
        return getUV().multiply(this.textureW, this.textureH);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec2.class,
                            argumentNames = "uv"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class},
                            argumentNames = {"u", "v"}
                    )
            },
            aliases = "uvPixels",
            value = "texture_task.set_uv_pixels")
    public TextureTask setUVPixels(Object u, Double v) {
        if (this.textureW == -1 || this.textureH == -1)
            throw new LuaError("Cannot call setUVPixels before defining the texture dimensions!");

        FiguraVec2 uv = LuaUtils.parseVec2("setUVPixels", u, v);
        uv.divide(this.textureW, this.textureH);
        setUV(uv.x, uv.y);

        return this;
    }

    @LuaWhitelist
    public TextureTask uvPixels(Object u, Double v) {
        return setUVPixels(u, v);
    }

    @LuaWhitelist
    @LuaMethodDoc("texture_task.get_color")
    public FiguraVec4 getColor() {
        return FiguraVec4.of(r, g, b, a).scale(1f / 255f);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec3.class,
                            argumentNames = "rgb"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = FiguraVec4.class,
                            argumentNames = "rgba"
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {Double.class, Double.class, Double.class, Double.class},
                            argumentNames = {"r", "g", "b", "a"}
                    )
            },
            aliases = "color",
            value = "texture_task.set_color"
    )
    public TextureTask setColor(Object r, Double g, Double b, Double a) {
        FiguraVec4 color = LuaUtils.parseVec4("setColor", r, g, b, a, 0, 0, 0, 1);
        color.scale(0xFF);
        this.r = Math.max(Math.min((int) Math.round(color.x), 255), 0);
        this.g = Math.max(Math.min((int) Math.round(color.y), 255), 0);
        this.b = Math.max(Math.min((int) Math.round(color.z), 255), 0);
        this.a = Math.max(Math.min((int) Math.round(color.w), 255), 0);
        return this;
    }

    @LuaWhitelist
    public TextureTask color(Object r, Double g, Double b, Double a) {
        return setColor(r, g, b, a);
    }

    @LuaWhitelist
    @LuaMethodDoc("texture_task.get_render_type")
    public String getRenderType() {
        return renderType.name();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "renderType"
            ),
            aliases = "renderType",
            value = "texture_task.set_render_type"
    )
    public TextureTask setRenderType(@LuaNotNil String renderType) {
        try {
            this.renderType = RenderTypes.valueOf(renderType.toUpperCase());
            return this;
        } catch (Exception ignored) {
            throw new LuaError("Illegal RenderType: \"" + renderType + "\".");
        }
    }

    @LuaWhitelist
    public TextureTask renderType(@LuaNotNil String renderType) {
        return setRenderType(renderType);
    }

    @Override
    public String toString() {
        return name + " (Texture Render Task)";
    }
}