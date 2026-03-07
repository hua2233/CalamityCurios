package hua223.calamity.integration.curios;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.PlayerVerticalSpeed;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Optional;

public class Wings extends BaseCurio implements ICuriosStorage {
    public static int extraFlyTime;
    public static float flyTimeAmplifier = 1f;

    private final int flyTime;
    private final float flySpeedAmplifier;
    private final int verticalSpeed;

    public Wings(Properties properties, int time, float amplifier, int vertical) {
        super(properties);
        flySpeedAmplifier = amplifier;
        flyTime = time;
        verticalSpeed = vertical;
    }

    public static void setFlyInfinite(ServerPlayer player) {
        Optional<IItemHandlerModifiable> optional = CuriosApi.getCuriosHelper().getEquippedCurios(player).resolve();

        if (optional.isPresent()) {
            IItemHandlerModifiable modifiable = optional.get();
            for (int i = 0; i < modifiable.getSlots(); i++) {
                Item stack = modifiable.getStackInSlot(i).getItem();
                if (stack instanceof Wings) {
                    float[] floats = GlobalCuriosStorage.getCountStorages(player, ((ICuriosStorage) stack).getClass());
                    floats[2] = 0;
                    DelayRunnable.addRunTask(160, () -> floats[2] = 1);
                }
            }
        }
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        NetMessages.sendToClient(new PlayerVerticalSpeed(verticalSpeed), player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        NetMessages.sendToClient(new PlayerVerticalSpeed(1f), player);
        if (player.isCreative() || player.isSpectator()) return;

        Abilities abilities = player.getAbilities();
        abilities.mayfly = false;
        abilities.flying = false;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (player.isCreative() || player.isSpectator()) return;
        Abilities abilities = player.getAbilities();
        float[] count = getCount(player);

        if (abilities.flying) {
            if (count[2] != 0 && count[0]-- <= 0) {
                count[1] = 0;
                abilities.setFlyingSpeed(0.05f);
                abilities.mayfly = false;
                abilities.flying = false;
                player.onUpdateAbilities();
            }
        } else {
            if (count[1] == 0 && player.isOnGround()) {
                count[1] = 1;
                count[0] = getFlyTime();
                abilities.setFlyingSpeed(0.05f * flySpeedAmplifier);
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    public int getCountSize() {
        return 3;
    }

    private int getFlyTime() {
        return (int) ((extraFlyTime + flyTime) * flyTimeAmplifier);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getDynamic("wings", 1, flyTime));
        tooltips.add(CMLangUtil.getDynamic("wings", 2, flySpeedAmplifier));
        tooltips.add(CMLangUtil.getTranslatable("wings", verticalSpeed + 2));
        return tooltips;
    }
}