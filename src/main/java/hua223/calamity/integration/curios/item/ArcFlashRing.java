package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.entity.ColorfulLightningBolt;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ArcFlashRing extends BaseCurio implements ICuriosStorage {
    public ArcFlashRing(Properties properties) {
        super(properties);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);

        Optional<ICuriosItemHandler> handler = CuriosApi.getCuriosInventory(slotContext.entity()).resolve();
        if (handler.isPresent()) {
            //Save Render State
            var memory = getMemory(slotContext.entity());
            memory.putTypeStorage(handler.get().getStacksHandler(slotContext.identifier()).get().getRenders());
            memory.count[0] = slotContext.index();
        }
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(AttributeRegistry.LIGHTNING_SPELL_POWER.get(), new AttributeModifier(uuid, "arc_flash_ring", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent(100)
    @SuppressWarnings("ConstantConditions")
    public final void onAttack(PlayerAttackListener listener) {
        double chance = 0.05f + listener.player.getAttributeValue(Attributes.LUCK);
        var memory = getMemory(listener.player);
        if (listener.player.tickCount == memory.count[1]) {
            //First hit without decay
            if (++memory.count[2] != 1) {
                chance *= Math.pow(0.93, memory.count[2]);
                if ((chance -= 0.01f) < 0) return;
            }
        } else {
            memory.count[1] = listener.player.tickCount;
            memory.count[2] = 0;
        }

        if (chance >= 1f || chance >= listener.player.getRandom().nextFloat()) {
            boolean turnOffRendering = memory.getTypeStorage(NonNullList.class).get((int) memory.count[0]) == Boolean.FALSE;
            ColorfulLightningBolt bolt = CalamityCurios.getEntityType(ColorfulLightningBolt.class).create(listener.player.level());
            bolt.setVisualOnly(true);
            bolt.setColor(turnOffRendering ? 0x0E00FFFF : 0x4B00FFFF);
            float amplifier = 3f;
            if (listener.isCalamityCriticalHits) amplifier += listener.amplifier;
            bolt.setDamage(listener.baseAmount * amplifier);
            bolt.setSilent(turnOffRendering);
            bolt.setPos(listener.entity.position());
            listener.player.level().addFreshEntity(bolt);
            listener.entity.thunderHit(listener.player.serverLevel(), bolt);
        }
    }

    @Override
    public Class<?>[] defineStorageType() {
        return new Class[] {NonNullList.class};
    }

    @Override
    public int getCountSize() {
        return 3;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "arc_flash_ring", 1, 2, 3, 4);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("arc_flash_ring", 5).withStyle(ChatFormatting.AQUA));
        return tooltips;
    }
}
