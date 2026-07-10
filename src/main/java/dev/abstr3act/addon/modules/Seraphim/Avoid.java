package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.events.EventCollision;
import dev.abstr3act.addon.module.SeraphimModule;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;

public class Avoid extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> voidAir = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Void")).description(".")).defaultValue(true)).build());
    public final Setting<Boolean> cactus = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Cactus")).description(".")).defaultValue(true)).build());
    public final Setting<Boolean> fire = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Fire")).description(".")).defaultValue(true)).build());
    public final Setting<Boolean> berryBush = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("BerryBush")).description(".")).defaultValue(true)).build());
    public final Setting<Boolean> powderSnow = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("PowderSnow")).description(".")).defaultValue(true)).build());
    public final Setting<Boolean> unloaded = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Unloaded")).description(".")).defaultValue(true)).build());
    public final Setting<Boolean> lava = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Lava")).description(".")).defaultValue(true)).build());
    public final Setting<Boolean> plate = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Plate")).description(".")).defaultValue(true)).build());
    public final Setting<Boolean> trapString = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Tripwire")).description(".")).defaultValue(true)).build());

    public Avoid() {
        super(Compassion.SERAPHIM, "Avoid", "Avoid hazards");
    }

    @EventHandler
    public void onCollide(EventCollision e) {
        if (!fullNullCheck()) {
            Block b = e.getState().getBlock();
            boolean avoidUnloaded = !this.mc.world.isChunkLoaded(e.getPos().getX() >> 4, e.getPos().getZ() >> 4) && this.unloaded.get();
            boolean avoidVoid = e.getPos().getY() < this.mc.world.getBottomY() && this.voidAir.get();
            boolean avoidCactus = b == Blocks.CACTUS && this.cactus.get();
            boolean avoidFire = (b == Blocks.FIRE || b == Blocks.SOUL_FIRE) && this.fire.get();
            boolean avoidBerryBush = b == Blocks.SWEET_BERRY_BUSH && this.berryBush.get();
            boolean avoidSusSnow = b == Blocks.POWDER_SNOW && this.powderSnow.get();
            boolean avoidLava = b == Blocks.LAVA && this.lava.get();
            boolean avoidPlate = (b instanceof PressurePlateBlock || b == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE || b == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE)
                && this.plate.get();
            boolean avoidTrapString = b instanceof TripwireBlock && e.getState().get(TripwireHookBlock.ATTACHED) && this.trapString.get();
            if (avoidUnloaded || avoidFire || avoidCactus || avoidLava || avoidBerryBush || avoidSusSnow || avoidPlate || avoidTrapString || avoidVoid) {
                e.setState(Blocks.DIRT.getDefaultState());
            }
        }
    }
}
