package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.Action;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LagAura extends AbnormallyModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
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
    public final Setting<Boolean> Str1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Latin Extended-A & IPA Extensions"))
                .description("Range 0x00CA to 0x02AF"))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> Str2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Greek & Coptic"))
                .description("Range 0x0370 to 0x03FF"))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> Str3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Cyrillic"))
                .description("Range 0x0400 to 0x04FF"))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> Str4 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("CJK Unified Ideographs"))
                .description("0x4E00 to 0x9FFF"))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> Str5 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Hangul Syllables"))
                .description("Range 0xAC00 to 0xD7AF"))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> Str6 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Alphabetic Presentation Forms"))
                .description("Range 0xFB00 to 0xFB4F"))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> Str7 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Halfwidth & Fullwidth Forms"))
                .description("Range 0xFF00 to 0xFF73"))
                .defaultValue(false))
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
    public final Setting<String> command = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("Command"))
                .description("Command for send private message"))
                .defaultValue("msg"))
                .build()
        );
    private final SettingGroup sgTargeting = this.settings.createGroup("Targeting");
    private final SettingGroup sgDelay = this.settings.createGroup("Delay");
    private final Setting<List<String>> blacklist = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Black List")).description("Player who you don't want to lag")).defaultValue(List.of("NoStrict")))
                .build()
        );
    private final Setting<Double> range = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("The maximum range the entity can be to attack it."))
                .defaultValue(6.0)
                .min(0.0)
                .sliderMax(20.0)
                .build()
        );
    private final Setting<SortPriority> priority = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("priority"))
                .description("How to filter targets within range."))
                .defaultValue(SortPriority.ClosestAngle))
                .build()
        );
    private final Setting<Integer> maxTargets = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("max-targets"))
                .description("How many entities to target at once."))
                .defaultValue(1))
                .min(1)
                .sliderRange(1, 5)
                .build()
        );
    private final Setting<Integer> multiMessage = this.sgTargeting
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("multi-targets"))
                .description("Send more message."))
                .defaultValue(1))
                .min(1)
                .sliderRange(1, 5)
                .build()
        );
    private final Setting<Integer> delay = this.sgDelay
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("How fast you send message."))
                .defaultValue(0))
                .min(0)
                .sliderMax(60)
                .build()
        );
    private final List<Entity> targets = new ArrayList<>();
    String msg;
    private int hitDelayTimer;
    private int switchTimer;

    public LagAura() {
        super(Compassion.ABNORMALLY, "LagAura", "Use random unicode text to fuck uo your enemy");
    }

    public static String generateRandomString(int length, boolean[] rangeEnabled) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        int[][] validRanges = new int[][]{{202, 687}, {880, 1023}, {1024, 1279}, {19968, 40959}, {44032, 55215}, {64256, 64335}, {65280, 65395}};
        boolean atLeastOneEnabled = false;

        for (boolean enabled : rangeEnabled) {
            if (enabled) {
                atLeastOneEnabled = true;
                break;
            }
        }

        if (!atLeastOneEnabled) {
            rangeEnabled[0] = true;
            TextLag textLag = new TextLag();
            textLag.Str1.set(true);
            AChatUtils.sendMsg(
                "h-hewwo, senpai! i-i'm sowwy, but no wanges wewe enabwed, so i had to automaticawwy enabwe the fiwst wange! pwease fowgive me, i hope it's okay with you x3."
            );
        }

        for (int i = 0; i < length; i++) {
            int randomCodePoint = 0;
            boolean validCharFound = false;

            while (!validCharFound) {
                int rangeIndex;
                do {
                    rangeIndex = random.nextInt(validRanges.length);
                } while (!rangeEnabled[rangeIndex]);

                int[] range = validRanges[rangeIndex];
                randomCodePoint = random.nextInt(range[1] - range[0] + 1) + range[0];
                if (Character.isValidCodePoint(randomCodePoint)) {
                    validCharFound = true;
                }
            }

            sb.append((char) randomCodePoint);
        }

        return sb.toString();
    }

    public void onDeactivate() {
        this.hitDelayTimer = 0;
        this.targets.clear();
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.mc.player.isAlive() && PlayerUtils.getGameMode() != GameMode.SPECTATOR) {
            TargetUtils.getList(this.targets, this::entityCheck, (SortPriority) this.priority.get(), this.maxTargets.get());
            if (this.delayCheck()) {
                this.targets.forEach(this::lag);
            }
        }
    }

    private boolean delayCheck() {
        if (this.switchTimer > 0) {
            this.switchTimer--;
            return false;
        } else if (this.hitDelayTimer > 0) {
            this.hitDelayTimer--;
            return false;
        } else {
            this.hitDelayTimer = this.delay.get();
            return true;
        }
    }

    private boolean entityCheck(Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        } else if (!entity.equals(this.mc.player) && !entity.equals(this.mc.cameraEntity)) {
            if ((!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isDead()) && entity.isAlive()) {
                if (!PlayerUtils.isWithin(entity, this.range.get())) {
                    return false;
                } else {
                    PlayerEntity player = (PlayerEntity) entity;
                    if (player.isCreative()) {
                        return false;
                    } else {
                        return !Friends.get().shouldAttack(player) ? false : !(this.blacklist.get()).contains(player.getName().getString());
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
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

    private void lag(Entity target) {
        boolean[] list = new boolean[]{
            this.Str1.get(),
            this.Str2.get(),
            this.Str3.get(),
            this.Str4.get(),
            this.Str5.get(),
            this.Str6.get(),
            this.Str7.get()
        };
        this.msg = generateRandomString(this.messageLength.get(), list);

        for (int i = 0; i < this.multiMessage.get(); i++) {
            this.mc.player.networkHandler.sendChatCommand((String) this.command.get() + " " + target.getName().getString() + " " + this.msg);
        }
    }

    public Entity getTarget() {
        return !this.targets.isEmpty() ? this.targets.get(0) : null;
    }
}
