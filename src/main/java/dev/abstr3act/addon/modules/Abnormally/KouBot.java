package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.utils.abnormally.BlockUtil;
import dev.abstr3act.addon.utils.abnormally.TPUtil;
import dev.abstr3act.addon.utils.abnormally.httpUtil;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.BoolSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.Script;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KouBot extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgAI = this.settings.createGroup("AI");
    private final SettingGroup sgLog = this.settings.createGroup("Logout");
    private final Setting<Boolean> autoLog = this.sgLog
        .add(((Builder) ((Builder) ((Builder) new Builder().name("auto-log")).description("Auto disconnect while in danger")).defaultValue(true)).build());
    private final Setting<Integer> health = this.sgLog
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("health"))
                .description("Automatically disconnects when health is lower or equal to this value."))
                .defaultValue(6))
                .range(0, 19)
                .sliderMax(19)
                .visible(this.autoLog::get))
                .build()
        );
    private final Setting<Boolean> smart = this.sgLog
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("smart")).description("Disconnects when you're about to take enough damage to kill you."))
                .defaultValue(true))
                .visible(this.autoLog::get))
                .build()
        );
    private final Setting<Boolean> onlyTrusted = this.sgLog
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("only-trusted"))
                .description("Disconnects when a player not on your friends list appears in render distance."))
                .defaultValue(false))
                .visible(this.autoLog::get))
                .build()
        );
    private final Setting<Boolean> instantDeath = this.sgLog
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("32K")).description("Disconnects when a player near you can instantly kill you."))
                .defaultValue(false))
                .visible(this.autoLog::get))
                .build()
        );
    private final Setting<Boolean> crystalLog = this.sgLog
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("crystal-nearby")).description("Disconnects when a crystal appears near you."))
                .defaultValue(false))
                .visible(this.autoLog::get))
                .build()
        );
    private final Setting<Integer> range = this.sgLog
        .add(
            ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) ((meteordevelopment.meteorclient.settings.IntSetting.Builder) new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                .name("range"))
                .description("How close a crystal has to be to you before you disconnect."))
                .defaultValue(4))
                .range(1, 10)
                .sliderMax(5)
                .visible(this.crystalLog::get))
                .visible(this.autoLog::get))
                .build()
        );
    private final Setting<Boolean> smartToggle = this.sgLog
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("smart-toggle"))
                .description("Disables Auto Log after a low-health logout. WILL re-enable once you heal."))
                .defaultValue(false))
                .visible(this.autoLog::get))
                .build()
        );
    private final Setting<Boolean> toggleOff = this.sgLog
        .add(
            ((Builder) ((Builder) ((Builder) ((Builder) new Builder().name("toggle-off")).description("Disables Auto Log after usage.")).defaultValue(true))
                .visible(this.autoLog::get))
                .build()
        );
    private final Setting<String> prefix = this.sgAI
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("prefix"))
                .description("Prefix to trigger command"))
                .defaultValue("#"))
                .build()
        );
    private final Setting<List<String>> operatorName = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) ((meteordevelopment.meteorclient.settings.StringListSetting.Builder) new meteordevelopment.meteorclient.settings.StringListSetting.Builder()
                .name("messages"))
                .description("Messages to use for spam."))
                .defaultValue(List.of("Meteor on Crack!")))
                .build()
        );
    private final Setting<Boolean> noAdmin = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("public-access")).description("If enabled, any one can use the bot")).defaultValue(true)).build());
    private final Setting<String> destruct_command = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("command"))
                .description("suicide command (kill suicide 514)"))
                .defaultValue("kill"))
                .build()
        );
    private final Setting<Boolean> privateMessage = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("private-message")).description("Public the bot message")).defaultValue(true)).build());
    private final Setting<String> command = this.sgGeneral
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("command"))
                .description("whisper command (msg, w, tell)"))
                .defaultValue("tell"))
                .visible(this.privateMessage::get))
                .build()
        );
    private final Setting<String> name = this.sgAI
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("name"))
                .description("Name of the bot"))
                .defaultValue("Sakura"))
                .build()
        );
    private final Setting<String> key = this.sgAI
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("key"))
                .description("API key"))
                .defaultValue("sk-lxNcfnMFNZtXuSQrgmOoqWQzOeXadhqnZncxJUJsFSYwCtup"))
                .build()
        );
    private final Setting<String> api = this.sgAI
        .add(
            ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) ((meteordevelopment.meteorclient.settings.StringSetting.Builder) new meteordevelopment.meteorclient.settings.StringSetting.Builder()
                .name("api"))
                .description("Chat bot API"))
                .defaultValue("https://api.chatarts.org/v1/chat/completions"))
                .build()
        );
    private final Setting<module> Module = this.sgAI
        .add(
            ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) ((meteordevelopment.meteorclient.settings.EnumSetting.Builder) new meteordevelopment.meteorclient.settings.EnumSetting.Builder()
                .name("AI-module"))
                .description("The module of bot"))
                .defaultValue(module.gpt_3_5_turbo_0125))
                .build()
        );
    private final Setting<Boolean> debug = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("debug")).description("Bot debugger (set operator to self)")).defaultValue(true)).build());
    private final StaticListener staticListener = new StaticListener();
    String msg;

    public KouBot() {
        super(Compassion.ABNORMALLY, "KouBot", "Bot Controller.");
    }

    private static String convertHexToString(String str) {
        String[] hexPairs = str.split("(?<=\\G.{2})");
        StringBuilder resultBuilder = new StringBuilder();

        for (String hexPair : hexPairs) {
            int decimalValue = Integer.parseInt(hexPair, 16);
            resultBuilder.append(Character.toChars(decimalValue));
        }

        return resultBuilder.toString();
    }

    private static String convertStringToHex(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] charArray = str.toCharArray();

        for (char c : charArray) {
            String charToHex = Integer.toHexString(c);
            stringBuilder.append(charToHex);
        }

        return stringBuilder.toString();
    }

    public List getOperatorName() {
        return this.debug.get() && this.mc.player != null ? Collections.singletonList(this.mc.player.getName().getString()) : this.operatorName.get();
    }

    private String getCommand() {
        return "/" + (String) this.command.get() + " ";
    }

    private String getSuicideCommand() {
        return "/" + (String) this.destruct_command.get();
    }

    @EventHandler
    public void onReceivedMessage(ReceiveMessageEvent event) {
        String message = event.getMessage().getString();
        String[] parts = message.split("> ", 2);
        if (parts.length >= 2) {
            String playerName = parts[0].substring(parts[0].indexOf(60) + 1);
            String messageContent = parts[1].replace("\u200c", "").replace("\u200f", "");
            if (messageContent.startsWith((String) this.prefix.get())) {
                if (!this.noAdmin.get() && !this.getOperatorName().contains(playerName) && !this.debug.get()) {
                    this.wrongOperator();
                    return;
                }

                parts = messageContent.split(" ");
                String cmd = parts[0];
                if (Objects.equals(cmd, (String) this.prefix.get() + "enable")) {
                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    Module mod = Modules.get().get(parts[1]);
                    if (mod == null) {
                        return;
                    }

                    if (!mod.isActive()) {
                        mod.toggle();
                    }

                    this.mc.player.networkHandler.sendChatMessage("Enabled " + mod.name);
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "disable")) {
                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    Module modx = Modules.get().get(parts[1]);
                    if (modx == null) {
                        return;
                    }

                    if (modx.isActive()) {
                        modx.toggle();
                    }

                    this.mc.player.networkHandler.sendChatMessage("Disabled " + modx.name);
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "sendme")) {
                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String value = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    Script script = MeteorStarscript.compile(value);
                    StringBuilder sb = new StringBuilder();
                    if (script != null) {
                        MeteorStarscript.run(script, sb);
                    }

                    if (sb.length() > 255) {
                        ChatUtils.sendMsg(Text.of("Text too long!"));
                        return;
                    }

                    ChatUtils.sendPlayerMsg(this.getCommand() + playerName + " [" + (String) this.name.get() + "] " + sb);
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "sakura")) {
                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String valuex = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    Script scriptx = MeteorStarscript.compile(valuex);
                    StringBuilder sbx = new StringBuilder();
                    this.doRequest(valuex, playerName);
                    if (scriptx != null) {
                        MeteorStarscript.run(scriptx, sbx);
                    }
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "bot")) {
                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String valuex = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    ChatUtils.sendPlayerMsg("#" + valuex);
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "send")) {
                    if (parts.length < 3) {
                        this.wrongArg();
                        return;
                    }

                    String target = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    ChatUtils.sendPlayerMsg(this.getCommand() + target);
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "test")) {
                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String valuex = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    StringBuilder modifiedString = new StringBuilder();

                    for (int i = 0; i < valuex.length(); i++) {
                        modifiedString.append(valuex.charAt(i)).append("\u200c\u200c");
                    }

                    ChatUtils.sendPlayerMsg(modifiedString + "\u200f");
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "pos")) {
                    if (!(this.operatorName.get()).contains(playerName)) {
                        this.noPermission();
                        return;
                    }

                    ChatUtils.sendPlayerMsg(
                        this.getCommand()
                            + playerName
                            + " I'm at position "
                            + Math.round(this.mc.player.getX())
                            + ", "
                            + Math.round(this.mc.player.getY())
                            + ", "
                            + Math.round(this.mc.player.getZ())
                            + "!"
                    );
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "logout")) {
                    if (!(this.operatorName.get()).contains(playerName)) {
                        this.noPermission();
                        return;
                    }

                    this.mc.player.networkHandler.sendChatMessage("Logout due to command triggered");
                    this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[Sakura] Triggered logout command from " + playerName)));
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "suicide")) {
                    if (!(this.operatorName.get()).contains(playerName)) {
                        this.noPermission();
                        return;
                    }

                    this.mc.player.networkHandler.sendChatMessage("Self destructed");
                    ChatUtils.sendPlayerMsg(this.getSuicideCommand());
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "clip")) {
                    if (!(this.operatorName.get()).contains(playerName)) {
                        this.noPermission();
                        return;
                    }

                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String valuex = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    if (this.isStringInt(valuex)) {
                        SuperVClip module = new SuperVClip();
                        this.doClip(
                            Integer.parseInt(valuex),
                            module.downClipWhenSneak.get(),
                            module.protectY.get(),
                            module.voidProtect.get(),
                            module.moveDistance.get(),
                            module.feedBack.get()
                        );
                    } else {
                        this.wrongArg();
                    }
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "add")) {
                    if (!(this.operatorName.get()).contains(playerName)) {
                        this.noPermission();
                        return;
                    }

                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String valuex = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    if (!(this.operatorName.get()).contains(valuex)) {
                        (this.operatorName.get()).add(valuex);
                        this.mc.player.networkHandler.sendChatMessage(valuex + " has been add to the operator list! ");
                    } else {
                        this.mc.player.networkHandler.sendChatMessage(valuex + " was already in the list! ");
                    }
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "remove")) {
                    if (!(this.operatorName.get()).contains(playerName)) {
                        this.noPermission();
                        return;
                    }

                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String valuex = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    if (!(this.operatorName.get()).contains(valuex)) {
                        (this.operatorName.get()).remove(valuex);
                        this.mc.player.networkHandler.sendChatMessage(valuex + " has been removed from the operator list! ");
                    } else {
                        this.mc.player.networkHandler.sendChatMessage(valuex + " was not in the list! ");
                    }
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "addfriend")) {
                    if (!(this.operatorName.get()).contains(playerName)) {
                        this.noPermission();
                        return;
                    }

                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String valuex = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    Friends.get().add(new Friend(valuex));
                    this.mc.player.networkHandler.sendChatMessage(valuex + " has been add to the friend list! ");
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "removefriend")) {
                    if (!(this.operatorName.get()).contains(playerName)) {
                        this.noPermission();
                        return;
                    }

                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String valuex = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    Friends.get().remove(new Friend(valuex));
                    this.mc.player.networkHandler.sendChatMessage(valuex + " has been removed from the friend list! ");
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "hex2str")) {
                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String valuex = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    this.mc.player.networkHandler.sendChatMessage(convertHexToString(valuex));
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "help")) {
                    this.mc.player.networkHandler.sendChatMessage("");
                } else if (Objects.equals(cmd, (String) this.prefix.get() + "str2hex")) {
                    if (parts.length < 2) {
                        this.wrongArg();
                        return;
                    }

                    String valuex = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
                    this.mc.player.networkHandler.sendChatMessage(convertStringToHex(valuex));
                } else {
                    this.wrongCommand(cmd);
                }
            }
        }
    }

    public void doClip(int clipValue, boolean downClipWhenSneak, int protectY, boolean voidProtect, Double moveDistance, boolean feedBack) {
        MinecraftClient mc = MinecraftClient.getInstance();
        double clipY = clipValue;
        Vec3d finalVec = mc.player.getPos();
        int offsetY = 0;
        int n = (int) clipY;
        if (offsetY <= n) {
            while (true) {
                Vec3d vec = new Vec3d(
                    finalVec.x, downClipWhenSneak && mc.player.isSneaking() ? finalVec.y - (clipY - offsetY) : finalVec.y + (clipY - offsetY), finalVec.z
                );
                if (vec.y > protectY && BlockUtil.isSafeBlock(BlockPos.ofFloored(vec))) {
                    finalVec = vec;
                    break;
                }

                if (offsetY == n) {
                    break;
                }

                offsetY++;
            }
        }

        if (voidProtect && finalVec.y <= -128.0) {
            finalVec = new Vec3d(finalVec.x, -128.0, finalVec.z);
        }

        Vec3d vec3d = mc.player.getPos();
        TPUtil.doTp(vec3d, finalVec, moveDistance, true);
        mc.player.setPosition(finalVec);
        if (feedBack) {
            mc.player.networkHandler.sendChatMessage("[Sakura] Try to clip " + clipValue + " m");
        }
    }

    public boolean isStringInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException var3) {
            return false;
        }
    }

    private void doRequest(String value, String playerName) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        try {
            CompletableFuture.<String>supplyAsync(() -> {
                try {
                    this.msg = httpUtil.sendPostRequest(value, (String) this.name.get(), (String) this.key.get(), (String) this.api.get(), (module) this.Module.get());
                    return this.msg;
                } catch (Exception var3x) {
                    var3x.printStackTrace();
                    return "Error occurred while processing the request: " + var3x.getMessage();
                }
            }, executorService).thenAccept(response -> {
                if (this.privateMessage.get()) {
                    ChatUtils.sendPlayerMsg("/tell " + playerName + " [" + (String) this.name.get() + "] " + this.msg);
                } else {
                    this.mc.player.networkHandler.sendChatMessage("[" + (String) this.name.get() + "] -> " + playerName + this.msg);
                }
            }).exceptionally(ex -> {
                ex.printStackTrace();
                ChatUtils.sendPlayerMsg("Error occurred: " + ex.getMessage());
                return null;
            }).whenComplete((result, ex) -> {
                executorService.shutdown();
                System.out.println("Shutdown service.");
            });
        } catch (Exception var5) {
            var5.printStackTrace();
            ChatUtils.sendPlayerMsg("Unexpected error occurred: " + var5.getMessage());
        }
    }

    private void noPermission() {
        this.mc.player.networkHandler.sendChatMessage("[Sakura] No Permission");
    }

    private void wrongArg() {
        this.mc.player.networkHandler.sendChatMessage("[Sakura] Invalid argument");
    }

    private void wrongOperator() {
        this.mc.player.networkHandler.sendChatMessage("[Sakura] Unknown bot operator");
    }

    private void wrongCommand(String command) {
        this.mc.player.networkHandler.sendChatMessage("[Sakura] Unknown command: " + command);
    }

    @EventHandler
    private void onTick(Post event) {
        if (this.autoLog.get()) {
            float playerHealth = this.mc.player.getHealth();
            if (playerHealth <= 0.0F) {
                this.toggle();
                return;
            }

            if (playerHealth <= (this.health.get()).intValue()) {
                this.mc.player.networkHandler.sendChatMessage("Logout due to low health");
                this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[AutoLog] Health was lower than " + this.health.get() + ".")));
                if (this.smartToggle.get()) {
                    this.toggle();
                    this.enableHealthListener();
                }
            }

            if (this.smart.get()
                && playerHealth + this.mc.player.getAbsorptionAmount() - PlayerUtils.possibleHealthReductions() < (this.health.get()).intValue()) {
                this.mc.player.networkHandler.sendChatMessage("Logout due to low health (predict)");
                this.mc
                    .player
                    .networkHandler
                    .onDisconnect(new DisconnectS2CPacket(Text.literal("[AutoLog] Health was going to be lower than " + this.health.get() + ".")));
                if (this.toggleOff.get()) {
                    this.toggle();
                }
            }

            for (Entity entity : this.mc.world.getEntities()) {
                if (entity instanceof PlayerEntity && entity.getUuid() != this.mc.player.getUuid()) {
                    if (this.onlyTrusted.get() && entity != this.mc.player && !Friends.get().isFriend((PlayerEntity) entity)) {
                        this.mc.player.networkHandler.sendChatMessage("Logout due to a non-trusted player appeared in render distance");
                        this.mc
                            .player
                            .networkHandler
                            .onDisconnect(new DisconnectS2CPacket(Text.literal("[AutoLog] A non-trusted player appeared in your render distance.")));
                        if (this.toggleOff.get()) {
                            this.toggle();
                        }
                        break;
                    }

                    if (PlayerUtils.isWithin(entity, 8.0)
                        && this.instantDeath.get()
                        && DamageUtils.getAttackDamage((PlayerEntity) entity, this.mc.player) > playerHealth + this.mc.player.getAbsorptionAmount()) {
                        this.mc.player.networkHandler.sendChatMessage("Logout due to anti-32k");
                        this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[AutoLog] Anti-32k measures.")));
                        if (this.toggleOff.get()) {
                            this.toggle();
                        }
                        break;
                    }
                }

                if (entity instanceof EndCrystalEntity && PlayerUtils.isWithin(entity, (this.range.get()).intValue()) && this.crystalLog.get()) {
                    this.mc.player.networkHandler.sendChatMessage("Logout due to end crystal appeared within specified range");
                    this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.literal("[AutoLog] End Crystal appeared within specified range.")));
                    if (this.toggleOff.get()) {
                        this.toggle();
                    }
                }
            }
        }
    }

    private void enableHealthListener() {
        if (this.autoLog.get()) {
            MeteorClient.EVENT_BUS.subscribe(this.staticListener);
        }
    }

    private void disableHealthListener() {
        MeteorClient.EVENT_BUS.unsubscribe(this.staticListener);
    }

    public static enum module {
        chatgpt_4o_latest("gpt-4o-latest"),
        gpt_3_5_turbo("gpt-3.5-turbo"),
        gpt_3_5_turbo_0125("gpt-3.5-turbo-0125"),
        gpt_3_5_turbo_1106("gpt-3.5-turbo-1106"),
        gpt_3_5_turbo_instruct("gpt-3.5-turbo-instruct"),
        gpt_4("gpt-4"),
        gpt_4_0125_preview("gpt-4-0125-preview"),
        gpt_4_0314("gpt-4-0314"),
        gpt_4_0613("gpt-4-0613"),
        gpt_4_1106_preview("gpt-4-1106-preview"),
        gpt_4_turbo("gpt-4-turbo"),
        gpt_4_turbo_2024_04_09("gpt-4-turbo-2024-04-09"),
        gpt_4_turbo_preview("gpt-4-turbo-preview"),
        gpt_4o("gpt-4o"),
        gpt_4o_2024_05_13("gpt-4o-2024-05-13"),
        gpt_4o_2024_08_06("gpt-4o-2024-08-06"),
        gpt_4o_mini("gpt-4o-mini"),
        gpt_4o_mini_2024_07_18("gpt-4o-mini-2024-07-18");

        private final String module;

        private module(String module) {
            this.module = module;
        }

        @Override
        public String toString() {
            return this.module;
        }
    }

    private class StaticListener {
        @EventHandler
        private void healthListener(Post event) {
            if (KouBot.this.isActive()) {
                KouBot.this.disableHealthListener();
            } else if (Utils.canUpdate() && !KouBot.this.mc.player.isDead() && KouBot.this.mc.player.getHealth() > (KouBot.this.health.get()).intValue()) {
                KouBot.this.toggle();
                KouBot.this.disableHealthListener();
            }
        }
    }
}
