package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.DeathListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = Community.class, isRoot = true)
public class Community extends BaseCurio implements ICuriosStorage {

    public Community(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getName(ItemStack stack) {
        return RenderUtil.getRainbow(Component.translatable(getDescriptionId(stack)));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("heal"))
            getCount(player)[1] = tag.getFloat("heal");
        else init(tag);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        syncHealth(player);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        init(stack.getOrCreateTag());
        return stack;
    }

    private static void init(CompoundTag tag) {
        tag.putFloat("damage", 0.04f);
        tag.putFloat("critical", 0.02f);
        tag.putFloat("armor", 4f);
        tag.putFloat("offset", 0.02f);
        tag.putFloat("health", 0.04f);
        tag.putFloat("speed", 0.04f);
        tag.putFloat("heal", 1f);
        tag.putFloat("flyTime", 0.2f);
        tag.putInt("upCount", 0);
    }

    @Override
    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        if (allowedIn(category))
            items.add(getDefaultInstance());
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;

        modifier.put(CalamityAttributes.DAMAGE_UP.get(), new VariableAttributeModifier(
            uuid, "community", tag.getFloat("damage"), AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(), new VariableAttributeModifier(
            uuid, "community", tag.getFloat("critical"), AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ARMOR, new VariableAttributeModifier(
            uuid, "community", tag.getFloat("armor"), AttributeModifier.Operation.ADDITION));

        modifier.put(CalamityAttributes.INJURY_OFFSET.get(), new VariableAttributeModifier(
            uuid, "community", tag.getFloat("offset"), AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.MAX_HEALTH, new VariableAttributeModifier(
            uuid, "community", tag.getFloat("health"), AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.MOVEMENT_SPEED, new VariableAttributeModifier(
            uuid, "community", tag.getFloat("speed"), AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onKillBoss(DeathListener listener) {
        if (!listener.isPlayerDeath && listener.entity.getType().is(Tags.EntityTypes.BOSSES)) {
            ServerPlayer player = listener.player;
            CalamityHelp.safeUpdateItemInSlot(this, player, stack -> {
                CompoundTag tag = stack.getOrCreateTag();
                byte count = tag.getByte("upCount");
                if (count == 10) return;
                tag.putByte("upCount", ++count);

                float v = 0.04f + count * 0.05f;
                float v2 = 0.02f + count * 0.025f;
                tag.putFloat("damage", v);
                UUID uuid = getUUID(player)[0];
                VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.DAMAGE_UP.get()), uuid, v);

                tag.putFloat("critical", v2);
                VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get()), uuid, v2);

                tag.putFloat("armor", tag.getFloat("armor") + 2f);
                VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.ARMOR), uuid, v);

                tag.putFloat("offset", v2);
                VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.INJURY_OFFSET.get()), uuid, v2);

                tag.putFloat("health", v);
                VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.MAX_HEALTH), uuid, v);

                tag.putFloat("speed", v);
                VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.MOVEMENT_SPEED), uuid, v);

                float f = (getCount(player)[1] = 0.4f + count);
                tag.putFloat("heal", f);
            });
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (++count[0] == 40) {
            count[0] = 0;

            if (player.getHealth() < player.getMaxHealth())
                player.heal(count[1]);
        }
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
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        Style style = Style.EMPTY.withColor(16755200);
        if (Screen.hasShiftDown()) {
            CompoundTag tag = stack.getOrCreateTag();
            tooltips.add(CMLangUtil.getDynamic("community", 1,
                tag.getFloat("damage") * 100, tag.getFloat("critical") * 100).setStyle(style));

            tooltips.add(CMLangUtil.getDynamic("community", 2,
                tag.getFloat("health") * 100, tag.getFloat("offset") * 100, tag.getFloat("armor")).setStyle(style));

            tooltips.add(CMLangUtil.getDynamic("community", 3,
                tag.getFloat("speed") * 100, tag.getFloat("flyTime") * 100, tag.getFloat("heal")).setStyle(style));

            tooltips.add(CMLangUtil.blankLine());
            tooltips.add(CMLangUtil.getDynamic("community", 4, tag.getByte("upCount") * 10).setStyle(style));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("community", 8).setStyle(style));
            tooltips.add(CMLangUtil.getTranslatable("community", 9).setStyle(style));

            tooltips.add(CMLangUtil.blankLine());
            tooltips.add(CMLangUtil.getTranslatable("community", 5).setStyle(style));
            tooltips.add((RenderUtil.getRainbow(CMLangUtil.getTranslatable("community", 6))));
            tooltips.add(RenderUtil.getRainbow(CMLangUtil.getTranslatable("community", 7)));

            tooltips.add(CMLangUtil.blankLine());
            tooltips.add(CMLangUtil.getView().withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        return tooltips;
    }
}

