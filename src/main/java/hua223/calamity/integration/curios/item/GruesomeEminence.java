package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class GruesomeEminence extends BaseCurio implements IKeyDataPackResponse {
    public GruesomeEminence(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, false);
    }

    @Override
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        player.getCooldowns().addCooldown(this, 1200);
        player.addEffect(new MobEffectInstance(CalamityEffects.GRUESOME_EVIL_SPIRITS.get(), 300));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getKeyCode() {
        return GLFW.GLFW_KEY_G;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean accept(Minecraft minecraft) {
        return notInCooling(minecraft);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "gruesome_eminence", 3, 4);
        tooltips.add(CMLangUtil.blankLine());
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.DARK_RED, "gruesome_eminence", 1, 2);
        return tooltips;
    }
}
