package hua223.calamity.register.effects;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.EnableTrippy;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

//I like to eat mushrooms! of course not this kind...
public class Trippy extends CalamityEffect implements IEffectsCallBack {
    public Trippy(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "50cd184c-6dc0-486d-b52b-bb73cb5cc417", 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(AttributeRegistry.SPELL_POWER.get(), "50cd184c-6dc0-486d-b52b-bb73cb5cc417", 1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer) NetMessages.sendToClient(new EnableTrippy(), (ServerPlayer) entity);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("trippy")
            .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
