package hua223.calamity.register.rune;

import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.capability.RuneEnchantment;
import hua223.calamity.events.EventTypes;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.main.CalamityLightBlock;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.rune.stone.Shining;
import hua223.calamity.register.rune.stone.Thorny;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.VariableAttributeModifier;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

//Only Armor
public abstract class ArmorRune extends Item implements IRuneReinforcement {
    public ArmorRune(Properties properties) {
        super(properties);
    }

    @Override
    public String getCodecName() {
        return "Rune";
    }

    protected static void addThornyEffect(ServerPlayer player, float value) {
        Thorny thorny = (Thorny) CalamityItems.THORNY.get();
        float[] data = player.Calamity$Player.getRuneData(thorny);
        data[0] += value;

        if (value > 0 && data[0] - value < value) EventTypes.applyEvent(thorny, player, true);
        else if (value < 0 && data[0] < value) EventTypes.applyEvent(thorny, player, false);
    }

    protected static void applyArmorLight(int level, ServerPlayer player) {
        if (level == 0) return;

        Shining shining = (Shining) CalamityItems.SHINING.get();
        int[] lightLevel = player.Calamity$Player.getRuneData(shining);
        lightLevel[0] += level;

        if (level > 0 && lightLevel[0] - level == 0) {
            DelayRunnable.conditionsLoop(() -> {
                if (lightLevel[0] <= 0 || player.isDeadOrDying()) return true;
                CalamityLightBlock.placePlayerDynamicLightSource(player, Math.min(15, lightLevel[0]));
                return false;
            }, 4);
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        Map<Attribute, Tuple<Double, AttributeModifier.Operation>> map = getAttribute();
        if (map != null) {
            UUID id = getSelfId();

            for (Map.Entry<Attribute, Tuple<Double, AttributeModifier.Operation>> entry : map.entrySet()) {
                AttributeInstance attribute = player.getAttribute(entry.getKey());
                Tuple<Double, AttributeModifier.Operation> tuple = entry.getValue();

                double amount = tuple.getA();
                VariableAttributeModifier modify = (VariableAttributeModifier) attribute.getModifier(id);

                if (to) {
                    if (modify == null) attribute.addTransientModifier(new VariableAttributeModifier(id, prefix(), amount, tuple.getB()));
                    else modify.addValue(amount, attribute);
                } else if (modify != null) {
                    double next = modify.getAmount() - amount;
                    if (next < amount) attribute.removeModifier(modify);
                    else modify.setValue(next, attribute);
                }
            }
        }
    }

    protected void removeOrApplyEventsBasedOnData(int slot, float change, boolean to, ServerPlayer player) {
        float[] data = player.Calamity$Player.getRuneData(this);
        if (to) {
            if (data[slot] < change) effectApply(true, player);
            data[slot] += change;
        } else if ((data[slot] -= change) <= change) effectApply(false, player);
    }

    protected void effectApply(boolean to, ServerPlayer player) {
        EventTypes.applyEvent(this, player, to);
    }

    public boolean canRemove() {
        return true;
    }

    @Override
    public void onRemove(ItemStack stack) {
        stack.resetHoverName();
    }

    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return null;
    }

    protected final UUID getSelfId() {
        return UUID.nameUUIDFromBytes(getClass().getName().getBytes());
    }

    @Override
    public @NotNull MutableComponent getName(@NotNull ItemStack stack) {
        return Component.translatable(getDescriptionId()).append(" ").append("[").append(Component.translatable(prefix())).append("]");
    }

    @Override
    public @NotNull String getDescriptionId() {
        return "item.calamity_curios.";
    }

    @Override
    public boolean isEffectiveSlot(EquipmentSlot slot) {
        return slot.isArmor();
    }

    @Override
    public void onApply(ItemStack stack) {
        stack.setHoverName(Component.translatable(prefix()).append(" ").append(stack.getItem().getName(stack)));
    }

    //Mixin
    public static void removeOrApplyRune(ItemStack baseItemStack, ArmorRune rune, ResultContainer resultSlots, CallbackInfo ci) {
        ItemStack result = baseItemStack.copy();
        Optional<RuneEnchantment> operation = result.getCapability(EnchantmentProvider.CURSE_ENCHANTMENT).resolve();
        if (operation.isEmpty()) return;
        RuneEnchantment enchantment = operation.get();
        IRuneReinforcement reinforcement = enchantment.getRunes();

        if ((rune == null && reinforcement != null && reinforcement.canRemove()) || rune != null && reinforcement == null && rune.canApply(result)) {
            enchantment.setRunes(result, rune);
            resultSlots.setItem(0, result);
            ci.cancel();
        }
    }

    protected abstract String prefix();

    @Override
    public boolean canApply(ItemStack stack) {
        return Predicate.ARMOR_CLASS.test(stack);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void saveNbt(CompoundTag tag) {
        IRuneReinforcement.super.saveNbt(tag);
        tag.putString("Rune", ForgeRegistries.ITEMS.getKey(this).toString());
    }

    @OnlyIn(Dist.CLIENT)
    protected static void displayThornyValue(List<Component> components, float value) {
        displayAsAPercentage("thorny", components, value);
    }

    @OnlyIn(Dist.CLIENT)
    protected static void displayLightValue(List<Component> components, int value) {
        components.add(CMLangUtil.getDynamic("shining", value).withStyle(ChatFormatting.GOLD));
    }

    @OnlyIn(Dist.CLIENT)
    protected static void displayAsAPercentage(String name, List<Component> components, float value) {
        components.add(CMLangUtil.getDynamic(name, (int) (100 * value)).withStyle(ChatFormatting.GOLD));
    }

    @OnlyIn(Dist.CLIENT)
    protected void preDisplayAttribute(List<Component> tooltips) {}

    @OnlyIn(Dist.CLIENT)
    protected void displayAttributeList(List<Component> tooltips) {
        Map<Attribute, Tuple<Double, AttributeModifier.Operation>> map = getAttribute();
        if (map == null || map.isEmpty()) return;

        preDisplayAttribute(tooltips);
        for (Map.Entry<Attribute, Tuple<Double, AttributeModifier.Operation>> entry : map.entrySet()) {
            Tuple<Double, AttributeModifier.Operation> tuple = entry.getValue();
            double amount = tuple.getA();
            MutableComponent info = amount > 0 ? Component.literal("+").withStyle(ChatFormatting.GOLD)
                : Component.empty().withStyle(ChatFormatting.DARK_RED);

            if (tuple.getB() == AttributeModifier.Operation.ADDITION) info.append(String.valueOf(amount));
            else info.append((int) (amount * 100) + "%");

            tooltips.add(info.append(" ").append(Component.translatable(entry.getKey().getDescriptionId())));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayAttributeList(tooltips);
    }

    static {
        SERIALIZERS.put("Rune", tag -> (IRuneReinforcement) ForgeRegistries.ITEMS.getValue(CalamityCurios.resource(tag.getString("Rune"))));
    }
}
