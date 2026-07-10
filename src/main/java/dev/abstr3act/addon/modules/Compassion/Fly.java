package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.sound.SoundEvents;

public class Fly extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> vulcanElytra = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("VulcanElytra"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Integer> swapDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("ElytraSwapDelay"))
                .description("."))
                .defaultValue(20))
                .sliderRange(0, 5000)
                .visible(this.vulcanElytra::get))
                .build()
        );
    private final Setting<Integer> swapBackDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("SwapBackDelay"))
                .description("."))
                .defaultValue(20))
                .sliderRange(0, 5000)
                .visible(this.vulcanElytra::get))
                .build()
        );
    public final Setting<Boolean> antikick = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("AntiKick"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> antiKickValue = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("GlideValue"))
                .description("."))
                .defaultValue(-0.3)
                .sliderRange(-1.0, 1.0)
                .visible(this.antikick::get))
                .build()
        );
    public final Setting<Boolean> fakeDamage = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("FakeDamage"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.GrimFlag)).build());
    private final Setting<Double> vspeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("VerticalSpeed"))
                .description("."))
                .defaultValue(1.0)
                .min(0.0)
                .sliderRange(0.0, 10.0)
                .build()
        );
    private final Setting<Double> hspeed = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("HorizontalSpeed"))
                .description("."))
                .defaultValue(1.0)
                .min(0.0)
                .sliderRange(0.0, 10.0)
                .build()
        );
    int t1;
    int t2;
    int slot = -1;
    int slot2 = -1;
    boolean swapped = false;
    private boolean flip;
    private float lastYaw;

    public Fly() {
        super(Compassion.COMPASSION, "Fly", "FLYYYY! No Fall is recommended with this module.");
    }

    public void onActivate() {
        this.t1 = 0;
        this.t2 = 0;
        if (this.fakeDamage.get() && this.mc.player != null) {
            this.forceHurtCam(this.mc.player);
        }
    }

    public void onDeactivate() {
        this.t1 = 0;
        this.t2 = 0;
        if (this.slot != -1 && this.mc.player.getInventory().getStack(37).getItem().equals(Items.ELYTRA)) {
            InvUtils.move().fromArmor(2).to(this.slot);
        }

        if (this.vulcanElytra.get()) {
            this.mc.player.setVelocity(0.0, 0.0, 0.0);
        }
    }

    public void forceHurtCam(PlayerEntity player) {
        if (player.getDamageSources() != null) {
            player.damage(player.getDamageSources().outOfWorld(), 0.1F);
            player.hurtTime = 10;
            player.lastDamageTaken = 0.1F;
            player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1.0F, 1.0F);
        }
    }

    @EventHandler
    private void onPreTick(Pre event) {
        float currentYaw = this.mc.player.getYaw();
        if (this.mc.player.fallDistance >= 3.0F && currentYaw == this.lastYaw && this.mc.player.getVelocity().length() < 0.003) {
            this.flip = !this.flip;
        }

        this.lastYaw = currentYaw;
    }

    public boolean isMoving() {
        return this.mc.player != null
            && this.mc.world != null
            && this.mc.player.input != null
            && (this.mc.player.input.movementForward != 0.0 || this.mc.player.input.movementSideways != 0.0);
    }

    public double[] forward(double d) {
        float f = this.mc.player.input.movementForward;
        float f2 = this.mc.player.input.movementSideways;
        float f3 = this.mc.player.getYaw();
        if (f != 0.0F) {
            if (f2 > 0.0F) {
                f3 += f > 0.0F ? -45 : 45;
            } else if (f2 < 0.0F) {
                f3 += f > 0.0F ? 45 : -45;
            }

            f2 = 0.0F;
            if (f > 0.0F) {
                f = 1.0F;
            } else if (f < 0.0F) {
                f = -1.0F;
            }
        }

        double d2 = Math.sin(Math.toRadians(f3 + 90.0F));
        double d3 = Math.cos(Math.toRadians(f3 + 90.0F));
        double d4 = f * d * d3 + f2 * d * d2;
        double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }

    @EventHandler
    private void onPostTick(Post event) {
        if (((Mode) this.mode.get()).equals(Mode.GrimFlag) && this.isMoving()) {
            this.mc.player.networkHandler.sendPacket(new PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY() + 1.0, this.mc.player.getZ(), true));
            this.mc.player.setPosition(this.mc.player.getX(), this.mc.player.getY() + 1.0, this.mc.player.getZ());
        }

        if (this.isMoving()) {
            double[] dir = this.forward(this.hspeed.get());
            this.mc.player.setVelocity(dir[0], 0.0, dir[1]);
        } else {
            this.mc.player.setVelocity(0.0, 0.0, 0.0);
        }

        if (((Mode) this.mode.get()).equals(Mode.VulcanElytra)) {
            this.mc.player.stopFallFlying();
            FindItemResult r = InvUtils.find(new Item[]{Items.ELYTRA});
            if (r.found()) {
                if (this.t1 < this.swapDelay.get()) {
                    this.t1++;
                } else {
                    InvUtils.move().from(r.slot()).toArmor(2);
                    this.slot2 = r.slot();
                    this.swapped = true;
                    this.t1 = 0;
                }

                if (this.t2 < this.swapBackDelay.get() && this.swapped) {
                    this.t2++;
                } else {
                    this.swapped = false;
                    InvUtils.move().from(this.slot2).toArmor(2);
                    this.slot2 = r.slot();
                    this.t2 = 0;
                }
            }
        } else {
            this.t1 = 0;
            this.t2 = 0;
        }

        if (this.mc.options.jumpKey.isPressed()) {
            this.mc.player.setVelocity(this.mc.player.getVelocity().add(0.0, this.vspeed.get(), 0.0));
        }

        if (this.mc.options.sneakKey.isPressed()) {
            this.mc.player.setVelocity(this.mc.player.getVelocity().add(0.0, -this.vspeed.get(), 0.0));
        }

        if (this.antikick.get()) {
            this.mc.player.setVelocity(this.mc.player.getVelocity().add(0.0, this.antiKickValue.get(), 0.0));
        }
    }

    public String getInfoString() {
        return ((Mode) this.mode.get()).name();
    }

    static enum Mode {
        VulcanElytra,
        Vanilla,
        GrimFlag;
    }
}
