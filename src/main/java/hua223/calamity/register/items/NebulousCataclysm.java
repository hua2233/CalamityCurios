package hua223.calamity.register.items;

import hua223.calamity.register.RegisterList;
import hua223.calamity.register.entity.projectiles.NebulaCloudCore;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.RenderUtil;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.render.StaffArmPose;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class NebulousCataclysm extends StaffItem implements IPresetSpellContainer {
    public NebulousCataclysm(Properties properties) {
        super(properties, new StaffTier(15, -3f, new AttributeContainer(
            AttributeRegistry.ENDER_SPELL_POWER, 0.55, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.SPELL_POWER, 0.1, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.COOLDOWN_REDUCTION, 0.08, AttributeModifier.Operation.MULTIPLY_BASE)));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        SpellSelectionManager spellSelectionManager = new SpellSelectionManager(player);
        SpellSelectionManager.SelectionOption selectionOption = spellSelectionManager.getSelection();
        if (selectionOption != null && !selectionOption.spellData.equals(SpellData.EMPTY)) {
            SpellData spellData = selectionOption.spellData;
            AbstractSpell spell = spellData.getSpell();
            int spellLevel = spell.getLevelFor(spellData.getLevel(), player);
            if (level.isClientSide()) {
                return ClientMagicData.isCasting() ? InteractionResultHolder.consume(itemStack) :
                    ClientMagicData.getPlayerMana() < spell.getManaCost(spellLevel) ||
                        ClientMagicData.getCooldowns().isOnCooldown(spell) || !ClientMagicData.getSyncedSpellData(player)
                        .isSpellLearned(spell) ? InteractionResultHolder.pass(itemStack) : InteractionResultHolder.consume(itemStack);
            } else {
                String castingSlot = hand.ordinal() == 0 ? SpellSelectionManager.MAINHAND : SpellSelectionManager.OFFHAND;
                return spell.attemptInitiateCast(itemStack, spellLevel, level, player, selectionOption.getCastSource(), true, castingSlot) ?
                    InteractionResultHolder.consume(itemStack) : InteractionResultHolder.fail(itemStack);
            }
        }

        return InteractionResultHolder.pass(itemStack);
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (!ISpellContainer.isSpellContainer(itemStack)) {
            ISpellContainerMutable spellContainer = ISpellContainer.create(1, true, false).mutableCopy();
            spellContainer.addSpell(RegisterList.NEBULOUS.get(), 1, false);
            ISpellContainer.set(itemStack, spellContainer.toImmutable());
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack itemStack) {
                return entity != null && entity.calamity$IsPlayer && entity.isUsingItem() ? RenderUtil.HOLD_POSE : StaffArmPose.STAFF_ARM_POS;
            }
        });
    }

    @Override
    public void onUseTick(Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int duration) {
        if (!level.isClientSide && duration % 32 == 10 && entity.calamity$IsPlayer) {
            if (entity.calamity$Player.Calamity$Player.consumeMana(RegisterList.NEBULOUS.get().getManaCost(1))) NebulaCloudCore.create(entity);
            else entity.stopUsingItem();
        }
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 7200;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag advanced) {
        components.add(CMLangUtil.blankLine());
        CMLangUtil.batchColorTexts(components, ChatFormatting.LIGHT_PURPLE, "nebulous_cataclysm", 1, 2);
    }
}
