package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.register.config.AutoConfig;
import hua223.calamity.register.config.CalamityConfig;
import hua223.calamity.util.CurioRepel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

@CurioRepel(isRoot = true)
@JeiInfo(zh_cn = "可以在下界堡垒的宝藏中被找到")
public class BloodyWormTooth extends BaseCurio {
    public BloodyWormTooth(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.ATTACK_DAMAGE,
            new AttributeModifier(uuid, "bloody_worm_tooth", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR, new AttributeModifier(uuid, "bloody_worm_tooth", 5, AttributeModifier.Operation.ADDITION));

    }

    @ApplyGlobalLoot
    public void onGlobalChest(ChestLootContext context) {
        if (context.fromSpecificName("nether_bridge") && context.chance(.1f)) context.addLoot(this);
    }
}
