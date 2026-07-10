package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.command.TestCommand;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({GenericContainerScreen.class})
public abstract class GenericContainerScreenMixin
    extends HandledScreen<GenericContainerScreenHandler>
    implements ScreenHandlerProvider<GenericContainerScreenHandler> {
    public GenericContainerScreenMixin(GenericContainerScreenHandler container, PlayerInventory playerInventory, Text name) {
        super(container, playerInventory, name);
    }

    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save Handler"), b -> this.saveScreenHandler()).dimensions(10, 10, 100, 10).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Kill Handler"), b -> this.killScreenHandler()).dimensions(10, 20, 100, 10).build());
    }

    @Unique
    private void saveScreenHandler() {
        TestCommand.savedScreen = MeteorClient.mc.currentScreen;
        TestCommand.savedScreenHandler = MeteorClient.mc.player.currentScreenHandler;
        AChatUtils.sendMsgSeraphim(Text.of("Saved current screen handler."));
    }

    @Unique
    private void killScreenHandler() {
        MeteorClient.mc.setScreen(null);
        AChatUtils.sendMsgSeraphim(Text.of("Set current screen to empty."));
    }
}
