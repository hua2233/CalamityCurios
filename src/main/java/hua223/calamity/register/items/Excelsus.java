package hua223.calamity.register.items;

import hua223.calamity.register.RegisterList;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.ExcelsusPro;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.util.Vector2f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Excelsus extends SwordItem implements IDataPackResponse {
    public Excelsus(int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(RegisterList.GOD_EATER, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (usedHand == InteractionHand.MAIN_HAND && !player.getCooldowns().isOnCooldown(this)) {
            if (level.isClientSide) {
                player.swing(usedHand);
            } else {
                ExcelsusPro.create(player, level);
                stack.hurtAndBreak(20, player, entity -> {});
                player.getCooldowns().addCooldown(this, 40);
            }

            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof ServerPlayer player) {
            //Directly applies the original damage of the particle beam，Prioritize different targets around the attacked target
            LivingEntity hurt = CalamityHelp.getClosestTarget(attacker, target.getBoundingBox().inflate(6), target.position());
            hurt = hurt == null ? target : hurt;
            hurt.hurt(player.damageSources().indirectMagic(player, player), 6);

            if (hurt.isAlive()) {
                MobEffect effect = CalamityEffects.GOD_SLAYER_INFERNO.get();
                CalamityHelp.addIfDoesNotExist(target, 60, 0, effect);
                if (hurt != target )CalamityHelp.addIfDoesNotExist(hurt, 40, 0, effect);
                getPack().putInt("id", hurt.getId());
                sendToClient(player);
            }
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            Entity target = level.getEntity(tag.getInt("id"));
            if (target != null && target.isAlive()) {
                ParticleEngine engine = Minecraft.getInstance().particleEngine;
                RandomSource random = level.getRandom();
                Vector2f offset = Vector2f.nextVector2Circular(3f, 3f, random);
                Vec3 start = target.getEyePosition();
                CalamitySounds.EXCELSUS_RAY.playLocalSound();

                Vec3 distance = new Vec3(offset.x, random.nextInt(4, 5) + random.nextFloat(), offset.y);
                double length = distance.length();
                int sectionLength = (int) (length / 0.15);
                Vec3 section = new Vec3(distance.x / sectionLength, distance.y / sectionLength, distance.z / sectionLength);
                int oppositeDirection = (int) (sectionLength * 0.2f) + 1;
                sectionLength -= oppositeDirection;


                spawnParticle(sectionLength, engine, start, section, random, false);
                //Pass through the target a little bit to prevent the end point from being in the model
                spawnParticle(oppositeDirection, engine, start, section, random, true);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnParticle(int count, ParticleEngine engine, Vec3 start, Vec3 section, RandomSource random, boolean isReverse) {
        float r1 = 140 / 255f;
        float g1 = 239 / 255f;
        float b1 = 254 / 255f;
        float r2 = 229 / 255f;
        float g2 = 30 / 255f;
        float b2 = 202 / 255f;

        for (int i = 0; i < count; i++) {
            double x = section.x * i;
            double y = section.y * i;
            double z = section.z * i;
            if (isReverse) {
                x = -x;
                y = -y;
                z = -z;
            }

            //it shouldn't escape
            Particle particle = engine.makeParticle(ParticleTypes.END_ROD, start.x + x, start.y + y,
                start.z + z, 0d, 0d, 0d);

            if (particle != null) {
                particle.setLifetime(10);
                particle.gravity = 0f;
                particle.scale(0.6f);
                if (random.nextFloat() > 0.33) particle.setColor(r1, g1, b1);
                else particle.setColor(r2, g2, b2);
                engine.add(particle);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag advanced) {
        tooltip.add(CMLangUtil.getTranslatable("excelsus", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(CMLangUtil.getTranslatable("excelsus", 2).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
