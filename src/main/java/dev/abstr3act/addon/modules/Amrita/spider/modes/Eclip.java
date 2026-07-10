package dev.abstr3act.addon.modules.Amrita.spider.modes;

import dev.abstr3act.addon.modules.Amrita.spider.SpiderMode;
import dev.abstr3act.addon.modules.Amrita.spider.SpiderModes;
import dev.abstr3act.addon.modules.Amrita.spider.SpiderPlus;
import dev.abstr3act.addon.utils.seraphim.movement.ElytraUtils;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;

public class Eclip extends SpiderMode {
    private int ticks = 0;
    private int slot = -1;
    private double blocks = 0.0;

    public Eclip() {
        super(SpiderModes.Elytra_clip);
    }

    @Override
    public void onActivate() {
        FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
        if (!elytra.found()) {
            this.settings.error(Names.get(Items.ELYTRA) + " not found", new Object[0]);
            this.settings.toggle();
        }
    }

    @Override
    public void onTickEventPre(Pre event) {
        if (this.work() && this.mc.player.horizontalCollision) {
            this.blocks = (((SpiderPlus) Modules.get().get(SpiderPlus.class)).Blocks.get()).intValue();
            this.clip();
        } else {
            this.ticks = 0;
        }
    }

    private boolean work() {
        ClientPlayerEntity player = this.mc.player;

        assert player != null;

        FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
        return elytra.found();
    }

    private void clip() {
        if (this.blocks != 0.0) {
            ClientPlayerEntity player = this.mc.player;

            assert player != null;

            switch (this.ticks) {
                case 0:
                    FindItemResult elytra = InvUtils.find(new Item[]{Items.ELYTRA});
                    this.slot = elytra.slot();
                    InvUtils.move().from(this.slot).toArmor(2);
                    this.ticks++;
                case 1:
                    this.mc.player.networkHandler.sendPacket(new OnGroundOnly(false));
                    this.ticks++;
                case 2:
                    this.mc.player.networkHandler.sendPacket(new OnGroundOnly(false));
                    this.ticks++;
                case 3:
                    ElytraUtils.startFly();
                    this.ticks++;
                case 4:
                    player.setPosition(player.getX(), player.getY() + this.blocks, player.getZ());
                    this.mc.player.networkHandler.sendPacket(new PositionAndOnGround(player.getX(), player.getY() + this.blocks, player.getZ(), false));
                    this.ticks++;
                case 5:
                    ElytraUtils.startFly();
                    this.ticks++;
                case 6:
                    this.ticks = 0;
                    this.blocks = 0.0;
                    InvUtils.move().fromArmor(2).to(this.slot);
            }
        }
    }
}
