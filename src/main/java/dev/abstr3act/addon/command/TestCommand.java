package dev.abstr3act.addon.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandSource;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class TestCommand extends Command {
    public static Screen savedScreen;
    public static ScreenHandler savedScreenHandler;

    public TestCommand() {
        super("screenhandler", "Use random string to fuck your enemy up", new String[0]);
    }

    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("open").executes(context -> {
            if (savedScreen != null && savedScreenHandler != null) {
                mc.setScreen(savedScreen);
                mc.currentScreen = savedScreen;
                mc.player.currentScreenHandler = savedScreenHandler;
                AChatUtils.sendMsgSeraphim(Text.of("Set screen handler to '+" + savedScreen.getTitle() + "+'"));
            } else {
                AChatUtils.sendMsgSeraphim(Text.of("Please save screen and screen handler in target GUI."));
            }

            return 1;
        }));
        builder.then(literal("debug").executes(context -> {
            MeteorClient.EVENT_BUS.subscribe(this);
            return 1;
        }));
        builder.then(literal("off").executes(context -> {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            return 1;
        }));
    }

    @EventHandler
    public void onTickEvent(Post event) {
        if (mc.player != null) {
            if (mc.player.currentScreenHandler != null) {
                AChatUtils.sendMsgSeraphim(Text.of("Screen: " + mc.player.currentScreenHandler.syncId));
                AChatUtils.sendMsgSeraphim(Text.of("Slot: " + mc.player.currentScreenHandler.slots.size()));
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 1, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        }
    }
}
