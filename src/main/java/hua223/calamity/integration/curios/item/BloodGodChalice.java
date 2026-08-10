package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.generators.DamageMapping;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PlayerHealListener;
import hua223.calamity.register.damage.DamageRequester;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.particle.ParticleRegister;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
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
    @DamageRequester(key = DamageMapping.BLEEDING, msg = "blood_god",
        style = ChatFormatting.DARK_RED, zh_cn = "%s成为了祭品")
    public static DamageSupplier supplier;

    public BloodGodChalice(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        super.equipHandle(player, stack);
        getMemory(player).putTypeStorage(supplier.get());
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        syncHealth(player);
        float amount = getCount(player)[1];
        if (amount > 0) player.hurt(getMemory(player).getTypeStorage(DamageSource.class), amount);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "blood_god_chalice", 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifier.put(AttributeRegistry.BLOOD_SPELL_POWER.get(), new AttributeModifier(uuid, "blood_god_chalice", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
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
        if (cache > 0) {
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

            player.hurt(memory.getTypeStorage(DamageSource.class),
                ICuriosStorage.getReducedValue(memory.count, 1, memory.count[2]));
        }

        if (tick == 100) {
            memory.count[0] = 0;
            double max = player.getMaxHealth();
            double health = player.getHealth();
            if (health < max) {
                float value = (float) ((max - health) * 0.3f);
                if (value > 0.2f) player.heal(value);
            }
        }
    }

    @Override
    public Class<?>[] defineStorageType() {
        return new Class[] {DamageSource.class};
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
