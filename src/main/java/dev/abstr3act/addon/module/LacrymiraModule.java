package dev.abstr3act.addon.module;

import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LacrymiraModule extends BaseModule {
    public LacrymiraModule(Category category, String name, String description) {
        super(category, name, description);
    }

    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && this.chatFeedback && this.mc.world != null) {
            ChatUtils.forceNextPrefixClass(this.getClass());
            Text msg = Text.of(Formatting.WHITE + (this.isActive() ? "§2[§a+§2]§f " : "§4[§c-§4]§f ") + "§f" + this.name + "§f");
            AChatUtils.sendMsgLacrymira(msg);
        }
    }

    public void sendMsg(String string) {
        AChatUtils.sendMsgLacrymira(Text.of(string));
    }
}
