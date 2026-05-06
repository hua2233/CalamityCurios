package hua223.calamity.mixins;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.mixed.ICalamityMagicExpand;
import hua223.calamity.mixed.ISelfCast;
import hua223.calamity.register.Items.edible.ManaPotion;
import hua223.calamity.util.VariableAttributeModifier;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.UUID;

@Mixin(value = MagicData.class, remap = false)
public abstract class MagicDataMixin implements ICalamityMagicExpand, ISelfCast<MagicData> {
    @Shadow private ServerPlayer serverPlayer;
    @Shadow private float mana;

    @Shadow public abstract float getMana();

    @Shadow public abstract void addMana(float mana);

    @Shadow public abstract void setMana(float mana);

    @Unique
    private byte calamity$MagicItemLeveL;
    @Unique
    private boolean calamity$AutomaticUsePotion;
;

    @Inject(method = "loadNBTData", at = @At("TAIL"))
    private void calamity$Load(CompoundTag compound, HolderLookup.Provider provider, CallbackInfo ci) {
        calamity$MagicItemLeveL = compound.getByte("MagicItemLeveL");
    }

    @Inject(method = "saveNBTData", at = @At("TAIL"))
    private void calamity$Save(CompoundTag compound, HolderLookup.Provider provider, CallbackInfo ci) {
        compound.putByte("MagicItemLeveL", calamity$MagicItemLeveL);
    }

    @Override
    public boolean calamity$TryUseMagicItem(int level, String name) {
        if (level > 7) {
            CalamityCurios.LOGGER.warn("Invalid magic item bit index");
        } else if ((calamity$MagicItemLeveL & 1 << level) == 0) {
            calamity$MagicItemLeveL = (byte) (calamity$MagicItemLeveL | 1 << level);
            UUID uuid = UUID.nameUUIDFromBytes("MagicItem".getBytes());
            VariableAttributeModifier.createOrIncrease(serverPlayer, AttributeRegistry.MAX_MANA.get(), uuid,
                name, 75, 600, AttributeModifier.Operation.ADDITION);
            VariableAttributeModifier.createOrIncrease(serverPlayer, AttributeRegistry.SPELL_POWER.get(), uuid,
                name, 0.05, 0.4, AttributeModifier.Operation.MULTIPLY_BASE);
            return true;
        }

        return false;
    }

    @Override
    public boolean calamity$UsePotionMana(float consume, boolean sync) {
        if (calamity$AutomaticUsePotion && consume <= serverPlayer.getAttributeValue(AttributeRegistry.MAX_MANA.get())) {
            ItemStack[] manaPotions = serverPlayer.getInventory().items.stream().filter(item -> item.getItem() instanceof ManaPotion)
                .sorted(Comparator.comparing(stack -> (ManaPotion) stack.getItem())).toArray(ItemStack[]::new);

                for (ItemStack stack : manaPotions) {
                    ManaPotion potion = (ManaPotion) stack.getItem();
                    for (int i = 0; i < stack.getCount(); i++) {
                        potion.apply(this, false, serverPlayer);
                        stack.shrink(1);
                        if (mana >= consume) {
                            if (sync) PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(cast()));
                            return true;
                        }
                    }
                }
        }

        return false;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void calamity$TryUseEnchantedStarfish(LivingEntity entity) {
        VariableAttributeModifier.createOrIncrease(entity, AttributeRegistry.MAX_MANA.get(), null,
            "EnchantedStarfish", 20, 200, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void calamity$SetAutomaticUsePotion(boolean auto) {
        this.calamity$AutomaticUsePotion = auto;
    }

    @Override
    public float calamity$GetMana() {
        return getMana();
    }

    @Override
    public void calamity$ChangeMana(float mana, boolean sync) {
        addMana(mana);
        if (sync && serverPlayer != null)
            PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(cast()));
    }

    @Override
    public boolean calamity$ConsumeMana(float mana) {
        if (this.mana >= mana || calamity$UsePotionMana(mana, false)) {
            setMana(this.mana - mana);
            if (serverPlayer != null)
                PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(cast()));
            return true;
        }

        return false;
    }
}
