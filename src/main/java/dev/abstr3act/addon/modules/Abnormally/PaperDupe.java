package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;
import java.util.Optional;

public class PaperDupe extends AbnormallyModule {
    public PaperDupe() {
        super(Compassion.ABNORMALLY, "Dupe", ".");
    }

    @EventHandler
    private void onTick(Post event) {
        FindItemResult mace = InvUtils.find(new Item[]{Items.WRITABLE_BOOK});
        if (InvUtils.find(new Item[]{Items.WRITABLE_BOOK}).found()) {
            InvUtils.move().from(mace.slot()).to(this.mc.player.getInventory().selectedSlot);
        }

        for (int i = 9; i < 44; i++) {
            if (36 + this.mc.player.getInventory().selectedSlot != i) {
                this.mc
                    .player
                    .networkHandler
                    .sendPacket(
                        new ClickSlotC2SPacket(
                            this.mc.player.currentScreenHandler.syncId,
                            this.mc.player.currentScreenHandler.getRevision(),
                            i,
                            1,
                            SlotActionType.THROW,
                            ItemStack.EMPTY,
                            Int2ObjectMaps.emptyMap()
                        )
                    );
            }
        }

        this.mc
            .player
            .networkHandler
            .sendPacket(new BookUpdateC2SPacket(this.mc.player.getInventory().selectedSlot, List.of(""), Optional.of("The quick brown fox jumps over the lazy dog")));
        InvUtils.move().from(this.mc.player.getInventory().selectedSlot).to(mace.slot());
        this.toggle();
    }
}
