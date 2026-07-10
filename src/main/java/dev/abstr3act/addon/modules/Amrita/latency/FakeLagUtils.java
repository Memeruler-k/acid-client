package dev.abstr3act.addon.modules.Amrita.latency;

import dev.abstr3act.addon.events.EventPlayerUpdate;
import dev.abstr3act.addon.events.legacy.WorldEvent;
import dev.abstr3act.addon.utils.fragment.blinkUtils.PacketUtil;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class FakeLagUtils {
    public final Set<DelayData> packetQueue = Collections.synchronizedSet(new LinkedHashSet<>());
    final Set<PositionData> positions = new LinkedHashSet<>();
    private Entity target;
    private TrackedPosition position;

    public FakeLagUtils() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public boolean isLagging() {
        return !this.packetQueue.isEmpty() || !this.positions.isEmpty();
    }

    LagResult shouldLag(Packet<?> packet) {
        if (!MeteorClient.mc.isInSingleplayer()) {
            return null;
        } else {
            return ((FakeLag) Modules.get().get(FakeLag.class)).shouldLag(packet) ? LagResult.QUEUE : null;
        }
    }

    @EventHandler
    public void repeatable(EventPlayerUpdate event) {
        if (MeteorClient.mc.isInSingleplayer()) {
            if (this.shouldLag(null) == null) {
                this.flush();
            }
        }
    }

    @EventHandler
    public void handleWorldChangeEvent(WorldEvent event) {
        System.out.println("EventWorld");
        this.clear();
    }

    public void flush() {
        synchronized (this.packetQueue) {
            this.packetQueue.removeIf(data -> {
                PacketUtil.sendPacket(data.getPacket());
                return true;
            });
        }

        synchronized (this.positions) {
            this.positions.clear();
        }
    }

    public void flush(int count) {
        synchronized (this.packetQueue) {
            int counter = 0;

            for (DelayData data : this.packetQueue) {
                Packet<?> packet = data.getPacket();
                if (packet instanceof PlayerMoveC2SPacket && ((PlayerMoveC2SPacket) packet).changesPosition()) {
                    counter++;
                }

                PacketUtil.sendPacket(packet);
                this.packetQueue.remove(data);
                if (counter >= count) {
                    break;
                }
            }
        }

        synchronized (this.positions) {
            this.positions.removeAll(this.positions.stream().limit(count).collect(Collectors.toSet()));
        }
    }

    public void cancel() {
        PositionData firstPosition = this.firstPosition();
        if (firstPosition != null) {
            MeteorClient.mc.player.setPosition(firstPosition.getVec());
            MeteorClient.mc.player.setVelocity(firstPosition.getVelocity());
            synchronized (this.packetQueue) {
                this.packetQueue.removeIf(data -> {
                    if (data.getPacket() instanceof PlayerMoveC2SPacket) {
                        return true;
                    } else {
                        PacketUtil.sendPacket(data.packet);
                        return true;
                    }
                });
            }

            synchronized (this.positions) {
                this.positions.clear();
            }
        }
    }

    public void clear() {
        synchronized (this.packetQueue) {
            this.packetQueue.clear();
        }

        synchronized (this.positions) {
            this.positions.clear();
        }
    }

    public boolean isAboveTime(long delay) {
        synchronized (this.packetQueue) {
            long entryPacketTime = this.packetQueue.stream().findFirst().map(DelayData::getDelay).orElse(0L);
            return System.currentTimeMillis() - entryPacketTime >= delay;
        }
    }

    public DelayData entryPacket() {
        synchronized (this.packetQueue) {
            return this.packetQueue.stream().findFirst().orElse(null);
        }
    }

    public PositionData firstPosition() {
        synchronized (this.positions) {
            return this.positions.stream().findFirst().orElse(null);
        }
    }

    public <T extends Packet<?>> void rewrite(Class<T> clazz, Consumer<T> action) {
        synchronized (this.packetQueue) {
            this.packetQueue.stream().filter(clazz::isInstance).map(clazz::cast).forEach(action);
        }
    }

    public <T extends Packet<?>> void rewriteAndFlush(Class<T> clazz, Consumer<T> action) {
        this.rewrite(clazz, action);
        this.flush();
    }

    public EvadingPacket findAvoidingArrowPosition() {
        int packetIndex = 0;
        Vec3d lastPosition = null;
        Vec3d bestPacketPosition = null;
        Integer bestPacketIdx = null;
        int bestTimeToImpact = 0;

        for (PositionData data : this.positions) {
            packetIndex++;
            Vec3d packetPosition = data.getVec();
            if (lastPosition == null || !(lastPosition.squaredDistanceTo(packetPosition) < 0.81)) {
                lastPosition = packetPosition;
            }
        }

        return bestPacketIdx != null && bestPacketPosition.squaredDistanceTo(lastPosition) > 0.9
            ? new EvadingPacket(bestPacketIdx, bestTimeToImpact)
            : null;
    }

    public static enum LagResult {
        QUEUE,
        PASS;
    }

    public static class DelayData {
        private final Packet<?> packet;
        private final long delay;

        public DelayData(Packet<?> packet, long delay) {
            this.packet = packet;
            this.delay = delay;
        }

        public Packet<?> getPacket() {
            return this.packet;
        }

        public long getDelay() {
            return this.delay;
        }
    }

    public static class EvadingPacket {
        private final int idx;
        private final Integer ticksToImpact;

        public EvadingPacket(int idx, Integer ticksToImpact) {
            this.idx = idx;
            this.ticksToImpact = ticksToImpact;
        }

        public int getIdx() {
            return this.idx;
        }

        public Integer getTicksToImpact() {
            return this.ticksToImpact;
        }
    }

    public static class PositionData {
        private final Vec3d vec;
        private final Vec3d velocity;
        private final long delay;

        public PositionData(Vec3d vec, Vec3d velocity, long delay) {
            this.vec = vec;
            this.velocity = velocity;
            this.delay = delay;
        }

        public Vec3d getVec() {
            return this.vec;
        }

        public Vec3d getVelocity() {
            return this.velocity;
        }

        public long getDelay() {
            return this.delay;
        }
    }
}
