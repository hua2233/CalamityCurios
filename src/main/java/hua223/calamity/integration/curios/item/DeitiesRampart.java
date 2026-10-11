package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.damage.DamageRequester;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.entity.projectiles.Meteor;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static hua223.calamity.generators.DamageMapping.SPUTTERING;

@CurioRepel(OdinsRefuge.class)
public class DeitiesRampart extends BaseCurio {
    @DamageRequester(key = SPUTTERING, msg = "protect",
        style = ChatFormatting.GOLD, zh_cn = "%s成为了他人的替死鬼")
    public static DamageSupplier supplier;
    private static Set<UUID> players;
    private static ObjectArrayList<ServerPlayer> sharer;

    public DeitiesRampart(Properties properties) {
        super(properties);
    }

    @ApplyEvent(205)
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        if (listener.isTriggerByLiving)
            Meteor.of(listener.entity, player, true);

        float maxHealth = player.getMaxHealth();
        float health = player.getHealth();
        int count = invincibleTimes(maxHealth, health);

        float halfHealth = player.getMaxHealth() / 2;
        if (listener.getCorrectionValue() > halfHealth) count += 20;

        player.calamity$SetInvulnerableTime(count);
        if (health < halfHealth) listener.amplifier -= 0.15f;
    }

    @SuppressWarnings("ConstantConditions")
    public static void rampartGuard(DamageListener listener) {
        if (players == null || players.contains(listener.player.getUUID())) return;

        Iterator<UUID> iterator = players.iterator();
        PlayerList list = listener.player.getServer().getPlayerList();

        while (iterator.hasNext()) {
            ServerPlayer player = list.getPlayer(iterator.next());
            if (player == null) iterator.remove();
            else if (player.level() == listener.player.level() && player.getHealth() > (player.getMaxHealth() / 4)) sharer.add(player);
        }

        if (players.isEmpty()) {
            players = null;
            sharer = null;
        } else if (!sharer.isEmpty()) {
            listener.amplifier -= 0.25f;
            DamageSource source = supplier.get();
            float hurt = listener.baseAmount * 0.25f / sharer.size();

            for (ServerPlayer player : sharer)
                player.hurt(source, hurt);

            sharer.clear();
        }
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
        if (players == null) {
            int count = player.server.getPlayerCount();
            players = new ObjectOpenHashSet<>(count);
            sharer = new ObjectArrayList<>(count);
        }

        players.add(player.getUUID());
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        if (players != null && players.remove(player.getUUID()) && players.isEmpty()) {
            players = null;
            sharer = null;
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.KNOCKBACK_RESISTANCE,
            new AttributeModifier(uuid, "deities_rampart", 10, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "deities_rampart", 12, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(uuid, "deities_rampart", 7, AttributeModifier.Operation.ADDITION));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "deities_rampart", 1, 2, 3, 4, 5, 6);
        return tooltips;
    }
}
