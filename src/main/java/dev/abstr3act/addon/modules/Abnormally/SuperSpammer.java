package dev.abstr3act.addon.modules.Abnormally;

import com.mojang.authlib.GameProfile;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class SuperSpammer extends AbnormallyModule {
    public static ArrayList<String> MsgList = new ArrayList<>();
    private static List<String> fileLines = new ArrayList<>();
    private static List<String> msgLines = new ArrayList<>();
    private static int currentIndex = 0;
    private static File file = new File(MeteorClient.FOLDER, "spammer.txt");
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<mode> Mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description("modes")).defaultValue(mode.File)).build());
    private final Setting<String> spamMessage = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("spam-message"))
                .description("%s: PlayerName, %n: TotemPops"))
                .defaultValue("人生自古谁无死？不幸的，%s在pop %n个图腾后已无法与您继续互动，让我们一起缅怀他"))
                .visible(() -> ((mode) this.Mode.get()).equals(mode.Custom)))
                .build()
        );
    private final Setting<String> spamMessagePrefix = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("prefix"))
                .description("%s: PlayerName, %n: TotemPops"))
                .defaultValue("none"))
                .visible(() -> ((mode) this.Mode.get()).equals(mode.File)))
                .build()
        );
    private final Setting<String> spamMessageSuffix = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("suffix"))
                .description("%s: PlayerName, %n: TotemPops"))
                .defaultValue("none"))
                .visible(() -> ((mode) this.Mode.get()).equals(mode.File)))
                .build()
        );
    private final Setting<String> spamMessagePrefixIP = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("prefix-ip"))
                .description("%s: PlayerName, %n: TotemPops"))
                .defaultValue("none"))
                .visible(() -> ((mode) this.Mode.get()).equals(mode.RandomIP)))
                .build()
        );
    private final Setting<String> spamMessageSuffixIP = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("suffix-ip"))
                .description("%s: PlayerName, %n: TotemPops"))
                .defaultValue("none"))
                .visible(() -> ((mode) this.Mode.get()).equals(mode.RandomIP)))
                .build()
        );
    private final Setting<String> spamMessagePrefixID = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("prefix-id"))
                .description("%s: PlayerName, %n: TotemPops"))
                .defaultValue("none"))
                .visible(() -> ((mode) this.Mode.get()).equals(mode.RandomIP)))
                .build()
        );
    private final Setting<String> spamMessageSuffixID = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("suffix-id"))
                .description("%s: PlayerName, %n: TotemPops"))
                .defaultValue("none"))
                .visible(() -> ((mode) this.Mode.get()).equals(mode.RandomIP)))
                .build()
        );
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("The delay between specified messages in ticks."))
                .defaultValue(20))
                .min(0)
                .sliderMax(200)
                .build()
        );
    private final Setting<Boolean> disableOnLeave = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("disable-on-leave"))
                .description("Disables spam when you leave a server."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> disableOnDisconnect = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("disable-on-disconnect"))
                .description("Disables spam when you are disconnected from a server."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> random = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("randomise"))
                .description("Selects a random message from your spam message list."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> bypass = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("bypass"))
                .description("Add random text at the end of the message to try to bypass anti spams."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> length = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("length"))
                .description("Number of characters used to bypass anti spam."))
                .visible(this.bypass::get))
                .defaultValue(16))
                .sliderRange(1, 256)
                .build()
        );
    private final PointerBuffer filters;
    String msg;
    private int messageI;
    private int timer;

    public SuperSpammer() {
        super(Compassion.ABNORMALLY, "SuperSpammer", "Spams specified messages in chat.");
        if (!file.exists()) {
            file = null;
        }

        this.filters = BufferUtils.createPointerBuffer(1);
        ByteBuffer txtFilter = MemoryUtil.memASCII("*.txt");
        this.filters.put(txtFilter);
        this.filters.rewind();
    }

    public static List<String> getOnlinePlayerNames() {
        MinecraftClient client = MinecraftClient.getInstance();
        return Objects.requireNonNull(client.getNetworkHandler())
            .getPlayerList()
            .stream()
            .map(PlayerListEntry::getProfile)
            .<String>map(GameProfile::getName)
            .collect(Collectors.toList());
    }

    public static String randomIP() {
        Random random = new Random();
        int a = random.nextInt(256);
        int b = random.nextInt(256);
        int c = random.nextInt(256);
        int d = random.nextInt(256);
        return a + "." + b + "." + c + "." + d;
    }

    public static String generateIDCard() {
        Random random = new Random();
        String areaCode = areaCode(random);
        String birthDate = birthDate(random);
        String sequenceCode = String.format("%03d", random.nextInt(1000));
        String idWithoutCheckCode = areaCode + birthDate + sequenceCode;
        char checkCode = checkCode(idWithoutCheckCode);
        return idWithoutCheckCode + checkCode;
    }

    private static String areaCode(Random random) {
        int areaCode = 110000 + random.nextInt(544327);
        return String.format("%06d", areaCode);
    }

    private static String birthDate(Random random) {
        int year = 2000 + random.nextInt(24);
        int month = 1 + random.nextInt(12);

        int day = switch (month) {
            case 2 -> random.nextInt(isLeapYear(year) ? 29 : 28) + 1;
            default -> random.nextInt(31) + 1;
            case 4, 6, 9, 11 -> random.nextInt(30) + 1;
        };
        return String.format("%04d%02d%02d", year, month, day);
    }

    private static boolean isLeapYear(int year) {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
    }

    private static char checkCode(String idWithoutCheckCode) {
        int[] weight = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodes = new char[]{'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;

        for (int i = 0; i < 17; i++) {
            sum += (idWithoutCheckCode.charAt(i) - '0') * weight[i];
        }

        return checkCodes[sum % 11];
    }

    public void onActivate() {
        if (file != null && (file.exists() || !((mode) this.Mode.get()).equals(mode.File))) {
            this.loadMsg();
            msgLines = getOnlinePlayerNames();
            this.timer = this.delay.get();
            this.messageI = 0;
        } else {
            AChatUtils.sendMsgLuna(Text.of("No file selected, please select a file in the GUI."));
            this.toggle();
        }
    }

    @EventHandler
    private void onTickEvent(Pre event) {
        msgLines = getOnlinePlayerNames();
    }

    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if (this.disableOnDisconnect.get() && event.screen instanceof DisconnectedScreen) {
            this.toggle();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (this.disableOnLeave.get()) {
            this.toggle();
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

    public void loadMsg() {
        try {
            if (file == null) {
                this.toggle();
                return;
            }

            File f = file;
            if (!f.exists()) {
                f.createNewFile();
            }

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                    MsgList.clear();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        MsgList.add(line);
                    }
                } catch (IOException var6) {
                    AChatUtils.sendMsgLuna(Text.of(var6.getMessage()));
                }
            }).start();
        } catch (IOException var2) {
            AChatUtils.sendMsgLuna(Text.of(var2.getMessage()));
        }
    }

    @EventHandler
    private void onTick(Post event) {
        if (MsgList.isEmpty()) {
            this.toggle();
        }

        if (!msgLines.isEmpty()) {
            if (this.timer <= 0) {
                if (((mode) this.Mode.get()).equals(mode.Custom)) {
                    int i;
                    if (this.random.get()) {
                        i = Utils.random(0, msgLines.size());
                    } else {
                        if (this.messageI >= msgLines.size()) {
                            this.messageI = 0;
                        }

                        i = this.messageI++;
                    }

                    String text = ((String) this.spamMessage.get()).replace("%s", msgLines.get(i));
                    if (this.bypass.get()) {
                        text = text + " " + RandomStringUtils.randomAlphabetic(this.length.get()).toLowerCase();
                    }

                    if (msgLines.get(i).contains(this.mc.player.getName().getString())) {
                        return;
                    }

                    ChatUtils.sendPlayerMsg(text);
                } else if (((mode) this.Mode.get()).equals(mode.File)) {
                    if (file == null || !file.exists()) {
                        AChatUtils.sendMsgSeraphim(Text.of("No file selected, please select a file in the GUI."));
                        this.toggle();
                        return;
                    }

                    int ix;
                    if (this.random.get()) {
                        ix = Utils.random(0, msgLines.size());
                    } else {
                        if (this.messageI >= msgLines.size()) {
                            this.messageI = 0;
                        }

                        ix = this.messageI++;
                    }

                    Random r = new Random();
                    this.msg = MsgList.get(r.nextInt(MsgList.size()));
                    System.out.println(this.msg);
                    String textx = ((String) this.spamMessagePrefix.get()).replace("%s", msgLines.get(ix))
                        + this.msg
                        + ((String) this.spamMessageSuffix.get()).replace("%s", msgLines.get(ix));
                    if (this.bypass.get()) {
                        textx = textx + " " + RandomStringUtils.randomAlphabetic(this.length.get()).toLowerCase();
                    }

                    if (msgLines.get(ix).contains(this.mc.player.getName().getString())) {
                        return;
                    }

                    ChatUtils.sendPlayerMsg(textx);
                } else if (((mode) this.Mode.get()).equals(mode.RandomIP)) {
                    int ixx;
                    if (this.random.get()) {
                        ixx = Utils.random(0, msgLines.size());
                    } else {
                        if (this.messageI >= msgLines.size()) {
                            this.messageI = 0;
                        }

                        ixx = this.messageI++;
                    }

                    String textxx = ((String) this.spamMessagePrefixIP.get()).replace("%s", msgLines.get(ixx))
                        + randomIP()
                        + ((String) this.spamMessageSuffixIP.get()).replace("%s", msgLines.get(ixx));
                    if (this.bypass.get()) {
                        textxx = textxx + " " + RandomStringUtils.randomAlphabetic(this.length.get()).toLowerCase();
                    }

                    if (msgLines.get(ixx).contains(this.mc.player.getName().getString())) {
                        return;
                    }

                    ChatUtils.sendPlayerMsg(textxx);
                } else if (((mode) this.Mode.get()).equals(mode.RandomIDCard)) {
                    int ixxx;
                    if (this.random.get()) {
                        ixxx = Utils.random(0, msgLines.size());
                    } else {
                        if (this.messageI >= msgLines.size()) {
                            this.messageI = 0;
                        }

                        ixxx = this.messageI++;
                    }

                    String textxxx = ((String) this.spamMessagePrefixID.get()).replace("%s", msgLines.get(ixxx))
                        + generateIDCard()
                        + ((String) this.spamMessageSuffixID.get()).replace("%s", msgLines.get(ixxx));
                    if (this.bypass.get()) {
                        textxxx = textxxx + " " + RandomStringUtils.randomAlphabetic(this.length.get()).toLowerCase();
                    }

                    if (msgLines.get(ixxx).contains(this.mc.player.getName().getString())) {
                        return;
                    }

                    ChatUtils.sendPlayerMsg(textxxx);
                }

                this.timer = this.delay.get();
            } else {
                this.timer--;
            }
        }
    }

    static enum mode {
        File,
        Custom,
        RandomIP,
        RandomIDCard;
    }
}
