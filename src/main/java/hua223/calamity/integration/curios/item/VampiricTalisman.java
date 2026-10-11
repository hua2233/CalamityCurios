package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.CriticalHitTriggerListener;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@JeiInfo(zh_cn = "手持并使用血石杀死僵尸村民时概率置换")
public class VampiricTalisman extends BaseCurio implements ICuriosStorage {
    public VampiricTalisman(Properties properties) {
        super(properties);
    }

    @ApplyEvent(202)
    public final void onAttack(PostAttackListener listener) {
        float[] count = getCount(listener.player);
        if (count[0] > 0 && listener.player.getHealth() < listener.player.getMaxHealth()) {
            listener.player.heal(Math.min(listener.getCorrectionValue(), count[0]));
            count[0] = 0;
        }
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitTriggerListener listener) {
        listener.applyAmplifier(0.12f);
        getCount(listener.player)[0] = listener.target.getHealth();
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @ApplyGlobalLoot
    public void onEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.ZOMBIE_VILLAGER) && context.chance(.25f)
            && context.player.getMainHandItem().is(CalamityItems.BLOOD_STONE.get()))
            context.player.setItemInHand(InteractionHand.MAIN_HAND, getDefaultInstance());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "vampiric_talisman", 1, 2);
        return tooltips;
    }
}
