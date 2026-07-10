package dev.abstr3act.addon;

import dev.abstr3act.addon.command.*;
import dev.abstr3act.addon.events.legacy.MotionEvent;
import dev.abstr3act.addon.hud.*;
import dev.abstr3act.addon.hud.keyStrokes.KeyStrokes;
import dev.abstr3act.addon.hud.notifications.NotificationsHudElement;
import dev.abstr3act.addon.hud.targethud.NewTargetHud;
import dev.abstr3act.addon.hud.text.CompassionTextHud;
import dev.abstr3act.addon.manager.AsyncManager;
import dev.abstr3act.addon.manager.RotationManager;
import dev.abstr3act.addon.modules.Abnormally.*;
import dev.abstr3act.addon.modules.Amrita.*;
import dev.abstr3act.addon.modules.Amrita.autoweb.AutoWeb;
import dev.abstr3act.addon.modules.Amrita.criticals.CriticalsPlus;
import dev.abstr3act.addon.modules.Amrita.crystalac.CrystalAC;
import dev.abstr3act.addon.modules.Amrita.hackerdetector.AntiCheat;
import dev.abstr3act.addon.modules.Amrita.invtotem.InventoryTotem;
import dev.abstr3act.addon.modules.Amrita.jesus.JesusPlus;
import dev.abstr3act.addon.modules.Amrita.killaura.KillAuraPlus;
import dev.abstr3act.addon.modules.Amrita.latency.Backtrack;
import dev.abstr3act.addon.modules.Amrita.latency.FakeLag;
import dev.abstr3act.addon.modules.Amrita.nofall.NoFallPlus;
import dev.abstr3act.addon.modules.Amrita.noslow.NoSlowPlus;
import dev.abstr3act.addon.modules.Amrita.scaffold3.Scaffold;
import dev.abstr3act.addon.modules.Amrita.spider.SpiderPlus;
import dev.abstr3act.addon.modules.Amrita.surround.Surround;
import dev.abstr3act.addon.modules.Amrita.velocity.VelocityPlus;
import dev.abstr3act.addon.modules.Compassion.*;
import dev.abstr3act.addon.modules.Fragment.AntiBot;
import dev.abstr3act.addon.modules.Fragment.PathFinder;
import dev.abstr3act.addon.modules.Lacrymira.*;
import dev.abstr3act.addon.modules.Lacrymira.BowAura.BowAura;
import dev.abstr3act.addon.modules.Luna.*;
import dev.abstr3act.addon.modules.Selena.GrimTridentDisabler;
import dev.abstr3act.addon.modules.Selena.GrimTridentFly;
import dev.abstr3act.addon.modules.Selena.VulcanBoatFly;
import dev.abstr3act.addon.modules.Selena.VulcanBoatJump;
import dev.abstr3act.addon.modules.Seraphim.*;
import dev.abstr3act.addon.modules.Seraphim.AutoBlock.AutoBlock;
import dev.abstr3act.addon.modules.Seraphim.InvTotem.InvTotem;
import dev.abstr3act.addon.modules.Seraphim.acidbot.AcidBot;
import dev.abstr3act.addon.modules.Seraphim.clicker.Clicker;
import dev.abstr3act.addon.modules.Seraphim.fly.SeraphimFly;
import dev.abstr3act.addon.modules.Seraphim.speed.SeraphimSpeed;
import dev.abstr3act.addon.setting.DoubleListSetting;
import dev.abstr3act.addon.utils.LogUtils;
import dev.abstr3act.addon.utils.PlayerManager;
import dev.abstr3act.addon.utils.RotationUtil;
import dev.abstr3act.addon.utils.render.Render2DEngine;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.orbit.EventHandler;

public class Compassion extends MeteorAddon {
    public static final Category ABNORMALLY = new Category("Abnormally");
    public static final Category COMPASSION = new Category("Compassion");
    public static final Category SERAPHIM = new Category("Seraphim");
    public static final Category LACRYMIRA = new Category("Lacrymira");
    public static final Category LUNA = new Category("Luna");
    public static final Category SELENA = new Category("Selena");
    public static final Category AMRITA = new Category("AMRITA");
    public static final Category CLIENT = new Category("Client");
    public static final HudGroup HUD_GROUP = new HudGroup("New HUD");
    public static final PlayerManager playerManager = new PlayerManager();
    public static final AsyncManager asyncManager = new AsyncManager();
    public static final RotationManager ROTATION = new RotationManager();
    public static final RotationUtil UTIL = new RotationUtil();
    public static String username;
    long t = 0L;

