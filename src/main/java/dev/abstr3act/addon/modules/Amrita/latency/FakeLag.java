package dev.abstr3act.addon.modules.Amrita.latency;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventPacket;
import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.events.TransferOrigin;
import dev.abstr3act.addon.events.legacy.WorldEvent;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.utils.TargetUtils;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class FakeLag extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> range = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Range")).description(".")).defaultValue(3.5).min(0.0).sliderRange(0.0, 6.0).build());
    private final Setting<Integer> delay = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("Delay"))
                .description("."))
                .defaultValue(450))
                .min(0)
                .sliderRange(0, 2000)
                .build()
        );
    private final Setting<Mode> mode = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("Mode"))
                .description("."))
                .defaultValue(Mode.Adaptive))
                .build()
        );
    private final Setting<Boolean> evadeArrows = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("EvadeArrows"))
                .description("."))
                .defaultValue(true))
                .build()
        );
    public FakeLagUtils fakeLagUtils = new FakeLagUtils();
    private int nextDelay = new Random().nextInt(300) + 300;

    public FakeLag() {
        super(Compassion.AMRITA, "FakeLag", "");
    }

    public static boolean isConsumable(ItemStack itemStack) {
        return isFood(itemStack) || itemStack.getItem() == Items.POTION || itemStack.getItem() == Items.MILK_BUCKET;
    }

    public static boolean isFood(ItemStack itemStack) {
        return getFoodComponent(itemStack) != null && itemStack.getUseAction() == UseAction.EAT;
    }

    public static FoodComponent getFoodComponent(ItemStack itemStack) {
        return (FoodComponent) itemStack.get(DataComponentTypes.FOOD);
    }

    public static List<Entity> getEntitiesBoxInRange(ClientWorld world, Vec3d midPos, double range, Predicate<Entity> predicate) {
        double rangeSquared = range * range;
        List<Entity> entities = new ArrayList<>();
        Box searchBox = new Box(midPos.add(-range, -range, -range), midPos.add(range, range, range));
        world.getEntities().forEach(entity -> {
            if (entity.getBoundingBox().intersects(searchBox) && predicate.test(entity) && entity.squaredDistanceTo(midPos) <= rangeSquared) {
                entities.add(entity);
            }
        });
        return entities;
    }

    public static List<Entity> getEntitiesBoxInRange(ClientWorld world, Vec3d midPos, double range) {
        return getEntitiesBoxInRange(world, midPos, range, entity -> true);
    }

    public String getInfoString() {
        return ((Mode) this.mode.get()).name();
    }

    @EventHandler
    public void handlePacketEvent(EventPacket event) {
        if (!event.isCancelled() && this.mc.isInSingleplayer()) {
            Packet<?> packet = event.getPacket();
            FakeLagUtils.LagResult lagResult = this.fakeLagUtils.shouldLag(packet);
            if (lagResult == null) {
                this.fakeLagUtils.flush();
            } else if (lagResult != FakeLagUtils.LagResult.PASS) {
                if (!(packet instanceof HandshakeC2SPacket) && !(packet instanceof QueryRequestC2SPacket) && !(packet instanceof QueryPingC2SPacket)) {
                    if (!(packet instanceof ChatMessageC2SPacket) && !(packet instanceof GameMessageS2CPacket) && !(packet instanceof CommandExecutionC2SPacket)) {
                        if (!(packet instanceof PlayerPositionLookS2CPacket) && !(packet instanceof DisconnectS2CPacket)) {
                            if (!(packet instanceof PlaySoundS2CPacket) || ((PlaySoundS2CPacket) packet).getSound().value() != SoundEvents.ENTITY_PLAYER_HURT) {
                                if (packet instanceof HealthUpdateS2CPacket && ((HealthUpdateS2CPacket) packet).getHealth() <= 0.0F) {
                                    this.fakeLagUtils.flush();
                                } else {
                                    if (event.getOrigin() == TransferOrigin.SEND) {
                                        event.cancel();
                                        synchronized (this.fakeLagUtils.packetQueue) {
                                            this.fakeLagUtils.packetQueue.add(new FakeLagUtils.DelayData(packet, System.currentTimeMillis()));
                                        }

                                        if (packet instanceof PlayerMoveC2SPacket && ((PlayerMoveC2SPacket) packet).changesPosition()) {
                                            synchronized (this.fakeLagUtils.positions) {
                                                this.fakeLagUtils
                                                    .positions
                                                    .add(
                                                        new FakeLagUtils.PositionData(
                                                            new Vec3d(
                                                                ((PlayerMoveC2SPacket) packet).getX(this.mc.player.getX()),
                                                                ((PlayerMoveC2SPacket) packet).getY(this.mc.player.getY()),
                                                                ((PlayerMoveC2SPacket) packet).getZ(this.mc.player.getZ())
                                                            ),
                                                            this.mc.player.getVelocity(),
                                                            System.currentTimeMillis()
                                                        )
                                                    );
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            this.fakeLagUtils.flush();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void handleWorldChangeEvent(WorldEvent event) {
        this.fakeLagUtils.clear();
    }

    @EventHandler
    public void run(EventPlayerUpdate event) {
        if (this.evadeArrows.get()) {
            FakeLagUtils.PositionData playerPosition = this.fakeLagUtils.firstPosition();
            if (playerPosition == null) {
                return;
            }

            FakeLagUtils.EvadingPacket evadingPacket = this.fakeLagUtils.findAvoidingArrowPosition();
            if (evadingPacket == null) {
                this.fakeLagUtils.flush();
            } else if (evadingPacket.getTicksToImpact() != null) {
                this.fakeLagUtils.flush(evadingPacket.getIdx() + 1);
            } else {
                this.fakeLagUtils.flush(evadingPacket.getIdx() + 1);
            }
        }
    }

    public boolean shouldLag(Packet<?> packet) {
        if (!this.isActive() || !this.mc.isInSingleplayer() || this.mc.player.isDead() || this.mc.player.isTouchingWater() || this.mc.currentScreen != null) {
            return false;
        } else if (this.fakeLagUtils.isAboveTime(this.nextDelay)) {
            this.nextDelay = new Random().nextInt(300) + 300;
            return false;
        } else if (!(packet instanceof PlayerPositionLookS2CPacket)
            && !(packet instanceof PlayerInteractBlockC2SPacket)
            && !(packet instanceof PlayerActionC2SPacket)
            && !(packet instanceof UpdateSignC2SPacket)
            && !(packet instanceof PlayerInteractEntityC2SPacket)
            && !(packet instanceof ResourcePackStatusC2SPacket)) {
            if (packet instanceof EntityVelocityUpdateS2CPacket velocityPacket
                && velocityPacket.getEntityId() == this.mc.player.getId()
                && (velocityPacket.getVelocityX() != 0.0 || velocityPacket.getVelocityY() != 0.0 || velocityPacket.getVelocityZ() != 0.0)) {
                return false;
            } else if (packet instanceof ExplosionS2CPacket explosionPacket
                && (explosionPacket.getPlayerVelocityX() != 0.0F || explosionPacket.getPlayerVelocityY() != 0.0F || explosionPacket.getPlayerVelocityZ() != 0.0F)) {
                return false;
            } else if (packet instanceof HealthUpdateS2CPacket) {
                return false;
            } else if (this.mc.player.isUsingItem() && isConsumable(this.mc.player.getActiveItem())) {
                return false;
            } else if (this.mode.get() == Mode.Adaptive) {
                return true;
            } else if (this.mode.get() != Mode.Dynamic) {
                return false;
            } else if (TargetUtils.getPlayerTarget((this.range.get()).floatValue(), SortPriority.LowestDistance) == null) {
                return false;
            } else {
                FakeLagUtils.PositionData firstPosition = this.fakeLagUtils.firstPosition();
                if (firstPosition == null) {
                    return true;
                } else {
                    Box playerBox = this.mc.player.getDimensions(this.mc.player.getPose()).getBoxAt(firstPosition.getVec());
                    List<Entity> entities = getEntitiesBoxInRange(
                        this.mc.world, firstPosition.getVec(), (this.range.get()).floatValue(), entity -> entity != this.mc.player
                    );
                    if (entities.isEmpty()) {
                        return false;
                    } else {
                        boolean intersects = entities.stream().anyMatch(entity -> entity.getBoundingBox().intersects(playerBox));
                        double serverDistance = entities.stream().mapToDouble(entity -> entity.getPos().distanceTo(firstPosition.getVec())).min().orElse(Double.MAX_VALUE);
                        double clientDistance = entities.stream().mapToDouble(entity -> entity.getPos().distanceTo(this.mc.player.getPos())).min().orElse(Double.MAX_VALUE);
                        return serverDistance >= clientDistance && !intersects;
                    }
                }
            }
        } else {
            return false;
        }
    }

    private static enum Mode {
        Adaptive,
        Dynamic;
    }
}
