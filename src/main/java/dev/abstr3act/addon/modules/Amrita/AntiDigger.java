package dev.abstr3act.addon.modules.Amrita;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AmritaModule;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.IntSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class AntiDigger extends AmritaModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<Integer> delay = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Delay")).description(".")).defaultValue(20)).sliderRange(0, 5000).build());
    private final Setting<Integer> triggerPitch = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Delay")).description(".")).defaultValue(64)).sliderRange(-90, 90).build());
    private final Setting<String> string = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("String"))
                .description(""))
                .defaultValue("[Acid7] 我发现死全家 %s 正在尝试挖掘自己亲妈的坟墓"))
                .build()
        );
    int i = 0;

    public AntiDigger() {
        super(Compassion.AMRITA, "AntiDigger", "Automatically Drinks Potions");
    }

    public static BlockHitResult getTargetBlock(PlayerEntity player, double maxDistance) {
        Vec3d startPos = player.getCameraPosVec(1.0F);
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d endPos = startPos.add(lookVec.multiply(maxDistance));
        return player.getWorld().raycast(new RaycastContext(startPos, endPos, ShapeType.OUTLINE, FluidHandling.NONE, player));
    }

    public void onDeactivate() {
        this.i = 0;
    }

    @EventHandler
    private void onTick(Pre event) {
        if (this.i <= 0) {
            for (PlayerEntity e : this.mc.world.getPlayers()) {
                if (e != this.mc.player && !Friends.get().isFriend(e)) {
                    BlockHitResult hitResult = getTargetBlock(e, this.mc.player.getEntityInteractionRange());
                    BlockPos blockPos = hitResult.getBlockPos();
                    BlockState blockState = this.mc.player.getWorld().getBlockState(blockPos);
                    if (e.getMainHandStack().getItem() instanceof PickaxeItem
                        && e.getPitch() >= (this.triggerPitch.get()).intValue()
                        && e.handSwinging
                        && blockState.getBlock() != null
                        && !(blockState.getBlock() instanceof AirBlock)) {
                        ChatUtils.sendPlayerMsg(((String) this.string.get()).replace("%s", e.getGameProfile().getName()));
                        this.i = this.delay.get();
                    }
                }
            }
        } else {
            this.i--;
        }
    }
}
