package hua223.calamity.net;

import hua223.calamity.net.packets.EffectResponsePack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;

/**
 * @see IDataPackResponse
 */
public interface IEffectDataResponse extends IDataPackResponse {
    @Override
    default DataPack createNetPack() {
       return new EffectResponsePack((MobEffect) this, PACK);
    }
}
