package hua223.calamity.register.items;

import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.CalamityPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class MagicItem extends AvailableItem implements IDataPackResponse {
    private final byte level;

    public MagicItem(Properties properties, int level) {
        super(properties);
        this.level = (byte) level;
    }

    public static void syncMagicLevel(CalamityPlayer modPlayer) {
        byte level = modPlayer.getMagicItemLevel();
        if (level != 0) {
            MagicItem pack = (MagicItem) CalamityItems.PHANTOM_HEART.get();
            pack.getPack().putByte("level", level);
            pack.sendToClient(modPlayer.getPlayer());
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().setClientMagicItemLevel(tag.getByte("level"));
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (entity instanceof ServerPlayer player && player.Calamity$Player.tryUseMagicItem(this.level)) {
            stack.shrink(1);
            getPack().putByte("level", player.Calamity$Player.getMagicItemLevel());
            sendToClient(player);
        }

        return stack;
    }
}
