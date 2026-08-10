package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.CriticalHitTriggerListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class VeneratedLocket extends BaseCurio implements ICuriosStorage {
    public VeneratedLocket(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        getMemory(player).putTypeStorage(new ArrayList<UUID>());
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        removeProjectile(getMemory(player), player, true);
    }

    @ApplyEvent
    @SuppressWarnings("unchecked")
    public final void onAttack(PlayerAttackListener listener) {
        if (listener.isFarAttack()) {
            ServerLevel level = listener.player.serverLevel();
            Projectile projectile = (Projectile) listener.getProjectile().getType().create(level);
            if (projectile != null) {
                RandomSource source = listener.entity.getRandom();
                Vec3 endPos = listener.entity.getEyePosition();
                Vec3 spawnPos = endPos.add( source.nextDouble() * 4 - 2,
                    source.nextInt(4, 7), source.nextDouble() * 4 - 2);
                projectile.setPos(spawnPos);
                spawnPos = endPos.subtract(spawnPos);
                projectile.shoot(spawnPos.x, spawnPos.y, spawnPos.z, 1f, 0.9f + source.nextFloat() * 0.2f);
                listener.entity.invulnerableTime = 0;
                level.addFreshEntity(projectile);
                getMemory(listener.player).getTypeStorage(ArrayList.class).add(projectile.getUUID());
            }
        } else CalamityHelp.addIfDoesNotExist(listener.entity, 60, 0, CalamityEffects.GOD_SLAYER_INFERNO.get());
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitTriggerListener listener) {
        listener.applyAmplifier(0.1f);
    }

    @Override
    protected void onPlayerTick(Player player) {
        GlobalCuriosStorage.CuriosMemory memory = getMemory(player);
        if (memory.count[0]++ == 60) {
            memory.count[0] = 0;
           removeProjectile(memory, player, false);
        }
    }

    @SuppressWarnings("unchecked")
    private static void removeProjectile(GlobalCuriosStorage.CuriosMemory memory, Player player, boolean erase) {
        ArrayList<UUID> list = memory.getTypeStorage(ArrayList.class);
        if (!list.isEmpty()) {
            ServerLevel level = (ServerLevel) player.level();
            Iterator<UUID> iterator = list.iterator();
            while (iterator.hasNext()) {
                Entity entity = level.getEntity(iterator.next());
                if (entity != null && entity.isAlive() &&
                    (erase || entity.tickCount > 60)) {
                    entity.discard();
                    iterator.remove();
                }
            }
        }
    }

    @ApplyGlobalLoot
    @SuppressWarnings("ConstantConditions")
    public void onGlobalEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.WANDERING_TRADER) && context.chance(0.1f) &&
            context.player.getServer().getLevel(Level.END).getDragonFight().hasPreviouslyKilledDragon())
            context.addLoot(this, 1);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    public Class<?>[] defineStorageType() {
        return new Class[] {ArrayList.class};
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.LIGHT_PURPLE, "venerated_locket", 1, 2, 3);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("venerated_locket", 4).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
