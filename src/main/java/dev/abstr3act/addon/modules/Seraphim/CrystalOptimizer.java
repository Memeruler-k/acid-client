package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.seraphim.PlayerUtils;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.EndCrystalEntity;

public class CrystalOptimizer extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public CrystalOptimizer() {
        super(Compassion.SERAPHIM, "CrystalOptimizer", ".");
    }

    @EventHandler
    public void onAttack(AttackEntityEvent event) {
        Entity ent = event.entity;
        if (ent != null) {
            if (!PlayerUtils.invalid()) {
                if (!this.mc.isInSingleplayer()) {
                    if (PlayerUtils.canBreak()) {
                        if (ent instanceof EndCrystalEntity crystal) {
                            crystal.kill();
                            crystal.remove(RemovalReason.KILLED);
                            crystal.onRemoved();
                        }
                    }
                }
            }
        }
    }
}
