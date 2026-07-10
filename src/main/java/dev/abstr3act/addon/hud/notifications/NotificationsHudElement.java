package dev.abstr3act.addon.hud.notifications;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import dev.abstr3act.addon.utils.notifications.DrawUtils;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.screens.HudElementScreen;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.AlignmentX;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;

public class NotificationsHudElement extends HudElement {
    private static final int MAX_VALID_NOTIFICATIONS = 5;
    static Identifier icons;
    private static NotificationsHudElement instance;    public static final HudElementInfo<NotificationsHudElement> INFO = new HudElementInfo(
        Compassion.HUD_GROUP, "Notifications", "Displays various notifications on your HUD.", NotificationsHudElement::new
    );
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<List<Module>> modules = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ModuleListSetting.Builder) ((meteordevelopment.meteorclient.settings.ModuleListSetting.Builder) ((meteordevelopment.meteorclient.settings.ModuleListSetting.Builder) new meteordevelopment.meteorclient.settings.ModuleListSetting.Builder()
                .name("Modules to display"))
                .description("The modules to display in the notifications."))
                .defaultValue(Modules.get().getList()))
                .build()
        );    public final Setting<Integer> timeToDisplay = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("time-to-display")).description("The time to display the notifications (in milliseconds)."))
                .onChanged(c -> {
                    Integer var7 = c / 2;
                    if (this.animationDuration.get() > var7) {
                        this.animationDuration.set(var7);
                    }

                    try {
                        Field max = IntSetting.class.getDeclaredField("max");
                        max.setAccessible(true);
                        max.set(this.animationDuration, var7);
                        Field sliderMax = IntSetting.class.getDeclaredField("sliderMax");
                        sliderMax.setAccessible(true);
                        sliderMax.set(this.animationDuration, var7);
                        if (MinecraftClient.getInstance().currentScreen instanceof HudElementScreen e) {
                            e.reload();
                        }
                    } catch (Exception var6) {
                        var6.printStackTrace();
                    }
                }))
                .defaultValue(3000))
                .min(500)
                .sliderRange(500, 5000)
                .build()
        );
    public final Setting<SettingColor> enableColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("enable-color"))
                .description("The background color of the notifications."))
                .defaultValue(new SettingColor(255, 255, 255, 102))
                .build()
        );    public final Setting<Integer> maxCount = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("max-count")).description("The maximum amount of notifications to display.")).defaultValue(3))
                .min(1)
                .sliderRange(1, 5)
                .onChanged(c -> {
                    if (this.dummyNotificationsDisplayCount.get() > c) {
                        this.dummyNotificationsDisplayCount.set(c);
                    }

                    try {
                        Field max = IntSetting.class.getDeclaredField("max");
                        max.setAccessible(true);
                        max.set(this.dummyNotificationsDisplayCount, c);
                        Field sliderMax = IntSetting.class.getDeclaredField("sliderMax");
                        sliderMax.setAccessible(true);
                        sliderMax.set(this.dummyNotificationsDisplayCount, c);
                        if (MinecraftClient.getInstance().currentScreen instanceof HudElementScreen e) {
                            e.reload();
                        }
                    } catch (Exception var6) {
                        var6.printStackTrace();
                    }
                }))
                .build()
        );
    public final Setting<SettingColor> disableColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("disable-color"))
                .description("The background color of the notifications."))
                .defaultValue(new SettingColor(255, 255, 255, 102))
                .build()
        );
    public final Setting<SettingColor> stripColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("strip-color"))
                .description("The background color of the notifications."))
                .defaultValue(new SettingColor(255, 255, 255, 255))
                .build()
        );
    public final Setting<Double> blurOpacity = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("blurOpacity"))
                .description("."))
                .defaultValue(0.55F)
                .sliderMin(0.0)
                .sliderMax(1.0)
                .build()
        );
    public final Setting<Double> blurStrength = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("blurStrength"))
                .description("."))
                .defaultValue(20.0)
                .sliderMin(5.0)
                .sliderMax(50.0)
                .build()
        );
    private final Setting<Mode> mode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("mode"))
                .description("The display style of the notifications."))
                .defaultValue(Mode.SIMULATAN))
                .build()
        );
    private final SettingGroup proportions = this.settings.createGroup("Proportions", false);
    public final Setting<Integer> globalAlpha = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("global-alpha")).description("The height of the notifications.")).defaultValue(255))
                .min(0)
                .sliderRange(0, 255)
                .build()
        );
    private final Setting<Double> scaleTitle = this.proportions
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("scale-title"))
                .description("The scale of the title."))
                .defaultValue(1.0)
                .min(0.1)
                .sliderRange(0.1, 5.0)
                .build()
        );
    private final Setting<Double> scaleDescription = this.proportions
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("Description-scale"))
                .description("The scale of the description."))
                .defaultValue(0.75)
                .min(0.1)
                .sliderRange(0.1, 5.0)
                .build()
        );
    private final Setting<Integer> notificationHeight = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("notification-height")).description("The height of the notifications.")).defaultValue(60))
                .min(0)
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<Integer> notificationPaddingY = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("notification-padding-y")).description("The padding between notifications on the Y axis."))
                .defaultValue(10))
                .min(0)
                .sliderRange(0, 20)
                .build()
        );
    private final Setting<Integer> innerNotificationPaddingX = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("inner-notification-padding-x"))
                .description("The padding between the border of the notifications and the text on the X axis in %."))
                .defaultValue(10))
                .min(0)
                .sliderRange(0, 30)
                .build()
        );    public final IntSetting dummyNotificationsDisplayCount = (IntSetting) this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("dummy-notifications-display-count")).description("The amount of dummy notifications to display."))
                .defaultValue(Math.max(this.maxCount.get() - 2, 1)))
                .min(0)
                .sliderRange(0, this.maxCount.get())
                .max(this.maxCount.get())
                .build()
        );
    private final Setting<Integer> width = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("width")).description("The width of the notifications.")).defaultValue(250))
                .min(200)
                .sliderRange(200, 500)
                .build()
        );
    private final Setting<Integer> progressBarHeight = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("progress-bar-height")).description("The height of the progress bar (0 to disable).")).defaultValue(5))
                .min(0)
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<VerticalAlign> verticalAlign = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("vertical-align"))
                .description("The vertical alignment of the notifications."))
                .defaultValue(VerticalAlign.BOTTOM))
                .build()
        );
    private final Setting<AlignmentX> titleAlignment = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("title-alignment"))
                .description("The horizontal alignment of the notification titles."))
                .defaultValue(AlignmentX.Center))
                .build()
        );
    private final Setting<AlignmentX> descriptionAlignment = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("description-alignment"))
                .description("The horizontal alignment of the notification descriptions."))
                .defaultValue(AlignmentX.Center))
                .build()
        );
    private final Setting<SettingColor> backgroundColor = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("background-color"))
                .description("The background color of the notifications."))
                .defaultValue(new SettingColor(32, 32, 32, 102))
                .build()
        );
    private final Setting<Integer> test = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("test-h")).description("The width of the notifications.")).defaultValue(3))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> titleOffset = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("title-offset")).description("The width of the notifications.")).defaultValue(0))
                .min(0)
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> dspOffset = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("description-offset")).description("The width of the notifications.")).defaultValue(0))
                .min(0)
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Boolean> strip = this.proportions
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Strip"))
                .description("The width of the notifications."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> thickness = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("strip-thickness"))
                .description("How round the rectangles of the notifications are."))
                .defaultValue(3.0)
                .min(0.0)
                .max(100.0)
                .sliderRange(0.0, 40.0)
                .build()
        );
    private final Setting<Double> radius = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("radius"))
                .description("How round the rectangles of the notifications are."))
                .defaultValue(10.0)
                .min(0.0)
                .max(10.0)
                .sliderRange(0.0, 10.0)
                .build()
        );
    private final Setting<Integer> icon_width = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Icon width")).description("The width of the notifications.")).defaultValue(64))
                .sliderRange(0, 100)
                .build()
        );
    private final Setting<Integer> icon_height = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Icon height")).description("The width of the notifications.")).defaultValue(64))
                .sliderRange(0, 100)
                .build()
        );    private final Setting<Integer> animationDuration = this.sgGeneral
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("animation-duration")).description("The duration of the animation in milliseconds.")).defaultValue(250))
                .min(0)
                .max(this.timeToDisplay.get() / 2)
                .sliderRange(0, this.timeToDisplay.get() / 2)
                .build()
        );
    private final Setting<Integer> icon_x_offset = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Icon X-offset")).description("The width of the notifications.")).defaultValue(0))
                .sliderRange(-100, 100)
                .build()
        );
    private final Setting<Integer> icon_y_offset = this.proportions
        .add(
            ((Builder) ((Builder) ((Builder) new Builder().name("Icon Y-offset")).description("The width of the notifications.")).defaultValue(0))
                .sliderRange(-100, 100)
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
        );
    private final Setting<Double> tx2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("tx2"))
                .description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
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
        );
    private final Setting<Double> ty2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("ty2"))
                .description("How round the rectangles of the notifications are."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> st1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("st1"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> st2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("st3"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> r1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("r1"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(0.0, 100.0)
                .build()
        );
    private final Setting<Double> rt1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("rt1"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(0.0, 100.0)
                .build()
        );
    private final Setting<Double> et1 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("et1"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    private final Setting<Double> et2 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("et3"))
                .description("."))
                .defaultValue(0.0)
                .sliderRange(-100.0, 100.0)
                .build()
        );
    public NotificationsHudElement() {
        super(INFO);
        MeteorClient.EVENT_BUS.subscribe(this);
        instance = this;
    }

    public static NotificationsHudElement getInstance() {
        return instance;
    }

    private static void drawIcon(MatrixStack matrixStack, double x0, double y0, double width, double height, icon iconc) {
        if (iconc != null) {
            switch (iconc) {
                case WARNING:
                    icons = Identifier.of("acid", "icons/notification/warning.png");
                    break;
                case DISABLE:
                    icons = Identifier.of("acid", "icons/notification/disable.png");
                    break;
                case ENABLE:
                    icons = Identifier.of("acid", "icons/notification/enable.png");
                    break;
                case INFO:
                    icons = Identifier.of("acid", "icons/notification/info.png");
                    break;
                case QUESTION:
                    icons = Identifier.of("acid", "icons/notification/question.png");
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GL13.glEnable(32925);
            RenderSystem.setShaderTexture(0, icons);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Render2DEngine.renderTexture(matrixStack, x0, y0, width, height, 0.0F, 0.0F, 256.0, 256.0, 256.0, 256.0, true);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GL13.glDisable(32925);
            RenderSystem.disableBlend();
        }
    }

    @EventHandler
    public void onLogin(GameJoinedEvent event) {
        NotificationsManager.clearNotifications();
    }

    public void render(HudRenderer renderer) {
        this.box
            .setSize(
                (this.width.get()).intValue(),
                (this.notificationHeight.get() + this.progressBarHeight.get()) * this.maxCount.get()
                    + this.notificationPaddingY.get() * (this.maxCount.get() - 1)
            );
        List<Notification> notifications = NotificationsManager.getNotifications(this.isInEditor());
        if (notifications != null && !notifications.isEmpty()) {
            Integer notificationHeight = this.notificationHeight.get();
            AlignmentX titleAlignment = (AlignmentX) this.titleAlignment.get();
            AlignmentX descriptionAlignment = (AlignmentX) this.descriptionAlignment.get();
            Integer notificationPaddingY = this.notificationPaddingY.get();
            Integer innerNotificationPadding = this.innerNotificationPaddingX.get();
            int titlePaddingX = titleAlignment == AlignmentX.Center ? innerNotificationPadding : innerNotificationPadding / 2;
            int descriptionPaddingX = descriptionAlignment == AlignmentX.Center ? innerNotificationPadding : innerNotificationPadding / 2;
            Integer progressBarHeight = this.progressBarHeight.get();
            Integer timeToDisplay = this.timeToDisplay.get();
            VerticalAlign verticalAlign = (VerticalAlign) this.verticalAlign.get();
            Double radius = this.radius.get();
            Float animationDuration = (float) (this.animationDuration.get()).intValue();
            renderer.post(
                () -> {
                    if (this.mode.get() != Mode.SIMULATAN) {
                        throw new NullPointerException("Mode " + this.mode.get() + " is not supported!");
                    } else {
                        double baseX = this.box.getRenderX();
                        double baseY = this.box.getRenderY();

                        for (int i = 0; i < notifications.size(); i++) {
                            Notification notification = notifications.get(i);
                            long startTime = notification.getStartTime() != 0L
                                ? notification.getStartTime()
                                : System.currentTimeMillis()
                                - (long) (notifications.size() - i - 1) * timeToDisplay.intValue() / (this.dummyNotificationsDisplayCount.get()).intValue();
                            long notificationTime = System.currentTimeMillis() - notification.getStartTime();
                            String description = notification.getDescription();
                            float descriptionWidth = (float) DrawUtils.getWidth(description == null ? "" : description);
                            double animationFactor = notification.getStartTime() == 0L
                                ? 0.0
                                : (
                                (float) notificationTime <= animationDuration
                                    ? (animationDuration - (float) notificationTime) * this.getWidth() / animationDuration
                                    : (
                                    (float) notificationTime >= timeToDisplay.intValue() - animationDuration
                                        ? this.getWidth() - (float) ((timeToDisplay.intValue() - notificationTime) * this.getWidth()) / animationDuration
                                        : 0.0
                                )
                            );
                            double x = baseX + animationFactor;
                            double y = baseY
                                + (notificationHeight + progressBarHeight + notificationPaddingY)
                                * (verticalAlign == VerticalAlign.TOP ? i : this.maxCount.get() - i - 1)
                                + -animationFactor / this.getWidth() * (notificationHeight + progressBarHeight + notificationPaddingY);
                            DrawUtils.renderer.begin();
                            float value = (float) (descriptionWidth / 2.0F * this.scaleDescription.get());
                            if (radius > 0.0) {
                                renderer.drawContext.getMatrices().push();
                                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                                Render2DEngine.drawRoundedBlur(
                                    renderer.drawContext.getMatrices(),
                                    (float) (x + (this.tx1.get()).floatValue() - value),
                                    (float) y + (this.ty1.get()).floatValue(),
                                    this.getWidth() + value + (this.tx2.get()).floatValue(),
                                    notificationHeight + progressBarHeight + (this.ty2.get()).floatValue(),
                                    0.0F,
                                    new Color(((SettingColor) this.backgroundColor.get()).getPacked())
                                );
                                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                                renderer.drawContext.getMatrices().pop();
                            } else {
                                renderer.drawContext.getMatrices().push();
                                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                                Render2DEngine.drawRoundedBlur(
                                    renderer.drawContext.getMatrices(),
                                    (float) (x + (this.tx1.get()).floatValue() - value),
                                    (float) y + (this.ty1.get()).floatValue(),
                                    this.getWidth() + value + (this.tx2.get()).floatValue(),
                                    notificationHeight + progressBarHeight + (this.ty2.get()).floatValue(),
                                    0.0F,
                                    new Color(((SettingColor) this.backgroundColor.get()).getPacked())
                                );
                                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                                renderer.drawContext.getMatrices().pop();
                            }

                            double newRectX = x + this.getWidth();
                            if (this.strip.get()) {
                                DrawUtils.drawRoundedQuad(
                                    newRectX + this.st1.get(),
                                    y + this.st2.get(),
                                    this.thickness.get(),
                                    notificationHeight + progressBarHeight + this.rt1.get(),
                                    this.r1.get(),
                                    new meteordevelopment.meteorclient.utils.render.color.Color(((SettingColor) this.stripColor.get()).getPacked())
                                );
                            }

                            double elapsedTime = System.currentTimeMillis() - startTime;
                            double normalizedTime = elapsedTime / timeToDisplay.doubleValue();
                            double progress = Math.sqrt(normalizedTime) * this.getWidth() + value;
                            if (radius > 0.0) {
                                DrawUtils.drawRoundedQuad(
                                    x - value,
                                    y + notificationHeight.intValue() - (this.test.get()).intValue(),
                                    progress,
                                    progressBarHeight + this.test.get(),
                                    radius,
                                    new meteordevelopment.meteorclient.utils.render.color.Color(
                                        notification.getColor().getRed(), notification.getColor().getGreen(), notification.getColor().getBlue(), this.globalAlpha.get()
                                    ),
                                    false
                                );
                            } else {
                                DrawUtils.drawQuad(
                                    x - value,
                                    y + notificationHeight.intValue() - (this.test.get()).intValue(),
                                    progress,
                                    progressBarHeight + this.test.get(),
                                    new meteordevelopment.meteorclient.utils.render.color.Color(
                                        notification.getColor().getRed(), notification.getColor().getGreen(), notification.getColor().getBlue(), this.globalAlpha.get()
                                    )
                                );
                            }

                            DrawUtils.renderer.render(null);
                            double titleHeight = description != null && !description.isEmpty() ? notificationHeight.intValue() * 0.7 : notificationHeight.intValue();
                            double scale = this.scaleTitle.get();
                            TextRenderer.get().begin(scale, false, true);
                            float titleX = titleAlignment == AlignmentX.Center
                                ? (float) (x + this.getWidth() / 2 - DrawUtils.getWidth(notification.getTitle()) / 2.0)
                                : (
                                titleAlignment == AlignmentX.Left
                                    ? (float) (x + titlePaddingX)
                                    : (float) (x + this.getWidth() - titlePaddingX - DrawUtils.getWidth(notification.getTitle()))
                            );
                            DrawUtils.render(
                                notification.getTitle(),
                                titleX - value,
                                y + (titleHeight - TextRenderer.get().getHeight()) / 2.0 + (this.titleOffset.get()).intValue(),
                                Color.WHITE,
                                false
                            );
                            TextRenderer.get().end();
                            if (description != null && !description.isEmpty()) {
                                scale = this.scaleDescription.get();
                                TextRenderer.get().begin(scale, false, true);
                                float descriptionX = descriptionAlignment == AlignmentX.Center
                                    ? (float) (x + this.getWidth() / 2 - descriptionWidth / 2.0F)
                                    : (
                                    descriptionAlignment == AlignmentX.Left
                                        ? (float) (x + descriptionPaddingX)
                                        : (float) (x + this.getWidth() - descriptionPaddingX - descriptionWidth)
                                );
                                DrawUtils.render(
                                    description,
                                    descriptionX - value,
                                    y + (this.dspOffset.get()).intValue() + titleHeight + (progressBarHeight.intValue() - TextRenderer.get().getHeight()) / 2.0,
                                    Color.WHITE,
                                    false
                                );
                                TextRenderer.get().end();
                            }

                            drawIcon(
                                renderer.drawContext.getMatrices(),
                                x + (this.icon_x_offset.get()).intValue() - value,
                                y + (this.icon_y_offset.get()).intValue(),
                                (this.icon_width.get()).intValue(),
                                (this.icon_height.get()).intValue(),
                                notification.getIcon()
                            );
                        }
                    }
                }
            );
        }
    }

    public static enum Mode {
        SIMULATAN;
    }

    public static enum VerticalAlign {
        TOP,
        BOTTOM;
    }

    public static enum icon {
        WARNING,
        DISABLE,
        ENABLE,
        INFO,
        QUESTION;
    }










}
