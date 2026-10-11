package hua223.calamity.events.listeners;

import hua223.calamity.events.EventConstructor;
import hua223.calamity.net.IDataPackResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;

public class TrackedListener extends BaseListener<PlayerEvent.StartTracking> {
    //触发者，监测者本体
    public final ServerPlayer tracker;
    //需求同步者，刚刚被监测到的玩家，跟踪者客户端已创建实例但需求同步其他数据
    public final ServerPlayer trackedPlayer;

    @EventConstructor
    public TrackedListener(PlayerEvent.StartTracking event) {
        super(event);
        tracker = (ServerPlayer) event.getEntity();
        trackedPlayer = (ServerPlayer) event.getTarget();
    }

    //同步跟踪玩家
    public final void sendTrackedToTheSelf(CompoundTag tag, IDataPackResponse response) {
        writeTracker(tag, trackedPlayer);
        response.sendToClient(tracker);
    }

    public static void writeTracker(CompoundTag tag, ServerPlayer player) {
        tag.putInt("id", player.getId());
    }

    @Nullable
    @SuppressWarnings("ConstantConditions")
    public static AbstractClientPlayer getInitiator(CompoundTag tag) {
        return (AbstractClientPlayer) Minecraft.getInstance().level.getEntity(tag.getInt("id"));
    }

    @Override
    public void canceledEvent() {
        throw new UnsupportedOperationException("This event cannot be cancelled!");
    }
}
