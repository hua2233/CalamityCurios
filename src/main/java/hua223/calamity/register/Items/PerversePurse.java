package hua223.calamity.register.Items;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class PerversePurse extends Item {
    public PerversePurse(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isCrouching()) {
            ItemStack stack = context.getItemInHand();
            Level level = context.getLevel();
            if (!level.isClientSide) {
                CompoundTag stackTag = stack.getOrCreateTag();
                System.out.println(stackTag);
                if (stackTag.contains("LastSave")) {
                    int id = stackTag.getInt("LastSave");

                    String before2 = String.valueOf(id - 2);
                    Optional<Entity> optional;
                    String key;
                    if (stackTag.contains(before2)) {
                        optional = EntityType.create(stackTag.getCompound(before2), level);
                        key = before2;
                    } else {
                        String before1 = String.valueOf(id - 1);
                        if (stackTag.contains(before1)) {
                            optional = EntityType.create(stackTag.getCompound(before1), level);
                            key = before1;
                        } else {
                            String last = String.valueOf(id);
                            optional = EntityType.create(stackTag.getCompound(last), level);
                            key = last;
                        }
                    }

                    if (optional.isPresent()) {
                        stackTag.remove(key);
                        Entity entity = optional.get();
                        BlockPos pos = context.getClickedPos();
                        entity.setPos(pos.getX(), pos.getY() + entity.getBbHeight(), pos.getZ());
                        level.addFreshEntity(entity);
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!player.level.isClientSide && player.isCrouching() && interactionTarget.isPickable()) {
            EntityType<?> type = interactionTarget.getType();
            if (!type.is(Tags.EntityTypes.BOSSES) && type != EntityType.PLAYER) {
                CompoundTag stackTag = stack.getOrCreateTag();

                if (!stackTag.contains("LastSave")) {
                    CompoundTag tag = new CompoundTag();
                    interactionTarget.save(tag);
                    interactionTarget.discard();
                    stackTag.put("0", tag);
                    stackTag.putInt("LastSave", 0);
                    player.setItemInHand(usedHand, stack);
                } else {
                    int id = stackTag.getInt("LastSave");
                    if (id < 3 || !stackTag.contains(String.valueOf(id - 1))
                        || !stackTag.contains(String.valueOf(id - 2))) {
                        CompoundTag tag = new CompoundTag();
                        interactionTarget.save(tag);
                        interactionTarget.discard();
                        stackTag.put(String.valueOf(++id), tag);
                        stackTag.putInt("LastSave", id);
                        player.setItemInHand(usedHand, stack);
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag advanced) {
        Style yellow = Style.EMPTY.withColor(ChatFormatting.YELLOW);
        tooltips.add(CMLangUtil.getTranslatable("perverse_purse", 1).setStyle(yellow));
        tooltips.add(CMLangUtil.getTranslatable("perverse_purse", 2).setStyle(yellow));
        tooltips.add(CMLangUtil.getTranslatable("perverse_purse", 3).setStyle(yellow));
        tooltips.add(CMLangUtil.blankLine());
        Style pink = Style.EMPTY.withColor(0xF38BBC);
        tooltips.add(CMLangUtil.getTranslatable("perverse_purse", 4).setStyle(pink));
        tooltips.add(CMLangUtil.getTranslatable("perverse_purse", 5).setStyle(pink));
    }
}
