package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin({GameMenuScreen.class})
public abstract class GameMenuScreenMixin extends Screen {
    public Pair<ServerAddress, ServerInfo> lastServerConnection;
    private ButtonWidget reconnectButton;
    private ServerInfo lastServer;

    private GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(
        at = {@At("TAIL")},
        method = {"initWidgets()V"}
    )
    private void onInitWidgets(CallbackInfo ci) {
        this.addButton();
    }

    @Unique
    private void addButton() {
        List<ClickableWidget> buttons = Screens.getButtons(this);
        this.reconnectButton = ButtonWidget.builder(Text.literal("Reconnect"), b -> this.tryConnecting()).dimensions(this.width / 2 - 102, 340, 204, 20).build();
        buttons.add(this.reconnectButton);
    }

    @Unique
    private void tryConnecting() {
        this.lastServer = this.client.getCurrentServerEntry();
        if (this.lastServer == null) {
            AChatUtils.sendMsgSeraphim(Text.of("Failed to reconnect server! You might in SinglePlay? "));
            MeteorClient.mc.currentScreen.close();
        } else {
            ServerAddress address = ServerAddress.parse(this.lastServer.address);
            this.lastServerConnection = new ObjectObjectImmutablePair(address, this.client.getCurrentServerEntry());
            Pair<ServerAddress, ServerInfo> lastServer = ((AutoReconnect) Modules.get().get(AutoReconnect.class)).lastServerConnection;
            MeteorClient.mc.getNetworkHandler().getConnection().disconnect(Text.of("Reconnecting..."));
            ConnectScreen.connect(new TitleScreen(), MeteorClient.mc, (ServerAddress) lastServer.left(), (ServerInfo) lastServer.right(), true, null);
        }
    }
}
