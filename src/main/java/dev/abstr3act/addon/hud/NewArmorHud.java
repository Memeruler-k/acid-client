package dev.abstr3act.addon.hud;

import dev.abstr3act.addon.utils.render.Render2DEngine;
import dev.abstr3act.addon.utils.render.shaders.AnimationUtility;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.*;

public class NewArmorHud extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();    public static final HudElementInfo<NewArmorHud> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "NewArmorHUD", "Displays your armor.", NewArmorHud::new
    );
    public final Setting<Integer> width = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Width"))
                .description(""))
                .defaultValue(5))
                .sliderRange(0, 1000)
                .build()
        );
    public final Setting<Integer> height = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Height"))
                .description("Ignores yourself drawing the shader."))
                .defaultValue(100))
                .sliderRange(0, 1000)
                .build()
        );
    public final Setting<Integer> x1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("X"))
                .description(""))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    public final Setting<Integer> y1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Y"))
                .description("Ignores yourself drawing the shader."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );    private final Setting<Orientation> orientation = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("orientation")).description("How to display armor."))
                .defaultValue(Orientation.Horizontal))
                .onChanged(val -> this.calculateSize()))
                .build()
        );
    public final Setting<Integer> x2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("X2"))
                .description(""))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    public final Setting<Integer> y2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Y2"))
                .description("Ignores yourself drawing the shader."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );    private final Setting<Double> scale = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("scale"))
                .description("The scale."))
                .defaultValue(2.0)
                .onChanged(aDouble -> this.calculateSize()))
                .min(1.0)
                .sliderRange(1.0, 5.0)
                .build()
        );
    private final SettingGroup sgDurability = this.settings.createGroup("Durability");
    private final SettingGroup sgBackground = this.settings.createGroup("Background");
    private final Setting<Boolean> flipOrder = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("flip-order"))
                .description("Flips the order of armor items."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> factor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Animation Factor"))
                .description("The scale."))
                .defaultValue(5.0)
                .min(0.0)
                .sliderRange(1.0, 5.0)
                .build()
        );
    private final Setting<Boolean> background = this.sgBackground
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("background"))
                .description("Displays background."))
                .defaultValue(false))
                .build()
        );
    private final Setting<SettingColor> backgroundColor = this.sgBackground
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("background-color"))
                .description("Color used for the background."))
                .visible(this.background::get))
                .defaultValue(new SettingColor(25, 25, 25, 50))
                .build()
        );
    private final Setting<SettingColor> backgroundColor2 = this.sgBackground
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("background-color2"))
                .description("Color used for the background."))
                .defaultValue(new SettingColor(25, 25, 25, 50))
                .build()
        );
    private final Setting<SettingColor> barColor = this.sgBackground
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("bar-color"))
                .description("Color used for the background."))
                .defaultValue(new SettingColor(25, 25, 25, 50))
                .build()
        );    private final Setting<Integer> border = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("border"))
                .description("How much space to add around the element."))
                .defaultValue(0))
                .onChanged(integer -> this.calculateSize()))
                .build()
        );
    float vAnimation;    private final Setting<Durability> durability = this.sgDurability
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("durability")).description("How to display armor durability."))
                .defaultValue(Durability.Bar))
                .onChanged(durability1 -> this.calculateSize()))
                .build()
        );
    float hAnimation;    private final Setting<SettingColor> durabilityColor = this.sgDurability
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("durability-color"))
                .description("Color of the text."))
                .visible(() -> this.durability.get() == Durability.Total || this.durability.get() == Durability.Percentage))
                .defaultValue(new SettingColor())
                .build()
        );
    public NewArmorHud() {
        super(INFO);
        this.calculateSize();
    }    private final Setting<Boolean> durabilityShadow = this.sgDurability
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("durability-shadow"))
                .description("Text shadow."))
                .visible(() -> this.durability.get() == Durability.Total || this.durability.get() == Durability.Percentage))
                .defaultValue(true))
                .build()
        );

    public static float getAverage(PlayerEntity player) {
        Iterable<ItemStack> armorItems = player.getArmorItems();
        int totalDurability = 0;
        int totalMaxDurability = 0;
        int armorCount = 0;

        for (ItemStack armor : armorItems) {
            if (!armor.isEmpty() && armor.getItem() instanceof ArmorItem) {
                int maxDurability = armor.getMaxDamage();
                int currentDurability = maxDurability - armor.getDamage();
                totalDurability += currentDurability;
                totalMaxDurability += maxDurability;
                armorCount++;
            }
        }

        return armorCount != 0 && totalMaxDurability != 0 ? (float) totalDurability / totalMaxDurability * 100.0F : 0.0F;
    }

    public void setSize(double width, double height) {
        super.setSize(width + this.border.get() * 2, height + this.border.get() * 2);
    }

    private void calculateSize() {
        switch ((Orientation) this.orientation.get()) {
            case Horizontal:
                this.setSize(16.0 * this.scale.get() * 4.0 + 8.0, 16.0 * this.scale.get());
                break;
            case Vertical:
                this.setSize(16.0 * this.scale.get(), 16.0 * this.scale.get() * 4.0 + 8.0);
        }
    }

    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;
        int slot = this.flipOrder.get() ? 3 : 0;
        if (this.background.get()) {
            Render2DEngine.drawRoundedBlur2(
                renderer.drawContext.getMatrices(),
                this.x + this.x2.get(),
                this.y + this.y2.get(),
                this.getWidth(),
                this.getHeight(),
                0.0F,
                new Color(((SettingColor) this.backgroundColor.get()).getPacked(), true)
            );
        }

        if (this.orientation.get() == Orientation.Vertical) {
            float durabilityPercentage = getAverage(MeteorClient.mc.player);
            int filledHeight = (int) ((this.height.get()).intValue() * (durabilityPercentage / 100.0F));
            this.vAnimation = AnimationUtility.ease(this.vAnimation, filledHeight, (this.factor.get()).floatValue());
            renderer.quad(
                this.getX() + this.x1.get(),
                this.getY() + this.y1.get(),
                (this.width.get()).intValue(),
                (this.height.get()).intValue(),
                (meteordevelopment.meteorclient.utils.render.color.Color) this.backgroundColor.get()
            );
            renderer.quad(
                this.getX() + this.x1.get(),
                this.getY() + this.y1.get(),
                (this.width.get()).intValue(),
                this.vAnimation,
                (meteordevelopment.meteorclient.utils.render.color.Color) this.barColor.get()
            );
        } else {
            float durabilityPercentage = getAverage(MeteorClient.mc.player);
            int filledWidth = (int) ((this.width.get()).intValue() * (durabilityPercentage / 100.0F));
            this.hAnimation = AnimationUtility.ease(this.hAnimation, filledWidth, (this.factor.get()).floatValue());
            renderer.quad(
                this.getX() + this.x1.get(),
                this.getY() + this.y1.get(),
                (this.height.get()).intValue(),
                (this.width.get()).intValue(),
                (meteordevelopment.meteorclient.utils.render.color.Color) this.backgroundColor.get()
            );
            renderer.quad(
                this.getX() + this.x1.get(),
                this.getY() + this.y1.get(),
                (this.height.get()).intValue(),
                this.hAnimation,
                (meteordevelopment.meteorclient.utils.render.color.Color) this.barColor.get()
            );
        }

        for (int position = 0; position < 4; position++) {
            ItemStack itemStack = this.getItem(slot);
            double armorX;
            double armorY;
            if (this.orientation.get() == Orientation.Vertical) {
                armorX = x;
                armorY = y + position * 18 * this.scale.get();
                float message = getAverage(MeteorClient.mc.player);
            } else {
                armorX = x + position * 18 * this.scale.get();
                armorY = y;
                float var22 = getAverage(MeteorClient.mc.player);
            }

            renderer.item(
                itemStack,
                (int) armorX,
                (int) armorY,
                (this.scale.get()).floatValue(),
                itemStack.isDamageable() && this.durability.get() == Durability.Bar
            );
            if (itemStack.isDamageable()
                && !this.isInEditor()
                && this.durability.get() != Durability.Bar
                && this.durability.get() != Durability.None) {
                String message = switch ((Durability) this.durability.get()) {
                    case Total -> Integer.toString(itemStack.getMaxDamage() - itemStack.getDamage());
                    case Percentage ->
                        Integer.toString(Math.round((itemStack.getMaxDamage() - itemStack.getDamage()) * 100.0F / itemStack.getMaxDamage()));
                    default -> "err";
                };
                double messageWidth = renderer.textWidth(message);
                if (this.orientation.get() == Orientation.Vertical) {
                    armorX = x + 8.0 * this.scale.get() - messageWidth / 2.0;
                    armorY = y + 18 * position * this.scale.get() + (18.0 * this.scale.get() - renderer.textHeight());
                } else {
                    armorX = x + 18 * position * this.scale.get() + 8.0 * this.scale.get() - messageWidth / 2.0;
                    armorY = y + (this.getHeight() - renderer.textHeight());
                }

                renderer.text(
                    message, armorX, armorY, (meteordevelopment.meteorclient.utils.render.color.Color) this.durabilityColor.get(), this.durabilityShadow.get()
                );
            }

            if (this.flipOrder.get()) {
                slot--;
            } else {
                slot++;
            }
        }
    }

    private ItemStack getItem(int i) {
        if (this.isInEditor()) {
            return switch (i) {
                case 1 -> Items.NETHERITE_LEGGINGS.getDefaultStack();
                case 2 -> Items.NETHERITE_CHESTPLATE.getDefaultStack();
                case 3 -> Items.NETHERITE_HELMET.getDefaultStack();
                default -> Items.NETHERITE_BOOTS.getDefaultStack();
            };
        } else {
            return MeteorClient.mc.player.getInventory().getArmorStack(i);
        }
    }
    public static enum Durability {
        None,
        Bar,
        Total,
        Percentage;
    }

    public static enum Orientation {
        Horizontal,
        Vertical;
    }














}
