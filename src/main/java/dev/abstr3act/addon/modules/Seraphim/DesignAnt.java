package dev.abstr3act.addon.modules.Seraphim;

import dev.abstr3act.addon.Compassion;
import dev.abstr3act.addon.module.SeraphimModule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DesignAnt extends SeraphimModule {
    public DesignAnt() {
        super(Compassion.SERAPHIM, "DesignAnt", "sb");
    }

    public String replaceName(String string) {
        if (string != null && this.isActive()) {
            Pattern pattern = Pattern.compile("(?i)ant");
            Matcher matcher = pattern.matcher(string);
            StringBuilder result = new StringBuilder();

            while (matcher.find()) {
                String match = matcher.group();
                String replacement = "§c" + match + "§r";
                matcher.appendReplacement(result, replacement);
            }

            matcher.appendTail(result);
            return result.toString();
        } else {
            return string;
        }
    }
}
