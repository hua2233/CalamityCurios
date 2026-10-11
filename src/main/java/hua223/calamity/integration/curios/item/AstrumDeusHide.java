package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.Meteor;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

import static hua223.calamity.register.items.CalamityItems.HIDE_OF_ASTRUM_DEUS;

@JeiInfo(item = HIDE_OF_ASTRUM_DEUS, zh_cn = "在末地要塞的宝箱中可以被找到")
public class AstrumDeusHide extends BaseCurio implements ICuriosStorage {
    public AstrumDeusHide(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) {
            getUUID(equipped)[0] = uuid;
            modifier.put(CalamityAttributes.CLOSE_RANGE.get(),
                new VariableAttributeModifier(uuid, "hide", 0, AttributeModifier.Operation.ADDITION));
        }
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.ASTRAL_INFECTION.get(), CalamityEffects.CURSED_INFERNO.get());
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        if (listener.isFarAttack()) return;

        ItemCooldowns cooldowns = listener.player.getCooldowns();
        if (!cooldowns.isOnCooldown(this)) {
            for (int i = 0; i < 6; i++)
                Meteor.of(listener.entity, listener.player, false);

            cooldowns.addCooldown(this, 600);
        }
    }

    @ApplyEvent
    @SuppressWarnings("ConstantConditions")
    public final void onDamage(DamageListener listener) {
        if (listener.isTriggerByLiving) {
            ServerPlayer player = listener.player;
            LivingEntity attacker = listener.entity;

            ItemCooldowns cooldowns = listener.player.getCooldowns();
            if (!cooldowns.isOnCooldown(this)) {
                for (int i = 0; i < 4; i++)
                    Meteor.of(attacker, player, false);

                player.calamity$SetInvulnerableTime(2);
                player.level().explode(player, player.getX(), player.getY(), player.getZ(),
                    2f, Level.ExplosionInteraction.NONE);
                cooldowns.addCooldown(this, 400);

                getCount(player)[0] = Math.min((int) listener.baseAmount * 3, 200);
                AttributeInstance instance = player.getAttribute(CalamityAttributes.CLOSE_RANGE.get());
                VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(getFirstUUID(player));
                modifier.setValue(0.3, instance);
            }

            attacker.hurt(player.level().damageSources().magic(), listener.baseAmount * 0.75f);
            MobEffect effect = CalamityEffects.ASTRAL_INFECTION.get();
            if (!attacker.hasEffect(effect))
                attacker.addEffect(new MobEffectInstance(effect, 200, 0), player);
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onPlayerTick(Player player) {
        var memory = getMemory(player);
        if (--memory.count[0] == 0) {
            AttributeInstance instance = player.getAttribute(CalamityAttributes.CLOSE_RANGE.get());
            VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(memory.uuids[0]);
            modifier.setValue(0, instance);
        }
    }

    @ApplyGlobalLoot(item = HIDE_OF_ASTRUM_DEUS)
    public void onChestLoot(ChestLootContext context) {
        if (context.fromFuzzyType("stronghold") && context.chance(.25f))
            context.addLoot(this);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "hide", 2, 3, 4, 5);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("hide", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        return tooltips;
    }
}
