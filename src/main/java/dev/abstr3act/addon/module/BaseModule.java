package dev.abstr3act.addon.module;

import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import dev.abstr3act.addon.utils.auth.CrashUtils;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BaseModule extends Module {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public BaseModule(Category category, String name, String description) {
        super(category, name, description);
    }

    public static boolean fullNullCheck() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc == null && mc.world == null && mc.player == null;
    }

    public static CompletableFuture<Void> wait(@Nonnull Runnable task, @Nonnull Long delay) {
        if (delay < 0L) {
            throw new IllegalArgumentException();
        } else {
            CompletableFuture<Void> future = new CompletableFuture<>();
            scheduler.schedule(() -> {
                try {
                    task.run();
                    future.complete(null);
                } catch (Exception var3) {
                    future.completeExceptionally(var3);
                }
            }, delay, TimeUnit.MILLISECONDS);
            return future;
        }
    }

    public static CompletableFuture<Void> wait(@Nonnull Runnable task, @Nonnull Long delay, @Nonnull TimeUnit timeUnit) {
        if (delay < 0L) {
            throw new IllegalArgumentException();
        } else {
            CompletableFuture<Void> future = new CompletableFuture<>();
            scheduler.schedule(() -> {
                try {
                    task.run();
                    future.complete(null);
                } catch (Exception var3x) {
                    future.completeExceptionally(var3x);
                }
            }, delay, timeUnit);
            return future;
        }
    }

    public void clickSlot(int id) {
        if (id != -1 && this.mc.interactionManager != null && this.mc.player != null) {
            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, id, 0, SlotActionType.PICKUP, this.mc.player);
        }
    }

    public float getPow2Value(Object value) {
        if (value instanceof Float) {
            return ((Float) value) * ((Float) value);
        }
        if (value instanceof Integer) {
            return (Integer) value * (Integer) value;
        }
        return 0.0f;
    }


    public void clickSlot(int id, SlotActionType type) {
        if (id != -1 && this.mc.interactionManager != null && this.mc.player != null) {
            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, id, 0, type, this.mc.player);
        }
    }

    public void clickSlot(int id, int button, SlotActionType type) {
        if (id != -1 && this.mc.interactionManager != null && this.mc.player != null) {
            this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, id, button, type, this.mc.player);
        }
    }

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

    public boolean isKeyPressed(int button) {
        if (button == -1 || this.mc == null) {
            return false;
        } else {
            return button < 10 ? false : InputUtil.isKeyPressed(this.mc.getWindow().getHandle(), button);
        }
    }

    public void sendPacket(Packet<?> packet) {
        if (this.mc.getNetworkHandler() != null) {
            this.mc.getNetworkHandler().sendPacket(packet);
        }
    }

    public void toggle(String reason) {
        NotificationsManager.add(new Notification(this.name, reason));
        this.toggle();
    }

    public void sendMessage(Text text, int id) {
        ((IChatHud) this.mc.inGameHud.getChatHud()).meteor$add(text, id);
    }

    public void sendSequenced(SequencedPacketCreator packetCreator) {
        if (this.mc.interactionManager != null && this.mc.world != null && this.mc.getNetworkHandler() != null) {
            PendingUpdateManager sequence = new PendingUpdateManager().incrementSequence();
            Packet<?> packet = packetCreator.predict(sequence.getSequence());
            this.mc.getNetworkHandler().sendPacket(packet);
            sequence.close();
        }
    }

    public void crash(method method) {
        switch (method) {
            case JVM:
                CrashUtils.jvmCrash();
                break;
            case Unsafe:
                CrashUtils.unsafe();
                break;
            case SystemExit:
                CrashUtils.exit();
        }
    }

    public void addError(String title, String description) {
        NotificationsManager.add(new Notification(title, description, null, NotificationsHudElement.icon.DISABLE));
    }

    public void addQuestion(String title, String description) {
        NotificationsManager.add(new Notification(title, description, null, NotificationsHudElement.icon.QUESTION));
    }

    public void addWarning(String title, String description) {
        NotificationsManager.add(new Notification(title, description, null, NotificationsHudElement.icon.WARNING));
    }

    public void addSuccess(String title, String description) {
        NotificationsManager.add(new Notification(title, description, null, NotificationsHudElement.icon.ENABLE));
    }

    public void addInfo(String title, String description) {
        NotificationsManager.add(new Notification(title, description, null, NotificationsHudElement.icon.INFO));
    }

    public static enum method {
        JVM,
        Unsafe,
        SystemExit;
    }
}
