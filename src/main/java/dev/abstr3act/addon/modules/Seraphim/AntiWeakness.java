package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.utils.seraphim.PlayerUtils;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class AntiWeakness extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Mode> mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Mode")).description(".")).defaultValue(Mode.Blatant)).build());

    public AntiWeakness() {
        super(Compassion.SERAPHIM, "AntiWeakness", "Anti dumbass");
    }

    @EventHandler
    public void onAttackEntity(AttackEntityEvent event) {
        if (!PlayerUtils.canBreakCrystal()) {
            if (event.entity instanceof EndCrystalEntity) {
                switch ((Mode) this.mode.get()) {
                    case Legit:
                        if (this.getSword() == -1) {
                            return;
                        }

                        InvUtils.swap(this.getSword(), false);
                        break;
                    case Blatant:
                        StatusEffectInstance weakness = this.mc.player.getStatusEffect(StatusEffects.WEAKNESS);
                        StatusEffectInstance strength = this.mc.player.getStatusEffect(StatusEffects.STRENGTH);
                        if (weakness != null
                            && (strength == null || strength.getAmplifier() <= weakness.getAmplifier())
                            && !PlayerUtils.isValidWeaknessItem(this.mc.player.getMainHandStack())) {
                            this.sendPacket(new UpdateSelectedSlotC2SPacket(this.getSword()));
                        }
                }
            }
        }
    }

    public int getSword() {
        for (int i = 0; i < this.mc.player.getInventory().size(); i++) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SwordItem) {
                return i;
            }
        }

        return -1;
    }

    static enum Mode {
        Legit,
        Blatant;
    }
}
