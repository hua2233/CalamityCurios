package hua223.calamity.register.Items;

import hua223.calamity.register.entity.projectiles.NebulaCloudCore;
import hua223.calamity.register.keys.ClientInteraction;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ILongPressAvailable;
import hua223.calamity.util.RenderUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.render.StaffArmPose;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class NebulousCataclysm extends StaffItem implements ILongPressAvailable {
    public NebulousCataclysm(Properties properties) {
        super(properties, 10, -2.8, Map.of(
            AttributeRegistry.ENDER_SPELL_POWER.get(), new AttributeModifier(UUID.fromString("50cd184c-6dc0-486d-b52b-bb73cb5cc410"),
                "NebulousCataclysm", 0.25, AttributeModifier.Operation.MULTIPLY_BASE),
            AttributeRegistry.SPELL_POWER.get(), new AttributeModifier(UUID.fromString("50cd184c-6dc0-486d-b52b-bb73cb5cc410"),
                "NebulousCataclysm", 0.1, AttributeModifier.Operation.MULTIPLY_BASE),
            AttributeRegistry.COOLDOWN_REDUCTION.get(), new AttributeModifier(UUID.fromString("50cd184c-6dc0-486d-b52b-bb73cb5cc410"),
                "NebulousCataclysm", 0.05, AttributeModifier.Operation.MULTIPLY_BASE)
        ));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return ClientInteraction.isLongPressActive() ? RenderUtil.HOLD_POSE : StaffArmPose.STAFF_ARM_POS;
            }
        });
    }

    @Override
    public void onServerResponse(ServerPlayer player, ItemStack stack) {
        NebulaCloudCore.create(player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(LocalPlayer player, ItemStack stack) {
        player.playSound(CalamitySounds.NEBULA.get());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isResponseTime(LocalPlayer player, int tick) {
        //Casting spells and releasing nebulae cannot be performed simultaneously
        return !player.isUsingItem() && tick % 32 == 10;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag advanced) {
        components.add(CMLangUtil.getTranslatable("nebulous_cataclysm").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
