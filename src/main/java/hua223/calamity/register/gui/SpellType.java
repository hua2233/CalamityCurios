package hua223.calamity.register.gui;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.EventTypes;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.events.listeners.ProjectileSpawnListener;
import hua223.calamity.integration.curios.item.RecklessnessGlove;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.packets.FatigueDataSync;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.DemonGate;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.delaytask.DelayRunnable;
import io.redspace.ironsspellbooks.api.item.weapons.MagicSwordItem;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.item.SpellBook;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

public enum SpellType {
    AFLAME {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SWORD_CLASS.getResult(stack);
        }

        @ApplyEvent
        public final void onAttack(PlayerAttackListener listener) {
            CalamityHelp.addIfDoesNotExist(listener.player.getRandom().nextFloat() > 0.7 ?
                    listener.entity : listener.player, 80, 0, CalamityEffects.VULNERABILITY_HEX.get());
        }
    },

    EPHEMERAL {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SWORD_CLASS.getResult(stack) || Predicate.PROJECTILE_CLASS.getResult(stack);
        }

        @Override
        public void onMainHandChange(boolean to, ItemStack stack, ServerPlayer player) {
            super.onMainHandChange(to, stack, player);
            int[] args = player.Calamity$Player.getSpellData().fatigueSlot;
            if (to) {
                args[1] = 1;
                if (args[0] < 100) startRecover(player, args);
            } else args[1] = 0;

            NetMessages.sendToClient(new FatigueDataSync().setRender(to), player);
        }

        @ApplyEvent
        public final void onAttack(PlayerAttackListener listener) {
            int[] args = listener.player.Calamity$Player.getSpellData().fatigueSlot;
            int value = args[0];

            if (value > 5) args[0] = (value - 5);
            else args[0] = 0;
            NetMessages.sendToClient(new FatigueDataSync().setProgress(args[0]), listener.player);

            startRecover(listener.player, args);
            listener.amplifier += (float) (Math.pow(2, value / 100f) - 1f) * 0.49f - 0.23f;// + 0.77 - 1f;
        }

        @SuppressWarnings("ConstantConditions")
        private void startRecover(ServerPlayer player, int[] args) {
            if (args[2] == 0) {
                args[2] = 1;

                FatigueDataSync pack = new FatigueDataSync();
                DelayRunnable.conditionsLoop(() -> {
                    if (player.isDeadOrDying() || player.hasDisconnected()) return true;
                    if (args[1] == 0) {
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

        @ApplyEvent
        public final void onHurt(HurtListener listener) {
            LivingEntity player = listener.player;
            float[] args = listener.player.Calamity$Player.getSpellData().lg;
            if (args[2] == 1) return;

            player.heal((float) (Math.pow(args[0], 5f / 3f) * 0.1f));
            args[0] = 0;
            CalamityHelp.addIfDoesNotExist(player, 360, 0, CalamityEffects.APOPTOSIS.get());
        }

        @ApplyEvent
        public final void onAttack(PlayerAttackListener listener) {
            listener.player.Calamity$Player.getSpellData().lg[0] += listener.getCorrectionValue();
        }
    },

    RESENTFUL {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.PROJECTILE_CLASS.getResult(stack);
        }

        @ApplyEvent
        @SuppressWarnings("ConstantConditions")
        public final void onHurt(HurtListener listener) {
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

        @ApplyEvent
        @SuppressWarnings("ConstantConditions")
        public final void onHurt(HurtListener listener) {
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

        @ApplyEvent
        @SuppressWarnings("ConstantConditions")
        public final void onHurt(HurtListener listener) {
            int tick = listener.player.getServer().getTickCount();
            if (listener.isFarAttack() || globalCooling >= tick) return;
            globalCooling = tick + 3600;
            DemonGate.spawn(listener.player);
        }
    },

    LECHEROUS {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.PROJECTILE_CLASS.getResult(stack);
        }
        @ApplyEvent
        public final void onProjectileShoot(ProjectileSpawnListener listener) {
            RecklessnessGlove.offsetProjectile(listener.projectile, 20);
        }

        @ApplyEvent(300)
        @SuppressWarnings("ConstantConditions")
        public final void onDeath(DeathListener listener) {
            if (!listener.isPlayerDeath && !listener.isCanceled()
                && listener.source.is(DamageTypeTags.IS_PROJECTILE)) {
                float maxHealth = listener.player.getMaxHealth();
                if (listener.player.getHealth() < maxHealth)
                    listener.player.heal(Math.min(listener.entity.getMaxHealth() * 0.4f, maxHealth * 0.4f));
            }
        }
    },

    TAINTED {
        @Override
        public void onMainHandChange(boolean to, ItemStack stack, ServerPlayer player) {
            SpellData data = player.Calamity$Player.getSpellData();
            if (to && stack.isEnchanted()) {
                int count = (int) stack.getAllEnchantments().keySet().stream().filter(Enchantment::isCurse).count();
                if (count > 0) {
                    super.onMainHandChange(true, stack, player);
                    data.taintedCurseAmplifier = count * 0.1f;
                }
            } else if (!to && data.taintedCurseAmplifier > 0)
                super.onMainHandChange(false, stack, player);
        }

        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SWORD_CLASS.getResult(stack);
        }

        @ApplyEvent
        public final void onAttack(PlayerAttackListener listener) {
            listener.amplifier += listener.player.Calamity$Player.getSpellData().taintedCurseAmplifier;
        }
    },

    OBLATORY {
        @Override
        @SuppressWarnings("ConstantConditions")
        public void onMainHandChange(boolean to, ItemStack stack, ServerPlayer player) {
            super.onMainHandChange(to, stack, player);
            player.Calamity$Player.getSpellData().oblatorySource = to ? DamageSupplier.MAGIC_PROJECTILE.get(player) : null;
            UUID id = UUID.nameUUIDFromBytes(name().getBytes());
            AttributeInstance instance = player.getAttribute(CalamityAttributes.MAGIC_REDUCTION.get());
            if (to) instance.addTransientModifier(new AttributeModifier(id,
                name(), 0.3, AttributeModifier.Operation.ADDITION));
            else instance.removeModifier(id);
        }

        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SPELL_CLASS.getResult(stack);
        }

        @ApplyEvent(0)
        public final void onAttack(PlayerAttackListener listener) {
            if (listener.isSpell()) {
                listener.amplifier += 0.25f;
                if (listener.player.getRandom().nextFloat() <= 0.3f) listener.player.hurt(
                    listener.player.Calamity$Player.getSpellData().oblatorySource, listener.player.getMaxHealth() * 0.08f);
            }
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

    private static final Map<UUID, SpellData> SPELL_TYPE_DATA_MAP = new Object2ObjectOpenHashMap<>(2);

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

    public void onMainHandChange(boolean to, ItemStack stack, ServerPlayer player) {
        EventTypes.applyEvent(this, player, to);
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public static SpellType getCanApply(ItemStack stack) {
        //pre
        AVAILABILITY.clear();
        isCanSwitch = false;
        Predicate.resetCache();

        for (SpellType type : SpellType.values())
            if (type.canApply(stack)) AVAILABILITY.add(type);

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

    public abstract boolean canApply(ItemStack stack);

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        for (SpellType type : SpellType.values())
            type.sprite = atlas.getSprite(CalamityCurios.ModResource(type.name().toLowerCase()));
    }

    private enum Predicate {
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

    public static final class SpellData {
        private final int[] fatigueSlot = new int[] {100, 0, 0};
        private final float[] lg = new float[3];
        private float taintedCurseAmplifier;
        private DamageSource oblatorySource;
    }
}
