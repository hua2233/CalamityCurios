package hua223.calamity.loots;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hua223.calamity.main.CalamityCurios;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import static hua223.calamity.main.CalamityCurios.MODID;

public class GlobalLootModifier extends LootModifier {
    private final GlobalLoot loot;

    public GlobalLootModifier(LootItemCondition[] conditions, GlobalLoot loot) {
        super(conditions);
        this.loot = loot;
    }

    public static void register(IEventBus bus) {
        DeferredRegister<Codec<? extends IGlobalLootModifier>> codec =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
        final Codec<GlobalLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .and(Codec.STRING.fieldOf("loot_type").forGetter(g -> g.loot.toString()))
            .apply(inst, (conditions, type) ->
                //If the corresponding enumeration type cannot be found, an error will be reported directly,
                //and forcibly throwing it may result in unexpected errors
                new GlobalLootModifier(conditions, GlobalLoot.valueOf(type))));
        codec.register("global_loot_modifier", () -> CODEC);
        codec.register(bus);
        LootTableTypeCondition.register(bus);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        return loot.apply(generatedLoot, context);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get()
            .getValue(CalamityCurios.ModResource("global_loot_modifier"));
    }
}
