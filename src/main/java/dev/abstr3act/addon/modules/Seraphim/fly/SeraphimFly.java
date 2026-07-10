package dev.abstr3act.addon.modules.Seraphim.fly;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.modules.Seraphim.fly.modes.Damage;
import dev.abstr3act.addon.modules.Seraphim.fly.modes.MatrixExploit;
import dev.abstr3act.addon.modules.Seraphim.fly.modes.MatrixExploit2;
import dev.abstr3act.addon.modules.Seraphim.fly.modes.VulcanClip;
import meteordevelopment.meteorclient.events.entity.DamageEvent;
import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Receive;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class SeraphimFly extends SeraphimModule {
    public static int t = 0;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<FlyModes> flyMode = this.sgGeneral
        .add(
            (new Builder<FlyModes>().name("mode")).description("The method of applying fly.")
                .defaultValue(FlyModes.Matrix_Exploit)
                .onModuleActivated(spiderModesSetting -> this.onFlyModeChanged((FlyModes) spiderModesSetting.get()))
                .onChanged(this::onFlyModeChanged)
                .build()
        );
    private FlyMode currentMode;    public final Setting<Double> speed_1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("speed-№1"))
                .description("Fly speed."))
                .defaultValue(1.25)
                .max(2500.0)
                .sliderRange(0.0, 2500.0)
                .visible(() -> this.flyMode.get() == FlyModes.Matrix_Exploit))
                .build()
        );
    public SeraphimFly() {
        super(Compassion.SERAPHIM, "SeraphimFly", "Bypass fly");
        this.onFlyModeChanged((FlyModes) this.flyMode.get());
    }    public final Setting<Double> speed2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("speed-№2"))
                .description("Fly speed."))
                .defaultValue(0.3)
                .max(5.0)
                .sliderRange(0.0, 5.0)
                .visible(() -> this.flyMode.get() == FlyModes.Matrix_Exploit_2))
                .build()
        );

    public static int getTicks() {
        return t;
    }    public final Setting<Double> speedDamage = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("damage-fly-speed"))
                .description("Fly speed."))
                .defaultValue(1.25)
                .max(2500.0)
                .sliderRange(0.0, 2500.0)
                .onChanged(e -> Damage.speed = e))
                .visible(() -> this.flyMode.get() == FlyModes.Damage))
                .build()
        );

    public void onActivate() {
        this.currentMode.onActivate();
    }    public final Setting<Double> speedDamageY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("speed-y"))
                .description("Fly speed Y."))
                .defaultValue(1.25)
                .max(2500.0)
                .sliderRange(0.0, 2500.0)
                .onChanged(e -> Damage.speedUp = e))
                .visible(() -> this.flyMode.get() == FlyModes.Damage))
                .build()
        );

    public void onDeactivate() {
        this.currentMode.onDeactivate();
    }    public final Setting<Integer> speedDamageTicks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("max-ticks"))
                .description("Max fly ticks."))
                .defaultValue(15))
                .max(2500)
                .sliderRange(0, 2500)
                .onChanged(e -> Damage.workingTicks = e))
                .visible(() -> this.flyMode.get() == FlyModes.Damage))
                .build()
        );

    @EventHandler
    private void onPreTick(Pre event) {
        this.currentMode.onTickEventPre(event);
    }    public final Setting<Integer> speedUpDamageTicks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("max-up-ticks"))
                .description("Max fly ticks."))
                .defaultValue(5))
                .max(2500)
                .sliderRange(0, 2500)
                .onChanged(e -> Damage.workingUpTicks = e))
                .visible(() -> this.flyMode.get() == FlyModes.Damage))
                .build()
        );

    @EventHandler
    private void onPostTick(Post event) {
        this.currentMode.onTickEventPost(event);
    }    public final Setting<Boolean> canClip = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("can-clip"))
                .description("Max fly ticks."))
                .visible(() -> this.flyMode.get() == FlyModes.Vulcan_Clip))
                .build()
        );

    @EventHandler
    public void onSendPacket(Send event) {
        this.currentMode.onSendPacket(event);
    }    public final Setting<Boolean> showInfo = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("show-info"))
                .description("Displays information about whether this mode is running on the server."))
                .visible(() -> this.flyMode.get() == FlyModes.Vulcan_Clip))
                .build()
        );

    @EventHandler
    public void onSentPacket(Sent event) {
        this.currentMode.onSentPacket(event);
    }

    @EventHandler
    public void onRecivePacket(Receive event) {
        this.currentMode.onRecivePacket(event);
    }

    @EventHandler
    public void onCanWalkOnFluid(CanWalkOnFluidEvent event) {
        this.currentMode.onCanWalkOnFluid(event);
    }

    @EventHandler
    public void onCollisionShape(CollisionShapeEvent event) {
        this.currentMode.onCollisionShape(event);
    }

    @EventHandler
    private void onPlayerMoveEvent(PlayerMoveEvent event) {
        this.currentMode.onPlayerMoveEvent(event);
    }

    @EventHandler
    private void onPlayerMoveSendPre(meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent.Pre event) {
        this.currentMode.onPlayerMoveSendPre(event);
    }

    @EventHandler
    private void onDamage(DamageEvent event) {
        this.currentMode.onDamage(event);
    }

    private void onFlyModeChanged(FlyModes mode) {
        switch (mode) {
            case Matrix_Exploit_2:
                this.currentMode = new MatrixExploit2();
                break;
            case Matrix_Exploit:
                this.currentMode = new MatrixExploit();
                break;
            case Vulcan_Clip:
                if (this.showInfo.get()) {
                    this.info("Vulcan fly work on 1.8.9 servers", new Object[0]);
                }

                this.currentMode = new VulcanClip();
                break;
            case Damage:
                this.currentMode = new Damage();
                break;
            case Damage_OldFag:
                this.currentMode = new Damage();
                Damage.workingUpTicks = 0;
                Damage.workingTicks = 15;
                Damage.speed = 0.396;
                Damage.speedUp = 0.0;
        }
    }


















}
