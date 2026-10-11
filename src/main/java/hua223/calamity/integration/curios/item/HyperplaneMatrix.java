package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.events.listeners.PreAttackListener;
import hua223.calamity.integration.curios.Wings;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.gui.HyperplaneMatrixOperatingInterface;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.item.IDraedonCreationBG;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.List;

//Developer's Gift
@CurioRepel(Wings.class)
public class HyperplaneMatrix extends Wings implements IDraedonCreationBG, IKeyDataPackResponse, ICuriosStorage {
    public HyperplaneMatrix() {
        super(RegisterList.CALAMITY_ONE);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        //default flight prohibition，fly by controller switch
        setKeyMapping(player, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        super.unEquipHandle(player, stack);
        setKeyMapping(player, false);
        if (!player.isCreative() && !player.isSpectator()) {
            Abilities abilities = player.getAbilities();
            if (abilities.invulnerable) {
                abilities.invulnerable = false;
                player.onUpdateAbilities();
            }
        }
    }

    public static void setFlag(ServerPlayer player, int absId, int flag) {
        HyperplaneMatrix matrix = (HyperplaneMatrix) CalamityItems.HYPERPLANE_MATRIX.get();
        GlobalCuriosStorage.CuriosMemory memory = GlobalCuriosStorage.getStorage(matrix, player);
        if (memory == null) return;

        boolean shouldRefreshAbilities = false;
        if (absId == 2) {
            boolean on = flag > 0;
            memory.count[4] = (on ? 1 : 0);
            Abilities abilities = player.getAbilities();
            float speed = on ? matrix.getFlySpeedAmplifier() : 0;
            player.Calamity$Player.flySpeedAmplifier = speed;
            matrix.getPack().putFloat("amplifier", speed);
            matrix.sendToClient(player);
            shouldRefreshAbilities = true;
            abilities.setFlyingSpeed(0.05f * (on ? matrix.getVerticalSpeed() : 1f));
            if (!player.isCreative() && !player.isSpectator()) abilities.mayfly = on;
        } else {
            int f = (absId == 6 ? 3 : 2);
            memory.count[f] = flag < 0 ? 0 : 1;
            //Immune death and injury
            if (f == 2 && !player.isCreative()) {
                player.getAbilities().invulnerable = memory.count[2] == 1;
                shouldRefreshAbilities = true;
            }
        }

        if (shouldRefreshAbilities) player.onUpdateAbilities();
    }

    @ApplyEvent
    public final void onDeath(DeathListener listener) {
        if (listener.isPlayerDeath && getCount(listener.player)[2] == 1)
            listener.canceledPlayerDeathIfNotCooldowns(this, .7f,
                0, 0x3E4147, 0x020408, 0x5A6664, 0x1B1C22);
    }

    @ApplyEvent
    public final void onAttack(PreAttackListener listener) {
        if (getCount(listener.player)[3] == 1) {
            listener.entity.setHealth(0);
            listener.entity.die(listener.source);
            listener.canceledEvent();
        }
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        //Prohibit charging
        if (count[4] == 0) count[1] = 0;
        super.onPlayerTick(player);
    }

    @Override
    protected boolean cancelFlight(Abilities abilities, float[] count) {
        return count[4] == 0 && super.cancelFlight(abilities, count);
    }

    @Override
    protected float getFlySpeedAmplifier() {
        return 1.05f;
    }

    @Override
    protected int getFlyTime() {
        return 0;
    }

    @Override
    protected float getVerticalSpeed() {
        return 1.05f;
    }

    @Override
    public int getCountSize() {
        return 5;
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_LEFT_ALT;
    }

    @Override
    public @NotNull ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(CalamitySounds.HYPERPLANE_MATRIX_OPEN.get(), 1f, 1f);
    }

    @Override
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        CompoundTag pack = getPack();
        float[] count = getCount(player);
        int[] hyperplaneMatrixOperatingSettings = new int[] {count[2] == 1 ? 1 : -1, count[4] == 1 ? 2 : -2, 3, 4, 5, count[3] == 1 ? 6 : -6};
        pack.putIntArray("i", hyperplaneMatrixOperatingSettings);
        sendToClient(player);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        if (tag.contains("i")) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen == null) minecraft.setScreen(new HyperplaneMatrixOperatingInterface(tag.getIntArray("i")));
        } super.onClientResponse(tag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean accept(Minecraft minecraft) {
        return minecraft.screen == null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("hyperplane_matrix", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("hyperplane_matrix", 2).withStyle(ChatFormatting.AQUA));
        return tooltips;
    }
}
