package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent.Send;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.lang.reflect.Method;

public class AutoMoan extends AbnormallyModule {
    public final Identifier UWU_SOUND = Identifier.of("acid", "sounds/uwu.ogg");
    public final Identifier ENABLE_SOUND = Identifier.of("acid", "sounds/enable.ogg");
    public final Identifier DISABLE_SOUND = Identifier.of("acid", "sounds/disable.ogg");
    public final Identifier MOAN1_SOUND = Identifier.of("acid", "sounds/moan1.ogg");
    public final Identifier MOAN2_SOUND = Identifier.of("acid", "sounds/moan2.ogg");
    public final Identifier MOAN3_SOUND = Identifier.of("acid", "sounds/moan3.ogg");
    public final Identifier MOAN4_SOUND = Identifier.of("acid", "sounds/moan4.ogg");
    public final Identifier CUTIE_SOUND = Identifier.of("acid", "sounds/cutie.ogg");
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> volume = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Volume")).description("Volume of moan")).defaultValue(30)).min(0).sliderRange(0, 100).build());
    private final Setting<HitSound> hitSound = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("HitSound"))
                .description("HitSound"))
                .defaultValue(HitSound.MOAN))
                .build()
        );
    public SoundEvent UWU_SOUNDEVENT = SoundEvent.of(this.UWU_SOUND);
    public SoundEvent ENABLE_SOUNDEVENT = SoundEvent.of(this.ENABLE_SOUND);
    public SoundEvent DISABLE_SOUNDEVENT = SoundEvent.of(this.DISABLE_SOUND);
    public SoundEvent MOAN1_SOUNDEVENT = SoundEvent.of(this.MOAN1_SOUND);
    public SoundEvent MOAN2_SOUNDEVENT = SoundEvent.of(this.MOAN2_SOUND);
    public SoundEvent MOAN3_SOUNDEVENT = SoundEvent.of(this.MOAN3_SOUND);
    public SoundEvent MOAN4_SOUNDEVENT = SoundEvent.of(this.MOAN4_SOUND);
    public SoundEvent CUTIE_SOUNDEVENT = SoundEvent.of(this.CUTIE_SOUND);

    public AutoMoan() {
        super(Compassion.ABNORMALLY, "AutoMoan", "Moan when you hit the enemy");
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public void playHitSound(HitSound value) {
        switch (value) {
            case UWU:
                this.playSound(this.UWU_SOUNDEVENT);
                break;
            case MOAN:
                SoundEvent sound = switch ((int) random(0.0F, 3.0F)) {
                    case 0 -> this.MOAN1_SOUNDEVENT;
                    case 1 -> this.MOAN2_SOUNDEVENT;
                    case 2 -> this.MOAN3_SOUNDEVENT;
                    default -> this.MOAN4_SOUNDEVENT;
                };
                this.playSound(sound);
                break;
            case CUTIE:
                this.playSound(this.CUTIE_SOUNDEVENT);
        }
    }

    public void onActivate() {
    }

    public void playSound(SoundEvent sound) {
        if (this.mc.player != null && this.mc.world != null) {
            this.mc
                .world
                .playSound(this.mc.player, this.mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, (this.volume.get()).intValue() / 100.0F, 1.0F);
        }
    }

    @EventHandler
    public void onHit(AttackEntityEvent event) {
    }

    @EventHandler
    private void onSendPacket(Send event) {
        if (event.packet instanceof IPlayerInteractEntityC2SPacket packet) {
            try {
                Class<?> packetClass = packet.getClass();
                Method getTypeMethod = packetClass.getDeclaredMethod("getType");
                getTypeMethod.setAccessible(true);
                Enum<?> interactType = (Enum<?>) getTypeMethod.invoke(packet);
                if (interactType.name().equals("ATTACK") && packet.getEntity() instanceof LivingEntity) {
                    this.playHitSound((HitSound) this.hitSound.get());
                }
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }
    }

    public static enum HitSound {
        UWU,
        MOAN,
        CUTIE;
    }
}
