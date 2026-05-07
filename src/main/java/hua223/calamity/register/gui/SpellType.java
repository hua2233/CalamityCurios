package hua223.calamity.register.gui;

import hua223.calamity.capability.CurseEnchantment;
import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.integration.curios.EventTypes;
import hua223.calamity.integration.curios.listeners.BaseListener;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.FatigueDataSync;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.DemonGate;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.delaytask.DelayRunnable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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
import net.minecraftforge.eventbus.api.Event;

import java.util.*;
import java.util.stream.Collectors;

public enum SpellType {
    AFLAME {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SWORD_CLASS.getResult(stack);
        }

        @Override
        public void getTriggerTypes() {
            APPLICABLE_TYPE.add(TriggerType.PLAYER_ATTACK);
        }

        @Override
        public void onTrigger(TriggerType type) {
            CalamityHelp.addIfDoesNotExist(type.getListener(HurtListener.class).entity,
                80, 0, CalamityEffects.VULNERABILITY_HEX.get());
        }
    },

    EPHEMERAL {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SWORD_CLASS.getResult(stack) || Predicate.PROJECTILE_CLASS.getResult(stack);
        }

        @Override
        public void getTriggerTypes() {
            APPLICABLE_TYPE.add(TriggerType.PLAYER_ATTACK);
            APPLICABLE_TYPE.add(TriggerType.MAIN_HAND_ITEM_CHANGE);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void onTrigger(TriggerType type) {
            switch (type) {
                case MAIN_HAND_ITEM_CHANGE -> {
                    LivingEquipmentChangeEvent event = type.getListener(LivingEquipmentChangeEvent.class);
                    LazyOptional<CurseEnchantment> optional = event.getTo().getCapability(EnchantmentProvider.CURSE_ENCHANTMENT);

                    ServerPlayer player = (ServerPlayer) event.getEntity();
                    if (optional.isPresent()) setRender(player, optional.orElse(null).getRunes() == this);
                    else setRender(player, false);
                }

                case PLAYER_ATTACK -> {
                    HurtListener listener = type.getListener(HurtListener.class);
                    listener.amplifier += getAmplifier(listener.player);
                }
            }
        }

        private float getAmplifier(ServerPlayer player) {
            SPELL_TYPE_DATA_MAP.get(player.getUUID());
            int[] args = getData(player).fatigueSlot;
            int value = args[0];

            if (value > 5) args[0] = (value - 5);
            else args[0] = 0;
            NetMessages.sendToClient(new FatigueDataSync().setProgress(args[0]), player);

            startRecover(player, args);
            return (float) (Math.pow(2, value / 100f) - 1f) * 0.49f - 0.23f;// + 0.77 - 1f;
        }

        private void setRender(ServerPlayer player, boolean render) {
            int[] args = getData(player).fatigueSlot;
            if (render) {
                if (args[1] == 0) {
                    args[1] = 1;
                    NetMessages.sendToClient(new FatigueDataSync().setRender(true), player);
                }
            } else {
                if (args[1] == 1) {
                    args[1] = 0;
                    NetMessages.sendToClient(new FatigueDataSync().setRender(false), player);
                }
            }
        }

        @SuppressWarnings("ConstantConditions")
        private void startRecover(ServerPlayer player, int[] args) {
            if (args[2] == 0) {
                args[2] = 1;

                FatigueDataSync pack = new FatigueDataSync();
                DelayRunnable.conditionsLoop(() -> {
                    if (player.isDeadOrDying() || player.hasDisconnected()) return true;
                    LazyOptional<CurseEnchantment> optional = player.getMainHandItem().getCapability(EnchantmentProvider.CURSE_ENCHANTMENT);
                    if (!optional.isPresent() || optional.orElse(null).getRunes() != this) {
                        args[2] = 0;
                    } else {
                        NetMessages.sendToClient(pack.setProgress(args[0] += 2), player);
                        if (args[0] >= 100) args[2] = 0;
                    }

                    return args[2] == 0;
                }, 20);
            }
        }
    },

    WITHERED {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SWORD_CLASS.getResult(stack) || Predicate.PROJECTILE_CLASS.getResult(stack);
        }

        @Override
        public void getTriggerTypes() {
            APPLICABLE_TYPE.add(TriggerType.PLAYER_HURT);
            APPLICABLE_TYPE.add(TriggerType.PLAYER_ATTACK);
        }

        @Override
        public void onTrigger(TriggerType type) {
            switch (type) {
                case PLAYER_HURT -> {
                    LivingEntity player = type.getListener(HurtListener.class).player;
                    float[] args = getData(player).lg;
                    if (args[2] == 1) return;

                    player.heal((float) (Math.pow(args[0], 5f / 3f) * 0.1f));
                    args[0] = 0;
                    CalamityHelp.addIfDoesNotExist(player, 360, 0, CalamityEffects.APOPTOSIS.get());
                }

                case PLAYER_ATTACK -> {
                    HurtListener listener = type.getListener(HurtListener.class);
                    getData(listener.player).lg[0] += listener.getCorrectionValue();
                }
            }
        }
    },

    RESENTFUL {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.PROJECTILE_CLASS.getResult(stack);
        }

        @Override
        public void getTriggerTypes() {
            APPLICABLE_TYPE.add(TriggerType.PLAYER_ATTACK);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void onTrigger(TriggerType type) {
            HurtListener listener = type.getListener(HurtListener.class);
            if (listener.isTriggerByLiving && listener.isFarAttack()) {
                listener.amplifier += Mth.lerp(Mth.clamp((float) listener.player.position().distanceToSqr(
                    listener.entity.position()), 9f, 225f), -0.25f, 0.75f);
            }
        }
    },

    BLOODTHIRSTY {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.PROJECTILE_CLASS.getResult(stack);
        }

        @Override
        public void getTriggerTypes() {
            APPLICABLE_TYPE.add(TriggerType.PLAYER_ATTACK);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void onTrigger(TriggerType type) {
            HurtListener listener = type.getListener(HurtListener.class);
            if (listener.isTriggerByLiving && listener.isFarAttack()) {
                listener.amplifier += Mth.lerp(Mth.clamp((float) listener.player.position().distanceToSqr(
                    listener.entity.position()), 9f, 225f), 0.75f, -0.25f);
            }
        }
    },

    PERSECUTED {
        private static int globalCooling;

        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SWORD_CLASS.getResult(stack);
        }

        @Override
        public void getTriggerTypes() {
            APPLICABLE_TYPE.add(TriggerType.PLAYER_ATTACK);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void onTrigger(TriggerType type) {
            HurtListener listener = type.getListener(HurtListener.class);
            int tick = listener.player.getServer().getTickCount();
            if (listener.isFarAttack() || globalCooling >= tick) return;
            globalCooling = tick + 3600;
            DemonGate.spawn(listener.player);
        }
    },

    LECHEROUS {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SWORD_CLASS.getResult(stack);
        }

        @Override
        public void getTriggerTypes() {
            APPLICABLE_TYPE.add(TriggerType.PROJECTILE);
        }


    },

    TAINTED {
        @Override
        public boolean canApply(ItemStack stack) {
            return false;
        }
    },

    OBLATORY {
        @Override
        public boolean canApply(ItemStack stack) {
            return false;
        }
    },

    TRAITOROUS {
        @Override
        public boolean canApply(ItemStack stack) {
            return false;
        }
    },

    INDIGNANT {
        @Override
        public boolean canApply(ItemStack stack) {
            return false;
        }
    },

    HELLBOUND {
        @Override
        public boolean canApply(ItemStack stack) {
            return false;
        }
    },

    EXHUMED {
        @Override
        public boolean canApply(ItemStack stack) {
            return false;
        }
    };

    private static final Map<UUID, SpellTypeData> SPELL_TYPE_DATA_MAP = new Object2ObjectOpenHashMap<>(2);
    private static final EnumSet<SpellType.TriggerType> APPLICABLE_TYPE = EnumSet.noneOf(SpellType.TriggerType.class);

    @OnlyIn(Dist.CLIENT)
    private static final EnumSet<SpellType> AVAILABILITY = EnumSet.noneOf(SpellType.class);
    @OnlyIn(Dist.CLIENT)
    private TextureAtlasSprite sprite;
    @OnlyIn(Dist.CLIENT)
    public static boolean isCanSwitch;
    @OnlyIn(Dist.CLIENT)
    private static int subscript;
    @OnlyIn(Dist.CLIENT)
    private static int heat;
    @OnlyIn(Dist.CLIENT)
    private static int tail;

    SpellType() {
    }

    protected final SpellTypeData getData(Entity player) {
        return SPELL_TYPE_DATA_MAP.get(player.getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public static SpellType getCanApply(ItemStack stack) {
        //pre
        AVAILABILITY.clear();
        isCanSwitch = false;

        for (SpellType type : SpellType.values())
            if (type.canApply(stack)) AVAILABILITY.add(type);
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
            }).orElseThrow(() -> new IndexOutOfBoundsException("Invalid spell index: " + subscript));
    }

    @OnlyIn(Dist.CLIENT)
    public static SpellType previousSpell() {
        return AVAILABILITY.stream()
            .filter(t -> t.ordinal() < subscript)
            .reduce((first, second) -> second)
            .map(t -> {
                subscript = t.ordinal();
                return t;
            }).orElseThrow(() -> new IndexOutOfBoundsException("Invalid spell index: " + subscript));
    }

    public String getType() {
        return "calamity_curios.spell.type." + name().toLowerCase();
    }

    public static boolean anyMatch(ItemStack stack) {
        return Arrays.stream(SpellType.values()).anyMatch(spellType -> spellType.canApply(stack));
    }

    public String getDescription() {
        return "calamity_curios.spell.description." + name().toLowerCase();
    }

    @OnlyIn(Dist.CLIENT)
    public TextureAtlasSprite getTexture() {
        return sprite;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getDescriptionComponent() {
        return Component.translatable(getDescription());
    }

    @OnlyIn(Dist.CLIENT)
    public Component getTypeComponent() {
        return Component.translatable(getType());
    }

    public static Set<SpellType> getAllMatch(ItemStack stack) {
        Set<SpellType> set = Arrays.stream(SpellType.values()).filter(spellType -> spellType.canApply(stack)).collect(Collectors.toSet());
        Predicate.resetCache();
        return set;
    }

    public abstract boolean canApply(ItemStack stack);

    public void onTrigger(TriggerType type) {
    }

    public static void init(Player player) {
        SPELL_TYPE_DATA_MAP.put(player.getUUID(), new SpellTypeData());
    }

    public void delete(ServerPlayer player) {
        SPELL_TYPE_DATA_MAP.remove(player.getUUID());
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        for (SpellType type : SpellType.values())
            type.sprite = atlas.getSprite(CalamityCurios.ModResource(type.name().toLowerCase()));
    }

    public final boolean canApplyEvent(TriggerType type) {
        APPLICABLE_TYPE.clear();
        getTriggerTypes();
        return APPLICABLE_TYPE.contains(type);
    }

    public void getTriggerTypes() {}

    public enum TriggerType {
        MAIN_HAND_ITEM_CHANGE,
        PLAYER_ATTACK,
        PROJECTILE,
        PLAYER_HURT;

        private static CurseEnchantment enchantment;
        private static SpellType.TriggerType type;
        private Object listener;
        private EventTypes<?> build;

        @SuppressWarnings("all")
        public static boolean canTriggerEnchant(Player player, SpellType.TriggerType type) {
            LazyOptional<CurseEnchantment> optional = player.getMainHandItem().getCapability(EnchantmentProvider.CURSE_ENCHANTMENT);
            if (optional.isPresent()) {
                CurseEnchantment curseEnchantment = optional.orElse(null);
                if (curseEnchantment.isEffective() && curseEnchantment.getRunes().canApplyEvent(type)) {
                    TriggerType.type = type;
                    enchantment = curseEnchantment;
                    return true;
                }
            }

            return false;
        }

        @SuppressWarnings("unchecked")
        public static <T extends BaseListener<?>> T listenerTriggerEnchant(EventTypes<T> types, Object... args) {
            type.listener = args;
            type.build = types;
            enchantment.getRunes().onTrigger(type);
            enchantment = null;
            T listener = (T) type.listener;
            type.listener = null;
            type.build = null;
            return listener;
        }

        public static void listenerTriggerEnchant(BaseListener<?> listener) {
            type.listener = listener;
            enchantment.getRunes().onTrigger(type);
            enchantment = null;
            type.listener = null;
        }

        public static void eventTriggerEnchant(Event event) {
            type.listener = event;
            enchantment.getRunes().onTrigger(type);
            enchantment = null;
            type.listener = null;
        }

        @SuppressWarnings("unchecked")
        public final  <T> T getListener(Class<T> clazz) {
            try {
                return (T) (listener = (build != null ? build.builderEvent((Object[]) listener) : listener));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private enum Predicate {
        SWORD_CLASS {
            @Override
            public boolean test(ItemStack stack) {
                return stack.getItem() instanceof SwordItem || stack.canApplyAtEnchantingTable(Enchantments.SHARPNESS);
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

        public boolean getResult(ItemStack stack) {
            return conditionHolds || (conditionHolds = test(stack));
        }
    }

    protected static final class SpellTypeData {
        private final int[] fatigueSlot = new int[] {100, 0, 0};
        private final float[] lg = new float[3];
    }
}
