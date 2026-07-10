package dev.abstr3act.addon.modules.Lacrymira;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LacrymiraModule;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

import java.net.InetSocketAddress;

public class Media extends LacrymiraModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> nameProtect = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("nameProtect")).description("nameProtect")).defaultValue(true)).build());
    public final Setting<Boolean> skinProtect = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("skinProtect")).description("skinProtect")).defaultValue(true)).build());
    public final Setting<String> protectedString = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Protected"))
                .description("."))
                .defaultValue("Protected"))
                .build()
        );
    public final Setting<Boolean> hideIP = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("hideIP")).description("replaceIP")).defaultValue(true)).build());
    public final Setting<String> targetIP = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("targetIP"))
                .description("."))
                .defaultValue("null"))
                .visible(this.hideIP::get))
                .build()
        );
    public final Setting<String> replaceIP = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("replaceIP"))
                .description("."))
                .defaultValue("null"))
                .visible(this.hideIP::get))
                .build()
        );

    public Media() {
        super(Compassion.CLIENT, "Media", "Media");
    }

    public String replaceName(String string) {
        if (this.mc.player == null) {
            return string;
        } else {
            for (PlayerListEntry ple : this.mc.player.networkHandler.getPlayerList()) {
                if (string.contains(ple.getProfile().getName())) {
                    return string.replace(ple.getProfile().getName(), (CharSequence) this.protectedString.get());
                }
            }

            return string;
        }
    }

    public String replaceIP(String string) {
        if (this.mc.player == null) {
            return string;
        } else {
            return string.contains((CharSequence) this.targetIP.get())
                ? string.replace((CharSequence) this.targetIP.get(), (CharSequence) this.replaceIP.get())
                : string;
        }
    }

    public String getServerIP() {
        ClientPlayNetworkHandler networkHandler = this.mc.getNetworkHandler();
        if (networkHandler != null) {
            InetSocketAddress address = (InetSocketAddress) networkHandler.getConnection().getAddress();
            if (address != null) {
                return address.getHostString();
            }
        }

        return null;
    }
}
