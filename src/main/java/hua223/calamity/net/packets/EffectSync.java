package hua223.calamity.net.packets;

import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

@CommunicationDirection(NetworkDirection.PLAY_TO_CLIENT)
public class EffectSync extends DataPack {
    private final ClientboundUpdateMobEffectPacket packet;

    public EffectSync(int id, MobEffectInstance instance) {
        this.packet = new ClientboundUpdateMobEffectPacket(id, instance);
    }

    public EffectSync(FriendlyByteBuf buf) {
        this.packet = new ClientboundUpdateMobEffectPacket(buf);
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        packet.write(byteBuf);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void handler(NetworkEvent.Context context) {
        Entity entity = Minecraft.getInstance().level.getEntity(packet.getEntityId());
        if (entity instanceof LivingEntity) {
            MobEffect mobeffect = packet.getEffect();
            MobEffectInstance mobeffectinstance = new MobEffectInstance(mobeffect, packet.getEffectDurationTicks(), packet.getEffectAmplifier(),
                packet.isEffectAmbient(), packet.isEffectVisible(), packet.effectShowsIcon(), null, Optional.ofNullable(packet.getFactorData()));
            ((LivingEntity)entity).calamity$ForciblyAddEffect(mobeffectinstance, null);
        }
    }
}
