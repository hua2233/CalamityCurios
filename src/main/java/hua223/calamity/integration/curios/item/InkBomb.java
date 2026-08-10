package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.ItemPro;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@CurioRepel(AbyssalMirror.class)
public class InkBomb extends BaseCurio implements IThrowableItem {
    public InkBomb(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving && !listener.player.getCooldowns().isOnCooldown(this)) {
            ItemStack stack = getDefaultInstance();
            float yRot = listener.player.getYRot();
            Level level = listener.player.level();
            Vec3 position = listener.player.getEyePosition();
            for (int i = 0; i < 4; i++) {
                ItemPro pro = of(stack, level);
                pro.setPos(position);
                pro.setOwner(listener.player);
                pro.shootFromRotation(listener.player, 0, yRot + i * 90, -20f, 0.5f, 1f);
                level.addFreshEntity(pro);
            }

            listener.player.heal(4f);
            listener.player.getCooldowns().addCooldown(this, 500);
        }
    }

    @Override
    public @NotNull Component getProName() {
        return Component.translatable(getDescriptionId());
    }

    @Override
    public void onHitEntity(ItemPro itemPro, EntityHitResult result) {
        if (!itemPro.level().isClientSide) {
            Entity entity = itemPro.getOwner();
            if (entity != null) new FriendlyEffectCloudBuilder(entity, itemPro.position(), 200, 3f)
                .setEffects(new MobEffectInstance(CalamityEffects.CONFUSED.get(), 80)).setCustomColor(0xFF2B1B17).build();
        }
    }

    @Override
    public void onHitBlock(ItemPro itemPro, BlockHitResult result) {
        onHitEntity(itemPro, null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "ink_bomb", 1, 2, 3);
        return tooltips;
    }
}
