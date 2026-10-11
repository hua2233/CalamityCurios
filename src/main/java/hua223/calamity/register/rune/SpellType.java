package hua223.calamity.register.rune;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PreAttackListener;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.UUID;

//Item And Weapon
public enum SpellType implements IRuneReinforcement {
    AFLAME {
        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SWORD_CLASS.getResult(stack);
        }

        @ApplyEvent
        public final void onAttack(PreAttackListener listener) {
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
        public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
            super.onSlotChange(to, stack, player);
            int[] args = player.Calamity$Player.getRuneData(this);
            if (to) {
                args[1] = 1;
                if (args[0] < 100) startRecover(player, args);
            } else args[1] = 0;

            NetMessages.sendToClient(new FatigueDataSync().setRender(to), player);
        }

        @ApplyEvent
        public final void onAttack(PreAttackListener listener) {
            int[] args = listener.player.Calamity$Player.getRuneData(this);
            int value = args[0];

            if (value > 5) args[0] = (value - 5);
            else args[0] = 0;
            NetMessages.sendToClient(new FatigueDataSync().setProgress(args[0]), listener.player);

            startRecover(listener.player, args);
            listener.amplifier += (float) (Math.pow(2, value / 100f) - 1f) * 0.49f - 0.23f;// + 0.77 - 1f;
        }

        @Override
        public int[] addData() {
            return new int[] {100, 0, 0};
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
            float[] args = listener.player.Calamity$Player.getRuneData(this);
            if (args[2] == 1) return;

            player.heal((float) (Math.pow(args[0], 5f / 3f) * 0.1f));
            args[0] = 0;
            CalamityHelp.addIfDoesNotExist(player, 360, 0, CalamityEffects.APOPTOSIS.get());
        }

        @ApplyEvent
        public final void onAttack(PreAttackListener listener) {
            float[] args = listener.player.Calamity$Player.getRuneData(this);
            args[0] += listener.getCorrectionValue();
        }

        @Override
        public float[] addData() {
            return new float[3];
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
        public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
            float[] taintedCurseAmplifier = player.Calamity$Player.getRuneData(this);
            if (to && stack.isEnchanted()) {
                int count = (int) stack.getAllEnchantments().keySet().stream().filter(Enchantment::isCurse).count();
                if (count > 0) {
                    super.onSlotChange(true, stack, player);
                    taintedCurseAmplifier[0] = count * 0.1f;
                }
            } else if (!to && taintedCurseAmplifier[0] > 0)
                super.onSlotChange(false, stack, player);
        }

        @Override
        public boolean canApply(ItemStack stack) {
            return Predicate.SWORD_CLASS.getResult(stack);
        }

        @ApplyEvent
        public final void onAttack(PreAttackListener listener) {
            float[] taintedCurseAmplifier = listener.player.Calamity$Player.getRuneData(this);
            listener.amplifier += taintedCurseAmplifier[0];
        }

        @Override
        public float[] addData() {
            return new float[1];
        }
    },

    OBLATORY {
        @Override
        @SuppressWarnings("ConstantConditions")
        public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
            super.onSlotChange(to, stack, player);
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
        public final void onAttack(PreAttackListener listener) {
            if (listener.isSpell()) {
                listener.amplifier += 0.25f;
                if (listener.player.getRandom().nextFloat() <= 0.3f)
                    listener.player.hurt(DamageSupplier.MAGIC_PROJECTILE.get(), listener.player.getMaxHealth() * 0.08f);
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

    @OnlyIn(Dist.CLIENT)
    private static int tail;
    @OnlyIn(Dist.CLIENT)
    private static int heat;
    @OnlyIn(Dist.CLIENT)
    private static int subscript;
    @OnlyIn(Dist.CLIENT)
    private TextureAtlasSprite sprite;
    @OnlyIn(Dist.CLIENT)
    public static boolean isCanSwitch;
    @OnlyIn(Dist.CLIENT)
    private static final EnumSet<SpellType> AVAILABILITY = EnumSet.noneOf(SpellType.class);

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

    @Override
    public final String getCodecName() {
        return "Spell";
    }

    @Override
    public boolean isEffectiveSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND;
    }

    @Override
    public void onApply(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(CalamityHelp.FONT_FLAG, 1);
        tag.putString("spell", name());
    }

    @Override
    public boolean canRemove() {
        return true;
    }

    @Override
    public void onRemove(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(CalamityHelp.FONT_FLAG);
        tag.remove("spell");
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
    public MutableComponent getTypeComponent() {
        return Component.translatable(getType());
    }

    @OnlyIn(Dist.CLIENT)
    public static void afterMainTextureLoad(TextureAtlas atlas) {
        for (SpellType type : SpellType.values())
            type.sprite = atlas.getSprite(CalamityCurios.ModResource(type.name().toLowerCase()));
    }

    @Override
    public void saveNbt(CompoundTag tag) {
        IRuneReinforcement.super.saveNbt(tag);
        tag.putString("SpellType", name());
    }

    static {
        SERIALIZERS.put("Spell", tag -> SpellType.valueOf(tag.getString("SpellType")));
    }
}
