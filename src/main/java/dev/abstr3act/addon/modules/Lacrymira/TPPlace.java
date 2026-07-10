package dev.abstr3act.addon.modules.Lacrymira;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LacrymiraModule;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class TPPlace extends LacrymiraModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<Double> maxDistance = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("max-distance")).description("The maximum distance you can teleport.")).defaultValue(20.0).min(0.0).build());
    private final Setting<Double> moveDistance = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("move-distance")).description("Max distance for a packet to move."))
                .defaultValue(20.0)
                .sliderMax(1.0)
                .sliderMin(128.0)
                .build()
        );
    boolean placed;

    public TPPlace() {
        super(Compassion.LACRYMIRA, "TPPlace", "Unrestricted block placements");
    }

    public float[] getRotations(BlockPos targetPos) {
        if (this.mc.player == null) {
            return new float[]{0.0F, 0.0F};
        } else {
            Vec3d playerPos = this.mc.player.getPos();
            double dx = targetPos.getX() + 0.5 - playerPos.x;
            double dy = targetPos.getY() + 0.5 - (playerPos.y + this.mc.player.getEyeHeight(this.mc.player.getPose()));
            double dz = targetPos.getZ() + 0.5 - playerPos.z;
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
            float pitch = (float) (-(Math.atan2(dy, horizontalDistance) * 180.0 / Math.PI));
            return new float[]{yaw, pitch};
        }
    }

    @EventHandler
    private void onTick(Post event) {
        BlockHitResult hitResult = (BlockHitResult) this.mc.player.raycast(this.maxDistance.get(), 0.05F, false);
        if (this.mc.player != null && this.mc.world != null) {
            if (!(this.mc.player.squaredDistanceTo(hitResult.getBlockPos().toCenterPos()) <= this.mc.player.getBlockInteractionRange())) {
                if (!this.mc.player.getMainHandStack().isEmpty()) {
                    if (this.mc.player.getMainHandStack().getItem() instanceof BlockItem) {
                        Direction side = hitResult.getSide();
                        BlockPos targetPos = hitResult.getBlockPos().offset(side);
                        if (this.mc.world.getBlockState(targetPos).isAir()) {
                            if (this.mc.options.useKey.isPressed()) {
                                Vec3d teleportOffset = this.getOffsetByDirection(side);
                                Vec3d teleportPos = new Vec3d(
                                    targetPos.getX() + 0.5 + teleportOffset.x, targetPos.getY() + 0.5 + teleportOffset.y, targetPos.getZ() + 0.5 + teleportOffset.z
                                );
                                TPUtil.doTp(this.mc.player.getPos(), teleportPos, this.moveDistance.get(), false);
                                BlockUtils.place(targetPos, Hand.MAIN_HAND, this.mc.player.getInventory().selectedSlot, false, 50, true, true, false);
                                this.placed = true;
                            }

                            if (this.placed) {
                                this.placed = false;
                            }
                        }
                    }
                }
            }
        }
    }

    private Vec3d getOffsetByDirection(Direction side) {
        return switch (side) {
            case DOWN -> new Vec3d(0.0, -1.0, 0.0);
            case UP -> new Vec3d(0.0, 1.0, 0.0);
            case NORTH -> new Vec3d(0.0, 0.0, -1.0);
            case SOUTH -> new Vec3d(0.0, 0.0, 1.0);
            case WEST -> new Vec3d(-1.0, 0.0, 0.0);
            case EAST -> new Vec3d(1.0, 0.0, 0.0);
            default -> throw new MatchException(null, null);
        };
    }
}
