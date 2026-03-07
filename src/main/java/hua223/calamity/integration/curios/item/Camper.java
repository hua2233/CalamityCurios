package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.effects.SurvivableEffectInstance;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class Camper extends BaseCurio implements ICuriosStorage {
    public Camper(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        ServerPlayer player = listener.player;

        if (player.walkDist != player.walkDistO) {
            listener.amplifier -= 0.9f;
        } else {
            listener.amplifier += 0.25f;
        }
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ARMOR, new AttributeModifier(uuid, "camper", 7, AttributeModifier.Operation.ADDITION));
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);

        //Save the current state, where walkDist synchronizes with walkDistO before the execution of EffectList, and updates after the execution of EffectList.
        //SurvivableEffectInstance is executing in an intermediate state and cannot obtain correct information
        boolean noMove = player.walkDist == player.walkDistO;
        count[1] = noMove ? 1 : 0;
        if (++count[0] % 5 == 0) {
            if (noMove) {
                ItemStack main = player.getMainHandItem();

                if (main == ItemStack.EMPTY && !player.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                    player.addEffect(new SurvivableEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1,
                        () -> count[1] == 1 && player.getMainHandItem() == ItemStack.EMPTY).setNoFlicker());
                    return;
                }

                Item item = main.getItem();
                if (item.isEdible() && !player.hasEffect(MobEffects.SATURATION)) {
                    player.addEffect(new SurvivableEffectInstance(MobEffects.SATURATION, 60, 1,
                        () -> count[1] == 1 && player.getMainHandItem().isEdible()).setNoFlicker());
                } else if (item instanceof SwordItem && !player.hasEffect(MobEffects.DAMAGE_BOOST)) {
                    player.addEffect(new SurvivableEffectInstance(MobEffects.DAMAGE_BOOST, 60, 2,
                        () -> count[1] == 1 && player.getMainHandItem().getItem() instanceof SwordItem).setNoFlicker());
                } else if ((item instanceof BowItem || item instanceof CrossbowItem) && !player.hasEffect(MobEffects.INVISIBILITY)) {
                    player.addEffect(new SurvivableEffectInstance(MobEffects.INVISIBILITY, 60, 2,
                        () -> {
                            Item i = player.getMainHandItem().getItem();
                            return count[1] == 1 && (i instanceof BowItem || i instanceof CrossbowItem);
                        }).setNoFlicker());
                } else if (item instanceof PickaxeItem && !player.hasEffect(MobEffects.DIG_SPEED)) {
                    player.addEffect(new SurvivableEffectInstance(MobEffects.DIG_SPEED, 60, 1,
                        () -> count[1] == 1 && player.getMainHandItem().getItem() instanceof PickaxeItem).setNoFlicker());
                }

                if (count[0] == 20) {
                    List<Entity> entities = player.level.getEntities(player, player.getBoundingBox().inflate(5),
                        entity -> entity.isPickable() && entity.isAttackable() && !entity.isAlliedTo(player));

                    if (!entities.isEmpty()) {
                        DamageSource source = DamageSource.playerAttack(player);
                        source.setMagic();
                        for (Entity entity : entities)
                            entity.hurt(source, 6f);
                    }

                    count[0] = 0;
                }
            } else if (count[0] == 20) {
                if (player.getHealth() < player.getMaxHealth())
                    player.heal(2f);

                count[0] = 0;
            }
        }
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("camper", 1));
        tooltips.add(CMLangUtil.getTranslatable("camper", 2));
        tooltips.add(CMLangUtil.getTranslatable("camper", 3));
        tooltips.add(CMLangUtil.getTranslatable("camper", 4));
        tooltips.add(CMLangUtil.getTranslatable("camper", 5).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
