package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.BlockUtil;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SuperVClip extends AbnormallyModule {
    public SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Integer> clipValue = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("clip-value")).description("the height to clip.")).defaultValue(160))
                .sliderMin(0)
                .sliderMax(256)
                .build()
        );
    public final Setting<Integer> protectY = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("protect-y")).description("The max height this VClip module can reach.")).defaultValue(-63))
                .sliderMin(-70)
                .sliderMax(256)
                .build()
        );
    public final Setting<Boolean> downClipWhenSneak = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("down-clip-when-sneaking"))
                .description("Whether clip down when sneaking."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> voidProtect = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("void-protect"))
                .description("Prevents you from clipping into the void."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> feedBack = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("send-feedback"))
                .description("Send feedback while clip"))
                .defaultValue(false))
                .build()
        );
    public final Setting<Double> moveDistance = this.sgGeneral
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

    public SuperVClip() {
        super(Compassion.ABNORMALLY, "SuperVClip", "Make you vclip.");
    }

    public void onActivate() {
        double clipY = (this.clipValue.get()).intValue();
        Vec3d finalVec = this.mc.player.getPos();
        int offsetY = 0;
        int n = (int) clipY;
        if (offsetY <= n) {
            while (true) {
                Vec3d vec = new Vec3d(
                    finalVec.x, this.downClipWhenSneak.get() && this.mc.player.isSneaking() ? finalVec.y - (clipY - offsetY) : finalVec.y + (clipY - offsetY), finalVec.z
                );
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
            MutableText msg = Text.empty();
            msg.append("Try VClip ");
            msg.append(Double.toString(finalVec.y - this.mc.player.getY()));
            msg.append(" m ...");
            AChatUtils.sendMsg(msg);
        }

        this.toggle();
    }
}
