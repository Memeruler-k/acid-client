package dev.abstr3act.addon.modules.Abnormally;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.AbnormallyModule;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

import java.util.Random;

public class AntiBlock extends AbnormallyModule {
    private static final String[] SPECIAL_SYMBOLS = new String[]{
        "ᴀ", "ʙ", "ᴄ", "ᴅ", "ᴇ", "ꜰ", "ɢ", "ʜ", "ɪ", "ᴊ", "ᴋ", "ʟ", "ᴍ", "ɴ", "ᴏ", "ᴩ", "q", "ʀ", "ꜱ", "ᴛ", "ᴜ", "ᴠ", "ᴡ", "x", "y", "ᴢ"
    };
    private static final String[] SPACES = new String[]{" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "　"};
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Setting<mode> Mode = this.sgGeneral
        .add(((Builder) ((Builder) ((Builder) new Builder().name("Bypass Mode")).description(".")).defaultValue(mode.StrictBypass)).build());

    public AntiBlock() {
        super(Compassion.ABNORMALLY, "anti-block", "Prevent your message to block by server");
    }

    public static String superBypass(String input) {
        Random random = new Random();
        StringBuilder convertedText = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (Character.isLetter(c)) {
                int choice = random.nextInt(3);
                switch (choice) {
                    case 0:
                        convertedText.append(Character.toUpperCase(c));
                        break;
                    case 1:
                        convertedText.append(Character.toLowerCase(c));
                        break;
                    case 2:
                        int index = Character.toLowerCase(c) - 'a';
                        if (index >= 0 && index < SPECIAL_SYMBOLS.length) {
                            convertedText.append(SPECIAL_SYMBOLS[index]);
                        } else {
                            convertedText.append(c);
                        }
                }
            } else {
                convertedText.append(c);
            }

            if (random.nextBoolean()) {
                String randomSpace = SPACES[random.nextInt(SPACES.length)];
                convertedText.append(randomSpace);
            }
        }

        return convertedText.toString();
    }

    public static String randomString() {
        Random random = new Random();
        int length = random.nextInt(5) + 2;
        StringBuilder hexBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int value = random.nextInt(16);
            hexBuilder.append(Integer.toHexString(value));
        }

        return "[" + hexBuilder.toString().toUpperCase() + "] ";
    }

    public static String insert(String input) {
        StringBuilder modifiedString = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            modifiedString.append(input.charAt(i));
            if (i < input.length() - 1) {
                String randomHex = randomString();
                modifiedString.append(randomHex);
            }
        }

        return modifiedString.toString();
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    @EventHandler
    public void onMessageSend(SendMessageEvent event) {
        String value = event.message;
        StringBuilder modifiedString = new StringBuilder();
        switch ((mode) this.Mode.get()) {
            case StrictBypass:
                Random random = new Random();
                String[] charList = new String[]{" ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", " ", "　"};

                for (int i = 0; i < value.length(); i++) {
                    modifiedString.append(value.charAt(i));
                    int randomLength = random.nextInt(3) + 1;

                    for (int j = 0; j < randomLength; j++) {
                        String randomChar = charList[random.nextInt(charList.length)];
                        modifiedString.append(randomChar);
                    }
                }

                event.message = modifiedString.toString();
                break;
            case NormalBypass:
                for (int i = 0; i < value.length(); i++) {
                    modifiedString.append(value.charAt(i)).append("\u200c");
                }

                event.message = String.valueOf(modifiedString);
                break;
            case StringObfuscation:
                event.message = insert(value);
                break;
            case SuperBypass:
                event.message = superBypass(randomString() + value + randomString());
        }
    }

    static enum mode {
        StrictBypass,
        NormalBypass,
        StringObfuscation,
        SuperBypass;
    }
}