    public static boolean fullNullCheck() {
        return MeteorClient.mc.player == null || MeteorClient.mc.world == null;
    }

    public void onInitialize() {
        LogUtils.getLogger().info("[Compassion] initializing modules.");
        this.initializeHud(Hud.get());
        Modules.get().add(new MaceKill());
        Modules.get().add(new MaceAura());
        Modules.get().add(new SuperVClip());
        Modules.get().add(new KouBot());
        Modules.get().add(new PacketDebugger());
        Modules.get().add(new AntiBlock());
        Modules.get().add(new AutoEZ());
        Modules.get().add(new SuperSpammer());
        Modules.get().add(new PopLag());
        Modules.get().add(new PopClip());
        Modules.get().add(new FakePlayer());
        Modules.get().add(new MoveDistance());
        Modules.get().add(new InventoryTotem());
        Modules.get().add(new AntiMiss());
        Modules.get().add(new JelloESP());
        Modules.get().add(new KillEffects());
        Modules.get().add(new AutoMoan());
        Modules.get().add(new Confuse());
        Modules.get().add(new PaperDupe());
        Modules.get().add(new SuperInstaMine());
        Modules.get().add(new CrystalChams());
        Modules.get().add(new TextLag());
        Modules.get().add(new AutoText());
        Modules.get().add(new OPLagAura());
        Modules.get().add(new OPPopLag());
        Modules.get().add(new AutoSex());
        Modules.get().add(new AntiCrash());
        Modules.get().add(new AntiBookBan());
        Modules.get().add(new AntiGhostBlock());
        Modules.get().add(new ClientSpoof());
        Modules.get().add(new AutoWither());
        Modules.get().add(new NoGround());
        Modules.get().add(new ExtraBind());
        Modules.get().add(new LagAura());
        Modules.get().add(new LowHopMiss());
        Modules.get().add(new VulcanBoatJump());
        Modules.get().add(new MinecartAura());
        Modules.get().add(new AutoCompassion());
        Modules.get().add(new TPAura());
        Modules.get().add(new ChatEncryption());
        Modules.get().add(new RandomSuffix());
        Modules.get().add(new AutoItem());
        Modules.get().add(new TitleDetector());
        Modules.get().add(new DeathNotifier());
        Modules.get().add(new AntiWeakness());
        Modules.get().add(new MultiTask());
        Modules.get().add(new AutoFire());
        Modules.get().add(new RapidFire());
        Modules.get().add(new GrimTridentDisabler());
        Modules.get().add(new GrimTridentFly());
        Modules.get().add(new InstaMine());
        Modules.get().add(new SandPop());
        Modules.get().add(new VulcanBoatFly());
        Modules.get().add(new DesyncESP());
        Modules.get().add(new Orbit());
        Modules.get().add(new KillSay());
        Modules.get().add(new NewPopLag());
        Modules.get().add(new Ignore());
        Modules.get().add(new NewTextLag());
        Modules.get().add(new MissHelper());
        Modules.get().add(new Predict());
        Modules.get().add(new SilentTotem());
        Modules.get().add(new CaptureESP());
        Modules.get().add(new TPUse());
        Modules.get().add(new PacketEat());
        Modules.get().add(new MaceMissLite());
        Modules.get().add(new AutoSell());
        Modules.get().add(new Disabler());
        Modules.get().add(new ServerCrasher());
        Modules.get().add(new AntiResourcePack());
        Modules.get().add(new AntiExceptions());
        Modules.get().add(new TPBot());
        Modules.get().add(new Fly());
        Modules.get().add(new MoveFix());
        Modules.get().add(new BedBase());
        Modules.get().add(new Surround());
        Modules.get().add(new NewBlink());
        Modules.get().add(new AutoWeb());
        Modules.get().add(new NoJumpDelay());
        Modules.get().add(new BlockSelection());
        Modules.get().add(new Animations());
        Modules.get().add(new SpeedMine());
        Modules.get().add(new TpMine());
        Modules.get().add(new BlurSetting());
        Modules.get().add(new TriggerBot());
        Modules.get().add(new AutoSprint());
        Modules.get().add(new AimBot());
        Modules.get().add(new UwUESP());
        Modules.get().add(new ConsoleSpammer());
        Modules.get().add(new AutoLag());
        Modules.get().add(new ReverseStealer());
        Modules.get().add(new PlayerNotifier());
        Modules.get().add(new TPLava());
        Modules.get().add(new SeraphimFly());
        Modules.get().add(new SeraphimSpeed());
        Modules.get().add(new AutoSwordCart());
        Modules.get().add(new VaultTool());
        Modules.get().add(new TestKillAura());
        Modules.get().add(new AnchorAssist());
        Modules.get().add(new CrystalObiAssist());
        Modules.get().add(new InvTotem());
        Modules.get().add(new AutoSkill());
        Modules.get().add(new CartAssist());
        Modules.get().add(new BowAssist());
        Modules.get().add(new CrystalOptimizer());
        Modules.get().add(new AutoSoup());
        Modules.get().add(new AutoRekit());
        Modules.get().add(new AutoPick());
        Modules.get().add(new LacrymiraAura());
        Modules.get().add(new NoServerSlot());
        Modules.get().add(new MaceAssist());
        Modules.get().add(new StaffAlert());
        Modules.get().add(new BalanceJump());
        Modules.get().add(new SwitchAssist());
        Modules.get().add(new AutoKitPVP());
        Modules.get().add(new ColorSetting());
        Modules.get().add(new NoDeathAnimation());
        Modules.get().add(new ChestFiller());
        Modules.get().add(new FlagLogger());
        Modules.get().add(new TPPlace());
        Modules.get().add(new TPAttack());
        Modules.get().add(new TPItem());
        Modules.get().add(new TPCrossbow());
        Modules.get().add(new AutoFreeze());
        Modules.get().add(new AutoRegen());
        Modules.get().add(new AutoBuff());
        Modules.get().add(new TPBowGod());
        Modules.get().add(new CommandAura());
        Modules.get().add(new NoBadInteracts());
        Modules.get().add(new BowAura());
        Modules.get().add(new TotemTint());
        Modules.get().add(new SwingAnimation());
        Modules.get().add(new TotemDebugger());
        Modules.get().add(new ExplosiveESP());
        Modules.get().add(new Avoid());
        Modules.get().add(new Freeze());
        Modules.get().add(new HUDSetting());
        Modules.get().add(new Media());
        Modules.get().add(new AntiBot());
        Modules.get().add(new PathFinder());
        Modules.get().add(new NewSprint());
        Modules.get().add(new AutoCoyote());
        Modules.get().add(new LongJump());
        Modules.get().add(new KillAura());
        Modules.get().add(new Phase());
        Modules.get().add(new AutoCrystal());
        Modules.get().add(new PacketDebuggerV2());
        Modules.get().add(new PotionNotifier());
        Modules.get().add(new NametagsV2());
        Modules.get().add(new Auto32K());
        Modules.get().add(new CrossbowCart());
        Modules.get().add(new AutoJumpReset());
        Modules.get().add(new TargetStrafe());
        Modules.get().add(new AcidBot());
        Modules.get().add(new Aura());
        Modules.get().add(new AntiCheat());
        Modules.get().add(new SuperKnockback());
        Modules.get().add(new AntiDigger());
        Modules.get().add(new Glide());
        Modules.get().add(new Clicker());
        Modules.get().add(new AutoBlock());
        Modules.get().add(new VelocityV2());
        Modules.get().add(new NoSlowV2());
        Modules.get().add(new AntiWeb());
        Modules.get().add(new HitParticles());
        Modules.get().add(new SilentTotemTest());
        Modules.get().add(new dev.abstr3act.addon.modules.Seraphim.KillAura());
        Modules.get().add(new DesignAnt());
        Modules.get().add(new DurabilityAlert());
        Modules.get().add(new AutoShop());
        Modules.get().add(new InventoryMove());
        Modules.get().add(new ChestStealer());
        Modules.get().add(new TridentDupe());
        Modules.get().add(new MotionCamera());
        Modules.get().add(new RemoteRegear());
        Modules.get().add(new Repeat());
        Modules.get().add(new TwoDItem());
        Modules.get().add(new BHop());
        Modules.get().add(new AutoBalley());
        Modules.get().add(new CriticalsPlus());
        Modules.get().add(new JesusPlus());
        Modules.get().add(new KillAuraPlus());
        Modules.get().add(new NoFallPlus());
        Modules.get().add(new NoSlowPlus());
        Modules.get().add(new Protocol());
        Modules.get().add(new LightningDetector());
        Modules.get().add(new Derp());
        Modules.get().add(new VelocityPlus());
        Modules.get().add(new SpiderPlus());
        Modules.get().add(new AutoPot());
        Modules.get().add(new AutoDripstone());
        Modules.get().add(new RageBot());
        Modules.get().add(new SneakDerp());
        Modules.get().add(new ProjectileAura());
        Modules.get().add(new FixMove());
        Modules.get().add(new InventoryCleaner());
        Modules.get().add(new InvManager());
        Modules.get().add(new DripStoneESP());
        Modules.get().add(new Scaffold());
        Modules.get().add(new DoubleAnchor());
        Modules.get().add(new CrystalAC());
        Modules.get().add(new dev.abstr3act.addon.modules.Amrita.Scaffold());
        Modules.get().add(new FakeLag());
        Modules.get().add(new Backtrack());
        Modules.get().add(new InteractFix());
        Modules.get().add(new DupeResolver());
        MeteorClient.EVENT_BUS.subscribe(this);
        Commands.add(new NotificationsCommand());
        Commands.add(new TextLagCommand());
        Commands.add(new FontCommand());
        Commands.add(new AcidBotCommand());
        Commands.add(new TestCommand());
        Commands.add(new ForceTargetCommand());
        Commands.add(new StrafeCommand());
        Commands.add(new Test2Command());
        SettingsWidgetFactory.registerCustomFactory(DoubleListSetting.class, theme -> (table, setting) -> {
            if (setting instanceof DoubleListSetting doubleListSetting) {
                DoubleListSetting.doubleListW(table, doubleListSetting);
            } else {
                throw new IllegalArgumentException("Setting is not an instance of DoubleListSetting.");
            }
        });
        LogUtils.getLogger()
            .info("[Compassion] Successfully loaded " + Modules.get().getList().size() + " modules in " + (System.currentTimeMillis() - this.t) + "ms");
        LogUtils.getLogger().warn("[Compassion] If there are any text render translate offsets or render errors, please press R to fix the offsets");
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (MeteorClient.mc.player != null) {
            long currentTime = System.nanoTime();
            if (RotationUtil.lastTime == 0L) {
                RotationUtil.lastTime = currentTime;
            }

            double deltaTime = (currentTime - RotationUtil.lastTime) / 1.0E9;
            RotationUtil.lastTime = currentTime;
            if (RotationUtil.baseYaw >= 180.0F) {
                RotationUtil.baseYaw = -180.0F;
            } else if (RotationUtil.baseYaw <= -180.0F) {
                RotationUtil.baseYaw = 180.0F;
            }

            if (RotationUtil.currentYaw != null && RotationUtil.currentPitch != null) {
                RotationUtil.interpolatedYaw = RotationUtil.interpolateAngle(event.yaw, RotationUtil.currentYaw, 180.0F, (float) (20.0 * deltaTime), false);
                RotationUtil.interpolatedPitch = RotationUtil.interpolateAngle(event.pitch, RotationUtil.currentPitch, 180.0F, (float) (20.0 * deltaTime), true);
                if (Float.isNaN(RotationUtil.interpolatedYaw)
                    || Float.isNaN(RotationUtil.interpolatedPitch)
                    || Float.isInfinite(RotationUtil.interpolatedYaw)
                    || Float.isInfinite(RotationUtil.interpolatedPitch)) {
                    RotationUtil.reset();
                    return;
                }

                event.yaw = RotationUtil.interpolatedYaw;
                event.pitch = RotationUtil.interpolatedPitch;
                MeteorClient.mc.player.bodyYaw = MeteorClient.mc.player.getYaw();
                RotationUtil.pitchTick++;
            }
        }
    }

