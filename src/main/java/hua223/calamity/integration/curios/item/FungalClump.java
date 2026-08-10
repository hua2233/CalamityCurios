package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.LogoutRelease;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class FungalClump extends BaseCurio implements ICuriosStorage {
    public FungalClump(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        UUID[] id = getUUID(listener.player);
        if (id[0] == null && listener.player != listener.entity) {
            listener.entity.addEffect(new MobEffectInstance(CalamityEffects.FUNGAL_CLUMP.get(), 600), listener.player);
            id[0] = listener.entity.getUUID();
        }
    }

    @Override
    protected void onPlayerTick(Player player) {
        UUID[] id = getUUID(player);
        if (id[0] != null) {
            Entity entity = ((ServerPlayer) player).serverLevel().getEntity(id[0]);
            if (entity == null || !entity.isAlive() || !((LivingEntity) entity).hasEffect(
                CalamityEffects.FUNGAL_CLUMP.get())) id[0] = null;
        }
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        UUID[] id = getUUID(player);
        if (id[0] != null) {
            Entity entity = player.serverLevel().getEntity(id[0]);
            if (entity != null && entity.isAlive() && entity instanceof LivingEntity living
                && living.hasEffect(CalamityEffects.FUNGAL_CLUMP.get())) living.removeEffect((CalamityEffects.FUNGAL_CLUMP.get()));
        }
    }

    @LogoutRelease
    public static void onLogOut(ServerPlayer player) {
        CalamityItems item = CalamityItems.FUNGAL_CLUMP;
        if (item.isEquip(player)) ((FungalClump) item.get()).unEquipHandle(player, null);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    public boolean storageCount() {
        return false;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GREEN, "fungal_clump", 1, 2);
        return tooltips;
    }
}
