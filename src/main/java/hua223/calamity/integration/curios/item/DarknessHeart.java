package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.config.AutoConfig;
import hua223.calamity.register.config.CalamityConfig;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

import java.util.List;

@JeiInfo(zh_cn = "击杀任意Boss时极小概率掉落")
public class DarknessHeart extends BaseCurio implements ICuriosStorage {

    @AutoConfig(template = CalamityConfig.ConfigTemplate.DROP)
    public static final float DROP_PROBABILITY = CalamityConfig.value(.01f);

    public DarknessHeart(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.rage.setAttenuation(false);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.rage.setAttenuation(true);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 20))
            player.Calamity$Player.rage.addValue(2f);
    }

    @ApplyGlobalLoot
    public void onEntityLoot(EntitiesLootContext context) {
        if (context.entity.getType().is(Tags.EntityTypes.BOSSES) && context.chance(DROP_PROBABILITY))
            context.addLoot(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "darkness_heart", 2, 3);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("darkness_heart", 1).withStyle(ChatFormatting.DARK_RED));
        return tooltips;
    }
}
