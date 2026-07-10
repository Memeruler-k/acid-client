package dev.abstr3act.addon.modules.Luna;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Vec3d;

public class MissHelper extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> movedistance = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("move-distance")).description("Value of move distance.")).defaultValue(10))
                .sliderMin(0)
                .sliderMax(50)
                .build()
        );
    private final Setting<Double> vclip1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("vclip2-distance"))
                .description("Value of vclip distance."))
                .defaultValue(30.0)
                .sliderMin(0.0)
                .sliderMax(250.0)
                .build()
        );
    private final Setting<Double> vclip2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("vclip1-distance"))
                .description("Value of vclip distance."))
                .defaultValue(100.0)
                .sliderMin(0.0)
                .sliderMax(250.0)
                .build()
        );
    private final Setting<Double> fallDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("fall-distance"))
                .description("Value of fall distance."))
                .defaultValue(5.0)
                .sliderMin(0.0)
                .sliderMax(250.0)
                .build()
        );
    private final Setting<Integer> attackCount = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("attack-count")).description("Value of attack count.")).defaultValue(2))
                .sliderMin(1)
                .min(1)
                .sliderMax(50)
                .build()
        );
    boolean popped = false;
    boolean attacked = false;
    private Setting<Boolean> packet = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("packet"))
                .description("Packet Mode"))
                .defaultValue(true))
                .build()
        );
    private Setting<Boolean> sneak = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("sneak"))
                .description("Sneak Mode"))
                .defaultValue(false))
                .build()
        );

    public MissHelper() {
        super(Compassion.ABNORMALLY, "MissHelper", "Increase miss chance");
    }

    @EventHandler
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity instanceof PlayerEntity) {
                    if (!entity.equals(this.mc.player) && !Friends.get().isFriend((PlayerEntity) entity) && Friends.get().isFriend((PlayerEntity) entity)) {
                        if (this.attacked && ((PlayerEntity) entity).hurtTime != 0) {
                            this.doAttack(this.vclip1.get(), entity, false);
                        }
                    }
                }
            }
        }
    }

    public void doAttack(double vclip, Entity entity, boolean first) {
        Vec3d pos = this.mc.player.getPos();
        TPUtil.doTp(pos.x, pos.y, pos.z, pos.x, pos.y + vclip, pos.z, (this.movedistance.get()).intValue(), false);
        TPUtil.doTp(pos.x, pos.y, pos.z, pos.x, pos.y + this.fallDistance.get(), pos.z, (this.movedistance.get()).intValue(), false);

        for (int i = 0; i < this.attackCount.get(); i++) {
            if (!this.packet.get()) {
                this.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, this.sneak.get()));
            } else {
                this.mc.interactionManager.attackEntity(this.mc.player, entity);
            }
        }

        TPUtil.doTp(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z, (this.movedistance.get()).intValue(), false);
        this.attacked = first;
    }
}
