package hua223.calamity.mixins;

import hua223.calamity.mixed.ICalamityMagicExpand;
import hua223.calamity.mixed.ISelfCast;
import hua223.calamity.register.Items.edible.ManaPotion;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.network.ClientboundSyncMana;
import io.redspace.ironsspellbooks.setup.Messages;
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
    private byte calamity$EnchantedStarfishUseCount;
    @Unique
    private byte calamity$MagicItemLeveL;
    @Unique
    private boolean calamity$AutomaticUsePotion;
;

    @Inject(method = "loadNBTData", at = @At("TAIL"))
    private void calamity$Load(CompoundTag compound, CallbackInfo ci) {
        calamity$EnchantedStarfishUseCount = compound.getByte("StarfishUseCount");
        calamity$MagicItemLeveL = compound.getByte("MagicItemLeveL");
    }

    @Inject(method = "saveNBTData", at = @At("TAIL"))
    private void calamity$Save(CompoundTag compound, CallbackInfo ci) {
        compound.putByte("StarfishUseCount", calamity$EnchantedStarfishUseCount);
        compound.putByte("MagicItemLeveL", calamity$MagicItemLeveL);
    }

    @Override
    public boolean calamity$TryUseMagicItem(int level, int value, String name) {
        if ((calamity$MagicItemLeveL & 1 << level) == 0) {
            calamity$MagicItemLeveL = (byte) (calamity$MagicItemLeveL | 1 << level);
            UUID uuid = UUID.nameUUIDFromBytes(("MagicItemLeveL" + calamity$MagicItemLeveL).getBytes());
            serverPlayer.getAttribute(AttributeRegistry.MAX_MANA.get())
                .addPermanentModifier(new AttributeModifier(uuid, name, 75, AttributeModifier.Operation.ADDITION));
            serverPlayer.getAttribute(AttributeRegistry.SPELL_POWER.get())
                .addPermanentModifier(new AttributeModifier(uuid, name, 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
            return true;
        }

        return false;
    }

    @Override
    public boolean calamity$UsePotionMana(float consume, boolean sync) {
        if (calamity$AutomaticUsePotion && consume <= serverPlayer.getAttributeValue(AttributeRegistry.MANA_REGEN.get())) {
            ItemStack[] manaPotions = serverPlayer.getInventory().items.stream().filter(item -> item.getItem() instanceof ManaPotion)
                .sorted(Comparator.comparing(stack -> (ManaPotion) stack.getItem())).toArray(ItemStack[]::new);

                for (ItemStack stack : manaPotions) {
                    ManaPotion potion = (ManaPotion) stack.getItem();
                    for (int i = 0; i < stack.getCount(); i++) {
                        potion.apply(this, false, serverPlayer);
                        stack.shrink(1);
                        if (mana >= consume) {
                            if (sync) Messages.sendToPlayer(new ClientboundSyncMana(cast()), serverPlayer);
                            return true;
                        }
                    }
                }
        }

        return false;
    }

    @Override
    public void calamity$TryUseEnchantedStarfish(LivingEntity entity, int value) {
        if (calamity$EnchantedStarfishUseCount < 10) {
            entity.getAttribute(AttributeRegistry.MAX_MANA.get())
                .addPermanentModifier(new AttributeModifier(UUID.nameUUIDFromBytes(("EnchantedStarfish" + value).getBytes()),
                    "EnchantedStarfish", value, AttributeModifier.Operation.ADDITION));
            calamity$EnchantedStarfishUseCount++;
        }
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
            Messages.sendToPlayer(new ClientboundSyncMana(cast()), serverPlayer);
    }

    @Override
    public boolean calamity$ConsumeMana(float mana) {
        if (this.mana >= mana || calamity$UsePotionMana(mana, false)) {
            setMana(this.mana - mana);
            if (serverPlayer != null)
                Messages.sendToPlayer(new ClientboundSyncMana(cast()), serverPlayer);
            return true;
        }

        return false;
    }
}
