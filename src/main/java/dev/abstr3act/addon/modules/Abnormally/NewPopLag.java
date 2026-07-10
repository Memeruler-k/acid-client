package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
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
import java.util.Random;

public class NewPopLag extends AbnormallyModule {
    public static ArrayList<String> MsgList = new ArrayList<>();
    static int[] chars = new int[]{
        38108,
        37230,
        37214,
        36890,
        36427,
        36367,
        36138,
        35885,
        35848,
        35210,
        34962,
        34582,
        34580,
        34577,
        34576,
        34575,
        34574,
        34572,
        34571,
        34566,
        34565,
        34564,
        34561,
        34559,
        34557,
        34556,
        34555,
        34554,
        34551,
        34550,
        34549,
        34543,
        34540,
        34539,
        34538,
        34536,
        34535
    };
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
    public final Setting<SettingColor> feedbackColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("feedback-color"))
                .description("Color of the feedback text."))
                .defaultValue(new SettingColor(176, 224, 230))
                .build()
        );
    private final Setting<Integer> messageLength = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("message-length")).description("The length of the message.")).defaultValue(200))
                .min(1)
                .sliderMin(1)
                .sliderMax(1000)
                .build()
        );
    private final Setting<Integer> amount = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("message-amount")).description("The amount of the message.")).defaultValue(3))
                .min(1)
                .sliderMin(1)
                .sliderMax(10)
                .build()
        );
    private final Setting<Boolean> totemsIgnoreFriends = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-friends"))
                .description("Ignores friends totem pops."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> totemsIgnoreOthers = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-others"))
                .description("Ignores other players totem pops."))
                .defaultValue(false))
                .build()
        );
    private final PointerBuffer filters;
    String msg = "none";

    public NewPopLag() {
        super(Compassion.LUNA, "NewPopLag", "They text lag me! ");
        if (!file.exists()) {
            file = null;
        }

        this.filters = BufferUtils.createPointerBuffer(1);
        ByteBuffer txtFilter = MemoryUtil.memASCII("*.txt");
        this.filters.put(txtFilter);
        this.filters.rewind();
    }

    private static boolean hasMatchingSubstring(String str, ArrayList<String> substrings) {
        for (String s : substrings) {
            if (str.contains(s)) {
                return true;
            }
        }

        return false;
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

    public void onActivate() {
        if (file != null && file.exists()) {
            loadMsg();
        } else {
            AChatUtils.sendMsgLuna(Text.of("No file selected, please select a file in the GUI."));
            this.toggle();
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

    @EventHandler
    private void onReceivePacket(Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket p) {
            if (p.getStatus() == 35) {
                Entity entity = p.getEntity(this.mc.world);
                if (entity instanceof PlayerEntity) {
                    if (!entity.equals(this.mc.player)
                        && (!Friends.get().isFriend((PlayerEntity) entity) || !this.totemsIgnoreOthers.get())
                        && (Friends.get().isFriend((PlayerEntity) entity) || !this.totemsIgnoreFriends.get())) {
                        Random random = new Random();

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

                            this.mc.player.networkHandler.sendChatCommand((String) this.command.get() + " " + entity.getName().getString() + " " + this.msg);
                        }
                    }
                }
            }
        }
    }
}
