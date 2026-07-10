package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventFixVelocity;
import dev.abstr3act.addon.events.EventKeyboardInput;
import dev.abstr3act.addon.events.EventPlayerJump;
import dev.abstr3act.addon.events.EventPlayerTravel;
import dev.abstr3act.addon.manager.Placement;
import dev.abstr3act.addon.module.CompassionModule;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MoveFix extends CompassionModule {
    public static MoveFix INSTANCE;
    public static float fixPitch;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<UpdateMode> updateMode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("UpdateMode")).description(".")).defaultValue(UpdateMode.All)).build());
    public final Setting<Placement> placement = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("placement")).description(".")).defaultValue(Placement.Strict)).build());
    public final Setting<Boolean> clientLook = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ClientLook"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> look = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Look"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> forceSync = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("forceSync"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> multiPlace = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("multiPlace"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> applyYaw = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("applyYaw"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> rotate = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("rotate"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> grimRotation = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("grimRotation"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> noSpamRotation = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("noSpamRotation"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    public final Setting<Boolean> blockCheck = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("blockCheck"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> grim = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Grim"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> rotation = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ShowRotation"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> packetPlace = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("packetPlace"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> inventorySync = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("inventorySync"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> snapBack = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("snapBack"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Boolean> travel = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Travel"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    public final Setting<Double> step = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Step"))
                .description("."))
                .defaultValue(0.6)
                .range(0.0, 1.0)
                .build()
        );
    public final Setting<Double> fov = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("FOV"))
                .description("."))
                .defaultValue(10.0)
                .range(0.0, 180.0)
                .build()
        );
    public final Setting<Double> rotationTime = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("rotationTime"))
                .description("."))
                .defaultValue(0.5)
                .range(0.0, 1.0)
                .build()
        );
    public float fixRotation;
    private float prevYaw;
    private float prevPitch;

    public MoveFix() {
        super(Compassion.COMPASSION, "MoveFix", "GrimAC fucker");
        INSTANCE = this;
    }

    private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        } else {
            Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply(speed);
            float f = MathHelper.sin(yaw * (float) (Math.PI / 180.0));
            float g = MathHelper.cos(yaw * (float) (Math.PI / 180.0));
            return new Vec3d(vec3d.x * g - vec3d.z * f, vec3d.y, vec3d.z * g + vec3d.x * f);
        }
    }

    public void onDeactivate() {
    }

    @EventHandler
    public void travel(EventPlayerTravel e) {
        if (this.grim.get() && this.travel.get()) {
            if (!this.mc.player.isRiding()) {
                if (e.isPre()) {
                    this.prevYaw = this.mc.player.getYaw();
                    this.prevPitch = this.mc.player.getPitch();
                    this.mc.player.setYaw(this.fixRotation);
                    this.mc.player.setPitch(fixPitch);
                } else {
                    this.mc.player.setYaw(this.prevYaw);
                    this.mc.player.setPitch(this.prevPitch);
                }
            }
        }
    }

    @EventHandler
    public void onJump(EventPlayerJump e) {
        if (this.grim.get()) {
            if (!this.mc.player.isRiding()) {
                if (e.isPre()) {
                    this.prevYaw = this.mc.player.getYaw();
                    this.prevPitch = this.mc.player.getPitch();
                    this.mc.player.setYaw(this.fixRotation);
                    this.mc.player.setPitch(fixPitch);
                } else {
                    this.mc.player.setYaw(this.prevYaw);
                    this.mc.player.setPitch(this.prevPitch);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(EventFixVelocity event) {
        if (this.grim.get() && !this.travel.get()) {
            if (!this.mc.player.isRiding()) {
                event.cancel();
                event.setVelocity(movementInputToVelocity(event.getMovementInput(), event.getSpeed(), this.fixRotation));
            }
        }
    }

    @EventHandler(
        priority = -999
    )
    public void onKeyInput(EventKeyboardInput e) {
        if (this.grim.get()) {
            if (!this.mc.player.isRiding() && !((Freecam) Modules.get().get(Freecam.class)).isActive()) {
                float mF = this.mc.player.input.movementForward;
                float mS = this.mc.player.input.movementSideways;
                float delta = (this.mc.player.getYaw() - this.fixRotation) * (float) (Math.PI / 180.0);
                float cos = MathHelper.cos(delta);
                float sin = MathHelper.sin(delta);
                this.mc.player.input.movementSideways = Math.round(mS * cos - mF * sin);
                this.mc.player.input.movementForward = Math.round(mF * cos + mS * sin);
            }
        }
    }

    public static enum UpdateMode {
        MovementPacket,
        UpdateMouse,
        All;
    }
}
