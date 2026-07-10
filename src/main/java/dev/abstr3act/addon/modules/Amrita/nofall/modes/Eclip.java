package dev.abstr3act.addon.modules.Amrita.nofall.modes;

import dev.abstr3act.addon.modules.Amrita.nofall.NoFallMode;
import dev.abstr3act.addon.modules.Amrita.nofall.NoFallModes;
import dev.abstr3act.addon.utils.seraphim.movement.ElytraUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class Eclip extends NoFallMode {
    private int ticks = 0;
    private int slot = -1;
    private int blocks = 0;
    private boolean cliped = false;
    private boolean groundcheck = false;
    private int timer = 0;
    private int teleports = 0;

    public Eclip() {
        super(NoFallModes.Elytra_Clip);
    }

    @Override
    public void onTickEventPre(Pre event) {
        FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
        if (!elytra.found()) {
            ChatUtils.error("Elytra not found", new Object[0]);
            this.settings.toggle();
        } else {
            if (this.mc.player.isOnGround() && this.groundcheck) {
                this.groundcheck = false;
                this.cliped = false;
                ChatUtils.infoPrefix("No Fall Plus", "Grounded in " + this.teleports + " teleports", new Object[0]);
                this.mc.player.fallDistance = 0.0F;
                this.teleports = 0;
            } else if (this.mc.player.fallDistance > 3.0F) {
                BlockHitResult result = this.mc
                    .world
                    .raycast(
                        new RaycastContext(this.mc.player.getPos(), this.mc.player.getPos().subtract(0.0, 10.0, 0.0), ShapeType.OUTLINE, FluidHandling.NONE, this.mc.player)
                    );
                if (result != null && result.getType() == Type.BLOCK) {
                    this.blocks = result.getBlockPos().add(0, 1, 0).getY();
                    this.cliped = true;
                } else if (result == null || result.getType() == Type.MISS) {
                    this.blocks = (int) this.mc.player.getPos().y - 10;
                    this.cliped = true;
                }
            }

            if (this.cliped) {
                this.clip();
            }
        }
    }

    @Override
    public void onSendPacket(Send event) {
        if (this.groundcheck) {
            if (event.packet instanceof PlayerMoveC2SPacket && ((IPlayerMoveC2SPacket) event.packet).getTag() != 1337) {
                ((PlayerMoveC2SPacketAccessor) event.packet).setOnGround(true);
            }
        }
    }

    private void clip() {
        if (this.blocks != 0) {
            ClientPlayerEntity player = this.mc.player;

            assert player != null;

            switch (this.ticks) {
                case 0:
                    FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
                    this.slot = elytra.slot();
                    InvUtils.move().from(this.slot).toArmor(2);
                    this.ticks++;
                case 1:
                    this.groundcheck = true;
                    this.mc.player.networkHandler.sendPacket(new OnGroundOnly(false));
                    this.ticks++;
                case 2:
                    this.mc.player.networkHandler.sendPacket(new OnGroundOnly(false));
                    this.ticks++;
                case 3:
                    ElytraUtils.startFly();
                    this.ticks++;
                case 4:
                    player.setPosition(player.getX(), this.blocks, player.getZ());
                    this.mc.player.networkHandler.sendPacket(new PositionAndOnGround(player.getX(), this.blocks, player.getZ(), true));
                    this.teleports++;
                    this.ticks++;
                case 5:
                    this.ticks = 0;
                    InvUtils.move().fromArmor(2).to(this.slot);
            }
        }
    }
}
