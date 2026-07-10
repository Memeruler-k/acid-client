package dev.abstr3act.addon.notifications;

import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Objects;

public class Notification {
    private static int nextId;
    @Nonnull
    private final String title;
    @Nullable
    private final String description;
    @Nonnull
    private final Color color;
    @Nullable
    private final NotificationsHudElement.icon icon;
    @Nullable
    private final NotificationEvent event;
    protected long startTime = -1L;

    public Notification(@Nonnull String title) {
        this(title, (String) null);
    }

    public Notification(@Nonnull String title, @Nullable String description) {
        this(title, description, null);
    }

    public Notification(@Nonnull String title, @Nullable Color color) {
        this(title, null, color);
    }

    public Notification(@Nonnull String title, @Nullable String description, @Nullable Color color) {
        this(title, description, color, null, NotificationsHudElement.icon.INFO);
    }

    public Notification(@Nonnull String title, @Nullable String description, @Nullable Color color, @Nullable NotificationsHudElement.icon icon) {
        this(title, description, color, null, icon);
    }

    public Notification(
        @Nonnull String title, @Nullable String description, @Nullable Color color, @Nullable NotificationEvent event, @Nullable NotificationsHudElement.icon icon
    ) {
        Objects.requireNonNull(title);
        this.title = title;
        this.description = description;
        this.color = color != null ? color : new Color(255, 255, 255);
        this.event = event;
        this.icon = icon;
    }

    private static synchronized int getNextId() {
        return nextId++;
    }

    @Nonnull
    public String getTitle() {
        return this.title;
    }

    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Nonnull
    public Color getColor() {
        return this.color;
    }

    @Nullable
    public NotificationsHudElement.icon getIcon() {
        return this.icon;
    }

    public long getStartTime() {
        return this.startTime;
    }

    @Nullable
    public NotificationEvent getEvent() {
        return this.event;
    }

    @Override
    public String toString() {
        return "Notification{title='" + this.title + "', description='" + this.description + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Notification that = (Notification) o;
            return this.title.equals(that.title) && Objects.equals(this.description, that.description) && this.color.equals(that.color);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.title, this.description, this.color);
    }
}
