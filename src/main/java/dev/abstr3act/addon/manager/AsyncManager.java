package dev.abstr3act.addon.manager;

import com.google.common.collect.Lists;
import dev.abstr3act.addon.events.EventSync;
import dev.abstr3act.addon.module.BaseModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncManager {
    public static ExecutorService executor = Executors.newCachedThreadPool();
    public final AtomicBoolean ticking = new AtomicBoolean(false);
    private ClientService clientService = new ClientService();
    private volatile Iterable<Entity> threadSafeEntityList = Collections.emptyList();
    private volatile List<AbstractClientPlayerEntity> threadSafePlayersList = Collections.emptyList();

    public AsyncManager() {
        this.clientService.setName("ThunderHack-AsyncProcessor");
        this.clientService.setDaemon(true);
        this.clientService.start();
    }

    public static void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception var2) {
        }
    }

    @EventHandler(
        priority = -200
    )
    public void onPostTick(Post e) {
        if (MeteorClient.mc.world != null) {
            this.threadSafeEntityList = Lists.newArrayList(MeteorClient.mc.world.getEntities());
            this.threadSafePlayersList = Lists.newArrayList(MeteorClient.mc.world.getPlayers());
            this.ticking.set(false);
        }
    }

    public Iterable<Entity> getAsyncEntities() {
        return this.threadSafeEntityList;
    }

    public List<AbstractClientPlayerEntity> getAsyncPlayers() {
        return this.threadSafePlayersList;
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (!this.clientService.isAlive()) {
            this.clientService = new ClientService();
            this.clientService.setName("ThunderHack-AsyncProcessor");
            this.clientService.setDaemon(true);
            this.clientService.start();
        }
    }

    @EventHandler(
        priority = 200
    )
    public void onTick(Post e) {
        this.ticking.set(true);
    }

    public void run(Runnable runnable, long delay) {
        executor.execute(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException var4) {
                var4.printStackTrace();
            }

            runnable.run();
        });
    }

    public void run(Runnable r) {
        executor.execute(r);
    }

    public static class ClientService extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!BaseModule.fullNullCheck()) {
                        Thread.sleep(100L);
                    } else {
                        Thread.yield();
                    }
                } catch (Exception var2) {
                    var2.printStackTrace();
                    AChatUtils.sendMsgCompassion(Text.of(var2.getMessage()));
                }
            }
        }
    }
}
