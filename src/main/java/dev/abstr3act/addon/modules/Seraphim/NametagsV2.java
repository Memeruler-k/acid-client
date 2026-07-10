package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.command.ForceTargetCommand;
import dev.abstr3act.addon.module.SeraphimModule;
import dev.abstr3act.addon.modules.Lacrymira.Media;
import dev.abstr3act.addon.utils.HashUtils;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.*;

public class NametagsV2 extends SeraphimModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgPlayers = this.settings.createGroup("Players");
    private final SettingGroup sgItems = this.settings.createGroup("Items");
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<Set<EntityType<?>>> entities = this.sgGeneral
        .add(
            ((Builder) ((Builder) new Builder().name("entities")).description("Select entities to draw nametags on."))
                .defaultValue(new EntityType[]{EntityType.PLAYER, EntityType.ITEM})
                .build()
        );
    private final Setting<Double> scale = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("scale"))
                .description("The scale of the nametag."))
                .defaultValue(1.1)
                .min(0.1)
                .build()
        );
    private final Setting<Double> itemScale = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("itemScale"))
                .description("The scale of the nametag."))
                .defaultValue(2.0)
                .min(0.1)
                .build()
        );
    private final Setting<Integer> itemOffsetX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("itemOffsetX"))
                .description("The scale of the nametag."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Integer> itemOffsetY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("itemOffsetY"))
                .description("The scale of the nametag."))
                .defaultValue(0))
                .sliderRange(-1000, 1000)
                .build()
        );
    private final Setting<Double> sizeX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("sizeX"))
                .description("The scale of the nametag."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> sizeY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("sizeY"))
                .description("The scale of the nametag."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> offsetX = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("offsetX"))
                .description("The scale of the nametag."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Double> offsetY = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("offsetY"))
                .description("The scale of the nametag."))
                .defaultValue(0.0)
                .sliderRange(-1000.0, 1000.0)
                .build()
        );
    private final Setting<Boolean> ignoreSelf = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-self"))
                .description("Ignore yourself when in third person or freecam."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> ignoreFriends = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-friends"))
                .description("Ignore rendering nametags for friends."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Boolean> ignoreBots = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-bots"))
                .description("Only render non-bot nametags."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> culling = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("culling"))
                .description("Only render a certain number of nametags at a certain distance."))
                .defaultValue(false))
                .build()
        );
    private final Setting<Double> maxCullRange = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("culling-range"))
                .description("Only render nametags within this distance of your player."))
                .defaultValue(20.0)
                .min(0.0)
                .sliderMax(200.0)
                .visible(this.culling::get))
                .build()
        );
    private final Setting<Integer> maxCullCount = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("culling-count"))
                .description("Only render this many nametags."))
                .defaultValue(50))
                .min(1)
                .sliderRange(1, 100)
                .visible(this.culling::get))
                .build()
        );
    private final Setting<Boolean> displayHealth = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("health"))
                .description("Shows the player's health."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Boolean> displayGameMode = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("gamemode"))
                .description("Shows the player's GameMode."))
                .defaultValue(false))
                .build()
        );
    private final Setting<SettingColor> gamemodeColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("gamemode-color"))
                .description("The color of the nametag gamemode."))
                .defaultValue(new SettingColor(232, 185, 35))
                .visible(this.displayGameMode::get))
                .build()
        );
    private final Setting<Boolean> displayDistance = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("getDistance"))
                .description("Shows the distance between you and the player."))
                .defaultValue(false))
                .build()
        );
    private final Setting<DistanceColorMode> distanceColorMode = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("distance-color-mode"))
                .description("The mode to color the nametag distance with."))
                .defaultValue(DistanceColorMode.Gradient))
                .visible(this.displayDistance::get))
                .build()
        );
    private final Setting<SettingColor> distanceColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("distance-color"))
                .description("The color of the nametag distance."))
                .defaultValue(new SettingColor(150, 150, 150))
                .visible(() -> this.displayDistance.get() && this.distanceColorMode.get() == DistanceColorMode.Flat))
                .build()
        );
    private final Setting<Boolean> displayPing = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ping"))
                .description("Shows the player's ping."))
                .defaultValue(true))
                .build()
        );
    private final Setting<SettingColor> pingColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("ping-color"))
                .description("The color of the nametag ping."))
                .defaultValue(new SettingColor(20, 170, 170))
                .visible(this.displayPing::get))
                .build()
        );
    private final Setting<Boolean> displayItems = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("items"))
                .description("Displays armor and hand items above the name tags."))
                .defaultValue(true))
                .build()
        );
    private final Setting<Double> itemSpacing = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("item-spacing"))
                .description("The spacing between items."))
                .defaultValue(2.0)
                .range(0.0, 10.0)
                .visible(this.displayItems::get))
                .build()
        );
    private final Setting<Boolean> ignoreEmpty = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("ignore-empty-slots"))
                .description("Doesn't add spacing where an empty item stack would be."))
                .defaultValue(true))
                .visible(this.displayItems::get))
                .build()
        );
    private final Setting<Durability> itemDurability = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("durability"))
                .description("Displays item durability as either a total, percentage, or neither."))
                .defaultValue(Durability.None))
                .visible(this.displayItems::get))
                .build()
        );
    private final Setting<Boolean> displayEnchants = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("display-enchants"))
                .description("Displays item enchantments on the items."))
                .defaultValue(false))
                .visible(this.displayItems::get))
                .build()
        );
    private final Setting<Set<RegistryKey<Enchantment>>> shownEnchantments = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.EnchantmentListSetting.Builder) ((meteordevelopment.meteorclient.settings.EnchantmentListSetting.Builder) ((meteordevelopment.meteorclient.settings.EnchantmentListSetting.Builder) new meteordevelopment.meteorclient.settings.EnchantmentListSetting.Builder()
                .name("shown-enchantments"))
                .description("The enchantments that are shown on nametags."))
                .visible(() -> this.displayItems.get() && this.displayEnchants.get()))
                .defaultValue(
                    new RegistryKey[]{Enchantments.PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.PROJECTILE_PROTECTION}
                )
                .build()
        );
    private final Setting<Position> enchantPos = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("enchantment-position"))
                .description("Where the enchantments are rendered."))
                .defaultValue(Position.Above))
                .visible(() -> this.displayItems.get() && this.displayEnchants.get()))
                .build()
        );
    private final Setting<Integer> enchantLength = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("enchant-name-length"))
                .description("The length enchantment names are trimmed to."))
                .defaultValue(3))
                .range(1, 5)
                .sliderRange(1, 5)
                .visible(() -> this.displayItems.get() && this.displayEnchants.get()))
                .build()
        );
    private final Setting<Double> enchantTextScale = this.sgPlayers
        .add(
            ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) ((meteordevelopment.meteorclient.settings.DoubleSetting.Builder) new meteordevelopment.meteorclient.settings.DoubleSetting.Builder()
                .name("enchant-text-scale"))
                .description("The scale of the enchantment text."))
                .defaultValue(1.0)
                .range(0.1, 2.0)
                .sliderRange(0.1, 2.0)
                .visible(() -> this.displayItems.get() && this.displayEnchants.get()))
                .build()
        );
    private final Setting<Boolean> itemCount = this.sgItems
        .add(
            ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) ((meteordevelopment.meteorclient.settings.BoolSetting.Builder) new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                .name("show-count"))
                .description("Displays the number of items in the stack."))
                .defaultValue(true))
                .build()
        );
    private final Setting<SettingColor> background = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("background-color"))
                .description("The color of the nametag background."))
                .defaultValue(new SettingColor(0, 0, 0, 75))
                .build()
        );
    private final Setting<SettingColor> nameColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("name-color"))
                .description("The color of the nametag names."))
                .defaultValue(new SettingColor())
                .build()
        );
    private final Setting<SettingColor> targetColor = this.sgRender
        .add(
            ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) ((meteordevelopment.meteorclient.settings.ColorSetting.Builder) new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
                .name("targetColor"))
                .description("The color of the nametag names."))
                .defaultValue(new SettingColor())
                .build()
        );
    private final Color WHITE = new Color(255, 255, 255);
    private final Color RED = new Color(255, 25, 25);
    private final Color AMBER = new Color(255, 105, 25);
    private final Color GREEN = new Color(25, 252, 25);
    private final Color GOLD = new Color(232, 185, 35);
    private final Vector3d pos = new Vector3d();
    private final double[] itemWidths = new double[6];
    private final List<Entity> entityList = new ArrayList<>();
    private double height;

    public NametagsV2() {
        super(Compassion.SERAPHIM, "NameTagsV2", "Displays customizable nametags above players.");
    }

    private static String ticksToTime(int ticks) {
        if (ticks > 72000) {
            int h = ticks / 20 / 3600;
            return h + " h";
        } else if (ticks > 1200) {
            int m = ticks / 20 / 60;
            return m + " m";
        } else {
            int s = ticks / 20;
            int ms = ticks % 20 / 2;
            return s + "." + ms + " s";
        }
    }

    @EventHandler
    private void onTick(Post event) {
        this.entityList.clear();
        boolean freecamNotActive = !Modules.get().isActive(Freecam.class);
        boolean notThirdPerson = this.mc.options.getPerspective().isFirstPerson();
        Vec3d cameraPos = this.mc.gameRenderer.getCamera().getPos();

        for (Entity entity : this.mc.world.getEntities()) {
            EntityType<?> type = entity.getType();
            if (((Set) this.entities.get()).contains(type)
                && (
                type != EntityType.PLAYER
                    || (!this.ignoreSelf.get() && (!freecamNotActive || !notThirdPerson) || entity != this.mc.player)
                    && (EntityUtils.getGameMode((PlayerEntity) entity) != null || !this.ignoreBots.get())
                    && (!Friends.get().isFriend((PlayerEntity) entity) || !this.ignoreFriends.get())
            )
                && (!this.culling.get() || PlayerUtils.isWithinCamera(entity, this.maxCullRange.get()))) {
                this.entityList.add(entity);
            }
        }

        this.entityList.sort(Comparator.comparing(e -> e.squaredDistanceTo(cameraPos)));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        int count = this.getRenderCount();
        boolean shadow = Config.get().customFont.get();

        for (int i = count - 1; i > -1; i--) {
            Entity entity = this.entityList.get(i);
            Utils.set(this.pos, entity, event.tickDelta);
            this.pos.add(0.0, this.getHeight(entity), 0.0);
            EntityType<?> type = entity.getType();
            if (NametagUtils.to2D(this.pos, this.scale.get())) {
                if (type == EntityType.PLAYER) {
                    this.renderNametagPlayer(event, (PlayerEntity) entity, shadow);
                } else if (type == EntityType.ITEM) {
                    this.renderNametagItem(((ItemEntity) entity).getStack(), shadow);
                } else if (type == EntityType.ITEM_FRAME) {
                    this.renderNametagItem(((ItemFrameEntity) entity).getHeldItemStack(), shadow);
                } else if (type == EntityType.TNT) {
                    this.renderTntNametag((TntEntity) entity, shadow);
                } else if (entity instanceof LivingEntity) {
                    this.renderGenericNametag((LivingEntity) entity, shadow);
                }
            }
        }
    }

    private int getRenderCount() {
        int count = this.culling.get() ? this.maxCullCount.get() : this.entityList.size();
        return MathHelper.clamp(count, 0, this.entityList.size());
    }

    public String getInfoString() {
        return Integer.toString(this.getRenderCount());
    }

    private double getHeight(Entity entity) {
        double height = entity.getEyeHeight(entity.getPose());
        if (entity.getType() != EntityType.ITEM && entity.getType() != EntityType.ITEM_FRAME) {
            height += 0.5;
        } else {
            height += 0.2;
        }

        return height;
    }

    private void renderNametagPlayer(Render2DEvent event, PlayerEntity player, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos, event.drawContext);
        GameMode gm = EntityUtils.getGameMode(player);
        String gmText = "BOT";
        if (gm != null) {
            gmText = switch (gm) {
                case SPECTATOR -> "SP";
                case SURVIVAL -> "S";
                case CREATIVE -> "C";
                case ADVENTURE -> "A";
                default -> throw new MatchException(null, null);
            };
        }

        String var69 = "[" + gmText + "] ";
        Color nameColor = PlayerUtils.getPlayerColor(player, (Color) this.nameColor.get());
        if (player == ForceTargetCommand.target) {
            nameColor = (Color) this.targetColor.get();
        }

        String name;
        if (player == this.mc.player) {
            name = ((NameProtect) Modules.get().get(NameProtect.class)).getName(player.getName().getString());
        } else {
            name = player.getName().getString().replaceAll("§.", "");
        }

        if (((Media) Modules.get().get(Media.class)).isActive()) {
            String hash = HashUtils.hashSHA256(player.getUuidAsString());
            name = (String) ((Media) Modules.get().get(Media.class)).protectedString.get() + "_" + hash.substring(0, Math.min(5, hash.length()));
        }

        float absorption = player.getAbsorptionAmount();
        int health = Math.round(player.getHealth() + absorption);
        double healthPercentage = health / (player.getMaxHealth() + absorption);
        String healthText = " " + health;
        Color healthColor = Color.WHITE;
        int ping = EntityUtils.getPing(player);
        String pingText = " [" + ping + "ms]";
        double dist = Math.round(PlayerUtils.distanceToCamera(player) * 10.0) / 10.0;
        String distText = " " + dist + "m";
        double gmWidth = text.getWidth(var69, shadow);
        double nameWidth = text.getWidth(name, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double pingWidth = text.getWidth(pingText, shadow);
        double distWidth = text.getWidth(distText, shadow);
        double width = nameWidth;
        boolean renderPlayerDistance = player != this.mc.cameraEntity || Modules.get().isActive(Freecam.class);
        if (this.displayHealth.get()) {
            width = nameWidth + healthWidth;
        }

        if (this.displayGameMode.get()) {
            width += gmWidth;
        }

        if (this.displayPing.get()) {
            width += pingWidth;
        }

        if (this.displayDistance.get() && renderPlayerDistance) {
            width += distWidth;
        }

        double widthHalf = width / 2.0;
        double heightDown = text.getHeight(shadow);
        this.drawBg(-widthHalf, -heightDown, width, heightDown, player);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        if (this.displayGameMode.get()) {
            hX = text.render(var69, hX, hY, (Color) this.gamemodeColor.get(), shadow);
        }

        double var71 = text.render(name, hX, hY, nameColor, shadow);
        if (this.displayHealth.get()) {
            var71 = text.render(healthText, var71, hY, healthColor, shadow);
        }

        if (this.displayPing.get()) {
            var71 = text.render(pingText, var71, hY, (Color) this.pingColor.get(), shadow);
        }

        if (this.displayDistance.get() && renderPlayerDistance) {
            switch ((DistanceColorMode) this.distanceColorMode.get()) {
                case Gradient:
                    text.render(distText, var71, hY, EntityUtils.getColorFromDistance(player), shadow);
                    break;
                case Flat:
                    text.render(distText, var71, hY, (Color) this.distanceColor.get(), shadow);
            }
        }

        text.end();
        if (this.displayItems.get()) {
            Arrays.fill(this.itemWidths, 0.0);
            boolean hasItems = false;
            int maxEnchantCount = 0;

            for (int i = 0; i < 6; i++) {
                ItemStack itemStack = this.getItem(player, i);
                if (this.itemWidths[i] == 0.0 && (!this.ignoreEmpty.get() || !itemStack.isEmpty())) {
                    this.itemWidths[i] = 32.0 + this.itemSpacing.get();
                }

                if (!itemStack.isEmpty()) {
                    hasItems = true;
                }

                if (this.displayEnchants.get()) {
                    ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(itemStack);
                    int size = 0;

                    for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
                        if (((Set) this.shownEnchantments.get()).contains(enchantment)) {
                            String enchantName = Utils.getEnchantSimpleName(enchantment, this.enchantLength.get()) + " " + enchantments.getLevel(enchantment);
                            this.itemWidths[i] = Math.max(this.itemWidths[i], text.getWidth(enchantName, shadow) / 2.0);
                            size++;
                        }
                    }

                    maxEnchantCount = Math.max(maxEnchantCount, size);
                }
            }

            double itemsHeight = hasItems ? 32 : 0;
            double itemWidthTotal = 0.0;

            for (double w : this.itemWidths) {
                itemWidthTotal += w;
            }

            double itemWidthHalf = itemWidthTotal / 2.0;
            double y = -heightDown - 7.0 - itemsHeight;
            double x = -itemWidthHalf;

            for (int i = 0; i < 6; i++) {
                ItemStack stack = this.getItem(player, i);
                RenderUtils.drawItem(
                    event.drawContext,
                    stack,
                    (int) x + this.itemOffsetX.get(),
                    (int) y + this.itemOffsetY.get(),
                    (this.itemScale.get()).floatValue(),
                    true
                );
                if (stack.isDamageable() && this.itemDurability.get() != Durability.None) {
                    text.begin(0.75, false, true);

                    String damageText = switch ((Durability) this.itemDurability.get()) {
                        case Total -> Integer.toString(stack.getMaxDamage() - stack.getDamage());
                        case Percentage ->
                            String.format("%.0f%%", (stack.getMaxDamage() - stack.getDamage()) * 100.0F / stack.getMaxDamage());
                        default -> "err";
                    };
                    Color damageColor = new Color(stack.getItemBarColor());
                    text.render(damageText, (int) x, (int) y, damageColor.a(255), true);
                    text.end();
                }

                if (maxEnchantCount > 0 && this.displayEnchants.get()) {
                    text.begin(0.5 * this.enchantTextScale.get(), false, true);
                    ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
                    Map<RegistryEntry<Enchantment>, Integer> enchantmentsToShow = new HashMap<>();

                    for (RegistryEntry<Enchantment> enchantmentx : enchantments.getEnchantments()) {
                        if (((Set) this.shownEnchantments.get()).contains(enchantmentx)) {
                            enchantmentsToShow.put(enchantmentx, enchantments.getLevel(enchantmentx));
                        }
                    }

                    double aW = this.itemWidths[i];
                    double enchantY = 0.0;

                    double addY = switch ((Position) this.enchantPos.get()) {
                        case Above -> -((enchantmentsToShow.size() + 1) * text.getHeight(shadow));
                        case OnTop -> (itemsHeight - enchantmentsToShow.size() * text.getHeight(shadow)) / 2.0;
                    };

                    for (RegistryEntry<Enchantment> enchantmentxx : enchantmentsToShow.keySet()) {
                        String enchantName = Utils.getEnchantSimpleName(enchantmentxx, this.enchantLength.get()) + " " + enchantmentsToShow.get(enchantmentxx);
                        Color enchantColor = this.WHITE;

                        double enchantX = switch ((Position) this.enchantPos.get()) {
                            case Above -> x + aW / 2.0 - text.getWidth(enchantName, shadow) / 2.0;
                            case OnTop -> x + (aW - text.getWidth(enchantName, shadow)) / 2.0;
                        };
                        text.render(enchantName, enchantX, y + addY + enchantY, enchantColor, shadow);
                        enchantY += text.getHeight(shadow);
                    }

                    text.end();
                }

                x += this.itemWidths[i];
            }
        } else if (this.displayEnchants.get()) {
            this.displayEnchants.set(false);
        }

        NametagUtils.end(event.drawContext);
    }

    private void renderNametagItem(ItemStack stack, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        String name = Names.get(stack);
        String count = " x" + stack.getCount();
        double nameWidth = text.getWidth(name, shadow);
        double countWidth = text.getWidth(count, shadow);
        double heightDown = text.getHeight(shadow);
        double width = nameWidth;
        if (this.itemCount.get()) {
            width = nameWidth + countWidth;
        }

        double widthHalf = width / 2.0;
        this.drawBg(-widthHalf, -heightDown, width, heightDown, null);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        double var20 = text.render(name, hX, hY, (Color) this.nameColor.get(), shadow);
        if (this.itemCount.get()) {
            text.render(count, var20, hY, this.GOLD, shadow);
        }

        text.end();
        NametagUtils.end();
    }

    private void renderGenericNametag(LivingEntity entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        String nameText = entity.getType().getName().getString();
        nameText = nameText + " ";
        float absorption = entity.getAbsorptionAmount();
        int health = Math.round(entity.getHealth() + absorption);
        double healthPercentage = health / (entity.getMaxHealth() + absorption);
        String healthText = String.valueOf(health);
        Color healthColor = Color.WHITE;
        double nameWidth = text.getWidth(nameText, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double heightDown = text.getHeight(shadow);
        double width = nameWidth + healthWidth;
        double widthHalf = width / 2.0;
        this.drawBg(-widthHalf, -heightDown, width, heightDown, entity);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        hX = text.render(nameText, hX, hY, (Color) this.nameColor.get(), shadow);
        text.render(healthText, hX, hY, healthColor, shadow);
        text.end();
        NametagUtils.end();
    }

    private void renderTntNametag(TntEntity entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        String fuseText = ticksToTime(entity.getFuse());
        double width = text.getWidth(fuseText, shadow);
        double heightDown = text.getHeight(shadow);
        double widthHalf = width / 2.0;
        this.drawBg(-widthHalf, -heightDown, width, heightDown, entity);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        text.render(fuseText, hX, hY, (Color) this.nameColor.get(), shadow);
        text.end();
        NametagUtils.end();
    }

    private ItemStack getItem(PlayerEntity entity, int index) {
        return switch (index) {
            case 0 -> entity.getMainHandStack();
            case 1 -> (ItemStack) entity.getInventory().armor.get(3);
            case 2 -> (ItemStack) entity.getInventory().armor.get(2);
            case 3 -> (ItemStack) entity.getInventory().armor.get(1);
            case 4 -> (ItemStack) entity.getInventory().armor.get(0);
            case 5 -> entity.getOffHandStack();
            default -> ItemStack.EMPTY;
        };
    }

    private void drawBg(double x, double y, double width, double height, @Nullable Entity entity) {
        double x1 = x + this.offsetX.get();
        double y1 = y + this.offsetY.get();
        double width1 = width + this.sizeX.get();
        double height1 = height + this.sizeY.get();
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x1 - 1.0, y1 - 1.0, width1 + 2.0, height1 + 2.0, (Color) this.background.get());
        Renderer2D.COLOR.render(null);
        if (entity != null) {
            if (entity instanceof LivingEntity) {
                double healthWidth = Math.min(width1 * (((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth()), width1);
                Renderer2D.COLOR.begin();
                Renderer2D.COLOR.quad(x1, y1 + height1, healthWidth, 2.0, new Color(255, 255, 255, 255));
                Renderer2D.COLOR.render(null);
            }
        }
    }

    public boolean excludeBots() {
        return this.ignoreBots.get();
    }

    public boolean playerNametags() {
        return this.isActive() && ((Set) this.entities.get()).contains(EntityType.PLAYER);
    }

    public static enum DistanceColorMode {
        Gradient,
        Flat;
    }

    public static enum Durability {
        None,
        Total,
        Percentage;
    }

    public static enum Position {
        Above,
        OnTop;
    }
}
