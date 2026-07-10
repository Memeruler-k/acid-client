package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class AutoBalley extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Double> range = this.sgGeneral
        .add(((Builder) ((Builder) new Builder().name("Range")).sliderRange(0.0, 16.0).description("")).defaultValue(5.0).build());
    private boolean hasTNT = false;

    public AutoBalley() {
        super(Compassion.AMRITA, "AutoBalley", "sb");
    }

    public void onActivate() {
    }

    @EventHandler
    private void onTickEvent(Pre event) {
        boolean foundTNT = this.tnts(this.range.get());
        if (foundTNT && !this.hasTNT) {
            Utils.leftClick();
            AChatUtils.sendMsgAmrita(Text.of("Pong!"));
        }

        this.hasTNT = foundTNT;
    }

    public boolean tnts(double radius) {
        World world = this.mc.world;
        if (world != null && this.mc.player != null) {
            boolean hasFallingBlock = !world.getEntitiesByClass(FallingBlockEntity.class, this.mc.player.getBoundingBox().expand(radius), e -> true).isEmpty();
            boolean hasTnt = !world.getEntitiesByClass(TntEntity.class, this.mc.player.getBoundingBox().expand(radius), e -> true).isEmpty();
            boolean hasArmorStand = !world.getEntitiesByClass(ArmorStandEntity.class, this.mc.player.getBoundingBox().expand(radius), e -> true).isEmpty();
            return hasFallingBlock || hasTnt || hasArmorStand;
        } else {
            return false;
        }
    }
}
