package dev.abstr3act.addon.notifications;

import com.google.common.collect.Lists;
import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.notifications.events.ModuleToggledNotificationEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class NotificationsManager {
    private static final Queue<Notification> notifications = new LinkedBlockingQueue<>();
    private static final List<Notification> notificationDummies = Collections.unmodifiableList(
        Lists.newArrayList(
            new Notification[]{
                new Notification("This is the first one!", "Hey, this is just a example!", Color.RED),
                new Notification("§cRed §rWhite &aGreen &rWhite", "Another example!", Color.WHITE),
                new Notification("3"),
                new Notification("no", "some random description", Color.PINK),
                new Notification("default color here"),
                new Notification("another test", "This is a test!", Color.RED),
                new Notification("luna is good client", new Color(10093567)),
                new Notification("_SIM_ good coder haha no", Color.GREEN),
                new Notification(
                    "Notification with a really really long title", "And description that is also really long and hopefully doesn't break everything....", Color.RED
                )
            }
        )
    );

    static {
        notificationDummies.forEach(n -> n.startTime = 0L);
    }

    public static void add(Notification notification) {
        if (notifications.contains(notification)) {
            notifications.stream().filter(n -> n.equals(notification)).findFirst().ifPresent(n -> notifications.add(notification));
        } else {
            notifications.add(notification);
        }
    }

    public static List<Notification> getNotifications(boolean isInEditor) {
        if (isInEditor) {
            return notificationDummies.stream()
                .limit((NotificationsHudElement.getInstance().dummyNotificationsDisplayCount.get()).intValue())
                .collect(Collectors.toList());
        } else {
            List<Notification> result = null;

            for (Notification notification : notifications) {
                if (result != null && result.size() >= NotificationsHudElement.getInstance().maxCount.get()) {
                    return result;
                }

                if ((
                    notification.startTime == -1L && (notification.startTime = System.currentTimeMillis()) != -1L
                        || notification.startTime + (NotificationsHudElement.getInstance().timeToDisplay.get()).intValue() >= System.currentTimeMillis()
                )
                    && (
                    notification.getEvent() == null
                        || !(
                        notification.getEvent() instanceof ModuleToggledNotificationEvent e
                            && !(NotificationsHudElement.getInstance().modules.get()).contains(e.getToggledModule())
                    )
                )) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }

                    result.add(notification);
                } else {
                    notifications.remove(notification);
                }
            }

            return result;
        }
    }

    public static void clearNotifications() {
        notifications.clear();
    }
}
