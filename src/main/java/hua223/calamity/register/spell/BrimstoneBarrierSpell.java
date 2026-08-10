package hua223.calamity.register.spell;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.effects.CalamityEffects;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BrimstoneBarrierSpell extends AbstractSpell {
    public BrimstoneBarrierSpell() {
        this.baseManaCost = 150;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return CalamityCurios.ModResource("brimstone_barrier");
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return !level.isClientSide && entity.calamity$IsPlayer && !entity.hasEffect(CalamityEffects.BRIMSTONE_BARRIER.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(CalamityEffects.BRIMSTONE_BARRIER.get(), 1000));
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig().setMinRarity(SpellRarity.LEGENDARY).setSchoolResource(SchoolRegistry.BLOOD_RESOURCE).setMaxLevel(1).setCooldownSeconds(45.0F).build();
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }
}
