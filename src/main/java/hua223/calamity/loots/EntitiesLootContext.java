package hua223.calamity.loots;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nullable;

@BaseLootContextPacker.GlobalLootType(GlobalLoot.ENTITY_LOOTS)
public class EntitiesLootContext extends BaseLootContextPacker {
    public final Entity killer;
    public final Entity entity;
    private int playerLootingLevel;
    public final DamageSource source;
    public final ServerPlayer player;

    public EntitiesLootContext(ObjectArrayList<ItemStack> generatedLoot, LootContext context,
                               RandomSource random, Entity entity, Entity killer) {
        super(generatedLoot, context, random);
        this.entity = entity;
        this.killer = killer;
        this.source = context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);

        if (killer instanceof ServerPlayer p) player = p;
        else if (source != null && source.getEntity() instanceof ServerPlayer p) player = p;
        else player = null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Entity> T verification(EntityType<T> type) {
        return onlyVerification(type) ? (T) entity : null;
    }

    public boolean lootingChance(float chance) {
        return chance(chance * getPlayerLootingLevel());
    }

    public int getPlayerLootingLevel() {
        return player == null ? 0 : playerLootingLevel == 0 ? (playerLootingLevel = 1 + EnchantmentHelper.getMobLooting(player)) : playerLootingLevel;
    }

    public boolean onlyVerification(EntityType<?> type) {
        return entity.getType() == type;
    }

    public boolean isDamageType(TagKey<DamageType> tag) {
        return source != null && source.is(tag);
    }

    @Override
    public boolean triggeredByPlayers() {
        return player != null;
    }

    public boolean onlyKiller(EntityType<?> type) {
        return killer != null && killer.getType() == type;
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T killer(EntityType<T> type) {
        return onlyKiller(type) ? (T) killer : null;
    }
}
