package dev.abstr3act.addon.modules.Amrita.spider.modes;

import dev.abstr3act.addon.modules.Amrita.spider.SpiderMode;
import dev.abstr3act.addon.modules.Amrita.spider.SpiderModes;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Sent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import net.minecraft.block.SlabBlock;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Vulcan extends SpiderMode {
    private int tick = 0;
    private boolean modify = false;
    private boolean start = false;
    private double startY = 0.0;
    private double lastY = 0.0;
    private double coff = 3.26E-11;
    private boolean block = false;
    private TypeStarted typeStarted = TypeStarted.Air;

    public Vulcan() {
        super(SpiderModes.Vulcan);
    }

    @Override
    public void onActivate() {
        this.tick = 0;
        this.start = false;
        this.modify = false;

        assert this.mc.player != null;

        this.startY = this.mc.player.getPos().y;
    }

    private boolean YGround(double height, double min, double max) {
        String yString = String.valueOf(height);
        yString = yString.substring(yString.indexOf("."));
        double y = Double.parseDouble(yString);
        return y >= min && y <= max;
    }

    private double RGround(double height) {
        String yString = String.valueOf(height);
        yString = yString.substring(yString.indexOf("."));
        return Double.parseDouble(yString);
    }

    @Override
    public void onSendPacket(Send event) {
        this.work(event.packet);
    }

    @Override
    public void onSentPacket(Sent event) {
        this.work(event.packet);
    }

    private void work(Packet<?> packet) {
        if (this.modify) {
            if (packet instanceof PlayerMoveC2SPacket move) {
                assert this.mc.player != null;

                double y = this.mc.player.getY();
                y = move.getY(y);
                if (this.YGround(y, this.RGround(this.startY) - 0.1, this.RGround(this.startY) + 0.1)) {
                    ((PlayerMoveC2SPacketAccessor) packet).setOnGround(true);
                }

                if (this.mc.player.isOnGround() && this.block) {
                    this.block = false;
                    this.startY = this.mc.player.getPos().y;
                    this.start = false;
                }
            }
        } else {
            assert this.mc.player != null;

            if (this.mc.player.isOnGround() && this.block) {
                this.block = false;
                this.startY = this.mc.player.getPos().y;
                this.start = false;
            }
        }
    }

    @Override
    public void onTickEventPre(Pre event) {
        if (this.modify) {
            ClientPlayerEntity player = this.mc.player;

            assert player != null;

            double y = player.getPos().y;
            if (this.lastY == y && this.tick > 1) {
                this.block = true;
            } else {
                this.lastY = y;
            }
        }
    }

    private TypeStarted getType(double startY) {
        TypeStarted temp = TypeStarted.Air;
        double y = this.RGround(startY);

        assert this.mc.player != null;

        if (this.mc.player.isOnGround()) {
            temp = TypeStarted.Block;

            assert this.mc.world != null;

            if (this.mc.world.getBlockState(this.mc.player.getBlockPos()).getBlock() instanceof SlabBlock) {
                temp = TypeStarted.Slab;
            }
        }

        return temp;
    }

    @Override
    public void onTickEventPost(Post event) {
        ClientPlayerEntity player = this.mc.player;

        assert player != null;

        Vec3d pl_velocity = player.getVelocity();
        Vec3d pos = player.getPos();
        ClientPlayNetworkHandler h = this.mc.getNetworkHandler();
        this.modify = player.horizontalCollision;
        if (this.mc.player.isOnGround()) {
            this.block = false;
            this.startY = this.mc.player.getPos().y;
            this.start = false;
            this.typeStarted = this.getType(this.startY);
        }

        if (player.horizontalCollision) {
            if (!this.start) {
                this.start = true;
                this.startY = this.mc.player.getPos().y;
                this.lastY = this.mc.player.getY();
            }

            if (!this.block) {
                if (this.tick == 0) {
                    this.mc.player.setVelocity(pl_velocity.x, 0.41999998688698, pl_velocity.z);
                    this.tick = 1;
                } else if (this.tick == 1) {
                    this.mc.player.setVelocity(pl_velocity.x, 0.33319999363698 - this.coff, pl_velocity.z);
                    this.tick = 2;
                } else if (this.tick == 2) {
                    this.mc.player.setVelocity(pl_velocity.x, 0.24813599862698 - this.coff, pl_velocity.z);
                    this.tick = 0;
                }

                switch (this.typeStarted) {
                    case Block:
                        if (this.mc.player.getPos().y >= this.startY + 2.0) {
                            this.block = true;
                        }
                        break;
                    case Slab:
                        if (this.mc.player.getPos().y >= this.startY + 2.5) {
                            this.block = true;
                        }
                        break;
                    case Air:
                        if (this.mc.player.getPos().y >= this.startY + 1.5) {
                            this.block = true;
                        }
                }
            }
        } else {
            this.modify = false;
            this.tick = 0;
        }
    }

    private static enum TypeStarted {
        Block,
        Slab,
        Air;
    }
}
