package hua223.calamity.register.rune;

import hua223.calamity.events.EventTypes;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.item.SpellBook;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.function.Function;

//所有的符文类型都是互斥的，无论其是咒术炼铸或护甲强化
public interface IRuneReinforcement {
    Object2ObjectOpenHashMap<String, Function<CompoundTag, IRuneReinforcement>> SERIALIZERS = new Object2ObjectOpenHashMap<>(2);

    boolean canApply(ItemStack stack);

    String getCodecName();

    void onApply(ItemStack stack);

    boolean canRemove();

    void onRemove(ItemStack stack);

    boolean isEffectiveSlot(EquipmentSlot slot);

    default Object addData() {
        return null;
    }

    default void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        EventTypes.applyEvent(this, player, to);
    }

    default void saveNbt(CompoundTag tag) {
        tag.putString("RuneSequence", getCodecName());
    }

    static IRuneReinforcement fromNbt(CompoundTag tag) {
        Function<CompoundTag, IRuneReinforcement> function = SERIALIZERS.get(tag.getString("RuneSequence"));
        return function == null ? null : function.apply(tag);
    }

    enum Predicate {
        SWORD_CLASS {
            @Override
            public boolean test(ItemStack stack) {
                return stack.getItem() instanceof SwordItem || stack.canApplyAtEnchantingTable(Enchantments.SHARPNESS);
            }
        },

        SPELL_CLASS {
            @Override
            public boolean test(ItemStack stack) {
                Item item = stack.getItem();
                return item instanceof SpellBook || item instanceof MagicSwordItem || item instanceof CastingItem;
            }
        },

        PROJECTILE_CLASS {
            @Override
            public boolean test(ItemStack stack) {
                return stack.getItem() instanceof ProjectileWeaponItem;
            }
        },

        ARMOR_CLASS {
            @Override
            protected boolean test(ItemStack stack) {
                return stack.getItem() instanceof ArmorItem;
            }
        };

        private boolean conditionHolds;

        public static void resetCache() {
            for (Predicate predicate : values())
                predicate.conditionHolds = false;
        }

        /**
         * 直接进行测试而不缓存结果，通常这应用于只需要返回任意一项通过时被调用
         *
         * @param stack 被测试的对象
         * @return 是否成立
         */
        protected abstract boolean test(ItemStack stack);

        public boolean getResult(ItemStack stack) {
            return conditionHolds || (conditionHolds = test(stack));
        }
    }
}
