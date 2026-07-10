package dev.abstr3act.addon.modules.Compassion;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.CompassionModule;
import dev.abstr3act.addon.modules.Fragment.AntiBot;
import dev.abstr3act.addon.setting.DoubleListSetting;
import dev.abstr3act.addon.utils.PredictUtility;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.BlockUtil;
import dev.abstr3act.addon.utils.abnormally.MSTimer;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Set;

public class MaceMissLite extends CompassionModule {
    public static MaceMissLite I;
    Vec3d predictPos;
    boolean isNaked = false;
    private SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Set<EntityType<?>>> targetTypes = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("target-type")).description("Entities to attack."))
                .onlyAttackable()
                .defaultValue(new EntityType[]{EntityType.PLAYER})
                .build()
        );
    private final Setting<SortPriority> priority = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("target-priority"))
                .description("Entity sorting priority"))
                .defaultValue(SortPriority.LowestDistance))
                .build()
        );
    private final Setting<Double> moveDistance = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("move-distance"))
                .description("Max distance for a packet to move."))
                .defaultValue(20.0)
                .sliderMax(1.0)
                .sliderMin(128.0)
                .build()
        );
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("Max distance of a target."))
                .defaultValue(20.0)
                .sliderMax(1.0)
                .sliderMin(128.0)
                .build()
        );
    private final Setting<Boolean> swingHand = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("swing-hand"))
                .description("Whether swing hand when attacking."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> ignoreBots = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreBots"))
                .description("IgnoreBots."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> ignoreFriends = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("IgnoreFriends"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> predict = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("predict"))
                .description("Predict position"))
                .defaultValue(true))
                .build()
        );
    private final Setting<Integer> predictTicks = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("predict-ticks"))
                .description("Value of predict ticks."))
                .defaultValue(5))
                .sliderMin(1)
                .min(1)
                .sliderMax(20)
                .visible(this.predict::get))
                .build()
        );
    private final Setting<Keybind> forceIgnore = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder) ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder) ((meteordevelopment.meteorclient.settings.KeybindSetting.Builder) new meteordevelopment.meteorclient.settings.KeybindSetting.Builder()
                .name("ForceIgnore"))
                .description("Force miss"))
                .defaultValue(Keybind.none()))
                .build()
        );
    private final Setting<Boolean> render = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("render"))
                .description("render predict position"))
                .defaultValue(false))
                .build()
        );
    private SettingGroup sgTP = this.settings.createGroup("Miss Totem VClips");
    private final Setting<List<Double>> vClip1 = this.sgTP
        .add(
            ((DoubleListSetting.Builder) ((DoubleListSetting.Builder) new DoubleListSetting.Builder().name("v-clip")).description("Distance of clip."))
                .defaultValue(10.0)
                .range(0.0, 1000.0)
                .sliderRange(0.0, 1000.0)
                .decimalPlaces(2)
                .onSliderRelease()
                .build()
        );
    private SettingGroup sgDestory = this.settings.createGroup("Destroy Armor VClips");
    private final Setting<Integer> ignoreValue = this.sgDestory
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Ignore Armor Value"))
                .description("Armor left value"))
                .defaultValue(4))
                .sliderMax(4)
                .max(4)
                .min(0)
                .sliderMin(0)
                .build()
        );
    private final Setting<Double> dv1 = this.sgDestory
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("destory-armor-v-clip-1"))
                .description("Distance of clip."))
                .defaultValue(80.0)
                .sliderMax(200.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Double> dv2 = this.sgDestory
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("destory-armor-v-clip-2"))
                .description("Distance of clip."))
                .defaultValue(120.0)
                .sliderMax(200.0)
                .sliderMin(1.0)
                .build()
        );
    private SettingGroup sgRender = this.settings.createGroup("Render Settings");
    public final Setting<ShapeMode> shapeMode = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("shape-mode"))
                .description("How the shapes are rendered."))
                .defaultValue(ShapeMode.Both))
                .visible(this.render::get))
                .build()
        );
    private final Setting<SettingColor> sideColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("side-color"))
                .description("Color of sides"))
                .defaultValue(new SettingColor(255, 255, 255))
                .visible(this.render::get))
                .build()
        );
    private final Setting<SettingColor> lineColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("line-color"))
                .description("Color of lines"))
                .defaultValue(new SettingColor(255, 255, 255))
                .visible(this.render::get))
                .build()
        );
    private Entity target = null;
    private MSTimer attackTimer = new MSTimer();
    private Setting<Boolean> useCooldown = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("use-cooldown"))
                .description("Whether use the item cooldown to determine when attack."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> useCooldownBaseTime = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("use-cooldown-base-time"))
                .description("The base time for using cooldown."))
                .defaultValue(0.75)
                .sliderMax(1.0)
                .sliderMin(0.1)
                .visible(() -> this.useCooldown.get()))
                .build()
        );
    private final Setting<Integer> attackDelay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("attack-delay"))
                .description("attack delay."))
                .defaultValue(50))
                .sliderMax(2000)
                .sliderMin(1)
                .visible(() -> !this.useCooldown.get()))
                .build()
        );

    public MaceMissLite() {
        super(Compassion.COMPASSION, "MaceMissLite", "ezzz");
        I = this;
    }

    public static int getRemainingArmorCount(PlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        int remainingArmorCount = 0;

        for (int i = 0; i < 4; i++) {
            ItemStack armorItem = (ItemStack) inventory.armor.get(i);
            if (!armorItem.isEmpty()) {
                remainingArmorCount++;
            }
        }

        return remainingArmorCount;
    }

    public void onActivate() {
        this.target = null;
    }

    public void onDeactivate() {
        this.target = null;
    }

    private boolean isReadyToAttack() {
        return this.useCooldown.get()
            ? this.mc.player.getAttackCooldownProgress(-1.0F) >= this.useCooldownBaseTime.get()
            : this.attackTimer.hasPassTime(this.attackDelay.get());
    }

    @EventHandler
    public void onTick(Post event) {
        this.updateTarget();
        this.doAura();
    }

    private void updateTarget() {
        Entity entity = TargetUtils.get(this::_targetCheck, (SortPriority) this.priority.get());
        if (entity != null) {
            this.target = entity;
        }
    }

    private boolean _targetCheck(Entity t) {
        if (!((Set) this.targetTypes.get()).contains(t.getType())) {
            return false;
        } else if (t instanceof PlayerEntity p) {
            if (p.isCreative()) {
                return false;
            } else if (p == this.mc.player) {
                return false;
            } else {
                return this.ignoreBots.get() && AntiBot.INSTANCE.inBotList(p) ? false : Friends.get().shouldAttack(p);
            }
        } else {
            return true;
        }
    }

    private void drawBoundingBox(Render3DEvent event, Vec3d predictedPos, Entity entity) {
        if (this.color != null) {
            this.lineColor.set((SettingColor) this.sideColor.get());
            this.sideColor.set((SettingColor) this.lineColor.get());
        }

        double x = MathHelper.lerp(event.tickDelta, predictedPos.x, predictedPos.x) - predictedPos.x;
        double y = MathHelper.lerp(event.tickDelta, predictedPos.y, predictedPos.y) - predictedPos.y;
        double z = MathHelper.lerp(event.tickDelta, predictedPos.z, predictedPos.z) - predictedPos.z;
        Box box = PredictUtility.predictBox((PlayerEntity) this.target, this.predictTicks.get());
        event.renderer
            .box(
                x + box.minX,
                y + box.minY,
                z + box.minZ,
                x + box.maxX,
                y + box.maxY,
                z + box.maxZ,
                (Color) this.sideColor.get(),
                (Color) this.lineColor.get(),
                (ShapeMode) this.shapeMode.get(),
                0
            );
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (this.target != null) {
            if (this.predictPos != null) {
                if (this.render.get()) {
                    this.drawBoundingBox(event, this.predictPos, this.target);
                }
            }
        }
    }

    private void doAura() {
        if (this.isReadyToAttack() && this.target != null) {
            if (!(this.mc.player.distanceTo(this.target) > this.range.get())) {
                if (this.target != this.mc.player) {
                    if (!(this.target instanceof PlayerEntity) || !((PlayerEntity) this.target).isDead()) {
                        if (!this.target.isLiving()) {
                            this.isNaked = false;
                        } else {
                            if (!this.isAboutToBeIgnored((PlayerEntity) this.target) && !((Keybind) this.forceIgnore.get()).isPressed()) {
                                this.doDestoryArmor(this.target);
                            } else {
                                this.doTpAura(this.target);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isAboutToBeIgnored(PlayerEntity entity) {
        return getRemainingArmorCount(entity) <= this.ignoreValue.get();
    }

    private void doDestoryArmor(Entity target) {
        if (target instanceof PlayerEntity) {
            if (this.isAboutToBeIgnored((PlayerEntity) target)) {
                this.isNaked = true;
                AChatUtils.sendMsgCompassion(Text.of("§7Target armor has been successfully destroyed§f"));
            }

            Vec3d targetVec = new Vec3d(
                PredictUtility.predictPosition(target, this.predictTicks.get()).x,
                PredictUtility.predictPosition(target, this.predictTicks.get()).y,
                PredictUtility.predictPosition(target, this.predictTicks.get()).z
            );
            Vec3d playerPos = new Vec3d(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
            Vec3d vec3d2 = TPUtil.findVClipVecToMove(targetVec, playerPos, 1.8, false);
            if (vec3d2 != null) {
                int b = (int) Math.ceil(playerPos.distanceTo(vec3d2) / this.moveDistance.get());
                int c = (int) Math.ceil(targetVec.distanceTo(targetVec) / this.moveDistance.get());
                int maxPacket = Math.max(b, c);
                if (maxPacket > 20) {
                    this.mc.player.sendMessage(Text.of("TP packet limit exceeded"));
                } else {
                    for (int i = 1; i < maxPacket; i++) {
                        TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
                    }

                    double[] attackHeights = new double[]{this.dv1.get(), this.dv2.get()};

                    for (double attackVclip : attackHeights) {
                        Vec3d newMiss = Vec3d.ofBottomCenter(BlockUtil.findVclipHole(this.mc.player.getBlockPos(), attackVclip));
                        FindItemResult mace = InvUtils.find(new Item[]{Items.MACE});
                        if (InvUtils.find(new Item[]{Items.MACE}).found()) {
                            InvUtils.move().from(mace.slot()).to(this.mc.player.getInventory().selectedSlot);
                        }

                        TPUtil.doTp(playerPos, newMiss, this.moveDistance.get(), false);
                        TPUtil.sendMovePacket(vec3d2.x, vec3d2.y, vec3d2.z, false);
                        if (this.swingHand.get()) {
                            this.mc.player.swingHand(Hand.MAIN_HAND);
                        }

                        this.mc.interactionManager.attackEntity(this.mc.player, target);
                        InvUtils.move().from(this.mc.player.getInventory().selectedSlot).to(mace.slot());
                    }

                    TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
                }
            }
        }
    }

    public void doTpAura(Entity target) {
        Vec3d targetVec = new Vec3d(
            PredictUtility.predictPosition(target, this.predictTicks.get()).x,
            PredictUtility.predictPosition(target, this.predictTicks.get()).y,
            PredictUtility.predictPosition(target, this.predictTicks.get()).z
        );
        Vec3d playerPos = new Vec3d(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
        Vec3d vec3d2 = TPUtil.findVClipVecToMove(targetVec, playerPos, 1.8, false);
        if (vec3d2 != null) {
            int b = (int) Math.ceil(playerPos.distanceTo(vec3d2) / this.moveDistance.get());
            int c = (int) Math.ceil(targetVec.distanceTo(targetVec) / this.moveDistance.get());
            int maxPacket = Math.max(b, c);
            if (maxPacket > 20) {
                this.mc.player.sendMessage(Text.of("TP packet limit exceeded"));
            } else {
                for (int i = 1; i < maxPacket; i++) {
                    TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
                }

                double[] attackHeights = (this.vClip1.get())
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .mapToObj(d -> (float) d)
                    .mapToDouble(f -> f.floatValue())
                    .toArray();

                for (double attackVclip : attackHeights) {
                    Vec3d newMiss = Vec3d.ofBottomCenter(BlockUtil.findVclipHole(this.mc.player.getBlockPos(), attackVclip));
                    FindItemResult mace = InvUtils.find(new Item[]{Items.MACE});
                    if (InvUtils.find(new Item[]{Items.MACE}).found()) {
                        InvUtils.move().from(mace.slot()).to(this.mc.player.getInventory().selectedSlot);
                    }

                    TPUtil.doTp(playerPos, newMiss, this.moveDistance.get(), false);
                    TPUtil.sendMovePacket(vec3d2.x, vec3d2.y, vec3d2.z, false);
                    if (this.swingHand.get()) {
                        this.mc.player.swingHand(Hand.MAIN_HAND);
                    }

                    this.mc.interactionManager.attackEntity(this.mc.player, target);
                    InvUtils.move().from(this.mc.player.getInventory().selectedSlot).to(mace.slot());
                }

                TPUtil.sendMovePacket(playerPos.x, playerPos.y, playerPos.z, false);
            }
        }
    }
}
