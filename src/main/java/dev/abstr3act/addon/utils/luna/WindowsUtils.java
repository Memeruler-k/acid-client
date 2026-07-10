package dev.abstr3act.addon.utils.luna;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class WindowsUtils {
    public static void main(String[] args) {
        try {
            System.setProperty("java.awt.headless", "false");
            String headless = System.getProperty("java.awt.headless");
            System.out.println("Headless mode: " + headless);
            if (!SystemTray.isSupported()) {
                System.out.println("SystemTray is not supported on this platform.");
                return;
            }

            SystemTray systemTray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Tray Example");
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(ex -> System.out.println("Tray icon clicked!"));
            systemTray.add(trayIcon);
            trayIcon.displayMessage("Hello", "This is a test message", MessageType.INFO);
        } catch (HeadlessException var5) {
            System.out.println("Caught HeadlessException: Graphics environment is not supported.");
        } catch (Exception var6) {
            var6.printStackTrace();
        }
    }
}
