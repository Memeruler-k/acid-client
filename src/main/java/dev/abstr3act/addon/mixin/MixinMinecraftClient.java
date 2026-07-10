package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventAttack;
import dev.abstr3act.addon.events.legacy.HandleInputEvent;
import dev.abstr3act.addon.events.legacy.WorldEvent;
import dev.abstr3act.addon.font.FontRenderers;
import dev.abstr3act.addon.hud.keyStrokes.ClickCounter;
import dev.abstr3act.addon.utils.render.WindowResizeCallback;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.MacWindowUtil;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourcePack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Mixin({MinecraftClient.class})
public abstract class MixinMinecraftClient {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    @Shadow
    @Final
    private Window window;

    @Inject(
        method = {"onResolutionChanged"},
        at = {@At("TAIL")}
    )
    private void captureResize(CallbackInfo ci) {
        ((WindowResizeCallback) WindowResizeCallback.EVENT.invoker()).onResized((MinecraftClient) (Object) this, this.window);
    }

    @Inject(
        at = {@At(
            value = "FIELD",
            target = "Lnet/minecraft/client/MinecraftClient;overlay:Lnet/minecraft/client/gui/screen/Overlay;",
            ordinal = 0
        )},
        method = {"tick()V"},
        cancellable = true
    )
    private void handleInputEvent(CallbackInfo ci) {
        if (this.player != null) {
            HandleInputEvent event = new HandleInputEvent();
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(
        method = {"setWorld"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void worldEvent(ClientWorld world, CallbackInfo ci) {
        WorldEvent event = new WorldEvent();
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"doAttack"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void doAttackHook(CallbackInfoReturnable<Boolean> cir) {
        EventAttack event = new EventAttack(null, true);
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = {"<init>"},
        at = {@At("TAIL")}
    )
    void postWindowInit(RunArgs args, CallbackInfo ci) {
        try {
            FontRenderers.modules = FontRenderers.create(15.0F, "montserrat");
            FontRenderers.categories = FontRenderers.create(20.0F, "montserrat");
            FontRenderers.sf_bold_mini = FontRenderers.create(28.0F, "montserrat");
            FontRenderers.sf_bold = FontRenderers.create(32.0F, "montserrat");
            FontRenderers.sf_medium = FontRenderers.create(32.0F, "montserrat");
            FontRenderers.monsterrat_12 = FontRenderers.create(24.0F, "montserrat");
            FontRenderers.monsterrat_2 = FontRenderers.create(4.0F, "montserrat");
            FontRenderers.monsterrat_4 = FontRenderers.create(8.0F, "montserrat");
            FontRenderers.monsterrat_8 = FontRenderers.create(16.0F, "montserrat");
            FontRenderers.monsterrat_16 = FontRenderers.create(32.0F, "montserrat");
            FontRenderers.monsterrat_32 = FontRenderers.create(64.0F, "montserrat");
            FontRenderers.exo_light_16 = FontRenderers.create(32.0F, "exo_light");
            FontRenderers.exo_light_32 = FontRenderers.create(64.0F, "exo_light");
            FontRenderers.exo_medium_16 = FontRenderers.create(32.0F, "exo_medium");
            FontRenderers.exo_medium_32 = FontRenderers.create(64.0F, "exo_medium");
            FontRenderers.exo_regular_16 = FontRenderers.create(32.0F, "exo_regular");
            FontRenderers.exo_regular_32 = FontRenderers.create(64.0F, "exo_regular");
            FontRenderers.exo_semibold_16 = FontRenderers.create(32.0F, "exo_semibold");
            FontRenderers.exo_semibold_32 = FontRenderers.create(64.0F, "exo_semibold");
            FontRenderers.geosans_light_16 = FontRenderers.create(32.0F, "geosans_light");
            FontRenderers.geosans_light_32 = FontRenderers.create(64.0F, "geosans_light");
            FontRenderers.geosans_light_oblique_16 = FontRenderers.create(32.0F, "geosans_light_oblique");
            FontRenderers.geosans_light_oblique_32 = FontRenderers.create(64.0F, "geosans_light_oblique");
            FontRenderers.user_text = FontRenderers.create(90.0F, "geosans_light");
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    @Inject(
        at = {@At("HEAD")},
        method = {"doAttack"}
    )
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        ClickCounter.registerLeftClick();
    }

    @Inject(
        at = {@At("HEAD")},
        method = {"doItemUse"}
    )
    private void onItemUse(CallbackInfo ci) {
        ClickCounter.registerRightClick();
    }

    @Redirect(
        method = {"<init>"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/Window;setIcon(Lnet/minecraft/resource/ResourcePack;Lnet/minecraft/client/util/Icons;)V"
        )
    )
    private void onChangeIcon(Window instance, ResourcePack resourcePack, Icons icons) throws IOException {
        if (GLFW.glfwGetPlatform() == 393218) {
            MacWindowUtil.setApplicationIconImage(icons.getMacIcon(resourcePack));
        } else {
            this.setWindowIcon(Compassion.class.getResourceAsStream("/icon.png"), Compassion.class.getResourceAsStream("/icon.png"));
        }
    }

    @ModifyArg(
        method = {"updateWindowTitle"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/Window;setTitle(Ljava/lang/String;)V"
        )
    )
    private String setTitle(String original) {
        return "韵律源点 Arcaea";
    }

    public void setWindowIcon(InputStream img16x16, InputStream img32x32) {
        MinecraftClient mc = MinecraftClient.getInstance();

        try {
            MemoryStack memorystack = MemoryStack.stackPush();

            try {
                Buffer buffer = GLFWImage.malloc(2, memorystack);
                List<InputStream> imgList = List.of(img16x16, img32x32);
                List<ByteBuffer> buffers = new ArrayList<>();

                for (int i = 0; i < imgList.size(); i++) {
                    NativeImage nativeImage = NativeImage.read(imgList.get(i));
                    ByteBuffer bytebuffer = MemoryUtil.memAlloc(nativeImage.getWidth() * nativeImage.getHeight() * 4);
                    bytebuffer.asIntBuffer().put(nativeImage.copyPixelsRgba());
                    buffer.position(i);
                    buffer.width(nativeImage.getWidth());
                    buffer.height(nativeImage.getHeight());
                    buffer.pixels(bytebuffer);
                    buffers.add(bytebuffer);
                }

                try {
                    if (GLFW.glfwGetPlatform() != 393219) {
                        GLFW.glfwSetWindowIcon(mc.getWindow().getHandle(), buffer);
                    }
                } catch (Exception var12) {
                    var12.printStackTrace();
                }

                buffers.forEach(MemoryUtil::memFree);
            } catch (Throwable var13) {
                if (memorystack != null) {
                    try {
                        memorystack.close();
                    } catch (Throwable var11) {
                        var13.addSuppressed(var11);
                    }
                }

                throw var13;
            }

            if (memorystack != null) {
                memorystack.close();
            }
        } catch (IOException var14) {
        }
    }
}
