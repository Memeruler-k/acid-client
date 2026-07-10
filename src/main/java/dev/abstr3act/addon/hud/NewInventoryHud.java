package dev.abstr3act.addon.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class NewInventoryHud extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();    public static final HudElementInfo<NewInventoryHud> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "NewInventory", "Displays your inventory.", NewInventoryHud::new
    );
    private final Setting<Boolean> containers = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("containers")).description("Shows the contents of a container when holding them.")).defaultValue(false))
                .build()
        );
    private final Setting<SettingColor> color = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("background-color"))
                .description("Color of the background."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );
    private final Setting<SettingColor> shadowColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("shadow-color"))
                .description("Color of the shadow."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );    private final Setting<Double> scale = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("scale"))
                .description("The scale."))
                .defaultValue(2.0)
                .min(1.0)
                .sliderRange(1.0, 5.0)
                .onChanged(aDouble -> this.calculateSize()))
                .build()
        );
    private final Setting<SettingColor> stripShadowColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("strip-shadow-color"))
                .description("Color of the strip shadow."))
                .defaultValue(new SettingColor(255, 255, 255))
                .build()
        );    private final Setting<Integer> stripLength = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("strip-length"))
                .description("The strip."))
                .defaultValue(2))
                .min(0)
                .sliderRange(1, 10)
                .onChanged(aDouble -> this.calculateSize()))
                .build()
        );
    private final Setting<Double> tx1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("tx1"))
                .description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );    private final Setting<Integer> itemRowOffsetY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("row-offset-y"))
                .description("offset"))
                .defaultValue(0))
                .min(-100)
                .sliderRange(-100, 100)
                .onChanged(aDouble -> this.calculateSize()))
                .build()
        );
    private final Setting<Double> tx2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("tx2"))
                .description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );    private final Setting<Integer> itemRowOffsetX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("row-offset-x"))
                .description("offset"))
                .defaultValue(0))
                .min(-100)
                .sliderRange(-100, 100)
                .onChanged(aDouble -> this.calculateSize()))
                .build()
        );
    private final Setting<Double> ty1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("ty1"))
                .description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );    private final Setting<Integer> height = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("height"))
                .description("height"))
                .defaultValue(0))
                .min(1)
                .sliderRange(1, 100)
                .onChanged(aDouble -> this.calculateSize()))
                .build()
        );
    private final Setting<Double> ty2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("ty2"))
                .description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );    private final Setting<Integer> width = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("width"))
                .description("width"))
                .defaultValue(0))
                .min(1)
                .sliderRange(1, 100)
                .onChanged(aDouble -> this.calculateSize()))
                .build()
        );
    private final ItemStack[] containerItems = new ItemStack[27];    private final Setting<Background> background = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("background"))
                .description("Background of inventory viewer."))
                .defaultValue(Background.Flat))
                .onChanged(bg -> this.calculateSize()))
                .build()
        );
    private NewInventoryHud() {
        super(INFO);
        this.calculateSize();
    }

    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;
        ItemStack container = this.getContainer();
        boolean hasContainer = this.containers.get() && container != null;
        if (hasContainer) {
            Utils.getItemsInContainerItem(container, this.containerItems);
        }

        Color drawColor = hasContainer ? Utils.getShulkerColor(container) : (Color) this.color.get();
        this.drawBackground(renderer, (int) x, (int) y, drawColor);
        double var14 = y + (this.itemRowOffsetY.get()).intValue();
        double var13 = x + (this.itemRowOffsetX.get()).intValue();
        if (MeteorClient.mc.player != null) {
            renderer.post(() -> {
                for (int row = 0; row < 3; row++) {
                    for (int i = 0; i < 9; i++) {
                        int index = row * 9 + i;
                        ItemStack stack = hasContainer ? this.containerItems[index] : MeteorClient.mc.player.getInventory().getStack(index + 9);
                        if (stack != null) {
                            int itemX = (int) (var13 + (1 + i * 18) * this.scale.get());
                            int itemY = (int) (var14 + (1 + row * 18) * this.scale.get());
                            renderer.item(stack, itemX, itemY, (this.scale.get()).floatValue(), true);
                        }
                    }
                }
            });
        }
    }

    private void calculateSize() {
        this.setSize(
            ((Background) this.background.get()).width * this.scale.get(),
            ((Background) this.background.get()).height * this.scale.get()
        );
    }

    private void drawBackground(HudRenderer renderer, int x, int y, Color color) {
        int w = this.getWidth() + this.width.get();
        int h = this.getHeight() + this.height.get();
        renderer.drawContext.getMatrices().push();
        renderer.quad(x, y - this.stripLength.get(), w, (this.stripLength.get()).intValue(), new Color(255, 255, 255, 255));
        Render2DEngine.drawRoundedBlur(
            renderer.drawContext.getMatrices(),
            x + (this.tx1.get()).floatValue(),
            y + (this.ty1.get()).floatValue(),
            w + (this.tx2.get()).floatValue(),
            h + (this.ty2.get()).floatValue(),
            0.0F,
            new java.awt.Color(color.getPacked())
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Render2DEngine.drawBlurredShadow(
            renderer.drawContext.getMatrices(), (float) x, (float) y, (float) w, (float) h, 40, Render2DEngine.getAwtColor((Color) this.shadowColor.get())
        );
        Render2DEngine.drawBlurredShadow(
            renderer.drawContext.getMatrices(),
            (float) x,
            (float) (y - this.stripLength.get()),
            (float) w,
            (float) (this.stripLength.get()).intValue(),
            40,
            Render2DEngine.getAwtColor((Color) this.stripShadowColor.get())
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        renderer.drawContext.getMatrices().pop();
    }

    private ItemStack getContainer() {
        if (!this.isInEditor() && MeteorClient.mc.player != null) {
            ItemStack stack = MeteorClient.mc.player.getOffHandStack();
            if (!Utils.hasItems(stack) && stack.getItem() != Items.ENDER_CHEST) {
                stack = MeteorClient.mc.player.getMainHandStack();
                return !Utils.hasItems(stack) && stack.getItem() != Items.ENDER_CHEST ? null : stack;
            } else {
                return stack;
            }
        } else {
            return null;
        }
    }
    public static enum Background {
        Flat(162, 54);

        private final int width;
        private final int height;

        private Background(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }














}
