package hua223.calamity.register.items;

import com.google.common.collect.ImmutableMultimap;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.IEquipmentInspection;
import hua223.calamity.util.PlayerServantsManager;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

public class DemonShade extends ArmorItem implements IKeyDataPackResponse, IEquipmentInspection {
    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (!level.isClientSide && type == Type.LEGGINGS) {
            CompoundTag tag = player.getPersistentData();
            int stillTime = tag.getInt("Still");

            if (player.walkDist == player.walkDistO) {
                if (stillTime > 100 && stillTime % 20 == 0 && player.getHealth() < player.getMaxHealth())
                    player.heal(stillTime > 300 ? 34f : (float) (34 * (1 - Math.exp(-0.023 * (stillTime - 100)))));
                tag.putInt("Still", stillTime + 1);
            } else if (stillTime != 0) tag.putInt("Still",  0);
        }

        if (stack.popTime > 0) --stack.popTime;
    }

    public DemonShade(Type type) {
        super(RegisterList.DEMON_SHADE, type, RegisterList.ITEM_CALAMITY);

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(defaultModifiers);
        UUID uuid = ARMOR_MODIFIER_UUID_PER_TYPE.get(type);
        switch (type) {
            case HELMET -> {
                builder.put(CalamityAttributes.DAMAGE_UP.get(),
                    new AttributeModifier(uuid, "DemonShade", 0.3, AttributeModifier.Operation.MULTIPLY_BASE));
                builder.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
                    new AttributeModifier(uuid, "DemonShade", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
            }
            case CHESTPLATE -> {
                registerResponseKeyMapping();
                builder.put(CalamityAttributes.DAMAGE_UP.get(),
                    new AttributeModifier(uuid, "DemonShade", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
                builder.put(CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(),
                    new AttributeModifier(uuid, "DemonShade", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
                builder.put(Attributes.ATTACK_SPEED,
                    new AttributeModifier(uuid, "DemonShade", 0.25, AttributeModifier.Operation.MULTIPLY_BASE));
            }
            case LEGGINGS -> {
                builder.put(AttributeRegistry.MAX_MANA.get(),
                    new AttributeModifier(uuid, "DemonShade", 400, AttributeModifier.Operation.ADDITION));
                builder.put(CalamityAttributes.AMMUNITION_ADD.get(),
                    new AttributeModifier(uuid, "DemonShade", 0.3, AttributeModifier.Operation.MULTIPLY_BASE));
            }
            case BOOTS -> {
                builder.put(AttributeRegistry.SPELL_POWER.get(),
                    new AttributeModifier(uuid, "DemonShade", 1.5, AttributeModifier.Operation.MULTIPLY_BASE));
                builder.put(Attributes.MOVEMENT_SPEED,
                    new AttributeModifier(uuid, "DemonShade", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        defaultModifiers = builder.build();
    }

    @Override
    public void onEquip(Player player) {//FEET
        if (type == Type.HELMET) PlayerServantsManager.
            loadPlayerServantsEntity((ServerPlayer) player, DemonShade::modifyServantAttribute);
        else if (type == Type.LEGGINGS) player.getPersistentData().putInt("Still", 0);

        for (int i = 0; i < 4; i++) {
            Item item = player.getInventory().armor.get(i).getItem();
            if (!(item instanceof DemonShade)) return;
        }

        setKeyMapping((ServerPlayer) player, true);
    }

    @Override
    public void onUnEquip(Player player) {
        if (type == Type.HELMET) PlayerServantsManager.
            removePlayerServantsEntity((ServerPlayer) player, DemonShade::modifyServantAttribute);
        else if (type == Type.LEGGINGS) player.getPersistentData().remove("Still");
        setKeyMapping((ServerPlayer) player, false);
    }

    @Override
    public boolean isEffectiveSlot(EquipmentSlot slot) {
        return slot.isArmor();
    }

    private static void modifyServantAttribute(LivingEntity servant) {
        PlayerServantsManager.changeAttribute(servant, Attributes.MAX_HEALTH, 2, AttributeModifier.Operation.MULTIPLY_TOTAL);
        PlayerServantsManager.changeAttribute(servant, Attributes.ATTACK_DAMAGE, 10, AttributeModifier.Operation.ADDITION);
        PlayerServantsManager.changeAttribute(servant, Attributes.ATTACK_DAMAGE, 0.3, AttributeModifier.Operation.MULTIPLY_BASE);
        PlayerServantsManager.changeAttribute(servant, Attributes.ARMOR, 20, AttributeModifier.Operation.ADDITION);
        PlayerServantsManager.changeAttribute(servant, Attributes.ARMOR_TOUGHNESS, 8, AttributeModifier.Operation.ADDITION);
        PlayerServantsManager.changeAttribute(servant, Attributes.MOVEMENT_SPEED, 0.3, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        List<LivingEntity> entities = player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(15));
        if (entities.isEmpty()) return;

        MobEffect effect = CalamityEffects.ENRAGE.get();
        for (LivingEntity entity : entities)
            if (entity.isAlive() && !entity.isAlliedTo(player))
                entity.calamity$ForciblyAddEffect(new MobEffectInstance(effect, 200, entity == player ? 0 : 1), player);
        player.getCooldowns().addCooldown(this, 300);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "calamity_curios:textures/models/armor/demon_shade_layer_" + (slot == EquipmentSlot.LEGS ? "2.png" : "1.png");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getKeyCode() {
        return GLFW.GLFW_KEY_B;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public boolean accept(Minecraft minecraft) {
        if (notInCooling(minecraft)) {
            CalamitySounds.DEMON_SHADE_ENRAGE.playLocalSound();
            return true;
        }

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("demon_shade." + type.getName()).withStyle(ChatFormatting.DARK_RED));
    }
}
