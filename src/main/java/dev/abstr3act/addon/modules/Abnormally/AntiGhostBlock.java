package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayDeque;

public class AntiGhostBlock extends AbnormallyModule {
    private final SettingGroup GBSettings = this.settings.getDefaultGroup();
    private final Setting<Integer> delay = this.GBSettings
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Fixer delay")).description("Delay for block fixing.")).defaultValue(50))
                .range(1, 250)
                .sliderRange(1, 250)
                .build()
        );
    private final Setting<Integer> range = this.GBSettings
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Fixer range")).description("Range to block.")).defaultValue(6)).range(1, 80).sliderRange(1, 80).build()
        );
    private final ArrayDeque<BlockPos> blocks = new ArrayDeque<>();
    private long millis = 0L;

    public AntiGhostBlock() {
        super(Compassion.ABNORMALLY, "AntiGhostBlock", "Automatically fix ghost blocks.");
    }

    @EventHandler
    public void onBlockBreak(BreakBlockEvent block) {
        this.blocks.add(block.blockPos);
    }

    public void onActivate() {
        this.millis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public void onDeactivate() {
        this.blocks.clear();
    }

    @EventHandler
    public void onLeave(GameLeftEvent event) {
        this.blocks.clear();
    }

    @EventHandler
    public void onTick(Post event) {
        if (!this.blocks.isEmpty()) {
            ClientPlayNetworkHandler conn = this.mc.getNetworkHandler();
            ClientPlayerEntity player = this.mc.player;
            if (conn != null && player != null && LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() >= this.millis) {
                BlockPos block = this.blocks.peek();

                assert block != null;

                assert this.mc.world != null;

                double distance = this.mc.player.squaredDistanceTo(block.getX(), block.getY(), block.getZ());
                BlockState state = this.mc.world.getBlockState(block);
                if (distance <= (this.range.get()).intValue() && state.isAir()) {
                    this.millis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + (this.delay.get()).intValue();
                    PlayerActionC2SPacket packet = new PlayerActionC2SPacket(Action.ABORT_DESTROY_BLOCK, block, Direction.UP, 0);
                    conn.sendPacket(packet);
                    this.blocks.remove();
                } else if (!state.isAir()) {
                    this.blocks.remove();
                }
            }
        }
    }
}
