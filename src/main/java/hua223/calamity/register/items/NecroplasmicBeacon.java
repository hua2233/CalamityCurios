package hua223.calamity.register.items;

import hua223.calamity.register.RegisterList;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class NecroplasmicBeacon extends Item {
    public NecroplasmicBeacon() {
        super(RegisterList.EPIC_ONE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide) {
            player.startUsingItem(usedHand);
            level.playSound(null, player.getOnPos(), CalamitySounds.NECROPLASMIC_BEACON.get(), SoundSource.PLAYERS);
            player.getCooldowns().addCooldown(this, 80);
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }



    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity.calamity$IsPlayer) {
            WitherBoss wither = EntityType.WITHER.create(level);
            if (wither != null) {
                Vec3 position = entity.getForward().normalize().scale(2).add(entity.position());
                wither.moveTo(position.x + 0.5, position.y + 0.55, position.z + 0.5F, 90.0F, 0.0F);
                wither.lookAt(EntityAnchorArgument.Anchor.EYES, entity.getEyePosition());
                wither.makeInvulnerable();
                entity.calamity$Player.getCooldowns().addCooldown(this, 2400);
                level.addFreshEntity(wither);
            }
        }

        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 60;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltips, TooltipFlag advanced) {
        Style gold = Style.EMPTY.withColor(ChatFormatting.GOLD);
        tooltips.add(CMLangUtil.getTranslatable("necroplasmic_beacon", 1).setStyle(gold));
        tooltips.add(CMLangUtil.getTranslatable("necroplasmic_beacon", 2).setStyle(gold));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("necroplasmic_beacon", 3).withStyle(ChatFormatting.RED));
    }
}
