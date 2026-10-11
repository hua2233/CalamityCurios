package hua223.calamity.register.entity;

import hua223.calamity.render.IllusionBufferSource;
import hua223.calamity.render.entity.ColorfulLightningBoltRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@AutoEntityRegister(trackingRange = 16, updateInterval = Integer.MAX_VALUE,
    renderClass = ColorfulLightningBoltRenderer.class, name = "calamity_lightning_bolt")
public class ColorfulLightningBolt extends LightningBolt {
    private float[] color = {0, 0, 0, 0};

    public ColorfulLightningBolt(EntityType<? extends LightningBolt> type, Level pLevel) {
        super(type, pLevel);
    }

    public void setColor(int color) {
        //Mapping default colors through hexadecimal，Their calculations are simple and convenient
        this.color[0] = color == -1 ? 0x66737340 : color;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(this, (int) color[0]);
        color = null;
        return packet;
    }

    @OnlyIn(Dist.CLIENT)
    public MultiBufferSource getColorfulBuffer(MultiBufferSource source) {
       return IllusionBufferSource.getSource(color[0], color[1], color[2], color[3]);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        int packColor = packet.getData();
        color[0] = FastColor.ARGB32.red(packColor) / 255f;
        color[1] = FastColor.ARGB32.green(packColor) / 255f;
        color[2] = FastColor.ARGB32.blue(packColor) / 255f;
        color[3] = FastColor.ARGB32.alpha(packColor) / 255f;
    }
}
