package dev.abstr3act.addon.modules.Luna;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.mixin.accessor.INClientPlayerInteractionManagerMixin;
import dev.abstr3act.addon.module.LunaModule;
import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;

import java.util.Objects;

public class InstaMine extends LunaModule {
    private final Mutable blockPos = new Mutable(0, -1, 0);
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<Boolean> rotate = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("rotate")).description("Faces the blocks being mined server side.")).defaultValue(true)).build());
    private final Setting<Boolean> render = this.sgRender
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("render")).description("Renders a block overlay on the block being broken.")).defaultValue(true))
                .build()
        );
    private final Setting<Boolean> armswing = this.sgRender
        .add(((Builder) ((Builder) ((Builder) new Builder().name("armswing")).description("send armswing packet.")).defaultValue(true)).build());
    private final Setting<ShapeMode> shapeMode = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("shape-mode"))
                .description("How the shapes are rendered."))
                .defaultValue(ShapeMode.Both))
                .build()
        );
    private final Setting<SettingColor> sideColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("side-color"))
                .description("The color of the sides of the blocks being rendered."))
                .defaultValue(new SettingColor(204, 0, 0, 10))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("line-color"))
                .description("The color of the lines of the blocks being rendered."))
                .defaultValue(new SettingColor(204, 0, 0, 255))
                .build()
        );
    BlockPos last;
    private Direction direction;

    public InstaMine() {
        super(Compassion.LUNA, "insta-mine", "Attempts to instantly mine blocks.");
    }

    public void onActivate() {
        this.last = new BlockPos(0, -128, 0);
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        this.direction = event.direction;
        this.blockPos.set(event.blockPos);
    }

    @EventHandler
    private void onTick(Pre event) {
        if (Objects.requireNonNull(this.mc.interactionManager).isBreakingBlock()) {
            this.last = ((INClientPlayerInteractionManagerMixin) this.mc.interactionManager).getCurrentBreakingPos();
        }

        if (this.last.getY() != -128) {
            this.mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, this.last, this.direction));
            if (this.armswing.get()) {
                this.mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
    }

    private boolean shouldMine() {
        return this.blockPos.getY() == -128 ? false : BlockUtils.canBreak(this.blockPos);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.render.get() && this.shouldMine()) {
            event.renderer.box(this.blockPos, (Color) this.sideColor.get(), (Color) this.lineColor.get(), (ShapeMode) this.shapeMode.get(), 0);
        }
    }
}
