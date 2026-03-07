package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.entity.BlackHolePet;
import hua223.calamity.register.entity.StarPet;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;

//Easter egg item，I really like Solyn
public class StarCharm extends BaseCurio implements ICuriosStorage {
    public StarCharm(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        setPet(player, getUUID(player));
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        UUID[] uuids = getUUID(player);
        UUID id = uuids[0] == null ? uuids[1] : uuids[0];

        Entity entity = player.getLevel().getEntity(id);
        if (entity != null && entity.isAlive())
            entity.discard();
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        CuriosApi.getCuriosHelper().addSlotModifier(modifier, "charm", uuid, 2, AttributeModifier.Operation.ADDITION);
        modifier.put(Attributes.LUCK,
            new AttributeModifier(uuid, "star_charm", 10, AttributeModifier.Operation.ADDITION));
    }

    @Override
    protected void onPlayerTick(Player player) {
        setPet(player, getUUID(player));
    }

    public static void setPet(Player player, UUID[] uuids) {
        int id = -1;

        if (player.level.isDay()) {
            if (uuids[0] == null) {
                uuids[0] = BlackHolePet.create(player);
                id = 1;
            }
        } else if (uuids[1] == null) {
            uuids[1] = StarPet.create(player);
            id = 0;
        }

        if (id == -1) return;
        Entity entity = ((ServerPlayer) player).getLevel().getEntity(uuids[id]);
        if (entity != null) {
            entity.discard();
            uuids[id] = null;
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    public boolean storageCount() {
        return false;
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("star_charm", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("star_charm", 2).setStyle(Style.EMPTY.withColor(0xF38BBC)));
        return tooltips;
    }
}
