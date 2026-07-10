package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.BlockUtil;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PopClip extends AbnormallyModule {
    private final SettingGroup sgNormal = this.settings.createGroup("Clip");
    public final Setting<Integer> clipValue = this.sgNormal
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("clip-value")).description("the height to clip.")).defaultValue(160))
                .sliderMin(0)
                .sliderMax(256)
                .build()
        );
    public final Setting<Integer> protectY = this.sgNormal
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("protect-y")).description("The max height this VClip module can reach.")).defaultValue(-63))
                .sliderMin(-70)
                .sliderMax(256)
                .build()
        );
    public final Setting<Boolean> voidProtect = this.sgNormal
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("void-protect"))
                .description("Prevents you from clipping into the void."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> feedBack = this.sgNormal
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("send-feedback"))
                .description("Send feedback while clip"))
                .defaultValue(false))
                .build()
        );
    public final Setting<Double> moveDistance = this.sgNormal
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("move-distance"))
                .description("Max distance for a packet to move."))
                .defaultValue(10.0)
                .sliderMax(50.0)
                .sliderMin(1.0)
                .sliderRange(1.0, 50.0)
                .build()
        );
    public boolean isPoped = false;

    public PopClip() {
        super(Compassion.ABNORMALLY, "PopClip", "Ez run");
    }

    @EventHandler
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity instanceof PlayerEntity) {
                    if (entity.equals(this.mc.player)) {
                        this.isPoped = true;
                        this.doClip();
                    }
                }
            }
        }
    }

    private void doClip() {
        double clipY = (this.clipValue.get()).intValue();
        Vec3d finalVec = this.mc.player.getPos();
        int offsetY = 0;
        int n = (int) clipY;
        if (offsetY <= n) {
            while (true) {
                Vec3d vec = new Vec3d(finalVec.x, finalVec.y + (clipY - offsetY), finalVec.z);
                if (vec.y > (this.protectY.get()).intValue() && BlockUtil.isSafeBlock(BlockPos.ofFloored(vec))) {
                    finalVec = vec;
                    break;
                }

                if (offsetY == n) {
                    break;
                }

                offsetY++;
            }
        }

        if (this.voidProtect.get() && finalVec.y <= -128.0) {
            finalVec = new Vec3d(finalVec.x, -128.0, finalVec.z);
        }

        Vec3d vec3d = this.mc.player.getPos();
        TPUtil.doTp(vec3d, finalVec, this.moveDistance.get(), true);
        this.mc.player.setPosition(finalVec);
        if (this.feedBack.get()) {
            AChatUtils.sendMsg("Cilp");
        }
    }
}
