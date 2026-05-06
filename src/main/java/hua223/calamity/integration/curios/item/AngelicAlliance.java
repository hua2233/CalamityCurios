package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.DeathListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;

public class AngelicAlliance extends BaseCurio implements ICuriosStorage, IKeyDataPackResponse {
    public AngelicAlliance(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier(uuid, "angelic_alliance", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        CuriosApi.addSlotModifier(modifier, "curio", uuid, 2, AttributeModifier.Operation.ADDITION);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, false);
    }

    @ApplyEvent(300)
    public final void onAttack(PlayerAttackListener listener) {
        if (listener.player.hasEffect(CalamityEffects.DIVINE_BLESS.get())) {
            float[] count = getCount(listener.player);
            CalamityHelp.addIfDoesNotExist(listener.entity, (int) (count[2] * 20), (int) count[3], CalamityEffects.HOLY_FLAMES.get());
            listener.amplifier += count[2] * 0.03f;
            listener.player.heal(listener.getCorrectionValue() * 0.1f);
        }
    }

    @ApplyEvent
    public final void onDeath(DeathListener listener) {
        if (listener.isPlayerDeath && listener.player.hasEffect(CalamityEffects.DIVINE_BLESS.get())) {
            listener.canceledEvent();
            listener.player.heal(listener.player.getMaxHealth() * (getCount(listener.player)[2] * 0.2f));
            listener.player.removeEffect(CalamityEffects.DIVINE_BLESS.get());
        }
    }

    @Override
    public void onServerResponse(ServerPlayer player) {
        List<? extends LivingEntity> servants = PlayerServantsManager.loadLevelServantsEntity(player);
        if (!servants.isEmpty()) {
            player.getCooldowns().addCooldown(this, 1200);
            int k = 0;
            for (LivingEntity entity : servants) {
                entity.discard();
                k++;
            }

            float[] count = getCount(player);
            player.heal(k * 2f);
            //heal value
            count[1] = Mth.clamp(k, 1f, 6f);
            //effect amplifier
            count[3] = Mth.clamp(k / 3, 0, 2);
            //base amplifier
            count[2] = Mth.clamp(k,  1, 6);
            player.addEffect(new MobEffectInstance(CalamityEffects.DIVINE_BLESS.get(), (int) (count[2] * 200)));
            player.level().playSound(null, player.blockPosition(), CalamitySounds.AA_ACTIVATION.get(), SoundSource.AMBIENT);
        }
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (count[0]++ > 19 && player.hasEffect(CalamityEffects.DIVINE_BLESS.get())) {
            count[0] = 0;
            player.heal(count[1]);
        }
    }

    @Override
    public int getCountSize() {
        return 4;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getKeyCode() {
        return 73;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "angelic_alliance", 2, 3, 4, 5);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("angelic_alliance", 1).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
