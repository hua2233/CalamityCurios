package hua223.calamity.loots;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;

public abstract class BaseLootContextPacker {
    protected final ObjectArrayList<ItemStack> generatedLoot;
    protected final LootContext context;
    public final RandomSource source;
    private boolean cancelDrop;

    public BaseLootContextPacker(ObjectArrayList<ItemStack> generatedLoot, LootContext context, RandomSource source) {
        this.generatedLoot = generatedLoot;
        this.context = context;
        this.source = source;
    }

    public ObjectArrayList<ItemStack> getGeneratedLoot() {
        return generatedLoot;
    }

    public LootContext getContext() {
        return context;
    }

    public boolean isCancelDrop() {
        return cancelDrop;
    }

    public void setCancelDrop() {
        cancelDrop = true;
        generatedLoot.clear();
    }

    public void addLoot(Item item, int count) {
        addLoot(new ItemStack(item, count));
    }

    public void addLoot(ItemStack stack) {
        generatedLoot.add(stack);
    }

    public int getRandomCount(int min, int max) {
        return Mth.nextInt(source, min, max);
    }

    public void addRandomLoot(int chance, ItemStack... stack) {
        int i = getRandomCount(0, stack.length - 1);
        if (chance > 100) chance = 100;

        if (getRandomCount(1, 100) <= chance)
            generatedLoot.add(stack[i]);
    }

    public ItemStack removeLoot(Item item) {
        Iterator<ItemStack> iterator = generatedLoot.iterator();
        while (iterator.hasNext()) {
            ItemStack loot = iterator.next();
            if (loot.is(item)) {
                iterator.remove();
                return loot;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean chance(float rate) {
        return source.nextFloat() < rate;
    }

    public boolean chance(float rate, int count) {
        for (int i = 0; i < count; i++)
            if (source.nextFloat() < rate)
                return true;

        return false;
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface GlobalLootType {
        GlobalLoot value();
    }
}
