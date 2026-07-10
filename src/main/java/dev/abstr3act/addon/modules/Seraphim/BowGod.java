package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.BlockUtil;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class BowGod extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> moveDistance = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("moveDistance")).description(".")).defaultValue(10.0).min(0.0).build());
    private final Setting<Double> powerDistance = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("powerDistance")).description(".")).defaultValue(50.0).min(0.0).build());

    public BowGod() {
        super(Compassion.SERAPHIM, "BowGod", "32k bow");
    }

    private boolean itemInHand() {
        return InvUtils.testInMainHand(new Item[]{Items.BOW});
    }

    @EventHandler
    private void onPacketSend(Send event) {
        if (!fullNullCheck()) {
            if (event.packet instanceof PlayerActionC2SPacket
                && ((PlayerActionC2SPacket) event.packet).getAction() == Action.RELEASE_USE_ITEM
                && this.mc.player.getActiveItem().getItem() == Items.BOW) {
                this.doShoot();
            }
        }
    }

    public void doSpoofs(@NotNull Vec3d vec3d, @NotNull Vec3d targetPos) {
        this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_SPRINTING));
        TPUtil.doTp(vec3d, targetPos, this.moveDistance.get(), false);
        this.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.STOP_SPRINTING));
        TPUtil.sendMovePacket(vec3d, false);
    }

    private void doShoot() {
        this.doBeforeShoot();
        Entity target = TargetUtils.getPlayerTarget(500.0, SortPriority.LowestDistance);
        if (target != null) {
            Vec3d farVec = this.getFarVec3d(this.mc.player.getPos(), target.getPos(), -this.powerDistance.get());
            this.doSpoofs(this.mc.player.getPos(), farVec);
        }
    }

    public void doBeforeShoot() {
        Vec3d playerPos = this.mc.player.getPos();
        TPUtil.sendMovePacket(playerPos.x, playerPos.y + 1.0E-4, playerPos.z, false);
    }

    private Vec3d getFarVec3d(Vec3d fromVec, Vec3d toVec, double distance) {
        for (double step = distance > 0.0 ? -1.0 : 1.0; distance != 0.0; distance += step) {
            Vec3d stepVec = this.getStraightVec(fromVec, toVec, distance);
            if (BlockUtil.checkCanMove(fromVec, stepVec) && BlockUtil.checkCanMove(stepVec, fromVec)) {
                return stepVec;
            }
        }

        return fromVec;
    }

    @NotNull
    public final Vec3d getStraightVec(@NotNull Vec3d fromVec, @NotNull Vec3d toVec, double range) {
        Vec3d vec = this.getVelocity(fromVec, toVec, range);
        return fromVec.add(vec);
    }

    @NotNull
    public final Vec3d getVelocity(@NotNull Vec3d fromVec, @NotNull Vec3d toVec, double speedPerTick) {
        Vec3d subtract = toVec.subtract(fromVec);
        double dis = Math.sqrt(subtract.x * subtract.x + subtract.y * subtract.y + subtract.z * subtract.z);
        double step = speedPerTick / dis;
        return new Vec3d(subtract.x * step, subtract.y * step, subtract.z * step);
    }
}
