package hua223.calamity.integration.curios;

import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.IDataPackResponse;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Wings extends BaseCurio implements ICuriosStorage, IDataPackResponse {
    protected final int flyTime;
    protected final float flySpeedAmplifier;
    protected final float verticalSpeed;

    public Wings(Properties properties, int time, float amplifier, float vertical) {
        super(properties);
        flySpeedAmplifier = amplifier;
        flyTime = time;
        verticalSpeed = vertical;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public final void onClientResponse(CompoundTag tag) {
        //Only used as a parent class to pass data packets for processing
        Minecraft.getInstance().player.calamity$WingsExpand[0] = tag.getFloat("speed");
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        getPack().putFloat("speed", verticalSpeed);
        sendToClient(player);
        if (player.isCreative() || player.isSpectator()) return;
        Abilities abilities = player.getAbilities();
        abilities.mayfly = true;
        player.onUpdateAbilities();
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        getPack().putFloat("speed", verticalSpeed);
        sendToClient(player);
        if (player.isCreative() || player.isSpectator()) return;
        Abilities abilities = player.getAbilities();
        abilities.mayfly = false;
        abilities.flying = false;
        player.onUpdateAbilities();
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (player.isCreative() || player.isSpectator()) return;
        Abilities abilities = player.getAbilities();
        float[] count = getCount(player);

        if (abilities.flying) {
            if (count[0]-- <= 0) {
                count[1] = 0;
                abilities.setFlyingSpeed(0.05f);
                abilities.mayfly = false;
                abilities.flying = false;
                player.onUpdateAbilities();
            }
        } else if (count[1] == 0 && player.onGround()) {
            count[1] = 1;
            count[0] = getFlyTime(player);
            abilities.setFlyingSpeed(0.05f * flySpeedAmplifier);
            abilities.mayfly = true;
            player.onUpdateAbilities();
        }
    }

    @Override
    public int getCountSize() {
        return 4;
    }

    protected int getFlyTime(Player player) {
        return (int) ((player.calamity$WingsExpand[1] + flyTime) * player.calamity$WingsExpand[2]);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.blankLine());
        Style style = Style.EMPTY.withColor(ChatFormatting.GOLD);
        tooltips.add(CMLangUtil.getDynamic("wings", 2, flyTime).setStyle(style));

        if (verticalSpeed > 1.6f)
            tooltips.add(CMLangUtil.getTranslatable("vertical", 3).withStyle(ChatFormatting.LIGHT_PURPLE));
        else if (verticalSpeed > 1.3f)
            tooltips.add(CMLangUtil.getTranslatable("vertical", 2).withStyle(ChatFormatting.AQUA));
        else tooltips.add(CMLangUtil.getTranslatable("vertical", 1).setStyle(style));

        tooltips.add(CMLangUtil.getDynamic("wings", 1,flySpeedAmplifier).setStyle(style));
        return tooltips;
    }
}