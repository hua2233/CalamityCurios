package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.effects.Bounding;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.IDataPackResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(value = SprintCurio.class)
public class StatisNinjaBelt extends SprintCurio implements IDataPackResponse {
    @OnlyIn(Dist.CLIENT)
    public static boolean autoJump;
    public final boolean immuneFall;

    public StatisNinjaBelt(Properties properties, boolean immuneFall) {
        super(properties);
        this.immuneFall = immuneFall;
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving && CalamityHelp.isCanDodge(
            listener.player, listener.baseAmount, 2, (int) Mth.clamp(listener.baseAmount * 40, 300, 1800))) {
            listener.canceledEvent();
        } else if (listener.source.is(DamageTypeTags.IS_FALL)) {
            if (immuneFall) {
                listener.canceledEvent();
            } else {
                float d = listener.baseAmount / 2 + 5;
                if (d >= listener.baseAmount) listener.canceledEvent();
                else listener.floating -= d;
            }
        }
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        super.equipHandle(player, stack);
        getPack().putBoolean("statis_ninja_belt", true);
        sendToClient(player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        super.unEquipHandle(player, stack);
        getPack().putBoolean("statis_ninja_belt", false);
        sendToClient(player);
    }

    @Override
    public int getTime() {
        return 6;
    }

    @Override
    public double getSpeed() {
        return 1.3;
    }

    @Override
    public int getCooldownTime() {
        return 240;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void onClientResponse(CompoundTag tag) {
        if (tag.contains("statis_ninja_belt")) {
            Minecraft minecraft = Minecraft.getInstance();
            boolean isApply = tag.getBoolean("statis_ninja_belt");
            Bounding.jumpPower += isApply ? 0.32f : -0.32f;
            OptionInstance<Boolean> option = minecraft.options.autoJump();
            if (isApply) {
                autoJump = option.get();
                option.set(true);
            } else option.set(autoJump);
            minecraft.player.calamity$CanClimbable(isApply);
        } else super.onClientResponse(tag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, stack.getRarity().color, "statis_ninja_belt", 1,
            immuneFall ? 6 : 2, 3, 4, 5);
        return tooltips;
    }
}
