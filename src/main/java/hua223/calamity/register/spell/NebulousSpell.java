package hua223.calamity.register.spell;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.items.CalamityItems;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.MagicHelper;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

public class NebulousSpell extends AbstractSpell {
    public NebulousSpell() {
        baseSpellPower = 22;
        baseManaCost = 20;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return CalamityCurios.ModResource("nebulous");
    }

    @Override
    public int getManaCost(int level) {
        return super.getManaCost(level);
    }

    @Override
    public boolean attemptInitiateCast(ItemStack stack, int spellLevel, Level level, Player player, CastSource castSource, boolean triggerCooldown, String slot) {
        if (!level.isClientSide && !player.isUsingItem() && stack.is(CalamityItems.NEBULOUS_CATACLYSM.get()) && slot.equals(SpellSelectionManager.MAINHAND)) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            MagicData playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
            if (!playerMagicData.isCasting()) {
                CastResult castResult = canBeCastedBy(spellLevel, castSource, playerMagicData, serverPlayer);
                if (castResult.message != null) serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(castResult.message));

                if (castResult.isSuccess() && !MinecraftForge.EVENT_BUS.post(new SpellPreCastEvent(player, getSpellId(), spellLevel, getSchoolType(), castSource))) {
                    MagicHelper.MAGIC_MANAGER.addCooldown(serverPlayer, this, castSource);
                    player.startUsingItem(InteractionHand.MAIN_HAND);
                    return true;
                }
            }

            Utils.serverSideCancelCast(serverPlayer);
        }

        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientPreCast(Level level, int spellLevel, LivingEntity entity, InteractionHand hand, @Nullable MagicData playerMagicData) {
    }

    @Override
    public void castSpell(Level world, int spellLevel, ServerPlayer serverPlayer, CastSource castSource, boolean triggerCooldown) {
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return new DefaultConfig().setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE).setCooldownSeconds(1).setMaxLevel(1).build();
    }

    @Override
    public CastType getCastType() {
        return CastType.NONE;
    }
}
