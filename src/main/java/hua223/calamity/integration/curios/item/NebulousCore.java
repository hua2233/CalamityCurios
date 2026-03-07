package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.DeathListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.entity.projectiles.Nebula;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class NebulousCore extends BaseCurio implements ICuriosStorage {
    public NebulousCore(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier("nebulous_core", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 20)) {
            if (resetOrUpdate(player, 1, 10)) {
                Nebula.spawnAroundPlayer(player);
            }
        }
    }

    @ApplyEvent
    public final void onDeath(DeathListener listener) {
        if (listener.isPlayerDeath) {
            ServerPlayer player = listener.player;
            ItemCooldowns cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(this)) return;
            listener.canceledEvent();
            player.setHealth(player.getMaxHealth() * 0.4f);
            cooldowns.addCooldown(this, 1800);
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("nebulous_core", 1));
        tooltips.add(CMLangUtil.getTranslatable("nebulous_core", 2));
        tooltips.add(CMLangUtil.getTranslatable("nebulous_core", 3));
        return tooltips;
    }
}
