package hua223.calamity.loots;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

@BaseLootContextPacker.GlobalLootType(GlobalLoot.FISHING_LOOTS)
public class FishingLootContext extends BaseLootContextPacker {
    private final float luck;
    public final Entity entity;
    public final ServerPlayer player;

    private Holder<Biome> biomeHolder;

    public FishingLootContext(ObjectArrayList<ItemStack> generatedLoot, LootContext context, Entity hook) {
        super(generatedLoot, context, hook.level().random);
        entity = hook;
        luck = Math.max(1, context.getLuck());
        player = context.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof ServerPlayer p ? p : null;
    }

    public boolean matchBiome(ResourceKey<Biome> biome) {
        return getBiomeHolder().is(biome);
    }

    public boolean matchBiome(TagKey<Biome> biome) {
        return getBiomeHolder().is(biome);
    }

    public Holder<Biome> getBiomeHolder() {
        if (biomeHolder == null) biomeHolder = entity.level().getBiome(entity.getOnPos());
        return biomeHolder;
    }

    public boolean luckChance(float chance) {
        return super.chance(chance * luck);
    }

    @Override
    public boolean triggeredByPlayers() {
        return player != null;
    }
}
