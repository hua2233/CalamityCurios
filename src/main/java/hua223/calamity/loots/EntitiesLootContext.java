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
    public final DamageSource source;
    public EntitiesLootContext(ObjectArrayList<ItemStack> generatedLoot, LootContext context,
                               RandomSource source, Entity entity, ServerPlayer player) {
        super(generatedLoot, context, source);
        this.entity = entity;
        this.player = player;
        this.source = context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
    }

    @Nullable
    public <T extends Entity> T verification(EntityType<T> type) {
        return onlyVerification(type) ? (T) entity : null;
    }

    public boolean onlyVerification(EntityType<?> type) {
        return entity.getType() == type;
    }
}
