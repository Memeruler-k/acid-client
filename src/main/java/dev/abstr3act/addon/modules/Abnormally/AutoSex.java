package dev.abstr3act.addon.modules.Abnormally;

import com.mojang.authlib.GameProfile;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.SetItem;
import dev.abstr3act.addon.utils.TargetUtils;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class AutoSex extends AbnormallyModule {
    static double renderY;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgPos = this.settings.createGroup("Sex Position");
    private final SettingGroup sgMessage = this.settings.createGroup("Message");
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("The maximum range to set target."))
                .defaultValue(6.0)
                .min(0.0)
                .sliderMax(6.0)
                .build()
        );
    private final Setting<Boolean> sexPos = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("sex-position"))
                .description("Set a position to stick to player."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> sexDelay = this.sgPos
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("Delay for sex movements in ticks"))
                .defaultValue(3))
                .sliderRange(0, 20)
                .visible(this.sexPos::get))
                .build()
        );
    private final Setting<Style> sexStyle = this.sgPos
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("style")).description("The style for sticking to player."))
                .defaultValue(Style.GulpGulp))
                .visible(this.sexPos::get))
                .build()
        );
    private final Setting<Boolean> randomCum = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("random-cum"))
                .description("Randomly drops cum."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> message = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("message"))
                .description("Sends dirty messages in chat."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> delay = this.sgMessage
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("delay"))
                .description("The delay between specified messages in ticks."))
                .defaultValue(80))
                .min(0)
                .sliderMax(200)
                .visible(this.message::get))
                .build()
        );
    private final Setting<Boolean> random = this.sgMessage
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("randomize"))
                .description("Selects a random message from your spam message list."))
                .defaultValue(true))
                .visible(this.message::get))
                .build()
        );
    private final Setting<List<String>> messages = this.sgMessage
        .add(
            ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                .name("messages"))
                .description("Messages to use for dirty talk."))
                .defaultValue(
                    Arrays.asList(
                        "I want you to make me your filthy slut~",
                        "Fuck me so hard I can't walk straight~",
                        "I want to taste every drop of you, %player%~",
                        "Fill all my holes~",
                        "Make me choke on you, %player%~",
                        "Make me squirt all over you~",
                        "Fuck me like the dirty slut I am, AUGHH~",
                        "I want you all over my face, %player%~",
                        "I want you to use me until I'm sore~",
                        "Make me cum so hard I can't stop shaking, %player%~",
                        "I want to be your cum slut~",
                        "Fuck me until I'm a dripping mess, %player%~",
                        "Make me scream your name while you fuck me~",
                        "I want you to use me however you want, %player%~",
                        "Fill my mouth and make me swallow, AHhhH~",
                        "Make me cum all over you~",
                        "Fuck me until I beg for mercy, %player%~",
                        "I want to be your personal cum dump~",
                        "Use me like your personal toy, %player%~",
                        "I want you to ruin me, %player%~",
                        "Make me cum all over your fingers, %player%~",
                        "I crave your touch everywhere",
                        "I need you to dominate me, mMMM!~"
                    )
                ))
                .visible(this.message::get))
                .build()
        );
    private final Setting<Boolean> isRender = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("render"))
                .description("Render the target."))
                .defaultValue(true))
                .build()
        );
    private final List<Entity> targets = new ArrayList<>();
    String regex = "[A-Za-z0-9_]+";
    double addition = 0.0;
    Entity target = null;
    private final Setting<Mode> targetMode = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("target-mode")).description("The mode at which to follow the player."))
                .defaultValue(Mode.Automatic))
                .onChanged(onChanged -> this.target = null))
                .build()
        );
    private int messageI;
    private int timer;
    private int timerSex;
    private int sexI;

    public AutoSex() {
        super(Compassion.ABNORMALLY, "AutoSex", "Automatic Minecraft Sex RP.");
    }

    private static double getRenderY() {
        Random rand = new Random();
        double randomValue = 0.2 + -0.05 * rand.nextDouble();
        if (renderY >= 0.3) {
            renderY = 0.0;
        }

        renderY += randomValue;
        return renderY;
    }

    private static boolean shouldCum() {
        double chance = Math.random();
        return chance <= 0.1;
    }

    public void onActivate() {
        if (this.targetMode.get() == Mode.Automatic) {
            this.setTarget();
        }
    }

    private boolean entityCheck(Entity entity) {
        if (!entity.equals(this.mc.player) && !entity.equals(this.mc.cameraEntity)) {
            if ((!(entity instanceof LivingEntity) || !((LivingEntity) entity).isDead()) && entity.isAlive()) {
                if (!PlayerUtils.isWithin(entity, this.range.get())) {
                    return false;
                } else if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, this.range.get())) {
                    return false;
                } else {
                    return Pattern.matches(this.regex, EntityUtils.getName(entity)) ? true : entity.isPlayer();
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (this.targetMode.get() == Mode.MiddleClick && event.action == KeyAction.Press && event.button == 2 && this.mc.currentScreen == null) {
            if (this.mc.targetedEntity instanceof PlayerEntity) {
                this.target = this.mc.targetedEntity;
                if (this.message.get()) {
                    this.startMsg();
                }
            } else {
                this.target = null;
                this.mc.player.getAbilities().flying = false;
            }
        }
    }

    @EventHandler
    private void onTick(Post event) {
        if (this.randomCum.get() && shouldCum()) {
            ItemStack milk = new ItemStack(Items.MILK_BUCKET);
            milk.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§4§l" + EntityUtils.getName(this.mc.player) + "'s §f§lCUM"));

            for (int i = 9; i < 11; i++) {
                SetItem.set(milk, i);
            }

            for (int i = 9; i < 11; i++) {
                InvUtils.drop().slot(i);
            }
        }

        if (this.target != null) {
            this.checkEntity();
            if (this.sexPos.get()) {
                this.mc.player.getAbilities().flying = true;
                if (this.timerSex <= 0) {
                    if (this.sexStyle.get() == Style.GulpGulp) {
                        Rotations.rotate(Rotations.getYaw(this.target), 45.0);
                        if (this.sexI == 0) {
                            Position head = this.target.raycast(0.2, 0.05F, false).getPos();
                            this.mc.player.setPosition(head.getX(), head.getY() - 0.5, head.getZ());
                            this.sexI = 1;
                        } else {
                            Position head = this.target.raycast(0.5, 0.05F, false).getPos();
                            this.mc.player.setPosition(head.getX(), head.getY() - 0.5, head.getZ());
                            this.sexI = 0;
                        }
                    }

                    if (this.sexStyle.get() == Style.Doggy) {
                        Rotations.rotate(Rotations.getYaw(this.target), 25.0);
                        if (this.sexI == 0) {
                            Position head = this.target.raycast(-0.2, 0.05F, false).getPos();
                            this.mc.player.setPosition(head.getX(), this.target.getY(), head.getZ());
                            this.sexI = 1;
                        } else {
                            Position head = this.target.raycast(-0.5, 0.05F, false).getPos();
                            this.mc.player.setPosition(head.getX(), this.target.getY(), head.getZ());
                            this.sexI = 0;
                        }
                    }

                    this.timerSex = this.sexDelay.get();
                } else {
                    this.timerSex--;
                }
            }

            if (this.message.get()) {
                if ((this.messages.get()).isEmpty()) {
                    return;
                }

                if (this.timer <= 0) {
                    int i;
                    if (this.random.get()) {
                        i = Utils.random(0, (this.messages.get()).size());
                    } else {
                        if (this.messageI >= (this.messages.get()).size()) {
                            this.messageI = 0;
                        }

                        i = this.messageI++;
                    }

                    String text = (String) (this.messages.get()).get(i);
                    ChatUtils.sendPlayerMsg(text.replaceAll("%player%", EntityUtils.getName(this.target)));
                    this.timer = this.delay.get();
                } else {
                    this.timer--;
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.target != null && this.isRender.get()) {
            Vec3d last = null;
            if (this.addition > 360.0) {
                this.addition = 0.0;
            }

            for (int i = 0; i < 360; i++) {
                Color c1 = new Color(255, 0, 255, 255);
                Vec3d tp = this.target.getPos();
                double rad = Math.toRadians(i);
                double sin = Math.sin(rad);
                double cos = Math.cos(rad);
                Vec3d c = new Vec3d(tp.x + sin, tp.y + getRenderY(), tp.z + cos);
                if (last != null) {
                    event.renderer.line(last.x, last.y, last.z, c.x, c.y, c.z, c1);
                }

                last = c;
            }
        }
    }

    public void onDeactivate() {
        this.target = null;
        this.mc.player.getAbilities().flying = false;
    }

    private void checkEntity() {
        List<String> playerNamesList = this.mc
            .player
            .networkHandler
            .getPlayerList()
            .stream()
            .map(PlayerListEntry::getProfile)
            .<String>map(GameProfile::getName)
            .toList();
        if (!playerNamesList.contains(EntityUtils.getName(this.target)) && this.targetMode.get() == Mode.Automatic) {
            this.target = null;
        }

        if (this.target == null && this.targetMode.get() == Mode.Automatic) {
            this.setTarget();
        }
    }

    private void setTarget() {
        TargetUtils.getList(this.targets, this::entityCheck, SortPriority.LowestDistance, 1);
        if (!this.targets.isEmpty()) {
            this.target = this.targets.get(0);
            this.startMsg();
        }
    }

    private void startMsg() {
        if (this.message.get()) {
            ChatUtils.sendPlayerMsg("Come here " + EntityUtils.getName(this.target) + " I want you uwu");
        }
    }

    private static enum Mode {
        MiddleClick,
        Automatic;
    }

    private static enum Style {
        GulpGulp,
        Doggy;
    }
}
