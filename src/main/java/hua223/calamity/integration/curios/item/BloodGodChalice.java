package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerHealListener;
import hua223.calamity.register.particle.ParticleRegister;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.damage.CalamityDamageSource;
import hua223.calamity.util.damage.CalamityDamageTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = BloodGodChalice.class, isRoot = true)
public class BloodGodChalice extends BaseCurio implements ICuriosStorage {
    public BloodGodChalice(Properties properties) {
        super(properties);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        syncHealth(player);
        float amount = getCount(player)[1];
        if (amount > 0) player.hurt(CalamityDamageSource.source(CalamityDamageTypes.BLOOD_GOD, player), amount);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "blood_god_chalice", 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @ApplyEvent
    public final void onHeal(PlayerHealListener listener) {
        float[] count = getCount(listener.player);
        listener.amplification += 0.25f;
        if (count[1] > 0)
            count[1] -= Math.min(count[1], listener.getCorrectionValue() / 2);
    }

    @ApplyEvent(800)
    public final void onHurt(HurtListener listener) {
        float cache = listener.getCorrectionValue() - 2;
        if (!listener.isCanceled() && cache > 0) {
            float[] count = getCount(listener.player);
            listener.setFinalAmount(2);

            count[1] += cache;
            count[2] = count[1] <= 2 ? count[1] : count[1] / 5;
        }
    }

    @Override
    protected void onPlayerTick(Player player) {
        var memory = getMemory(player);
        float tick = memory.count[0]++;
        if (tick % 20 == 0 && memory.count[1] > 0) {
            Level level = player.level();
            ((ServerLevel) level).sendParticles(ParticleRegister.BLOOD.get(), player.getX(),
                player.getY(), player.getZ(), level.random.nextInt(1, 4),
                0, 0, 0 , 0);

            player.hurt(CalamityDamageSource.source(CalamityDamageTypes.BLOOD_GOD, player),
                ICuriosStorage.getReducedValue(memory.count, 1, memory.count[2]));
        }

        if (tick == 100) {
            memory.count[0] = 0;
            double max = player.getMaxHealth();
            double health = player.getHealth();
            if (health < max) player.heal((float) Math.max(1, (max - health) * 0.4f));
        }
    }

    @Override
    public int getCountSize() {
        return 3;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "blood_god_chalice", 2, 3, 4, 5);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("blood_god_chalice", 1).withStyle(ChatFormatting.DARK_RED));
        return tooltips;
    }
}
