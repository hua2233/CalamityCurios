package hua223.calamity.events.levelevent.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.util.Vector2f;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public abstract class ClientLevelEvent {
    private static ClientLevelEvent activeWorldEvent;
    protected static EventSkyRender render;
    public static int borderRenderDistance;
    protected static float skyDarkenValue;
    protected static float oldDarkenValue;
    private static float screenShakePower;
    private static boolean screenShakeProcessed;

    protected boolean stop;

    public ClientLevelEvent() {
        if (activeWorldEvent != null) {
            activeWorldEvent.interruptEvent();
            render = null;
        }
        MinecraftForge.EVENT_BUS.register(this);
        activeWorldEvent = this;
    }

    public boolean isStop() {
        return stop;
    }

    @SubscribeEvent
    public final void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (event.getPlayer() != null) activeWorldEvent.interruptEvent();
    }

    @SubscribeEvent
    public final void setSkyFog(ViewportEvent.ComputeFogColor event) {
        if (render != null) render.setSkyFogColor(event);
    }

    protected void victoryEvent() {
        interruptEvent();
    }

    protected void failureEvent() {
        interruptEvent();
    }

    public void interruptEvent() {
        stop = true;
        activeWorldEvent = null;
        render = null;
        borderRenderDistance = 0;
        skyDarkenValue = 0;
        oldDarkenValue = 0;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public static ClientLevelEvent getActiveWorldEvent() {
        return activeWorldEvent;
    }

    public static EventSkyRender getRender() {
        return render;
    }

    public float getSkyDarkenValue(float partialTick) {
        return 1f;
    }

    public static void setScreenShakePower(float power) {
        screenShakeProcessed = false;
        screenShakePower = power;
    }

    @SuppressWarnings("ConstantConditions")
    public static void screenShakeHandle(ViewportEvent.ComputeCameraAngles event) {
        if (screenShakePower > 0) {
            Vector2f offset = Vector2f.nextVector2Circular(screenShakePower,
                screenShakePower, Minecraft.getInstance().level.random);
            event.setRoll(event.getRoll() + offset.x);
            event.setYaw(event.getYaw() + offset.y);
            if (!screenShakeProcessed) {
                screenShakeProcessed = true;
                DelayRunnable.addRunTask(3, () -> {
                    if (screenShakeProcessed) {
                        screenShakeProcessed = false;
                        screenShakePower = Mth.clamp(screenShakePower - 0.185f, 0f, 10f);
                    }
                });
            }
        }
    }

    public abstract void handlerDataPack(CompoundTag tag, IDataPackResponse parser);

    public abstract static class EventSkyRender {
        public abstract void renderSky(PoseStack poseStack, Minecraft minecraft, Matrix4f projectionMatrix,
                                     float partialTick, VertexBuffer skyBuffer, VertexBuffer darkBuffer, ClientLevel level);

        public abstract void setSkyFogColor(ViewportEvent.ComputeFogColor event);
    }
}
