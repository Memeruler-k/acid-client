package dev.abstr3act.addon.modules.Luna;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.luna.MathUtils;
import dev.abstr3act.addon.utils.luna.RotateManager;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public final class DesyncESP extends AbnormallyModule {
    public static DesyncESP INSTANCE;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Type> type = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("type")).description("Type")).defaultValue(Type.ClientSide)).build());
    public final Setting<SettingColor> color = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("color"))
                .description("Color"))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    Model model;
    boolean update = true;
    float lastYaw;
    float lastPitch;

    public DesyncESP() {
        super(Compassion.LUNA, "DesyncESP", "fku");
        INSTANCE = this;
    }

    private static void prepareScale(MatrixStack matrixStack) {
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(1.6F, 1.8F, 1.6F);
        matrixStack.translate(0.0F, -1.501F, 0.0F);
    }

    @EventHandler
    public void onLogin(GameJoinedEvent event) {
        this.update = true;
    }

    @EventHandler
    public void onUpdate(Post event) {
        if (!this.nullCheck()) {
            if (this.update) {
                this.model = new Model();
                this.update = false;
            }
        }
    }

    @EventHandler
    public void onUpdateWalkingPost(PlayerMoveEvent event) {
        this.lastYaw = this.mc.player.getYaw();
        this.lastPitch = this.mc.player.getPitch();
    }

    public boolean nullCheck() {
        return this.mc.player == null || this.mc.world == null;
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!this.nullCheck() && this.model != null) {
            if (this.mc.options.getPerspective() != Perspective.FIRST_PERSON) {
                if (!(Math.abs(this.lastYaw - this.mc.player.getYaw()) < 1.0F) || !(Math.abs(this.lastPitch - this.mc.player.getPitch()) < 1.0F)) {
                    RenderSystem.depthMask(false);
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(770, 771, 0, 1);
                    double x = this.mc.player.prevX
                        + (this.mc.player.getX() - this.mc.player.prevX) * this.mc.getRenderTickCounter().getTickDelta(true)
                        - this.mc.getEntityRenderDispatcher().camera.getPos().getX();
                    double y = this.mc.player.prevY
                        + (this.mc.player.getY() - this.mc.player.prevY) * this.mc.getRenderTickCounter().getTickDelta(true)
                        - this.mc.getEntityRenderDispatcher().camera.getPos().getY();
                    double z = this.mc.player.prevZ
                        + (this.mc.player.getZ() - this.mc.player.prevZ) * this.mc.getRenderTickCounter().getTickDelta(true)
                        - this.mc.getEntityRenderDispatcher().camera.getPos().getZ();
                    float bodyYaw = this.type.get() == Type.ServerSide
                        ? RotateManager.getPrevRenderYawOffset()
                        + (RotateManager.getRenderYawOffset() - RotateManager.getPrevRenderYawOffset()) * this.mc.getRenderTickCounter().getTickDelta(true)
                        : this.mc.player.prevBodyYaw + (this.mc.player.bodyYaw - this.mc.player.prevBodyYaw) * this.mc.getRenderTickCounter().getTickDelta(true);
                    float headYaw = this.type.get() == Type.ServerSide
                        ? RotateManager.getPrevRotationYawHead()
                        + (RotateManager.getRotationYawHead() - RotateManager.getPrevRotationYawHead()) * this.mc.getRenderTickCounter().getTickDelta(true)
                        : this.mc.player.prevHeadYaw + (this.mc.player.headYaw - this.mc.player.prevHeadYaw) * this.mc.getRenderTickCounter().getTickDelta(true);
                    float pitch = this.type.get() == Type.ServerSide
                        ? RotateManager.getPrevPitch()
                        + (RotateManager.getRenderPitch() - RotateManager.getPrevPitch()) * this.mc.getRenderTickCounter().getTickDelta(true)
                        : this.mc.player.prevPitch + (this.mc.player.getPitch() - this.mc.player.prevPitch) * this.mc.getRenderTickCounter().getTickDelta(true);
                    event.matrices.push();
                    event.matrices.translate((float) x, (float) y, (float) z);
                    event.matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtils.rad(180.0F - bodyYaw)));
                    prepareScale(event.matrices);
                    this.model
                        .modelPlayer
                        .animateModel(
                            this.mc.player,
                            this.mc.player.limbAnimator.getPos(this.mc.getRenderTickCounter().getTickDelta(true)),
                            this.mc.player.limbAnimator.getSpeed(this.mc.getRenderTickCounter().getTickDelta(true)),
                            this.mc.getRenderTickCounter().getTickDelta(true)
                        );
                    this.model
                        .modelPlayer
                        .setAngles(
                            this.mc.player,
                            this.mc.player.limbAnimator.getPos(this.mc.getRenderTickCounter().getTickDelta(true)),
                            this.mc.player.limbAnimator.getSpeed(this.mc.getRenderTickCounter().getTickDelta(true)),
                            this.mc.player.age,
                            headYaw - bodyYaw,
                            pitch
                        );
                    RenderSystem.enableBlend();
                    GL11.glDisable(2929);
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ZERO);
                    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                    this.model.modelPlayer.render(event.matrices, buffer, 10, 0, new Color(255, 255, 255, 255).getRGB());
                    BufferRenderer.drawWithGlobalProgram(buffer.end());
                    RenderSystem.disableBlend();
                    GL11.glEnable(2929);
                    event.matrices.pop();
                    RenderSystem.disableBlend();
                    RenderSystem.depthMask(true);
                }
            }
        }
    }

    public static enum Type {
        ClientSide,
        ServerSide;
    }

    private class Model {
        private final PlayerEntityModel<PlayerEntity> modelPlayer = new PlayerEntityModel(
            new Context(
                DesyncESP.this.mc.getEntityRenderDispatcher(),
                DesyncESP.this.mc.getItemRenderer(),
                DesyncESP.this.mc.getBlockRenderManager(),
                DesyncESP.this.mc.getEntityRenderDispatcher().getHeldItemRenderer(),
                DesyncESP.this.mc.getResourceManager(),
                DesyncESP.this.mc.getEntityModelLoader(),
                DesyncESP.this.mc.textRenderer
            )
                .getPart(EntityModelLayers.PLAYER),
            false
        );

        public Model() {
            this.modelPlayer.getHead().scale(new Vector3f(-0.3F, -0.3F, -0.3F));
        }
    }
}
