package hua223.calamity.util.damage;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CalamityDamageSource extends DamageSource {
    private ChatFormatting formatting;
    private Set<TagKey<DamageType>> extra;

    protected CalamityDamageSource(@NotNull ResourceKey<DamageType> type, @NotNull Level level, Entity directEntity, Entity causingEntity) {
        super(getHolder(level, type), directEntity, causingEntity);
    }

    @SuppressWarnings("ALL")
    public static DamageSource getCustomizeDeathMessages(Component text, DamageSources damageSources) {
        return new DamageSource(damageSources.genericKill().typeHolder()) {
            @Override
            public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity entity) {
                return text;
            }
        };
    }

    private static Holder<DamageType> getHolder(Level level, ResourceKey<DamageType> key) {
        Optional<Holder.Reference<DamageType>> option = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(key);
        return option.isPresent() ? option.get() : level.damageSources().genericKill().typeHolder();
    }

    public static CalamityDamageSource source(@NotNull CalamityDamageTypes type, @NotNull Entity directEntity, Entity causingEntity) {
        return source(type.type, directEntity, causingEntity).setStyle(type.style);
    }

    public static CalamityDamageSource source(@NotNull CalamityDamageTypes type, @NotNull Entity directEntity) {
        return source(type, directEntity, directEntity);
    }

    public static CalamityDamageSource source(@NotNull CalamityDamageTypes type, @NotNull Level level) {
        return source(type.type, level).setStyle(type.style);
    }

    public static CalamityDamageSource source(@NotNull ResourceKey<DamageType> type, @NotNull Entity directEntity, Entity causingEntity) {
        return new CalamityDamageSource(type, directEntity.level(), directEntity, causingEntity);
    }

    public static CalamityDamageSource source(@NotNull ResourceKey<DamageType> type, @NotNull Entity directEntity) {
        return source(type, directEntity, directEntity);
    }

    public static CalamityDamageSource source(@NotNull ResourceKey<DamageType> type, @NotNull Level level) {
        return new CalamityDamageSource(type, level, null, null);
    }

    @SafeVarargs
    public final CalamityDamageSource addDamageTag(TagKey<DamageType>... tag) {
        if (extra == null) extra = type.getTagKeys().collect(Collectors.toSet());
        Collections.addAll(extra, tag);
        return this;
    }

    @Override
    public boolean is(@NotNull TagKey<DamageType> damageTypeKey) {
        return extra != null ? extra.contains(damageTypeKey) : super.is(damageTypeKey);
    }

    public CalamityDamageSource setStyle(ChatFormatting formatting) {
        this.formatting = formatting;
        return this;
    }

    @Override
    public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity entity) {
        MutableComponent component = (MutableComponent) super.getLocalizedDeathMessage(entity);
        return formatting == null ? component : component.withStyle(formatting);
    }
}
