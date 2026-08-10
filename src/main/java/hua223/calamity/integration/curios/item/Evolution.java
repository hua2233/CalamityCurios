package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Evolution extends BaseCurio implements ICuriosStorage {
    public Evolution(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (listener.isFarAttack() && listener.getProjectile()
            .getPersistentData().contains("evolution"))
            listener.amplifier += 10f;
    }

    @ApplyEvent(140)
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        if (listener.isFarAttack()) {
            Projectile projectile = listener.getProjectile();
            GlobalCuriosStorage.CuriosMemory memory = getMemory(player);
            EntityType<?> type = projectile.getType();

            if (CalamityHelp.isCanDodge(player, listener.baseAmount,
                player.getMaxHealth() * 0.1f, (int) Math.min(listener.baseAmount * 20, 900))) {
                Vec3 move = projectile.getDeltaMovement().scale(-1.5);
                projectile.setDeltaMovement(move);
                projectile.setYRot((float)(Mth.atan2(move.x, move.z) * (180F / Math.PI)));
                projectile.setXRot((float)(Mth.atan2(move.y, move.horizontalDistance()) * (180F / Math.PI)));
                projectile.yRotO = projectile.getYRot();
                projectile.xRotO = projectile.getXRot();
                projectile.setOwner(player);
                projectile.getTags().add("Indestructible");
                projectile.getPersistentData().putBoolean("evolution", true);

                memory.putTypeStorage(type);
                memory.count[0] = 3;
                player.heal(listener.baseAmount / 2);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1, 100));
                listener.canceledEvent();
            } else if (memory.getTypeStorage(EntityType.class) == type && memory.count[0] > 0)
                listener.amplifier -= memory.count[0]-- * 0.33f;
        }
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    public Class<?>[] defineStorageType() {
        return new Class[] {EntityType.class};
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.LIGHT_PURPLE, "evolution", 1, 2, 3, 4, 5);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("evolution", 6).withStyle(ChatFormatting.DARK_AQUA));
        return tooltips;
    }
}
