package hua223.calamity.events.levelevent;

import hua223.calamity.events.levelevent.client.ClientLevelEvent;
import hua223.calamity.net.IDataPackResponse;
import net.jodah.typetools.TypeResolver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public abstract class LevelEventActiveItem<T extends LevelEvent<?>> extends Item implements IDataPackResponse {
    private final Class<T> eClass;

    @SuppressWarnings("unchecked")
    public LevelEventActiveItem(Properties properties) {
        super(properties);
        eClass = (Class<T>) TypeResolver.resolveRawArgument(LevelEventActiveItem.class, getClass());
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (!level.isClientSide && (level.dimension() == getUseDimension() || eventInProgress())
            && usedHand == InteractionHand.MAIN_HAND && !player.getCooldowns().isOnCooldown(this)) {
            if (!eventInProgress()) activeEvent((ServerPlayer) player);
            player.startUsingItem(usedHand);
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    protected ResourceKey<Level> getUseDimension() {
        return Level.OVERWORLD;
    }

    protected abstract void activeEvent(ServerPlayer player);

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean canBeHurtBy(@NotNull DamageSource damageSource) {
        return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    protected boolean eventInProgress() {
        return LevelEvent.inProgress(eClass);
    }

    @SuppressWarnings("unchecked")
    protected T getEvent() {
        return (T) LevelEvent.getActiveWorldEvent();
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract void createClientEvent(CompoundTag tag);

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        if (ClientLevelEvent.getActiveWorldEvent() == null) createClientEvent(tag);
        else ClientLevelEvent.getActiveWorldEvent().handlerDataPack(tag, this);
    }
}
