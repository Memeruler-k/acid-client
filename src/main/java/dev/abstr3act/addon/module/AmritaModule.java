package dev.abstr3act.addon.module;

import dev.abstr3act.addon.utils.abnormally.AChatUtils;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class AmritaModule extends BaseModule {
    public AmritaModule(Category category, String name, String description) {
        super(category, name, description);
    }

    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && this.chatFeedback && this.mc.world != null) {
            ChatUtils.forceNextPrefixClass(this.getClass());
            Text msg = Text.of(Formatting.WHITE + (this.isActive() ? "§2[§a+§2]§f " : "§4[§c-§4]§f ") + "§f" + this.name + "§f");
            AChatUtils.sendMsgAmrita(msg);
        }
    }

    public void sendMsg(String string) {
        AChatUtils.sendMsgAmrita(Text.of(string));
    }

    public List<BlockEntity> getBlockEntities() {
        List<BlockEntity> list = new ArrayList<>();

        for (WorldChunk chunk : this.getLoadedChunks()) {
            list.addAll(chunk.getBlockEntities().values());
        }

        return list;
    }

    public List<WorldChunk> getLoadedChunks() {
        List<WorldChunk> chunks = new ArrayList<>();
        int viewDist = this.mc.options.getViewDistance().getValue();

        for (int x = -viewDist; x <= viewDist; x++) {
            for (int z = -viewDist; z <= viewDist; z++) {
                WorldChunk chunk = this.mc.world.getChunkManager().getWorldChunk((int) this.mc.player.getX() / 16 + x, (int) this.mc.player.getZ() / 16 + z);
                if (chunk != null) {
                    chunks.add(chunk);
                }
            }
        }

        return chunks;
    }
}
