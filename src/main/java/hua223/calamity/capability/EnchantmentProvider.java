package hua223.calamity.capability;

import hua223.calamity.util.CalamityHelp;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnchantmentProvider implements ICapabilitySerializable<CompoundTag> {
    private static final ObjectOpenHashSet<Item> EXHUMED = new ObjectOpenHashSet<>();
    public static final Capability<RuneEnchantment> CURSE_ENCHANTMENT = CapabilityManager.get(new CapabilityToken<>() {});

    protected RuneEnchantment enchantment;
    private final LazyOptional<RuneEnchantment> optional = LazyOptional.of(() -> enchantment);

    private EnchantmentProvider() {
        enchantment = new RuneEnchantment();
    }

    public static void addExhumedItem(Item item) {
        EXHUMED.add(item);
    }

    public static void addIfCan(ResourceLocation key, AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        Item item = stack.getItem();
        event.addCapability(key, new EnchantmentProvider());
        if (EXHUMED.contains(item)) stack.getOrCreateTag().putInt(CalamityHelp.FONT_FLAG, 1);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return capability == CURSE_ENCHANTMENT ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return enchantment.saveNbt();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        enchantment.loadNbt(tag);
    }
}

