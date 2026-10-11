package hua223.calamity.net.packets;

import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import hua223.calamity.register.damage.DamageSupplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.List;

@CommunicationDirection(NetworkDirection.PLAY_TO_SERVER)
public class PrismRayHit extends DataPack {
    private int[] ids;

    public PrismRayHit(List<LivingEntity> ids) {
        this.ids = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++)
            this.ids[i] = ids.get(i).getId();
    }

    public PrismRayHit(FriendlyByteBuf buf) {
        ids = buf.readVarIntArray(buf.readVarInt());
    }

    @OnlyIn(Dist.CLIENT)
    public void expansion(List<LivingEntity> ids) {
        int rawLength = this.ids.length;
        int[] expansionIds = new int[ids.size() + rawLength];
        System.arraycopy(this.ids, 0, expansionIds, 0, rawLength);
        for (int i = 0; i < ids.size(); i++)
            expansionIds[rawLength + i] = ids.get(i).getId();
        this.ids = expansionIds;
    }

    @Override
    protected void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeVarInt(ids.length);
        byteBuf.writeVarIntArray(ids);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        Level level = player.level();
        Arrays.sort(ids);
        DamageSource source = DamageSupplier.MAGIC_PROJECTILE.get(player);
        float baseAttack = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        for (int i = 0; i < ids.length; i++) {
            int magnification = 1;
            int id = ids[i];
            while (i + 1 < ids.length && id == ids[i + 1]) {
                magnification++;
                i++;
            }

            if (level.getEntity(id) instanceof LivingEntity entity)
                entity.hurt(source, baseAttack * magnification);
        }
    }
}
