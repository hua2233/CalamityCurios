package hua223.calamity.register.effects;

import hua223.calamity.register.effects.factor.UniversalFactorEffect;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityDamageSource;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Plague extends UniversalFactorEffect<float[], Plague.PlagueFactor> implements IEffectsCallBack {
    protected Plague(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "50cd184c-6dc0-486d-b52b-bb73cb5cc4c1", -0.03, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "50cd184c-6dc0-486d-b52b-bb73cb5cc4c1", -0.02, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity target, int amplifier) {
        if (!target.level.isClientSide) {
            MobEffectInstance source = target.getEffect(this);
            PlagueFactor factor = source.calamity$GetUniversalFactor(this);
            int value = amplifier + 1;
            //If the host has no transmission value, the plague will kill it
            if (target.getHealth() > value || (source.getDuration() < 20 || --factor.getFactor()[4] == 0))
                target.hurt(CalamityDamageSource.getPlague().setOwner(factor.getOwner()), value);

            if (factor.infect) {
                List<LivingEntity> entities = target.level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(5));
                if (entities.isEmpty()) return;
                int duration = factor.getInfectionTime();
                int level = factor.getInfectionLevel(source.getDuration());
                LivingEntity owner = factor.getOwner();
                RandomSource random = target.getRandom();
                for (LivingEntity victim : entities)
                    if (!victim.isDeadOrDying() && victim != owner && !victim.hasEffect(this)) {
                        MobEffectInstance instance = new MobEffectInstance(this, duration, level);
                        instance.calamity$GetUniversalFactor(this).fromInfectionSource(random, factor);
                        victim.addEffect(instance);
                    }
                factor.infect = false;
            }
        }
    }

    @Override
    protected PlagueFactor factory() {
        return new PlagueFactor(this);
    }

    @Override
    public BiConsumer<MobEffectInstance, PlagueFactor> createFactorUpdater() {
        return ((instance, factor) -> {
            float[] f = factor.getFactor();
            if (++f[0] > 40) {
                f[0] = 0;
                factor.infect = true;
            }
        });
    }

    @Override
    //MobEffectInstance Cannot be reverse associated to a specific owner instance, not initialized here
    public Function<MobEffectInstance, float[]> initFactorData() {
        return instance -> new float[] {0,
            instance.getDuration(), instance.getAmplifier(), instance.getAmplifier(), 1};
    }

    @Override
    public Tag save(PlagueFactor factor) {
        CompoundTag tag = new CompoundTag();
        LivingEntity owner = factor.getOwner();
        if (owner != null && !owner.isDeadOrDying()) {
            tag.putUUID("owner", owner.getUUID());
            tag.put("world", Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE,
                owner.getLevel().dimension()).result().get());
        }

        float[] count = factor.getFactor();
        tag.putFloat("plagueTick", count[0]);
        tag.putFloat("maxDuration", count[1]);
        tag.putFloat("level", count[2]);
        tag.putFloat("sourceLevel", count[3]);
        tag.putFloat("fragile", count[4]);
        tag.putBoolean("infect", factor.infect);
        return tag;
    }


    @Override
    //Unable to determine if the owner will be loaded before the epidemic entity
    public Optional<PlagueFactor> load(CompoundTag tag) {
        PlagueFactor factor = new PlagueFactor(new float[]
            {tag.getFloat("plagueTick"),
            tag.getFloat("maxDuration"),
            tag.getFloat("level"),
            tag.getFloat("sourceLevel"),
            tag.getFloat("fragile")});

        factor.infect = tag.getBoolean("infect");
        factor.setLazyLoadingNbt(tag);
        return Optional.of(factor);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        PlagueFactor factor = effect.calamity$GetUniversalFactor(this);
        factor.setLurk(entity);
        if (factor.getOwner() != null) return;

        if (source instanceof LivingEntity living)
            factor.setOwner(living);
        else if (source instanceof AreaEffectCloud cloud && cloud.getOwner() != null) {
            factor.setOwner(cloud.getOwner());
            //area effect cloud should only be used for the first time, otherwise its infectivity will be very strong
            cloud.victims.put(entity, cloud.getDuration() + cloud.getWaitTime() + 20);
        }
    }

    public static class PlagueFactor extends UniversalFactorEffect.UniversalFactor<float[]> {
        private CompoundTag nbt;
        private boolean infect;
        public PlagueFactor(UniversalFactorEffect<float[], ?> supplier) {
            super(supplier);
        }

        public PlagueFactor(float[] factor) {
            super(factor);
        }

        @Override
        public LivingEntity getOwner() {
            if (owner == null && nbt != null) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                ResourceKey<Level> key = Level.RESOURCE_KEY_CODEC.decode(NbtOps.INSTANCE,
                    nbt.get("world")).result().get().getFirst();
                ServerLevel level = server.getLevel(key);
                LivingEntity owner = (LivingEntity) level.getEntity(nbt.getUUID("owner"));
                if (owner != null) setOwner(owner);
                nbt = null;
            }

            return owner;
        }

        private void setLazyLoadingNbt(CompoundTag tag) {
            if (tag.contains("owner")) {
                nbt = tag;
            }
        }

        public int getInfectionTime() {
            return (int) Mth.lerp(factor[2] / factor[3], 20, factor[1]);
        }

        public int getInfectionLevel(int remaining) {
            return Math.round(Mth.lerp(remaining / factor[1], 0, factor[2]));
        }

        private void fromInfectionSource(RandomSource source, PlagueFactor factor) {
            owner = factor.owner;
            int reduce = source.nextInt(0, 3);

            this.factor[1] = factor.factor[1];
            if (factor.factor[3] > reduce) this.factor[3] = factor.factor[3] - reduce;
            else this.factor[3] = 0;
        }

        private void setLurk(LivingEntity entity) {
            factor[4] = entity.getRandom().nextInt(1, 5);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("plague").withStyle(ChatFormatting.DARK_GREEN));
    }
}
