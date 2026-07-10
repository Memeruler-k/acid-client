package dev.abstr3act.addon.mixin;

import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.notifications.Notification;
import dev.abstr3act.addon.notifications.NotificationsManager;
import dev.abstr3act.addon.notifications.events.ModuleToggledNotificationEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin({Modules.class})
public class ModulesMixin {
    @Inject(
        method = {"addActive"},
        at = {@At(
            value = "INVOKE",
            target = "Lmeteordevelopment/orbit/IEventBus;post(Ljava/lang/Object;)Ljava/lang/Object;"
        )},
        remap = false
    )
    private void addActive(Module module, CallbackInfo ci) {
        if (NotificationsHudElement.getInstance() != null) {
            NotificationsManager.add(
                new Notification(
                    "Module",
                    "Enabled " + Utils.nameToTitle(module.name),
                    new Color(
                        ((SettingColor) NotificationsHudElement.getInstance().enableColor.get()).r,
                        ((SettingColor) NotificationsHudElement.getInstance().enableColor.get()).g,
                        ((SettingColor) NotificationsHudElement.getInstance().enableColor.get()).b,
                        NotificationsHudElement.getInstance().globalAlpha.get()
                    ),
                    new ModuleToggledNotificationEvent(module),
                    NotificationsHudElement.icon.ENABLE
                )
            );
        }
    }

    @Inject(
        method = {"removeActive"},
        at = {@At(
            value = "INVOKE",
            target = "Lmeteordevelopment/orbit/IEventBus;post(Ljava/lang/Object;)Ljava/lang/Object;"
        )},
        remap = false
    )
    private void removeActive(Module module, CallbackInfo ci) {
        if (NotificationsHudElement.getInstance() != null) {
            NotificationsManager.add(
                new Notification(
                    "Module",
                    "Disabled " + Utils.nameToTitle(module.name),
                    new Color(
                        ((SettingColor) NotificationsHudElement.getInstance().disableColor.get()).r,
                        ((SettingColor) NotificationsHudElement.getInstance().disableColor.get()).g,
                        ((SettingColor) NotificationsHudElement.getInstance().disableColor.get()).b,
                        NotificationsHudElement.getInstance().globalAlpha.get()
                    ),
                    new ModuleToggledNotificationEvent(module),
                    NotificationsHudElement.icon.DISABLE
                )
            );
        }
    }
}
