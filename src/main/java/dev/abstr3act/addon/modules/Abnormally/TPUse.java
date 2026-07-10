package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import dev.abstr3act.addon.utils.TargetUtils;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import dev.abstr3act.addon.utils.abnormally.MSTimer;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import dev.abstr3act.addon.utils.luna.EntityMovementUtil;
import dev.abstr3act.addon.utils.luna.MathUtils;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class TPUse extends AbnormallyModule {
    private final MSTimer attackTimer = new MSTimer();
    public SettingGroup sgGeneral = this.settings.getDefaultGroup();
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
                .sliderMax(128.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Double> range = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("range"))
                .description("Max distance of a target."))
                .defaultValue(20.0)
                .sliderMax(128.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Double> y_prev = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("y prev"))
                .description("Y prev"))
                .defaultValue(7.0)
                .sliderMax(128.0)
                .sliderMin(1.0)
                .build()
        );
    private final Setting<Double> prev = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("prev"))
                .description("Prev"))
                .defaultValue(0.0)
                .sliderMax(5.0)
                .sliderMin(0.1)
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
    private final Setting<Boolean> useCooldown = this.sgGeneral
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
                .visible(this.useCooldown::get))
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
    private final Setting<ClickMode> mode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("click-mode"))
                .description("attack delay."))
                .defaultValue(ClickMode.LeftClick))
                .build()
        );
    private final Setting<Integer> leftClickAmount = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("LeftClick Amount"))
                .description("."))
                .min(1)
                .sliderRange(1, 10)
                .visible(
                    () -> ((ClickMode) this.mode.get()).equals(ClickMode.LeftClick) || ((ClickMode) this.mode.get()).equals(ClickMode.Both)
                ))
                .build()
        );
    private final Setting<Integer> rightClickAmount = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("RightClick Amount"))
                .description("."))
                .min(1)
                .sliderRange(1, 10)
                .visible(
                    () -> ((ClickMode) this.mode.get()).equals(ClickMode.RightClick) || ((ClickMode) this.mode.get()).equals(ClickMode.Both)
                ))
                .build()
        );
    private final Setting<Boolean> autoSwitch = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Auto Switch"))
                .description("."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> puttyMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("Putty Switch Mode"))
                .description("."))
                .defaultValue(false))
                .visible(this.autoSwitch::get))
                .build()
        );
    private final Setting<SearchMode> itemSearchMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Search Mode"))
                .description("."))
                .defaultValue(SearchMode.ByItem))
                .build()
        );
    private final Setting<PatchMode> itemPatchMode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Name Patch Mode"))
                .description("."))
                .defaultValue(PatchMode.Contains))
                .visible(() -> ((SearchMode) this.itemSearchMode.get()).equals(SearchMode.ByName) && this.autoSwitch.get()))
                .build()
        );
    private final Setting<String> name = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("ItemName"))
                .description("Item name"))
                .defaultValue(""))
                .visible(() -> ((SearchMode) this.itemSearchMode.get()).equals(SearchMode.ByName) && this.autoSwitch.get()))
                .build()
        );
    private final Setting<Item> items = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) ((meteordevelopment.meteorclient.settings.ItemSetting.Builder) new meteordevelopment.meteorclient.settings.ItemSetting.Builder()
                .name("Item"))
                .description("."))
                .defaultValue(Items.AIR))
                .visible(() -> ((SearchMode) this.itemSearchMode.get()).equals(SearchMode.ByItem) && this.autoSwitch.get()))
                .build()
        );
    private Entity target = null;

    public TPUse() {
        super(Compassion.ABNORMALLY, "TPUse", "Use custom items to destroy your enemy in a super large range");
    }

    public static int patchItem(PlayerEntity player, String itemName, boolean exactMatch) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = (ItemStack) inventory.main.get(i);
            if (!stack.isEmpty()) {
                String currentItemName = stack.getName().getString();
                if (exactMatch && currentItemName.equals(itemName) || !exactMatch && currentItemName.contains(itemName)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public void onActivate() {
        this.target = null;
    }

    public void onDeactivate() {
        this.target = null;
    }

    private boolean isReadyToAttack() {
        if (this.useCooldown.get()) {
            assert this.mc.player != null;

            return this.mc.player.getAttackCooldownProgress(-1.0F) >= this.useCooldownBaseTime.get();
        } else {
            return this.attackTimer.hasPassTime(this.attackDelay.get());
        }
    }

    @EventHandler
    public void onTick(Pre event) {
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
            } else {
                return p == this.mc.player ? false : Friends.get().shouldAttack(p);
            }
        } else {
            return true;
        }
    }

    @Override
    protected void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        if (this.mc.getNetworkHandler() != null && this.mc.world != null) {
            PendingUpdateManager pendingUpdateManager = this.mc.world.getPendingUpdateManager().incrementSequence();

            try {
                int i = pendingUpdateManager.getSequence();
                this.mc.getNetworkHandler().sendPacket(packetCreator.predict(i));
            } catch (Throwable var6) {
                if (pendingUpdateManager != null) {
                    try {
                        pendingUpdateManager.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            if (pendingUpdateManager != null) {
                pendingUpdateManager.close();
            }
        }
    }

    private void switchItem() {
        if (((SearchMode) this.itemSearchMode.get()).equals(SearchMode.ByItem)) {
            InvUtils.swap(InvUtils.find(new Item[]{(Item) this.items.get()}).slot(), !this.puttyMode.get());
        } else {
            InvUtils.swap(
                patchItem(this.mc.player, (String) this.name.get(), ((PatchMode) this.itemPatchMode.get()).equals(PatchMode.Match)),
                !this.puttyMode.get()
            );
        }
    }

    private void doAura() {
        if (this.isReadyToAttack() && this.target != null) {
            assert this.mc.player != null;

            if (!(this.mc.player.distanceTo(this.target) > this.range.get())) {
                Vec3d targetVec = EntityMovementUtil.getPrevPos(this.target, this.prev.get());
                this.attackTimer.reset();
                Vec3d playerPos = new Vec3d(this.mc.player.prevX, this.mc.player.prevY, this.mc.player.prevZ);
                Vec3d vec3d = TPUtil.findVClipVecToMove(playerPos, targetVec, 1.8, false);
                if (vec3d != null) {
                    Vec3d vec3d2 = TPUtil.findVClipVecToMove(targetVec, playerPos, 1.8, false);
                    if (vec3d2 != null) {
                        int a = (int) Math.ceil(playerPos.distanceTo(vec3d) / this.moveDistance.get());
                        int b = (int) Math.ceil(playerPos.distanceTo(vec3d2) / this.moveDistance.get());
                        int c = (int) Math.ceil(targetVec.distanceTo(targetVec) / this.moveDistance.get());
                        int maxPacket = Math.max(a, Math.max(b, c));
                        if (maxPacket > 20) {
                            AChatUtils.sendMsg(Text.of("Tp packet limit exceeded"));
                        } else {
                            TPUtil.doTp(playerPos, vec3d, this.moveDistance.get(), false);
                            TPUtil.doTp(vec3d, targetVec.add(0.0, this.y_prev.get(), 0.0), this.moveDistance.get(), false);
                            if (this.swingHand.get()) {
                                this.mc.player.swingHand(Hand.MAIN_HAND);
                            }

                            this.sendPacket(new LookAndOnGround(MathUtils.getTargetToPlayerYaw(this.mc.player, this.target), 90.0F, false));
                            this.switchItem();
                            switch ((ClickMode) this.mode.get()) {
                                case LeftClick:
                                    for (int i = 0; i < this.leftClickAmount.get(); i++) {
                                        this.sendSequencedPacket(id -> new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.target.getBlockPos(), Direction.DOWN));
                                    }
                                    break;
                                case RightClick:
                                    for (int i = 0; i < this.rightClickAmount.get(); i++) {
                                        this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()));
                                        PlayerInteractEntityC2SPacket.attack(this.target, this.mc.player.isSneaking());
                                        this.mc.player.interact(this.target, Hand.MAIN_HAND);
                                    }
                                    break;
                                case Both:
                                    for (int i = 0; i < this.rightClickAmount.get(); i++) {
                                        this.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, this.mc.player.getYaw(), this.mc.player.getPitch()));
                                        PlayerInteractEntityC2SPacket.attack(this.target, this.mc.player.isSneaking());
                                        this.mc.player.interact(this.target, Hand.MAIN_HAND);
                                    }

                                    for (int i = 0; i < this.leftClickAmount.get(); i++) {
                                        this.sendSequencedPacket(id -> new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, this.target.getBlockPos(), Direction.DOWN));
                                    }
                            }

                            InvUtils.swapBack();
                            TPUtil.doTp(targetVec.add(0.0, this.y_prev.get(), 0.0), playerPos, this.moveDistance.get(), false);
                        }
                    }
                }
            }
        }
    }

    static enum ClickMode {
        LeftClick,
        RightClick,
        Both;
    }

    static enum PatchMode {
        Contains,
        Match;
    }

    static enum SearchMode {
        ByName,
        ByItem;
    }
}
