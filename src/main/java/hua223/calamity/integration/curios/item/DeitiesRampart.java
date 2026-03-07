package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.entity.projectiles.Meteor;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DeitiesRampart extends BaseCurio {
    private static Set<UUID> players;

    public DeitiesRampart(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        if (listener.isTriggerByLiving)
            Meteor.of(listener.entity, player, true);

        float maxHealth = player.getMaxHealth();
        float health = player.getHealth();
        int count = invincibleTimes(maxHealth, health);

        float halfHealth = player.getMaxHealth() / 2;
        if (listener.baseAmount > halfHealth) count += 20;

        player.invulnerableTime += count;
        if (health < halfHealth) listener.amplifier -= 0.15f;
    }

//    @ApplyEvent
//    public final void onDeath(DeathListener listener) {
//        //Should I consider using UUID
//        if (listener.isPlayerDeath) DelayRunnable.addRunTask(4, () -> {
//            if (!listener.player.isAlive()) {
//                players.remove(listener.player);
//                if (players.isEmpty()) players = null;
//            }
//        });
//    }

    @Override
    public void onLogOut(Player player) {
        if (!player.isLocalPlayer())
            players.remove(player.getUUID());
    }

    @SuppressWarnings("ConstantConditions")
    public static void rampartGuard(HurtListener listener) {
        if (players == null || players.contains(listener.player.getUUID())) return;

        PlayerList list = listener.player.getServer().getPlayerList();
        int[] count = {0};
        final float base = listener.baseAmount * 0.25f;
        DamageSource source = CalamityDamageSource.getAbyss();
        players.stream().map(list::getPlayer).filter(player -> {
            if (player.getHealth() > (player.getMaxHealth() / 4)) {
                count[0]++;
                return true;
            }
            return false;
        }).forEach(player -> player.calamity$HurtNoInvulnerable(source, base / count[0]));

        if (count[0] > 0) listener.amplifier -= 0.25f;
    }

    private static int invincibleTimes(float maxHealth, float health) {
        int init = 10;
        int percentage = ((int) (maxHealth / health * 100)) / 100;

        if (percentage < 0.75) {
            int amplifier = percentage / 15;
            return init + amplifier * 6;
        } else return init + 30;
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        if (players == null) players = new HashSet<>();
        players.add(player.getUUID());
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        players.remove(player.getUUID());
        if (players.isEmpty()) players = null;
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(uuid, "deities_rampart", 10, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "deities_rampart", 12, AttributeModifier.Operation.ADDITION));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("deities_rampart", 1));
        tooltips.add(CMLangUtil.getTranslatable("deities_rampart", 2));
        tooltips.add(CMLangUtil.getTranslatable("deities_rampart", 3));
        tooltips.add(CMLangUtil.getTranslatable("deities_rampart", 4));
        tooltips.add(CMLangUtil.getTranslatable("deities_rampart", 5));
        tooltips.add(CMLangUtil.getTranslatable("deities_rampart", 6));
        return tooltips;
    }
}
