package hua223.calamity.register.damage;

import  hua223.calamity.generators.CalamityGen;
import hua223.calamity.main.AnnotationProcessor;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Map;

public class DamageSupplier {
    private static final ArrayList<DamageSupplier> SUPPLIERS = new ArrayList<>();

    public static final TagKey<DamageType> NOT_TRIGGER_EVENT = TagKey.create(
        Registries.DAMAGE_TYPE, CalamityCurios.ModResource("not_trigger_event"));

    public static final TagKey<DamageType> CALAMITY_MAGIC = TagKey.create(
        Registries.DAMAGE_TYPE, CalamityCurios.ModResource("magic"));

    //death msg reference vanilla magic msg
    public static final DamageSupplier MAGIC_PROJECTILE = new DamageSupplier(
        "calamity_curios:magic_projectile", "death.attack.magic", null);

    private final String key;
    private final String msg;
    private final ChatFormatting formatting;
    private Holder<DamageType> holder;

    private DamageSupplier(@NotNull String key, String msg, ChatFormatting formatting) {
        this.key = key;
        this.msg = msg;
        this.formatting = formatting;
        SUPPLIERS.add(this);
    }

    public DamageSource get() {
        return get(null, null, null);
    }

    public DamageSource get(Entity directEntity) {
        return get(directEntity, null, null);
    }

    public DamageSource get(@Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return get(directEntity, causingEntity, null);
    }

    public DamageSource get(@Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition) {
        return new DamageSource(holder, directEntity, causingEntity, damageSourcePosition) {
            @Override
            public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity livingEntity) {
                MutableComponent component = Component.translatable(msg != null ? msg : holder.value().msgId(), livingEntity.getName());
                if (formatting != null) component.withStyle(formatting);
                return component;
            }
        };
    }

    public static void onServerStart(RegistryAccess access) {
        Registry<DamageType> registry = access.registryOrThrow(Registries.DAMAGE_TYPE);
        Holder.Reference<DamageType> kill = registry.getHolderOrThrow(DamageTypes.GENERIC_KILL);
        for (DamageSupplier supplier : SUPPLIERS) supplier.holder = registry.getHolder(ResourceKey.create(
            Registries.DAMAGE_TYPE, CalamityCurios.resource(supplier.key))).orElse(kill);
    }

    public static void findRequester(AnnotationProcessor annotationProcessor) {
        annotationProcessor.addStartProcessingEntries(DamageRequester.class, annotationProcessor.runData ? CalamityGen.findDamageGather() : processor -> {
            Map<String, Object> map = processor.getAnnotationData().annotationData();
            Object key = map.get("key");
            Object id = map.get("id");
            ModAnnotation.EnumHolder style = (ModAnnotation.EnumHolder) map.get("style");

            String location = id == null ? CalamityCurios.MODID : id + ":" + key;
            DamageSupplier supplier = new DamageSupplier(location, (String) map.get("msg"),
                style == null ? null : ChatFormatting.valueOf(style.getValue()));
            processor.lookup.findStaticVarHandle(processor.getDataClass(),
                processor.getAnnotationData().memberName(), DamageSupplier.class).set(supplier);
        });
    }
}
