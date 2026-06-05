package hua223.calamity.integration.curios;

import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.IDataPackResponse;
import net.minecraft.ChatFormatting;
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

public abstract class Wings extends BaseCurio implements ICuriosStorage, IDataPackResponse  {
    public Wings(Properties properties) {
        super(properties);
    }

    protected abstract float getFlySpeedAmplifier();

    protected abstract int getFlyTime();

    protected abstract float getVerticalSpeed();

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        if (player.isCreative() || player.isSpectator()) return;
        Abilities abilities = player.getAbilities();
        player.Calamity$Player.flySpeedAmplifier = getFlySpeedAmplifier();
        getPack().putFloat("amplifier", getFlySpeedAmplifier());
        sendToClient(player);
        abilities.mayfly = true;
        abilities.setFlyingSpeed(0.05f * getVerticalSpeed());
        player.onUpdateAbilities();
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        if (player.isCreative() || player.isSpectator()) return;
        Abilities abilities = player.getAbilities();
        player.Calamity$Player.flySpeedAmplifier = 0;
        getPack().putFloat("amplifier", 0);
        sendToClient(player);
        abilities.mayfly = false;
        abilities.flying = false;
        abilities.setFlyingSpeed(0.05f);
        player.onUpdateAbilities();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().flySpeedAmplifier = tag.getFloat("amplifier");
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (player.isCreative() || player.isSpectator()) return;
        Abilities abilities = player.getAbilities();
        float[] count = getCount(player);

        if (player.onGround()) {
            float time = getFlyTime(player);
            if (count[0] != time) {
                count[0] = time;
                if (count[1] == 1) {
                    count[1] = 0;
                    abilities.mayfly = true;
                    abilities.setFlyingSpeed(0.05f * getVerticalSpeed());
                    player.onUpdateAbilities();
                }
            }
        } else if (cancelFlight(abilities, count)) {
            count[1] = 1;
            abilities.setFlyingSpeed(0.05f);
            abilities.mayfly = false;
            abilities.flying = false;
            player.onUpdateAbilities();
        }
    }

    protected boolean cancelFlight(Abilities abilities, float[] count) {
        return abilities.flying && count[0]-- < 1;
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    protected final int getFlyTime(Player player) {
        return (int) ((player.Calamity$Player.extraFlyTime + getFlyTime()) * player.Calamity$Player.flyTimeAmplifier);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.blankLine());
        Style style = Style.EMPTY.withColor(ChatFormatting.GOLD);
        tooltips.add(CMLangUtil.getDynamic("wings", 2, getFlyTime()).setStyle(style));

        if (getVerticalSpeed() > 4f)
            tooltips.add(CMLangUtil.getTranslatable("vertical", 3).withStyle(ChatFormatting.LIGHT_PURPLE));
        else if (getVerticalSpeed() > 2.5f)
            tooltips.add(CMLangUtil.getTranslatable("vertical", 2).withStyle(ChatFormatting.AQUA));
        else tooltips.add(CMLangUtil.getTranslatable("vertical", 1).setStyle(style));

        tooltips.add(CMLangUtil.getDynamic("wings", 1, getFlySpeedAmplifier()).setStyle(style));
        return tooltips;
    }
}