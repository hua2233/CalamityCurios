package hua223.calamity.loots;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nullable;

@BaseLootContextPacker.GlobalLootType(GlobalLoot.ENTITY_LOOTS)
public class EntitiesLootContext extends BaseLootContextPacker {
    public final Entity entity;
    public final ServerPlayer player;
    public final Entity killer;
    public final DamageSource source;
    public EntitiesLootContext(ObjectArrayList<ItemStack> generatedLoot, LootContext context,
                               RandomSource source, Entity entity, Entity killer) {
        super(generatedLoot, context, source);
        this.entity = entity;
        this.killer = killer;
        this.player = killer != null && killer.getType() == EntityType.PLAYER ? (ServerPlayer) killer : null;
        this.source = context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Entity> T verification(EntityType<T> type) {
        return onlyVerification(type) ? (T) entity : null;
    }

    public boolean onlyVerification(EntityType<?> type) {
        return entity.getType() == type;
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
        return onlyVerification(type) ? (T) killer : null;
    }
}
