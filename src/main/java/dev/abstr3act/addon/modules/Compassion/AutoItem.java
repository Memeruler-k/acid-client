package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventInteractBlock;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class AutoItem extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> waterBucket = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("WaterBucket")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> block = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Blocks")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> silent = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("SilentSwap")).description(".")).defaultValue(false)).build());
    private final Setting<Integer> TBlock = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("BlockSwapBackDelay"))
                .min(0)
                .description("."))
                .defaultValue(5))
                .sliderRange(0, 60)
                .visible(() -> !this.silent.get()))
                .build()
        );
    int bucketTimer;
    int blockTimer = 0;
    boolean swapped = false;

    public AutoItem() {
        super(Compassion.COMPASSION, "AutoItem", ".");
    }

    @EventHandler
    public void onTick(Post event) {
        if (this.mc.options.useKey.isPressed() && this.isInWeb() && !this.mc.player.isInFluid()) {
            FindItemResult slot = InvUtils.findInHotbar(new Item[]{Items.WATER_BUCKET});
            if (slot.found()) {
                return;
            }

            InvUtils.swap(InvUtils.findInHotbar(new Item[]{Items.ENDER_PEARL}).slot(), false);
        }

        if (this.swapped) {
            if (this.blockTimer < this.TBlock.get()) {
                this.blockTimer++;
            } else {
                this.blockTimer = 0;
                InvUtils.swapBack();
                this.swapped = false;
            }
        }
    }

    @EventHandler
    private void onInteractBlock(EventInteractBlock event) {
        if (!this.mc.player.isBlocking() && !this.mc.player.isUsingItem()) {
            if (this.isInWeb()) {
                FindItemResult slot = InvUtils.findInHotbar(new Item[]{Items.WATER_BUCKET});
                InvUtils.swap(slot.slot(), true);
            } else if (this.mc.player.wasOnFire && this.mc.player.getYaw() >= 82.0F) {
                FindItemResult waterBucket = InvUtils.findInHotbar(new Item[]{Items.WATER_BUCKET});
                InvUtils.swap(waterBucket.slot(), false);
            } else if (!this.mc.player.isInLava()
                && !this.mc.player.isInFluid()
                && !this.mc.player.isOnGround()
                && this.mc.player.fallDistance < 4.0F
                && this.mc.player.getPitch() >= (PlayerUtils.isMoving() ? (this.mc.player.isSprinting() ? 70 : 75) : 82)) {
                FindItemResult result = InvUtils.findInHotbar(itemStack -> this.validItem(itemStack, event.getHitResult().getBlockPos()));
                InvUtils.swap(result.slot(), true);
                this.blockTimer = 0;
                if (this.silent.get()) {
                    event.cancel();
                    this.sendPacket(new PlayerInteractBlockC2SPacket(event.getHand(), event.getHitResult(), 0));
                    InvUtils.swapBack();
                } else {
                    this.swapped = true;
                }
            }
        }
    }

    private boolean validItem(ItemStack itemStack, BlockPos pos) {
        if (!(itemStack.getItem() instanceof BlockItem)) {
            return false;
        } else {
            Block block = ((BlockItem) itemStack.getItem()).getBlock();
            return !Block.isShapeFullCube(block.getDefaultState().getCollisionShape(this.mc.world, pos))
                ? false
                : !(block instanceof FallingBlock) || !FallingBlock.canFallThrough(this.mc.world.getBlockState(pos));
        }
    }

    public boolean isInWeb() {
        Box pBox = this.mc.player.getBoundingBox();
        BlockPos pBlockPos = BlockPos.ofFloored(this.mc.player.getPos());

        for (int x = pBlockPos.getX() - 2; x <= pBlockPos.getX() + 2; x++) {
            for (int y = pBlockPos.getY() - 1; y <= pBlockPos.getY() + 4; y++) {
                for (int z = pBlockPos.getZ() - 2; z <= pBlockPos.getZ() + 2; z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    if (pBox.intersects(new Box(bp)) && this.mc.world.getBlockState(bp).getBlock() == Blocks.COBWEB) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
