package hua223.calamity.integration.curios;

import com.google.common.collect.Multimap;
import hua223.calamity.register.RegisterList;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
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

import java.util.Set;
import java.util.UUID;

public abstract class Decks extends BaseCurio implements ICuriosStorage {
    private final Set<Card> subCards = new ObjectOpenHashSet<>(9);

    protected Decks() {
        super(RegisterList.CURIOS_CALAMITY);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        if (isUnsealing(stack)) for (Card curio : getCards()) curio.onEquip(slotContext, prevStack, stack);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        if (isUnsealing(stack)) CalamityHelp.setCalamityFlag(player, 10, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        if (isUnsealing(stack)) CalamityHelp.setCalamityFlag(player, 10, false);
    }

    protected final boolean isUnsealing(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("Unsealing");
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        IItemHandlerModifiable handler = CuriosApi.getCuriosHelper().getEquippedCurios(slotContext.entity()).orElseGet(null);
        for (int i = 0; i < handler.getSlots(); i++) {
            Item item = handler.getStackInSlot(i).getItem();
            if (item == this || item instanceof Card card && card.getAffiliatedWith() == this)
                return false;
        }
        return true;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
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
    //The default does not have storage attributes, it is only for the correct creation of storage mappings for the deck cards
    public void addToStorage(Player player) {
        if (getCountSize() != 0) ICuriosStorage.super.addToStorage(player);
        for (Card subCard : getCards())
            if (subCard instanceof ICuriosStorage storage)
                storage.addToStorage(player);
    }

    @Override
    public int getCountSize() {
        return 0;
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putBoolean("Unsealing", false);
        return stack;
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (isUnsealing(stack)) {
            for (Card card : getCards())
                card.setAttributeModifiers(uuid, stack, modifier, equipped);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        for (BaseCurio curio : getCards()) curio.onUnequip(slotContext, newStack, stack);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level.isClientSide && isUnsealing(stack))
            onPlayerTick(entity.calamity$Player);
    }

    @Override

   protected void onPlayerTick(Player player) {
        for (Card curio : getCards())
            if (curio.startServerTick())
                curio.onPlayerTick(player);
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        if (allowedIn(category)) {
            items.add(getDefaultInstance());
            ItemStack stack = new ItemStack(this);
            unblock(stack);
            items.add(stack);
        }
    }

    public final Set<Card> getCards() {
        return subCards;
    }

    public abstract Item getUnsealingRope();
}