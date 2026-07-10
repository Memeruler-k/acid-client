package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventHeldItemRenderer;
import dev.abstr3act.addon.mixin.accessor.IHeldItemRenderer;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.modules.Seraphim.SwingAnimation;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;

public class Animations extends CompassionModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Boolean> oldAnimationsM = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("DisableSwapMain")).description(".")).defaultValue(false)).build());
    private final Setting<Boolean> oldAnimationsOff = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("DisableSwapOff")).description(".")).defaultValue(false)).build());
    private final Setting<Mode> mode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Mode"))
                .description("."))
                .defaultValue(Mode.Default))
                .build()
        );
    public boolean flip;

    public Animations() {
        super(Compassion.COMPASSION, "Animations", ".");
    }

    @EventHandler
    public void onUpdate(Pre event) {
        if (!fullNullCheck()) {
            if (this.oldAnimationsM.get()
                && ((IHeldItemRenderer) this.mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressMainHand() <= 1.0F) {
                ((IHeldItemRenderer) this.mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressMainHand(1.0F);
                ((IHeldItemRenderer) this.mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackMainHand(this.mc.player.getMainHandStack());
            }

            if (this.oldAnimationsOff.get()
                && ((IHeldItemRenderer) this.mc.getEntityRenderDispatcher().getHeldItemRenderer()).getEquippedProgressOffHand() <= 1.0F) {
                ((IHeldItemRenderer) this.mc.getEntityRenderDispatcher().getHeldItemRenderer()).setEquippedProgressOffHand(1.0F);
                ((IHeldItemRenderer) this.mc.getEntityRenderDispatcher().getHeldItemRenderer()).setItemStackOffHand(this.mc.player.getOffHandStack());
            }
        }
    }

    @EventHandler
    public void onPacketSend(Send e) {
        if (e.packet instanceof HandSwingC2SPacket) {
            this.flip = !this.flip;
        }
    }

    private void renderSwordAnimation(MatrixStack matrices, float f, float swingProgress, float equipProgress, Arm arm) {
        if (arm != Arm.LEFT
            || this.mode.get() != Mode.Eleven
            && this.mode.get() != Mode.Ten
            && this.mode.get() != Mode.Nine
            && this.mode.get() != Mode.Three
            && this.mode.get() != Mode.Thirteen
            && this.mode.get() != Mode.Fourteen) {
            switch ((Mode) this.mode.get()) {
                case Default:
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    this.translateToViewModelOff(matrices);
                    this.applySwingOffset(matrices, arm, swingProgress);
                    this.translateBacklOff(matrices);
                    break;
                case One: {
                    float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    this.applyEquipOffset(matrices, arm, n);
                    int i = arm == Arm.RIGHT ? 1 : -1;
                    this.translateToViewModel(matrices);
                    float f1 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * (45.0F + f1 * -20.0F)));
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * g * -20.0F));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * 0.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * -45.0F));
                    this.translateBack(matrices);
                    break;
                }
                case Two:
                    this.applyEquipOffset(matrices, arm, 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2)));
                    break;
                case Three: {
                    float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    this.applyEquipOffset(matrices, arm, n);
                    int i = arm == Arm.RIGHT ? 1 : -1;
                    this.translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * (45.0F + f * -20.0F)));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * g * -70.0F));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-70.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * -45.0F));
                    this.translateBack(matrices);
                    break;
                }
                case Four:
                    this.applyEquipOffset(matrices, arm, 0.0F);
                    this.translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swingProgress > 0.0F ? -MathHelper.sin(swingProgress * 13.0F) * 37.0F : 0.0F));
                    this.translateBack(matrices);
                    break;
                case Five: {
                    this.applyEquipOffset(matrices, arm, 0.0F);
                    int i = arm == Arm.RIGHT ? 1 : -1;
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    this.translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * g * -20.0F));
                    this.translateBack(matrices);
                    break;
                }
                case Six:
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    this.translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swingProgress * (this.flip ? 360.0F : -360.0F)));
                    this.translateBack(matrices);
                    break;
                case Seven:
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    float a = -MathHelper.sin(swingProgress * 3.0F) / 2.0F + 1.0F;
                    matrices.scale(a, a, a);
                    break;
                case Eight:
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    this.translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swingProgress * -360.0F));
                    this.translateBack(matrices);
                    break;
                case Nine: {
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    this.applyEquipOffset(matrices, arm, 0.0F);
                    this.translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-30.0F * (1.0F - g) - 30.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110.0F));
                    this.translateBack(matrices);
                    break;
                }
                case Ten: {
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    matrices.translate(0.0F, 0.0F, 0.0F);
                    this.applyEquipOffset(matrices, arm, 0.0F);
                    this.translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-60.0F * g - 50.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110.0F));
                    this.translateBack(matrices);
                    break;
                }
                case Eleven: {
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    this.applyEquipOffset(matrices, arm, 0.0F);
                    this.translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-60.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110.0F + 20.0F * g));
                    this.translateBack(matrices);
                    break;
                }
                case Twelve: {
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    this.applyEquipOffset(matrices, arm, 0.0F);
                    matrices.translate(0.0F, 0.0F, -g / 4.0F);
                    this.translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-120.0F));
                    this.translateBack(matrices);
                    break;
                }
                case Thirteen: {
                    float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    this.applyEquipOffset(matrices, arm, 0.0F);
                    this.translateToViewModel(matrices);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-MathHelper.sin(swingProgress * 3.0F) * 60.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60.0F * g));
                    this.translateBack(matrices);
                    break;
                }
                case Fourteen:
                    if (swingProgress > 0.0F) {
                        float gx = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                        matrices.translate(0.56F, equipProgress * -0.2F - 0.5F, -0.7F);
                        this.translateToViewModel(matrices);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F));
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(gx * -85.0F));
                        matrices.translate(-0.1F, 0.28F, 0.2F);
                        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-85.0F));
                        this.translateBack(matrices);
                    } else {
                        float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                        float m = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2));
                        float f1 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
                        matrices.translate(n, m, f1);
                        this.applyEquipOffset(matrices, arm, equipProgress);
                        this.applySwingOffset(matrices, arm, swingProgress);
                    }
            }
        } else {
            this.applyEquipOffset(matrices, arm, equipProgress);
            matrices.translate(-1.0F, 1.0F, 1.0F);
            this.applySwingOffset(matrices, arm, swingProgress);
            matrices.translate(1.0F, -1.0F, -1.0F);
        }
    }

    public boolean shouldAnimate() {
        return this.isActive() && this.mode.get() != Mode.Normal;
    }

    public void renderFirstPersonItemCustom(
        AbstractClientPlayerEntity player,
        float tickDelta,
        float pitch,
        Hand hand,
        float swingProgress,
        ItemStack item,
        float equipProgress,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light
    ) {
        if (!player.isUsingSpyglass()) {
            boolean bl = hand == Hand.MAIN_HAND;
            Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
            matrices.push();
            float f = 0.0F;
            if (item.isOf(Items.CROSSBOW)) {
                boolean bl2 = CrossbowItem.isCharged(item);
                boolean bl3 = arm == Arm.RIGHT;
                int i = bl3 ? 1 : -1;
                if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    matrices.translate(i * -0.4785682F, -0.094387F, 0.05731531F);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * 65.3F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * -9.785F));
                    f = item.getMaxUseTime(player) - (this.mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                    float g = f / CrossbowItem.getPullTime(item, player);
                    if (g > 1.0F) {
                        g = 1.0F;
                    }

                    if (g > 0.1F) {
                        float h = MathHelper.sin((f - 0.1F) * 1.3F);
                        float j = g - 0.1F;
                        float k = h * j;
                        matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
                    }

                    matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
                    matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
                    matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(i * 45.0F));
                } else {
                    f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                    float gx = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2));
                    float h = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
                    matrices.translate(i * f, gx, h);
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    this.applySwingOffset(matrices, arm, swingProgress);
                    if (bl2 && swingProgress < 0.001F && bl) {
                        matrices.translate(i * -0.641864F, 0.0F, 0.0F);
                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * 10.0F));
                    }
                }

                EventHeldItemRenderer event = new EventHeldItemRenderer(hand, item, equipProgress, matrices);
                MeteorClient.EVENT_BUS.post(event);
                this.renderItem(
                    player,
                    item,
                    bl3 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
                    !bl3,
                    matrices,
                    vertexConsumers,
                    light
                );
            } else {
                boolean bl2 = arm == Arm.RIGHT;
                float m = 0.0F;
                if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                    int l = bl2 ? 1 : -1;
                    label97:
                    switch (item.getUseAction()) {
                        case NONE:
                            this.applyEquipOffset(matrices, arm, equipProgress);
                            break;
                        case BLOCK:
                            switch ((SwingAnimation.AnimationMode) ((SwingAnimation) Modules.get().get(SwingAnimation.class)).animationMode.get()) {
                                case PULL:
                                    ((SwingAnimation) Modules.get().get(SwingAnimation.class)).pullDown(matrices, arm, equipProgress, swingProgress);
                                    break label97;
                                case NORMAL:
                                    ((SwingAnimation) Modules.get().get(SwingAnimation.class)).transform(matrices, arm, equipProgress, swingProgress);
                                default:
                                    break label97;
                            }
                        case EAT:
                        case DRINK:
                            this.applyEatOrDrinkTransformationCustom(matrices, tickDelta, arm, item);
                            this.applyEquipOffset(matrices, arm, equipProgress);
                            break;
                        case BOW:
                            this.applyEquipOffset(matrices, arm, equipProgress);
                            matrices.translate(l * -0.2785682F, 0.18344387F, 0.15731531F);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(l * 35.3F));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(l * -9.785F));
                            m = item.getMaxUseTime(this.mc.player) - (this.mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                            f = m / 20.0F;
                            float var24 = (f * f + f * 2.0F) / 3.0F;
                            if (var24 > 1.0F) {
                                var24 = 1.0F;
                            }

                            if (var24 > 0.1F) {
                                float gx = MathHelper.sin((m - 0.1F) * 1.3F);
                                float h = var24 - 0.1F;
                                float j = gx * h;
                                matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                            }

                            matrices.translate(var24 * 0.0F, var24 * 0.0F, var24 * 0.04F);
                            matrices.scale(1.0F, 1.0F, 1.0F + var24 * 0.2F);
                            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(l * 45.0F));
                            break;
                        case SPEAR:
                            this.applyEquipOffset(matrices, arm, equipProgress);
                            matrices.translate(l * -0.5F, 0.7F, 0.1F);
                            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
                            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(l * 35.3F));
                            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(l * -9.785F));
                            m = item.getMaxUseTime(this.mc.player) - (this.mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                            float var22 = m / 10.0F;
                            if (var22 > 1.0F) {
                                var22 = 1.0F;
                            }

                            if (var22 > 0.1F) {
                                float gx = MathHelper.sin((m - 0.1F) * 1.3F);
                                float h = var22 - 0.1F;
                                float j = gx * h;
                                matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                            }

                            matrices.translate(0.0F, 0.0F, var22 * 0.2F);
                            matrices.scale(1.0F, 1.0F, 1.0F + var22 * 0.2F);
                            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(l * 45.0F));
                            break;
                        case BRUSH:
                            this.applyBrushTransformation(matrices, tickDelta, arm, item, equipProgress);
                    }
                } else if (player.isUsingRiptide()) {
                    this.applyEquipOffset(matrices, arm, equipProgress);
                    int l = bl2 ? 1 : -1;
                    matrices.translate(l * -0.4F, 0.8F, 0.3F);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(l * 65.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(l * -85.0F));
                } else {
                    this.renderSwordAnimation(matrices, f, swingProgress, equipProgress, arm);
                }

                EventHeldItemRenderer event = new EventHeldItemRenderer(hand, item, equipProgress, matrices);
                MeteorClient.EVENT_BUS.post(event);
                this.renderItem(
                    player,
                    item,
                    bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
                    !bl2,
                    matrices,
                    vertexConsumers,
                    light
                );
            }

            matrices.pop();
        }
    }

    private void applyBrushTransformation(MatrixStack matrices, float tickDelta, Arm arm, @NotNull ItemStack stack, float equipProgress) {
        this.applyEquipOffset(matrices, arm, equipProgress);
        float f = this.mc.player.getItemUseTimeLeft() - tickDelta + 1.0F;
        float g = 1.0F - f / stack.getMaxUseTime(this.mc.player);
        float m = -15.0F + 75.0F * MathHelper.cos(g * 45.0F * (float) Math.PI);
        if (arm != Arm.RIGHT) {
            matrices.translate(0.1, 0.83, 0.35);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
            matrices.translate(-0.3, 0.22, 0.35);
        } else {
            matrices.translate(-0.25, 0.22, 0.35);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
        }
    }

    public void applyEquipOffset(@NotNull MatrixStack matrices, Arm arm, float equipProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate(i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }

    public void applySwingOffset(@NotNull MatrixStack matrices, Arm arm, float swingProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * (45.0F + f * -20.0F)));
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * g * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * -45.0F));
    }

    public void renderItem(
        LivingEntity entity,
        ItemStack stack,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light
    ) {
        if (!stack.isEmpty()) {
            this.mc
                .getItemRenderer()
                .renderItem(
                    entity,
                    stack,
                    renderMode,
                    leftHanded,
                    matrices,
                    vertexConsumers,
                    entity.getWorld(),
                    light,
                    OverlayTexture.DEFAULT_UV,
                    entity.getId() + renderMode.ordinal()
                );
        }
    }

    private void applyEatOrDrinkTransformationCustom(MatrixStack matrices, float tickDelta, Arm arm, @NotNull ItemStack stack) {
        float f = this.mc.player.getItemUseTimeLeft() - tickDelta + 1.0F;
        float g = f / stack.getMaxUseTime(this.mc.player);
        if (g < 0.8F) {
            float h = MathHelper.abs(MathHelper.cos(f / 4.0F * (float) Math.PI) * 0.005F);
            matrices.translate(0.0F, h, 0.0F);
        }

        float h = 1.0F - (float) Math.pow(g, 27.0);
        int i = arm == Arm.RIGHT ? 1 : -1;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * h * 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i * h * 30.0F));
    }

    private void translateToViewModel(MatrixStack matrices) {
    }

    private void translateToViewModelOff(MatrixStack matrices) {
    }

    private void translateBack(MatrixStack matrices) {
    }

    private void translateBacklOff(MatrixStack matrices) {
    }

    private static enum Mode {
        Normal,
        Default,
        One,
        Two,
        Three,
        Four,
        Five,
        Six,
        Seven,
        Eight,
        Nine,
        Ten,
        Eleven,
        Twelve,
        Thirteen,
        Fourteen;
    }
}
