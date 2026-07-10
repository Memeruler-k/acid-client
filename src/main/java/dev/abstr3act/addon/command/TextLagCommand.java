package dev.abstr3act.addon.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.abstr3act.addon.modules.Abnormally.TextLag;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.PlayerListEntryArgumentType;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.Action;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.awt.image.BufferedImage;
import java.util.List;

public class TextLagCommand extends Command {
    public BufferedImage buffered;
    TextLag textLag = new TextLag();
    Friend friend;
    int timer = 0;
    String msg;

    public TextLagCommand() {
        super("textlag", "Use random string to fuck your enemy up", new String[0]);
    }

    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("start").then(argument("player", PlayerListEntryArgumentType.create()).executes(context -> {
            GameProfile profile = PlayerListEntryArgumentType.get(context).getProfile();
            this.friend = new Friend(profile.getName(), profile.getId());
            this.textLag.target.set(this.friend.getName());
            this.textLag.whitelist.set(List.of(this.friend.getName()));
            AChatUtils.sendMsg("hewwo thewe, " + this.friend.getName() + ", why awe you making my heawt beat so fast? >w< x3 ");
            MeteorClient.EVENT_BUS.subscribe(this);
            return 1;
        })));
        builder.then(literal("stop").executes(context -> {
            AChatUtils.sendMsg("stop the text waggwing >.<");
            MeteorClient.EVENT_BUS.unsubscribe(this);
            return 1;
        }));
    }

    @EventHandler
    private void onTick(Post event) {
        boolean[] list = new boolean[]{
            this.textLag.Str1.get(),
            this.textLag.Str2.get(),
            this.textLag.Str3.get(),
            this.textLag.Str4.get(),
            this.textLag.Str5.get(),
            this.textLag.Str6.get(),
            this.textLag.Str7.get()
        };
        if (this.timer <= 0) {
            this.msg = TextLag.generateRandomString(this.textLag.messageLength.get(), list);
            mc.player.networkHandler.sendChatCommand((String) this.textLag.command.get() + " " + this.friend.getName() + " " + this.msg);
            this.timer = this.textLag.delay.get();
        } else {
            this.timer--;
        }
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent event) {
        StringBuilder builder = new StringBuilder();
        event.getMessage().asOrderedText().accept((i, style, codePoint) -> {
            builder.append(new String(Character.toChars(codePoint)));
            return true;
        });
        StringBuilder wrap = new StringBuilder();
        if (event.getMessage().contains(Text.of(this.msg))) {
            event.setMessage(
                Text.literal("Sent [Lag Message] to " + this.friend.getName())
                    .setStyle(
                        Style.EMPTY
                            .withColor(TextColor.fromRgb(((SettingColor) this.textLag.feedbackColor.get()).getPacked()))
                            .withHoverEvent(
                                new HoverEvent(
                                    Action.SHOW_TEXT,
                                    Text.literal(wrap.toString())
                                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(((SettingColor) this.textLag.feedbackColor.get()).getPacked())))
                                )
                            )
                    )
            );
        }
    }
}
