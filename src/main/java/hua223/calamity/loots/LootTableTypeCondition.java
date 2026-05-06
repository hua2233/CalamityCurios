package hua223.calamity.loots;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static hua223.calamity.main.CalamityCurios.MODID;

public class LootTableTypeCondition implements LootItemCondition {
    private final String path;

    private LootTableTypeCondition(final String path) {
        this.path = path;
    }

    public static void register(IEventBus bus) {
        DeferredRegister<LootItemConditionType> condition = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MODID);
        final LootItemConditionType LOOT_TABLE_ID = new LootItemConditionType(new LootTableTypeCondition.TypeSerializer());
        condition.register("table_type", () -> LOOT_TABLE_ID);
        condition.register(bus);
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return Objects.requireNonNull(BuiltInRegistries.LOOT_CONDITION_TYPE.get(CalamityCurios.ModResource("table_type")));
    }

    public static LootTableTypeCondition of(String path) {
        return new LootTableTypeCondition(path);
    }

    @Override
    public boolean test(LootContext context) {
        //if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer)) return false;
        return context.getQueriedLootTableId().getPath().startsWith(path);
    }

    public static class TypeSerializer implements Serializer<LootTableTypeCondition> {
        @Override
        public void serialize(JsonObject object, LootTableTypeCondition instance, @NotNull JsonSerializationContext ctx) {
            object.addProperty("table_type", instance.path);
        }

        @Override
        public @NotNull LootTableTypeCondition deserialize(@NotNull JsonObject object, @NotNull JsonDeserializationContext ctx) {
            return new LootTableTypeCondition(GsonHelper.getAsString(object, "table_type"));
        }
    }
}
