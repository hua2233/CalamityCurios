package hua223.calamity.integration.curios;

import com.google.common.collect.Multimap;
import hua223.calamity.register.RegisterList;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class Decks extends BaseCurio {
    private final Set<Card> subCards = new ObjectOpenHashSet<>(9);

    protected Decks() {
        super(RegisterList.CURIOS_CALAMITY);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        if(slotContext.entity() instanceof ServerPlayer player) {
            boolean unsealing = isUnsealing(stack);
            if (unsealing) CalamityHelp.setCalamityFlag(player, 10, true);

            for (Card subCard : getCards()) {
                if (unsealing) subCard.onEquip(slotContext, prevStack, stack);
                if (subCard instanceof ICuriosStorage storage)
                    storage.addToStorage(player);
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        if (slotContext.entity() instanceof ServerPlayer player) {
            CalamityHelp.setCalamityFlag(slotContext.entity(), 10, false);
            boolean unsealing = isUnsealing(stack);
            for (Card subCard : getCards()) {
                if (unsealing) subCard.onUnequip(slotContext, newStack, stack);
                if (subCard instanceof ICuriosStorage storage)
                    storage.removeStorage(player);
            }
        }
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (isUnsealing(stack))
            for (Card card : getCards())
                card.setAttributeModifiers(uuid, stack, modifier, equipped);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide && entity.calamity$IsPlayer && isUnsealing(stack)) {
            for (Card subCard : subCards) subCard.onPlayerTick(entity.calamity$Player);
            onPlayerTick(entity.calamity$Player);
        }
    }

    protected final boolean isUnsealing(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("Unsealing");
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(slotContext.entity()).resolve();
        if (optional.isPresent()) {
            IItemHandlerModifiable handler = optional.get().getEquippedCurios();
            for (int i = 0; i < handler.getSlots(); i++) {
                Item item = handler.getStackInSlot(i).getItem();
                if (item == this || (item instanceof Card card && card.getAffiliatedWith() == this))
                    return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return isUnsealing(stack);
    }

    public void unblock(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Unsealing", true);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        boolean unsealing = isUnsealing(stack);
        Component component = Component.translatable(getDescriptionId(stack))
            .withStyle(unsealing ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA);
        component.getSiblings().add(unsealing ? Component.literal(" ● 启") : Component.literal(" ● 封"));
        return component;
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putBoolean("Unsealing", false);
        return stack;
    }

    public void fillItemCategory(CreativeModeTab.Output output) {
        output.accept(getDefaultInstance());
        ItemStack stack = new ItemStack(this);
        unblock(stack);
        output.accept(stack);
    }

    public final Set<Card> getCards() {
        return subCards;
    }

    public abstract Item getUnsealingRope();

//    @Override
//    //The default does not have storage attributes, it is only for the correct creation of storage mappings for the deck cards
//    public void addToStorage(Player player) {
//        if (getCountSize() != 0) ICuriosStorage.super.addToStorage(player);
//        for (Card subCard : getCards())
//            if (subCard instanceof ICuriosStorage storage)
//                storage.addToStorage(player);
//    }
}