    public void onRegisterCategories() {
        LogUtils.getLogger()
            .info(
                "\n  /$$$$$$                                                                /$$                    \n /$$__  $$                                                              |__/                    \n| $$  \\__/  /$$$$$$  /$$$$$$/$$$$   /$$$$$$  /$$$$$$   /$$$$$$$ /$$$$$$$ /$$  /$$$$$$  /$$$$$$$ \n| $$       /$$__  $$| $$_  $$_  $$ /$$__  $$|____  $$ /$$_____//$$_____/| $$ /$$__  $$| $$__  $$\n| $$      | $$  \\ $$| $$ \\ $$ \\ $$| $$  \\ $$ /$$$$$$$|  $$$$$$|  $$$$$$ | $$| $$  \\ $$| $$  \\ $$\n| $$    $$| $$  | $$| $$ | $$ | $$| $$  | $$/$$__  $$ \\____  $$\\____  $$| $$| $$  | $$| $$  | $$\n|  $$$$$$/|  $$$$$$/| $$ | $$ | $$| $$$$$$$/  $$$$$$$ /$$$$$$$//$$$$$$$/| $$|  $$$$$$/| $$  | $$\n \\______/  \\______/ |__/ |__/ |__/| $$____/ \\_______/|_______/|_______/ |__/ \\______/ |__/  |__/\n                                  | $$                                                          \n                                  | $$                                                          \n                                  |__/                                                          "
            );
        this.t = System.currentTimeMillis();
        username = "FracturePrism";
        LogUtils.getLogger().info("Welcome user: [" + username + "] " + MeteorClient.mc.getSession().getUsername());
        LogUtils.getLogger().info("[Compassion] initializing shaders.");
        Render2DEngine.initShaders();
        LogUtils.getLogger().info("[Compassion] initializing category.");
        Modules.registerCategory(ABNORMALLY);
        Modules.registerCategory(COMPASSION);
        Modules.registerCategory(LUNA);
        Modules.registerCategory(SELENA);
        Modules.registerCategory(SERAPHIM);
        Modules.registerCategory(LACRYMIRA);
        Modules.registerCategory(CLIENT);
        Modules.registerCategory(AMRITA);
    }

