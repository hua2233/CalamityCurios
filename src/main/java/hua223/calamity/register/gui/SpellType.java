package hua223.calamity.register.gui;

import hua223.calamity.capability.CurseEnchantment;
import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.FatigueDataSync;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.delaytask.DelayRunnable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.*;
import java.util.stream.Collectors;

public enum SpellType {
    AFLAME {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return Predicate.SWORD_CLASS.getResult(stack, holds);
        }

        @Override
        public void onTrigger(Event e, TriggerType type) {
            if (type == TriggerType.PLAYER_ATTACK) {
                MobEffect effect = CalamityEffects.VULNERABILITY_HEX.get();
                LivingEntity entity = ((LivingHurtEvent) e).getEntity();
                if (!entity.hasEffect(effect))
                    entity.addEffect(new MobEffectInstance(effect, 80));
            }
        }
    },

    EPHEMERAL {
        private static Map<UUID, int[]> fatigueSlot;

        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return Predicate.SWORD_CLASS.getResult(stack, holds)
                || Predicate.PROJECTILE_CLASS.getResult(stack, holds);
        }

        @Override
        public void init(Player player) {
            super.init(player);
            if (!player.level.isClientSide) {
                if (fatigueSlot == null) fatigueSlot = new Object2ObjectOpenHashMap<>(4);
                fatigueSlot.putIfAbsent(player.getUUID(), new int[]{100, 0, 0});
            }
        }

        @Override
        public void delete(ServerPlayer player) {
            if (fatigueSlot != null) fatigueSlot.remove(player.getUUID());
        }

        @Override
        public void onTrigger(Event e, TriggerType type) {
            switch (type) {
                case MAIN_HAND_ITEM_CHANGE -> {
                    LivingEquipmentChangeEvent event = (LivingEquipmentChangeEvent) e;
                    LazyOptional<CurseEnchantment> optional = event.getTo().getCapability(EnchantmentProvider.CURSE_ENCHANTMENT);

                    if (optional.isPresent()) setRender(event.getEntity(), optional.orElse(null).getRunes() == this);
                    else setRender(event.getEntity(), false);
                }

                case PLAYER_ATTACK -> {
                    LivingHurtEvent event = (LivingHurtEvent) e;
                    event.setAmount(event.getAmount() * getAmplifier((ServerPlayer) event.getSource().getEntity()));
                }
            }
        }

        private float getAmplifier(ServerPlayer player) {
            int[] args = fatigueSlot.get(player.getUUID());
            int value = args[0];

            if (value > 5) args[0] = (value - 5);
            else args[0] = 0;
            NetMessages.sendToClient(new FatigueDataSync().setProgress(args[0]), player);

            startRecover(player, args);
            return (float) (Math.pow(2, value / 100f) - 1f) * 0.49f + 0.77f;
        }

        private static void setRender(LivingEntity player, boolean render) {
            int[] args = fatigueSlot.get(player.getUUID());
            if (render) {
                if (args[1] == 0) {
                    args[1] = 1;
                    NetMessages.sendToClient(new FatigueDataSync().setRender(true), (ServerPlayer) player);
                }
            } else {
                if (args[1] == 1) {
                    args[1] = 0;
                    NetMessages.sendToClient(new FatigueDataSync().setRender(false), (ServerPlayer) player);
                }
            }
        }

        private void startRecover(ServerPlayer player, int[] args) {
            if (args[2] == 0) {
                args[2] = 1;

                DelayRunnable.conditionsLoop(() -> {
                    if (player.isDeadOrDying() || player.hasDisconnected()) return true;
                    LazyOptional<CurseEnchantment> optional = player.getMainHandItem().getCapability(EnchantmentProvider.CURSE_ENCHANTMENT);
                    if (!optional.isPresent() || optional.orElse(null).getRunes() != this) {
                        args[2] = 0;
                    } else {
                        NetMessages.sendToClient(new FatigueDataSync().setProgress(args[0] += 2), player);
                        if (args[0] >= 100) args[2] = 0;
                    }

                    return args[2] == 0;
                }, 20);
            }
        }
    },

    WITHERED {
        private static Map<UUID, float[]> lg;

        @Override
        public void init(Player player) {
            super.init(player);
            if (!player.level.isClientSide) {
                if (lg == null) lg = new Object2ObjectOpenHashMap<>(2);
                lg.put(player.getUUID(), new float[3]);
            }
        }

        @Override
        public void delete(ServerPlayer player) {
            lg.remove(player.getUUID());
        }

        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return Predicate.SWORD_CLASS.getResult(stack, holds)
                || Predicate.PROJECTILE_CLASS.getResult(stack, holds);
        }

        @Override
        public void onTrigger(Event e, TriggerType type) {
            switch (type) {
                case PLAYER_HURT -> {
                    LivingEntity player = ((LivingHurtEvent) e).getEntity();
                    float[] args = lg.get(player.getUUID());
                    if (args[2] == 1) return;

                    player.heal((float) (Math.pow(args[0], 5f / 3f) * 0.1f));
                    args[0] = 0;

                    MobEffect effect = CalamityEffects.APOPTOSIS.get();
                    if (!player.hasEffect(effect)) player.addEffect(new MobEffectInstance(effect, 360));
                }

                case PLAYER_ATTACK -> {
                    LivingHurtEvent event = (LivingHurtEvent) e;
                    lg.get(event.getSource().getEntity().getUUID())[0] += event.getAmount();
                }
            }
        }
    },

    RESENTFUL {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return false;
        }
    },

    BLOODTHIRSTY {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return false;
        }
    },

    PERSECUTED {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return false;
        }
    },

    LECHEROUS {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return false;
        }
    },

    TAINTED {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return false;
        }
    },

    OBLATORY {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return false;
        }
    },

    TRAITOROUS {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return false;
        }
    },

    INDIGNANT {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return false;
        }
    },

    HELLBOUND {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return false;
        }
    },

    EXHUMED {
        @Override
        public boolean canApply(ItemStack stack, boolean holds) {
            return false;
        }
    };
    @OnlyIn(Dist.CLIENT)
    private static final EnumSet<SpellType> AVAILABILITY = EnumSet.noneOf(SpellType.class);
    @OnlyIn(Dist.CLIENT)
    public static boolean isCanSwitch;
    @OnlyIn(Dist.CLIENT)
    private static int subscript;
    @OnlyIn(Dist.CLIENT)
    private static int heat;
    @OnlyIn(Dist.CLIENT)
    private static int tail;
    @OnlyIn(Dist.CLIENT)
    public Component type;
    @OnlyIn(Dist.CLIENT)
    public Component description;
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation texture;

    SpellType() {
    }

    @OnlyIn(Dist.CLIENT)
    public static SpellType getCanApply(ItemStack stack) {
        //pre
        AVAILABILITY.clear();
        isCanSwitch = false;

        for (SpellType type : SpellType.values())
            if (type.canApply(stack, true)) AVAILABILITY.add(type);
        Predicate.resetCache();

        if (AVAILABILITY.isEmpty()) {
            return null;
        } else {
            Iterator<SpellType> iterator = AVAILABILITY.iterator();
            SpellType spell = iterator.next();
            subscript = spell.ordinal();
            if (AVAILABILITY.size() > 1) {
                isCanSwitch = true;
                heat = spell.ordinal();

                SpellType spell2 = null;
                while (iterator.hasNext()) spell2 = iterator.next();
                tail = spell2.ordinal();
            }
            return spell;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isBoundary(boolean tail) {
        return tail ? subscript == SpellType.tail : subscript == heat;
    }

    @OnlyIn(Dist.CLIENT)
    public static SpellType nextSpell() {
        return AVAILABILITY.stream()
            .filter(t -> t.ordinal() > subscript)
            .findFirst()
            .map(t -> {
                subscript = t.ordinal();
                return t;
            })
            .orElseThrow(() -> new IndexOutOfBoundsException("Invalid spell index: " + subscript));
    }

    @OnlyIn(Dist.CLIENT)
    public static SpellType previousSpell() {
        return AVAILABILITY.stream()
            .filter(t -> t.ordinal() < subscript)
            .reduce((first, second) -> second)
            .map(t -> {
                subscript = t.ordinal();
                return t;
            })
            .orElseThrow(() -> new IndexOutOfBoundsException("Invalid spell index: " + subscript));
    }

    public String getType() {
        return "calamity_curios.spell.type." + name().toLowerCase();
    }

    public static boolean anyMatch(ItemStack stack) {
        return Arrays.stream(SpellType.values()).anyMatch(spellType -> spellType.canApply(stack, false));
    }

    public String getDescription() {
        return "calamity_curios.spell.description." + name().toLowerCase();
    }

    public static Set<SpellType> getAllMatch(ItemStack stack) {
        Set<SpellType> set = Arrays.stream(SpellType.values()).filter(spellType -> spellType.canApply(stack, true)).collect(Collectors.toSet());
        Predicate.resetCache();
        return set;
    }

    public abstract boolean canApply(ItemStack stack, boolean holds);

    public void onTrigger(Event event, TriggerType type) {
    }

    public void init(Player player) {
        if (player.level.isClientSide && type == null) {
            String runes = name().toLowerCase();
            type = Component.translatable("calamity_curios.spell.type." + runes);
            description = Component.translatable("calamity_curios.spell.description." + runes);
            texture = CalamityCurios.ModResource("textures/gui/cursespell/" + runes + ".png");
        }
    }

    public void delete(ServerPlayer player) {
    }

    public enum TriggerType {
        MAIN_HAND_ITEM_CHANGE,
        PLAYER_ATTACK,
        PLAYER_HURT
    }

    private enum Predicate {
        SWORD_CLASS {
            @Override
            public boolean test(ItemStack stack) {
                return stack.getItem() instanceof SwordItem
                    || stack.canApplyAtEnchantingTable(Enchantments.SHARPNESS);
            }
        },
        PROJECTILE_CLASS {
            @Override
            public boolean test(ItemStack stack) {
                return stack.getItem() instanceof ProjectileWeaponItem;
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

        public boolean getResult(ItemStack stack, boolean holds) {
            return holds ? conditionHolds || (conditionHolds = test(stack)) : test(stack);
        }
    }
}
