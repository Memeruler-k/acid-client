package dev.abstr3act.addon.hud;

import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.player.PlayerEntity;

public class Radar extends HudElement {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();    public static final HudElementInfo<Radar> INFO = new HudElementInfo(
        dev.abstr3act.addon.Compassion.HUD_GROUP, "Radar", "Displays information about your combat target.", Radar::new
    );
    private final Setting<Integer> size = this.sgGeneral.add(((Builder) ((Builder) new Builder().name("size")).defaultValue(80)).min(20).max(300).build());
    private final Setting<SettingColor> color3 = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder().name("player-color"))
                .defaultValue(new Color(-979657829))
                .build()
        );
    public Radar() {
        super(INFO);
    }

    public static float getTickDelta() {
        return MeteorClient.mc.getRenderTickCounter().getTickDelta(true);
    }

    public void render(HudRenderer renderer) {
        Render2DEngine.drawRoundedBlur(
            renderer.drawContext.getMatrices(),
            this.getX(),
            this.getY(),
            (this.size.get()).intValue(),
            (this.size.get()).intValue(),
            1.0F,
            java.awt.Color.BLACK
        );
        Render2DEngine.verticalGradient(
            renderer.drawContext.getMatrices(),
            this.getX(),
            this.getY() + ((this.size.get()).intValue() / 2.0F - 2.0F),
            this.getX() + this.size.get(),
            this.getY() + (this.size.get()).intValue() / 2.0F,
            new java.awt.Color(0, true),
            new java.awt.Color(2063597568, true)
        );
        Render2DEngine.verticalGradient(
            renderer.drawContext.getMatrices(),
            this.getX(),
            this.getY() + (this.size.get()).intValue() / 2.0F,
            this.getX() + this.size.get(),
            this.getY() + ((this.size.get()).intValue() / 2.0F + 2.0F),
            new java.awt.Color(2063597568, true),
            new java.awt.Color(0, true)
        );
        Render2DEngine.horizontalGradient(
            renderer.drawContext.getMatrices(),
            this.getX() + ((this.size.get()).intValue() / 2.0F - 2.0F),
            this.getY() - 1,
            this.getX() + (this.size.get()).intValue() / 2.0F,
            this.getY() + this.size.get() - 1,
            new java.awt.Color(0, true),
            new java.awt.Color(2063597568, true)
        );
        Render2DEngine.horizontalGradient(
            renderer.drawContext.getMatrices(),
            this.getX() + (this.size.get()).intValue() / 2.0F,
            this.getY() - 1,
            this.getX() + ((this.size.get()).intValue() / 2.0F + 2.0F),
            this.getY() + this.size.get() - 1,
            new java.awt.Color(2063597568, true),
            new java.awt.Color(0, true)
        );

        for (PlayerEntity entityPlayer : MeteorClient.mc.world.getPlayers()) {
            if (entityPlayer != MeteorClient.mc.player) {
                float posX = (float) (entityPlayer.prevX + (entityPlayer.prevX - entityPlayer.getX()) * getTickDelta() - MeteorClient.mc.player.getX()) * 2.0F;
                float posZ = (float) (entityPlayer.prevZ + (entityPlayer.prevZ - entityPlayer.getZ()) * getTickDelta() - MeteorClient.mc.player.getZ()) * 2.0F;
                float cos = (float) Math.cos(MeteorClient.mc.player.getYaw(getTickDelta()) * 0.017453292);
                float sin = (float) Math.sin(MeteorClient.mc.player.getYaw(getTickDelta()) * 0.017453292);
                float rotY = -(posZ * cos - posX * sin);
                float rotX = -(posX * cos + posZ * sin);
                if (rotY > (this.size.get()).intValue() / 2.0F - 6.0F) {
                    rotY = (this.size.get()).intValue() / 2.0F - 6.0F;
                } else if (rotY < -((this.size.get()).intValue() / 2.0F - 8.0F)) {
                    rotY = -((this.size.get()).intValue() / 2.0F - 8.0F);
                }

                if (rotX > (this.size.get()).intValue() / 2.0F - 5.0F) {
                    rotX = (this.size.get()).intValue() / 2.0F - 5.0F;
                } else if (rotX < -((this.size.get()).intValue() / 2.0F - 5.0F)) {
                    rotX = -((this.size.get()).intValue() / 2.0F - 5.0F);
                }

                if (Friends.get().isFriend(entityPlayer)) {
                    Render2DEngine.drawRound(
                        renderer.drawContext.getMatrices(),
                        this.getX() + (this.size.get()).intValue() / 2.0F + rotX - 2.0F,
                        this.getY() + (this.size.get()).intValue() / 2.0F + rotY - 2.0F,
                        8.0F,
                        8.0F,
                        4.0F,
                        (Color) Config.get().friendColor.get()
                    );
                } else {
                    Render2DEngine.drawRound(
                        renderer.drawContext.getMatrices(),
                        this.getX() + (this.size.get()).intValue() / 2.0F + rotX - 2.0F,
                        this.getY() + (this.size.get()).intValue() / 2.0F + rotY - 2.0F,
                        8.0F,
                        8.0F,
                        4.0F,
                        (Color) this.color3.get()
                    );
                }
            }
        }

        this.setSize((this.size.get()).intValue(), (this.size.get()).intValue());
    }


}