    private void initializeHud(Hud hud) {
        LogUtils.getLogger().info("[Compassion] initializing HUDs.");
        hud.register(CatGirl.INFO);
        hud.register(ClientBoard.INFO);
        hud.register(NotificationsHudElement.INFO);
        hud.register(PopNotifier.INFO);
        hud.register(Icon.INFO);
        hud.register(Particle.INFO);
        hud.register(NewArrayList.INFO);
        hud.register(NewTargetHud.INFO);
        hud.register(Username.INFO);
        hud.register(STFUIcon.INFO);
        hud.register(Welcomer.INFO);
        hud.register(CooldownHUD.INFO);
        hud.register(GappleHUD.INFO);
        hud.register(NewInventoryHud.INFO);
        hud.register(CompassionTextHud.INFO);
        hud.register(Potions.INFO);
        hud.register(AccountInfo.INFO);
        hud.register(HealthBar.INFO);
        hud.register(OxygenBar.INFO);
        hud.register(ArmorBar.INFO);
        hud.register(HungerBar.INFO);
        hud.register(HotBar.INFO);
        hud.register(Radar.INFO);
        hud.register(FontDebugger.INFO);
        hud.register(KeyStrokes.INFO);
        hud.register(ElytraIndicator.INFO);
        hud.register(NewArmorHud.INFO);
        hud.register(ScoreboardHud.INFO);
        hud.register(TextRadar.INFO);
        hud.register(EventTimer.INFO);
        hud.register(dev.abstr3act.addon.hud.Compassion.INFO);
    }

    public String getPackage() {
        return "dev.abstr3act.addon";
    }

    public String getVersion() {
        return "dev_5.7.10";
    }
}
