package hua223.calamity.capability;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.PersistentCurseFontSync;
import hua223.calamity.register.gui.SpellType;
import hua223.calamity.render.CurseFont;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class EnchantmentProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<CurseEnchantment> CURSE_ENCHANTMENT = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final String FONT_FLAG = "curse";
    private static final Map<Item, EnchantmentProvider> EXHUMED = new Object2ObjectOpenHashMap<>(6);

    protected CurseEnchantment enchantment;
    private final LazyOptional<CurseEnchantment> optional = LazyOptional.of(() -> enchantment);

    //render
    private int[] styleSet;

    private EnchantmentProvider() {
        enchantment = new CurseEnchantment();
    }

    /**
     * 设置默认的渲染数据到服务端，此方法默认在服务端调用，同步客户端请使用具体的渲染参数方法
     *
     * @param stack 需要长久被渲染的物品
     */
    public static void addDefaultProvider(ItemStack stack) {
        int start = stack.getRarity().color.getColor();
        EnchantmentProvider provider = new EnchantmentProvider();
        provider.styleSet = new int[]{1, start, 0, 0};
        EXHUMED.put(stack.getItem(), provider);
    }

    public static void addStyleProvider(Item item, boolean gradual, int start, int end, int semiCycle, boolean isServerSide) {
        EnchantmentProvider provider = new EnchantmentProvider();
        if (isServerSide) provider.styleSet = new int[]{gradual ? 0 : 1, start, end, semiCycle};
        else CurseFont.createFont(item, gradual, start, end, semiCycle);
        EXHUMED.put(item, provider);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isExhumed(ItemStack stack) {
        return EXHUMED.containsKey(stack.getItem());
    }

    public static void syncRenderData(ServerPlayer player) {
        Set<Map.Entry<Item, EnchantmentProvider>> providers = EXHUMED.entrySet();
        PersistentCurseFontSync fontSync = new PersistentCurseFontSync(providers.size());
        for (Map.Entry<Item, EnchantmentProvider> entry : providers) {
            int[] styleSet = entry.getValue().styleSet;
            fontSync.addSync(entry.getKey(), styleSet[0] == 0, styleSet[1], styleSet[2], styleSet[3]);
        }
        NetMessages.sendToClient(fontSync, player);
    }

    public static void addIfCan(ResourceLocation key, AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        Item item = stack.getItem();
        if (EXHUMED.containsKey(item)) {
            stack.getOrCreateTag().putString(FONT_FLAG, "EXHUMED");
            event.addCapability(key, EXHUMED.get(item));
        } else if (SpellType.anyMatch(stack)) {
            event.addCapability(key, new EnchantmentProvider());
        }
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

