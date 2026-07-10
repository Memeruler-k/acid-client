package dev.abstr3act.addon.modules.Luna;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.LunaModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.Action;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NewTextLag extends LunaModule {
    public static ArrayList<String> MsgList = new ArrayList<>();
    private static File file = new File(MeteorClient.FOLDER, "textlag.txt");
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<String> command = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Command"))
                .description("Command for send private message"))
                .defaultValue("msg"))
                .build()
        );
    public final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("The delay between specified messages in ticks."))
                .defaultValue(20))
                .min(0)
                .sliderMax(200)
                .build()
        );
    public final Setting<Integer> messageLength = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("message-length"))
                .description("The length of the message."))
                .defaultValue(200))
                .min(1)
                .sliderMin(1)
                .sliderMax(1000)
                .build()
        );
    public final Setting<SettingColor> feedbackColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("feedback-color"))
                .description("Color of the feedback text."))
                .defaultValue(new SettingColor(176, 224, 230))
                .build()
        );
    private final Setting<Boolean> multi = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Multi mode")).description("UwU")).defaultValue(false)).build());
    public final Setting<List<String>> whitelist = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                .name("Target"))
                .description("Target"))
                .defaultValue(List.of("NoStrict")))
                .visible(this.multi::get))
                .build()
        );
    public final Setting<String> target = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Target"))
                .description("Target :P"))
                .defaultValue("NoStrict"))
                .visible(() -> !this.multi.get()))
                .build()
        );
    private final Setting<Integer> amount = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("message-amount"))
                .description("The amount of the message."))
                .defaultValue(3))
                .min(1)
                .sliderMin(1)
                .sliderMax(10)
                .build()
        );
    private final PointerBuffer filters;
    String msg;
    private int t;
    private int currentIndex = 0;

    public NewTextLag() {
        super(Compassion.LUNA, "NewTextLag", "Use random string to fuck your enemy up");
        if (!file.exists()) {
            file = null;
        }

        this.filters = BufferUtils.createPointerBuffer(1);
        ByteBuffer txtFilter = MemoryUtil.memASCII("*.txt");
        this.filters.put(txtFilter);
        this.filters.rewind();
    }

    public static void loadMsg() {
        try {
            File f = file;
            if (!f.exists()) {
                f.createNewFile();
            }

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                    ArrayList<String> lines = new ArrayList<>();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }

                    MsgList.clear();
                    boolean newline = lines.contains("");
                    if (!newline) {
                        MsgList.addAll(lines);
                    } else {
                        StringBuilder spamChunk = new StringBuilder();

                        for (String l : lines) {
                            if (l.equals("")) {
                                if (!spamChunk.isEmpty()) {
                                    MsgList.add(spamChunk.toString());
                                    spamChunk = new StringBuilder();
                                }
                            } else {
                                spamChunk.append(l).append(" ");
                            }
                        }

                        MsgList.add(spamChunk.toString());
                    }
                } catch (IOException var10) {
                    AChatUtils.sendMsgLuna(Text.of(var10.getMessage()));
                }
            }).start();
        } catch (IOException var1) {
            AChatUtils.sendMsgLuna(Text.of(var1.getMessage()));
        }
    }

    @EventHandler
    public void onGameJoin(GameJoinedEvent event) {
        this.toggle();
    }

    public void onActivate() {
        if (file != null && file.exists()) {
            loadMsg();
        } else {
            AChatUtils.sendMsgLuna(Text.of("No file selected, please select a file in the GUI."));
            this.toggle();
        }
    }

    public String nextItem(List<String> items) {
        if (this.currentIndex < items.size()) {
            this.currentIndex++;
            return items.get(this.currentIndex);
        } else {
            this.currentIndex = 0;
            return "";
        }
    }

    @EventHandler
    private void onTick(Post event) {
        if ((this.whitelist.get()).isEmpty() && this.multi.get()) {
            AChatUtils.sendMsg("oh noes! no targets in da list! x3");
            this.toggle();
        }

        Random random = new Random();
        if (this.t >= this.delay.get()) {
            for (int i = 0; i < this.amount.get(); i++) {
                this.msg = MsgList.get(random.nextInt(MsgList.size()));
                if (this.msg.length() > this.messageLength.get()) {
                    this.msg = this.msg.substring(0, this.messageLength.get());
                }

                while (this.msg.length() < this.messageLength.get()) {
                    String nextMsg = MsgList.get(random.nextInt(MsgList.size()));
                    if (this.msg.length() + nextMsg.length() > this.messageLength.get()) {
                        break;
                    }

                    this.msg = this.msg + " " + nextMsg;
                }

                if (this.multi.get()) {
                    this.mc.player.networkHandler.sendChatCommand((String) this.command.get() + " " + this.nextItem((List<String>) this.whitelist.get()) + " " + this.msg);
                } else {
                    this.mc.player.networkHandler.sendChatCommand((String) this.command.get() + " " + (String) this.target.get() + " " + this.msg);
                }
            }

            this.t = 0;
        } else {
            this.t++;
        }
    }

    public WWidget getWidget(GuiTheme theme) {
        WHorizontalList list = theme.horizontalList();
        WButton selectFile = (WButton) list.add(theme.button("Select File")).widget();
        WLabel fileName = (WLabel) list.add(theme.label(file != null && file.exists() ? file.getName() : "No file selected.")).widget();
        selectFile.action = () -> {
            String path = TinyFileDialogs.tinyfd_openFileDialog(
                "Select File", new File(MeteorClient.FOLDER, "textlag.txt").getAbsolutePath(), this.filters, null, false
            );
            if (path != null) {
                file = new File(path);
                fileName.set(file.getName());
            }
        };
        return list;
    }

    @EventHandler
    public void onReceiveMessage(ReceiveMessageEvent event) {
        StringBuilder builder = new StringBuilder();
        event.getMessage().asOrderedText().accept((i, style, codePoint) -> {
            builder.append(new String(Character.toChars(codePoint)));
            return true;
        });
        StringBuilder wrap = new StringBuilder();
        if (this.msg != null) {
            if (event.getMessage().getString().contains(this.msg)) {
                event.setMessage(
                    Text.literal("[Lag Message]")
                        .setStyle(
                            Style.EMPTY
                                .withColor(TextColor.fromRgb(((SettingColor) this.feedbackColor.get()).getPacked()))
                                .withHoverEvent(
                                    new HoverEvent(
                                        Action.SHOW_TEXT,
                                        Text.literal(wrap.toString()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(((SettingColor) this.feedbackColor.get()).getPacked())))
                                    )
                                )
                        )
                );
            }
        }
    }
}
