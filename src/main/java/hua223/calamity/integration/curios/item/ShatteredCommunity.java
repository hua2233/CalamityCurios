package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.Rage;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.render.hud.RageHud;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

@CurioRepel(Community.class)
public class ShatteredCommunity extends BaseCurio implements
    ICuriosStorage, IKeyDataPackResponse, IDataPackResponse {
    public ShatteredCommunity(Properties properties) {
        super(properties);
    }

    @ApplyEvent(2000)
    public final void onPostAttack(PostAttackListener listener) {
        Rage rage = listener.player.Calamity$Player.rage;
        rage.addValue(Math.min(3, listener.baseAmount / 7));
        if (rage.isActive()) {
            listener.amplifier += rage.getLevelBonus();
            rage.addLevelUpProgress((int) listener.getCorrectionValue());
        }
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, true);
        player.Calamity$Player.rage.setEnabled(true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, false);
        player.Calamity$Player.rage.setEnabled(false);
        syncHealth(player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull Component getName(@NotNull ItemStack stack) {
        MutableComponent component = Component.translatable(getDescriptionId(stack));
        float tick = (RenderUtil.getLocalTick() + Minecraft.getInstance().getPartialTick()) % 80;
        float progress = Mth.sin(tick / 80f * Mth.PI);
        component.setStyle(Style.EMPTY.withColor(FastColor.ARGB32.lerp(progress, 0xFF803E80, 0xFFF569F5)));
        return component;
    }

    @Override
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        player.Calamity$Player.rage.activeRage();
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 200)) {
            float maxHealth = player.getMaxHealth();
            if (player.getHealth() < maxHealth)
                player.heal(maxHealth * 0.15f);
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
            new AttributeModifier(uuid, "shattered", 0.05, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid, "shattered", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));

        modifier.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "shattered", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));

        modifier.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "shattered", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));

        modifier.put(Attributes.ARMOR, new AttributeModifier(
            uuid, "shattered", (10 + equipped.getAttributeValue(Attributes.ARMOR) * 0.2), AttributeModifier.Operation.ADDITION));

        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new AttributeModifier(uuid, "shattered", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_V;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        byte stateInfo = tag.getByte("bit");
        for (int i = 0; i < 6; i++)
            if (((stateInfo >> i) & 1) == 1) byBit(i, tag);
    }

    @OnlyIn(Dist.CLIENT)
    private static void byBit(int bit, CompoundTag tag) {
        switch (bit) {
            case 0 -> RageHud.INSTANCE.setEnabled(tag.getBoolean("state"));
            case 1 -> RageHud.INSTANCE.setDisplayCount(tag.getByte("count"));
            case 2 -> {
                float v = tag.getFloat("value");
                if (v < 0) {
                    RageHud.INSTANCE.playAnimation();
                    v = -v;
                }
                RageHud.INSTANCE.setProgress(v);
            }

            case 3 -> RageHud.INSTANCE.setCurrentDamage(tag.getInt("damage"));

            case 4 -> RageHud.INSTANCE.setShatteredLevel(tag.getByte("level"), tag.getInt("upDamage"));

            case 5 -> RageHud.INSTANCE.active();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.YELLOW, "shattered", 2, 3, 4, 5);
        Style style = Style.EMPTY.withColor(ChatFormatting.GOLD);
        RageHud rage = RageHud.INSTANCE;

        tooltips.add(CMLangUtil.getDynamic("shattered", 6,
            rage.getShatteredLevel()).setStyle(style));
        tooltips.add(CMLangUtil.getDynamic("shattered", 7,
            rage.getUpProgress()).setStyle(style));
        tooltips.add(CMLangUtil.getDynamic("shattered", 8,
            rage.getLevelBonus()).setStyle(style));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("shattered", 1)
            .withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}