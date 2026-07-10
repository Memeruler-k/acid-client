package dev.abstr3act.addon.modules.Compassion;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;

public class NotificationHelper {
    public static void sendNotification(String title, String message) {
        new Thread(() -> {
            try {
                System.setProperty("java.awt.headless", "false");
                if (SystemTray.isSupported()) {
                    SystemTray systemTray = SystemTray.getSystemTray();
                    Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
                    TrayIcon trayIcon = new TrayIcon(image, "Minecraft Tray");
                    trayIcon.setImageAutoSize(true);
                    trayIcon.addActionListener(ex -> System.out.println("Tray icon clicked!"));
                    systemTray.add(trayIcon);
                    trayIcon.displayMessage(title, message, MessageType.INFO);
                } else {
                    sendNativeNotification(title, message);
                }
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        sendNativeNotification("A", "B");
    }

    private static void sendNativeNotification(String title, String message) {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                Runtime.getRuntime().exec("powershell -command \"New-BurntToastNotification -Text '" + title + "', '" + message + "'\"");
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"osascript", "-e", "display notification \"" + message + "\" with title \"" + title + "\""});
            } else if (os.contains("nux")) {
                Runtime.getRuntime().exec(new String[]{"notify-send", title, message});
            } else {
                System.out.println("Native notifications are not supported on this platform.");
            }
        } catch (IOException var4) {
            var4.printStackTrace();
        }
    }
}
