package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

import static hua223.calamity.register.items.CalamityItems.WARBANNER_SUN;

@JeiInfo(item = WARBANNER_SUN, zh_cn = "小概率从远古骑士掉落，受抢夺影响")
public class SunWarBanner extends BaseCurio implements ICuriosStorage {
//    @AutoConfig(template = CalamityConfig.ConfigTemplate.DROP)
//    public static final float DROP_PROBABILITY = CalamityConfig.value();

    public SunWarBanner(Properties properties) {
        super(properties);
    }

    public static int getAmplifier(int distance) {
        int amplifier = 6;
        if (distance > 100) {
            return amplifier;
        } else {
            int difference = ((distance / 20) + 1);
            return amplifier - difference;
        }
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level().isClientSide) return;
        getUUID(equipped)[0] = uuid;
        VariableAttributeModifier var = new VariableAttributeModifier(uuid, "sun_war_banner", 0, AttributeModifier.Operation.MULTIPLY_BASE);
        modifier.put(Attributes.ATTACK_SPEED, var);
        modifier.put(Attributes.ATTACK_DAMAGE, var);
        modifier.put(CalamityAttributes.CLOSE_RANGE.get(), var);
    }

    @SuppressWarnings("ConstantConditions")
    private void updateAttribute(Player player, double amplifier) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        ((VariableAttributeModifier) instance.getModifier(getFirstUUID(player)))
            .setBatchValue(amplifier, instance, player.getAttribute(Attributes.ATTACK_SPEED),
                player.getAttribute(CalamityAttributes.CLOSE_RANGE.get()));
    }

    private static List<Mob> getNearbyEnemies(Player player) {
        double x = player.getX(), y = player.getY(), z = player.getZ();
        return CalamityHelp.getAttackableEntity(Mob.class, player, new AABB(x - 10, y - 5, z - 10,
            x + 10, y + 5, z + 10));
    }

    @ApplyGlobalLoot(item = WARBANNER_SUN)
    public void onEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityRegistry.KEEPER.get())
            && context.lootingChance(.02f)) context.addLoot(this);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        getCount(player)[1] = 6;
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) >= 10) {
            float[] count = getCount(player);
            count[0] = 0;
            List<Mob> mobs = getNearbyEnemies(player);

            float lastAmplifier = count[1];

            if (mobs.isEmpty()) {
                if (lastAmplifier > 0) {
                    updateAttribute(player, 0);
                    count[1] = 6;
                }
                return;
            }

            double distance = 150;
            for (LivingEntity mob : mobs) {
                distance = Math.min(distance, player.distanceToSqr(mob));
            }

            int amplifier = getAmplifier((int) distance);

            if (amplifier == lastAmplifier) {
                return;
            } else if (amplifier == 6) {
                updateAttribute(player, 0);
                count[1] = 6;
                return;
            }

            updateAttribute(player, amplifier * 0.04);
            count[1] = amplifier;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "warbanner", 2, 3);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("warbanner", 1).withStyle(ChatFormatting.RED));
        return tooltips;
    }
}
