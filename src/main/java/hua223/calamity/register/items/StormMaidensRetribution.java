package hua223.calamity.register.items;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.entity.ColorfulLightningBolt;
import hua223.calamity.register.entity.projectiles.ItemPro;
import hua223.calamity.register.particle.ParticleRegister;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.CalamityOutlineRenderer;
import hua223.calamity.render.IllusionBufferSource;
import hua223.calamity.render.Item.StormMaidensModel;
import hua223.calamity.render.Item.StormMaidensRenderer;
import hua223.calamity.util.*;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

//Developer's Prayer
public class StormMaidensRetribution extends TridentItem implements IThrowableItem, IDataPackResponse {
    public StormMaidensRetribution(Properties properties) {
        super(properties);
        defaultModifiers = ImmutableMultimap.of
            (Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "storm_maidens_retribution", 22, AttributeModifier.Operation.ADDITION),
            Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "storm_maidens_retribution", -2.7, AttributeModifier.Operation.ADDITION),
            AttributeRegistry.LIGHTNING_SPELL_POWER.get(), new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "storm_maidens_retribution", 0.7, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND) {
            if (level.isClientSide) {
                IllusionBufferSource.create();
                CalamityOutlineRenderer.start(() -> {
                    ItemStack main = player.getMainHandItem();
                    if (!player.isUsingItem() || !main.is(this)) return true;
                    CompoundTag tag = main.getOrCreateTag();
                    Entity entity = player.level().getEntity(tag.getInt("target"));
                    if (entity != null) CalamityOutlineRenderer.addRenderTarget(entity,
                        FastColor.ARGB32.lerp(tag.getFloat("RawAlpha"), 0xFFFF0000, 0xFFCE042C));

                    return false;
                });
            } else player.startUsingItem(hand);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }
    @Override
    public int getDamage(ItemStack stack) {
        return 100;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (livingEntity.calamity$IsPlayer && remainingUseDuration >= getUseDuration(stack) - 30) {
            CompoundTag tag = stack.getOrCreateTag();
            int useTime = getUseDuration(stack) - remainingUseDuration;
            boolean complete = useTime == 30;

            if (level.isClientSide) {
                float value;
                if (complete) {
                    value = 0f;
                    CalamitySounds.MAGIC.playLocalSound();
                } else {
                    value = (1f - (useTime % 10) / 10f) * 0.6f;
                    if (value == 0.6f) CalamitySounds.LIGHTNING.playLocalSound();
                }

                tag.putFloat("RawAlpha", value);
            } else {
                //It must be synchronized from the server, otherwise there may be strange enemy search problems
                LivingEntity entity = CalamityHelp.getLookedEntity(livingEntity, level, 25);
                if (entity != null && tag.getInt("target") != entity.getId()) {
                    tag.putInt("target", entity.getId());
                    getPack().putInt("target", entity.getId());
                    sendToClient((ServerPlayer) livingEntity);
                }

                if (complete) {
                    Vec3 spawn = livingEntity.getEyePosition().add(livingEntity.getLookAngle().normalize());
                    ColorfulLightningBolt bolt = CalamityCurios.getEntityType(ColorfulLightningBolt.class).create(level);
                    bolt.setPos(spawn);
                    bolt.setSilent(true);
                    bolt.setVisualOnly(true);
                    bolt.setColor(0x28664A3F);
                    level.addFreshEntity(bolt);
                }
            }
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity player, int timeLeft) {
        CompoundTag tag = stack.getOrCreateTag();
        if (level.isClientSide) IllusionBufferSource.destroy();
        else if (timeLeft <= getUseDuration(stack) - 30) {
            Entity entity = level.getEntity(tag.getInt("target"));
            ItemPro pro = of(stack, level);
            pro.setOwner(player);
            pro.noPhysics = true;
            pro.setNoGravity(true);
            pro.setPos(player.getEyePosition());
            pro.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5f, 0F);
            CompoundTag compoundTag = pro.getPersistentData();
            if (entity != null) compoundTag.putInt("target", entity.getId());
            compoundTag.putDouble("speed", pro.getDeltaMovement().length());
            player.level().addFreshEntity(pro);
            CalamitySounds.THROW.playSound(entity == null ? player : entity);
        }

        tag.remove("RawAlpha");
        tag.remove("target");
    }

    @Override
    public @NotNull Component getProName() {
        return Component.translatable("storm_maidens_spear");
    }

    @Override
    public boolean customTick(ItemPro pro) {
        if (!pro.level().isClientSide) {
            if (pro.tickCount > 400) {
                pro.discard();
                return true;
            }

            CompoundTag tag = pro.getPersistentData();
            double speed = tag.getFloat("speed");
            if (pro.tickCount % 20 == 0) speed *= 1.1F;
            Entity target = pro.level().getEntity(tag.getInt("target"));

            if (target != null) {
                Vec3 position = target.position().add(0.0F, target.getBbHeight() / (double)2.0F, 0.0F);
                Vec3 direction = position.subtract(pro.position());
                if (direction.length() < 2f) {
                    target.kill();
                    writeVec3("pos", position, true);
                    sendToAllClient();
                    pro.discard();
                    return false;
                } else pro.setDeltaMovement(direction);
            }

            Vec3 vec3 = pro.getDeltaMovement().normalize().scale(speed);
            pro.setDeltaMovement(vec3);
            pro.move(MoverType.SELF, pro.getDeltaMovement());
        }

        return false;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onHitBlock(ItemPro itemPro, BlockHitResult result) {
        int[] palette = new int[] {0x4BFF0000, 0x4BFFC800, 0x4BFFAEAE, 0x4BCD5C5C};
        BlockPos pos = result.getBlockPos();
        Level level = itemPro.level();
        EntityType<ColorfulLightningBolt> type = CalamityCurios.getEntityType(ColorfulLightningBolt.class);
        for (int i = 0; i < 3; i++) {
            ColorfulLightningBolt bolt = type.create(level);
            bolt.setDamage(300);
            bolt.setPos(pos.getX(), pos.getY(), pos.getZ());
            bolt.setColor(CalamityHelp.multicolorLerp(level.random.nextFloat(), palette));
            level.addFreshEntity(bolt);
        }
    }

    @Override
    public void onHitEntity(ItemPro itemPro, EntityHitResult result) {
        Entity entity = result.getEntity();
        if (!entity.level().isClientSide) {
            entity.kill();
            writeVec3("pos", entity.position().add(0.0F, entity.getBbHeight() / (double)2.0F, 0.0F), true);
            sendToAllClient();
        }
    }

    @Override
    public boolean destroyAfterHitting(ItemPro itemPro) {
        return !itemPro.level().isClientSide;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean customRender(ItemPro pro, ItemRenderer renderer, ItemStack stack, float partialTick,
                                PoseStack pose, @NotNull MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, pro.yRotO, pro.getYRot()) + 90F));
        pose.mulPose(Axis.ZP.rotationDegrees(-Mth.lerp(partialTick, pro.xRotO, pro.getXRot()) + 120F));
        pose.scale(2f, 2f, 0.85f);
        pose.translate(0.3f, 0, -0.5);
        RenderUtil.renderItemModelList(Minecraft.getInstance().getItemRenderer(),
            StormMaidensModel.getMainModel(), stack, pose, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void onClientResponse(CompoundTag tag) {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = player.clientLevel;
        if (tag.contains("target", 3)) {
            ItemStack stack = player.getMainHandItem();
            if (stack.is(this)) stack.getOrCreateTag().putInt("target", tag.getInt("target"));
        } else {
            Vec3 position = readVec3("pos", tag);
            for (int i = 0; i < 6; i++) {
                Vector2f vector2f = Vector2f.nextVector2Circular(.06f, .06f, level.random);
                level.addParticle(ParticleRegister.ELECTRIC_EXPLOSION_RING.get(),
                    position.x + vector2f.x, position.y + vector2f.y, position.z, 0, 0, 0);
            }

            for (int i = 0; i < 4; i++)
                level.addParticle(ParticleRegister.STORM_LIGHTNING.get(), true, position.x + (
                    level.random.nextBoolean() ? i : -i) / 10f, position.y + 30, position.z - i / 100f, position.y - 10, 0, 0);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new StormMaidensRenderer();
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "storm_maidens_retribution",  1, 2);
        tooltips.add(CMLangUtil.blankLine());
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.DARK_RED, "storm_maidens_retribution",  3, 4);
    }
}
