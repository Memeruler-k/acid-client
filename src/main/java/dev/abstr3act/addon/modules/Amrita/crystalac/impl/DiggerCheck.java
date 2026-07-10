package dev.abstr3act.addon.modules.Amrita.crystalac.impl;

import dev.abstr3act.addon.modules.Amrita.crystalac.Check;
import dev.abstr3act.addon.modules.Seraphim.clicker.Timer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class DiggerCheck extends Check {
    Timer timer = new Timer();
    int i;

    public static BlockHitResult getTargetBlock(PlayerEntity player, double maxDistance) {
        Vec3d startPos = player.getCameraPosVec(1.0F);
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d endPos = startPos.add(lookVec.multiply(maxDistance));
        return player.getWorld().raycast(new RaycastContext(startPos, endPos, ShapeType.OUTLINE, FluidHandling.NONE, player));
    }

    @Override
    public void onUpdate(PlayerEntity player) {
        BlockHitResult hitResult = getTargetBlock(player, MeteorClient.mc.player.getEntityInteractionRange());
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = MeteorClient.mc.player.getWorld().getBlockState(blockPos);
        if (player.getMainHandStack().getItem() instanceof PickaxeItem
            && player.getPitch() >= 21.0F
            && player.handSwinging
            && blockState.getBlock() != null
            && !(blockState.getBlock() instanceof AirBlock)) {
            this.i++;
        }

        if (this.i >= 20) {
            this.flag(player, "挖坑狗你妈死了我挖你妈坟墓呢");
            this.i = 0;
        }
    }

    @Override
    public String getName() {
        return "Digger";
    }

    @Override
    public void onPacketReceive(Receive event, PlayerEntity entity) {
    }
}
