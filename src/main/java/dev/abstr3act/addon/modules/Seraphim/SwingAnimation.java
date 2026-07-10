package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class SwingAnimation extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<AnimationMode> animationMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("AnimationMode"))
                .description(""))
                .defaultValue(AnimationMode.PULL))
                .build()
        );
    private final Setting<Double> armTranslateRight = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("ArmTranslateRight")).description("."))
                .defaultValue(-0.1F)
                .sliderMin(-10.0)
                .sliderMax(10.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> armTranslateLeft = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("ArmTranslateLeft")).description("."))
                .defaultValue(0.1F)
                .sliderMin(-10.0)
                .sliderMax(10.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> armTranslateY = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("ArmTranslateY")).description("."))
                .defaultValue(0.1F)
                .sliderMin(-10.0)
                .sliderMax(10.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> armTranslateZ = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("ArmTranslateZ")).description("."))
                .defaultValue(0.0)
                .sliderMin(-10.0)
                .sliderMax(10.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> rotateZRight = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("RotateZRight")).description("."))
                .defaultValue(1.0)
                .sliderMin(-10.0)
                .sliderMax(10.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> rotateZLeft = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("RotateZLeft")).description("."))
                .defaultValue(-1.0)
                .sliderMin(-100.0)
                .sliderMax(100.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> rotateZFactor = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("RotateZFactor")).description("."))
                .defaultValue(10.0)
                .sliderMin(-100.0)
                .sliderMax(100.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> rotateX_A = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Rotate X_A")).description("."))
                .defaultValue(-35.0)
                .sliderMin(-360.0)
                .sliderMax(360.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> rotateX_B = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Rotate X_B")).description("."))
                .defaultValue(-102.25)
                .sliderMin(-360.0)
                .sliderMax(360.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> rotateY = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Rotate Y")).description("."))
                .defaultValue(13.365F)
                .sliderMin(-360.0)
                .sliderMax(360.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> rotateZ = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Rotate Z")).description("."))
                .defaultValue(78.05F)
                .sliderMin(-360.0)
                .sliderMax(360.0)
                .visible(() -> ((AnimationMode) this.animationMode.get()).equals(AnimationMode.CUSTOM)))
                .build()
        );
    private final Setting<Double> translateY = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("translateY")).description(".")).defaultValue(0.0).min(0.0).sliderMax(10.0).build());
    private final Setting<Double> swingProgressScale = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("swingProgressScale")).description(".")).defaultValue(1.0).min(0.0).sliderMax(10.0).build());

    public SwingAnimation() {
        super(Compassion.SERAPHIM, "SwingAnimation", "Old animations");
    }

    public void custom(MatrixStack matrices, Arm arm, float equipProgress, float swingProgress) {
        matrices.translate(
            arm == Arm.RIGHT ? this.armTranslateRight.get() : this.armTranslateLeft.get(),
            this.armTranslateY.get(),
            this.armTranslateZ.get()
        );
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrices.multiply(
            RotationAxis.POSITIVE_Z
                .rotationDegrees(
                    (arm == Arm.RIGHT ? (this.rotateZRight.get()).floatValue() : (this.rotateZLeft.get()).floatValue())
                        * g
                        * (this.rotateZFactor.get()).floatValue()
                )
        );
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * (this.rotateX_A.get()).floatValue()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-(this.rotateX_B.get()).floatValue()));
        matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees((this.rotateY.get()).floatValue()));
        matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees((this.rotateZ.get()).floatValue()));
    }

    @EventHandler
    private void onPacketEvent(Send event) {
        if (event.packet instanceof PlayerInteractItemC2SPacket packet) {
            ItemStack offhandItem = this.mc.player.getStackInHand(Hand.OFF_HAND);
            Hand hand = packet.getHand();
            ItemStack itemInHand = this.mc.player.getStackInHand(hand);
            if (hand == Hand.MAIN_HAND && itemInHand.getItem() instanceof SwordItem) {
                if (!(offhandItem.getItem() instanceof ShieldItem)) {
                    this.mc
                        .execute(
                            () -> this.mc
                                .interactionManager
                                .sendSequencedPacket(
                                    this.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, sequence, this.mc.player.getYaw(), this.mc.player.getPitch())
                                )
                        );
                } else {
                    event.cancel();
                    this.mc
                        .getNetworkHandler()
                        .sendPacket(new PlayerInteractItemC2SPacket(Hand.OFF_HAND, packet.getSequence(), this.mc.player.getYaw(), this.mc.player.getPitch()));
                }
            }
        }
    }

    public void pullDown(MatrixStack matrices, Arm arm, float equipProgress, float swingProgress) {
        matrices.translate(arm == Arm.RIGHT ? -0.1F : 0.1F, 0.1F, 0.0F);
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * g * 10.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -35.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
        matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365F));
        matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05F));
    }

    public void transform(MatrixStack matrices, Arm arm, float equipProgress, float swingProgress) {
        matrices.translate(arm == Arm.RIGHT ? -0.1F : 0.1F, this.translateY.get(), 0.0);
        this.applySwingOffset(matrices, arm, swingProgress * (this.swingProgressScale.get()).floatValue());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
        matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365F));
        matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05F));
    }

    public void pullDown2(MatrixStack matrices, Arm arm, float equipProgress, float swingProgress) {
        matrices.translate(arm == Arm.RIGHT ? -0.1F : 0.1F, 0.1F, 0.0F);
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1 : -1) * g * 10.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -35.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
        matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365F));
        matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05F));
    }

    public void transform2(MatrixStack matrices, Arm arm, float equipProgress, float swingProgress) {
        matrices.translate(arm == Arm.RIGHT ? -0.1F : 0.1F, this.translateY.get(), 0.0);
        this.applySwingOffset(matrices, arm, swingProgress * (this.swingProgressScale.get()).floatValue());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
        matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y).rotationDegrees(13.365F));
        matrices.multiply((arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z).rotationDegrees(78.05F));
    }

    protected void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) {
        int armSide = arm == Arm.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armSide * (45.0F + f * -20.0F)));
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(armSide * g * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armSide * -45.0F));
    }

    public static enum AnimationMode {
        PULL,
        NORMAL,
        CUSTOM;
    }
}
