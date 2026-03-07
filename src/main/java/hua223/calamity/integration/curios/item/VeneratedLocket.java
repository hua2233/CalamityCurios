package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class VeneratedLocket extends BaseCurio {
    public VeneratedLocket(Properties properties) {
        super(properties);
        GlobalLoot.mountTo(this);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (listener.isFarAttack()) {
            ServerLevel level = listener.player.getLevel();
            Projectile projectile = (Projectile) listener.projectile.getType().create(level);
            if (projectile != null) {
                RandomSource source = listener.entity.getRandom();
                Vec3 endPos = listener.entity.getEyePosition();
                Vec3 spawnPos = endPos.add( source.nextDouble() * 4 - 2,
                    source.nextInt(4, 7), source.nextDouble() * 4 - 2);
                projectile.setPos(spawnPos);
                spawnPos = endPos.subtract(spawnPos);
                projectile.shoot(spawnPos.x, spawnPos.y, spawnPos.z, 1f, 0.9f + source.nextFloat() * 0.2f);
                level.addFreshEntity(projectile);
            }
        } else if (!listener.entity.hasEffect(CalamityEffects.GOD_SLAYER_INFERNO.get()))
            listener.entity.addEffect(new MobEffectInstance(CalamityEffects.GOD_SLAYER_INFERNO.get(), 60));
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.applyAmplifier(0.1f);
    }

    @ApplyGlobalLoot
    @SuppressWarnings("ConstantConditions")
    public final void onGlobalEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.WANDERING_TRADER) && context.chance(0.1f) &&
            context.player.getServer().getLevel(Level.END).dragonFight().hasPreviouslyKilledDragon())
            context.addLoot(this, 1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("venerated_locket", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltips.add(CMLangUtil.getTranslatable("venerated_locket", 2).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltips.add(CMLangUtil.getTranslatable("venerated_locket", 3).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltips.add(CMLangUtil.getTranslatable("venerated_locket", 4).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
