package hua223.calamity.register.effects;

import hua223.calamity.net.IEffectDataResponse;
import hua223.calamity.register.config.ClientConfigValue;
import hua223.calamity.render.CalamityOutlineRenderer;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.function.Consumer;

public class Omniscience extends CalamityEffect implements IEffectsCallBack, IEffectDataResponse {
    public Omniscience(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer) sendToClient((ServerPlayer) entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void onClientResponse(CompoundTag tag) {
        LocalPlayer player = Minecraft.getInstance().player;
        Level level = player.level();

        final Consumer<BlockPos> tick = pos -> {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && level.hasChunk(pos.getX(), pos.getY())) {
                if (state.is(Tags.Blocks.ORES)) CalamityOutlineRenderer.addRenderTarget(pos, state, 0xFFFFFFFF);
                else if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity loot && loot.calamity$Detected)
                    CalamityOutlineRenderer.addRenderTarget(pos, state, 0xFFFFAA00);
            }
        };


        CalamityOutlineRenderer.start(() -> {
            if (player.isDeadOrDying() || !player.hasEffect(this)) return true;
            //Apply Client Tick
            AABB scope = player.getBoundingBox().inflate(ClientConfigValue.DETECTING_RADIUS);
            BlockPos.betweenClosedStream(scope).forEach(tick);

            List<Entity> entities = level.getEntities(player, scope);
            if (!entities.isEmpty()) for (Entity entity : entities)
                if (entity instanceof Enemy || (entity instanceof Projectile
                    projectile && projectile.calamity$Detected)) CalamityOutlineRenderer.addRenderTarget(entity, 0xFFFF0000);

            return false;
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("omniscience").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
