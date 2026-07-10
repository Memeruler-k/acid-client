package dev.abstr3act.addon.events;

import io.netty.channel.ChannelPipeline;

public class EventPipeline {
    private final ChannelPipeline channelPipeline;
    private final boolean local;

    public EventPipeline(ChannelPipeline channelPipeline, boolean local) {
        this.channelPipeline = channelPipeline;
        this.local = local;
    }

    public ChannelPipeline getChannelPipeline() {
        return this.channelPipeline;
    }

    public boolean isLocal() {
        return this.local;
    }
}
