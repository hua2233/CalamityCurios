package hua223.calamity.util;

import hua223.calamity.capability.Adrenaline;
import hua223.calamity.capability.CalamityCap;
import hua223.calamity.capability.Rage;
import hua223.calamity.events.EventTypes;
import hua223.calamity.events.MethodHandlerSorter;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.items.MagicItem;
import hua223.calamity.register.items.SupremeManaPotion;
import hua223.calamity.register.rune.IRuneReinforcement;
import hua223.calamity.register.items.edible.LifeFruit;
import hua223.calamity.render.IPlayerPostRenderer;
import hua223.calamity.util.delaytask.DelayRunnable;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.*;

//Calamity Player Expand
public final class CalamityPlayer {
    //#Base
    private final ServerPlayer player;

    //#Capability
    public final Rage rage;
    public final MagicData data;
    public final Adrenaline adrenaline;
    public final CalamityCap calamityCap;

    //#Curios Data
    public boolean canSprintingHit;
    public boolean blessingGiver;
    public boolean fungalSymbiosis;
    public float extraFlyTime;
    public float flySpeedAmplifier;
    public float flyTimeAmplifier = 1f;
    private float invisible = 1f;
    public boolean hasRadianceEffect;
    public int azureAbyssFlag;
    public boolean noAttackCooling;
    public boolean fireImmune;
    public boolean cardDeck;
    private byte magicItemLevel;
    public boolean automaticUsePotion;

    ObjectOpenHashSet<GlobalCuriosStorage.CuriosMemory> curiosStorage;
    private Map<EventTypes<?>, List<MethodHandlerSorter>> curioEvents;
    private Object2ObjectOpenHashMap<IRuneReinforcement, Object> runeDatas;

    //#Client
    @OnlyIn(Dist.CLIENT)
    public float sneakingSpeedBonus;
    @OnlyIn(Dist.CLIENT)
    public boolean fluidStand;
    @OnlyIn(Dist.CLIENT)
    public float jumpPower = 1f;
    @OnlyIn(Dist.CLIENT)
    public boolean canClimbable;

    //#Tracking data
    @OnlyIn(Dist.CLIENT)
    public boolean freeze;
    @OnlyIn(Dist.CLIENT)
    public boolean crescent;
    @OnlyIn(Dist.CLIENT)
    public byte nihilityShell;
    @OnlyIn(Dist.CLIENT)
    public float shieldDurability;
    @OnlyIn(Dist.CLIENT)
    public float shieldDurabilityMax;
    @OnlyIn(Dist.CLIENT)
    public boolean disableLegRendering;

    //#Tracking Renderer Sync
    @OnlyIn(Dist.CLIENT)
    private IPlayerPostRenderer renderer;

    public CalamityPlayer(Player player) {
        if (player.level().isClientSide) {
            this.player = null;
            data = null;
            adrenaline = null;
            rage = null;
            calamityCap = null;
        } else {
            this.player = (ServerPlayer) player;
            adrenaline = new Adrenaline(this.player);
            rage = new Rage(this.player);
            calamityCap = new CalamityCap(this.player);
            data = MagicData.getPlayerMagicData(player);
        }
    }

    public void save(CompoundTag tag) {
        tag.putByte("MagicItemLeveL", magicItemLevel);
        adrenaline.save(tag);
        rage.save(tag);
        calamityCap.save(tag);
    }

    public void load(CompoundTag tag) {
        magicItemLevel = tag.getByte("MagicItemLeveL");
        adrenaline.load(tag);
        rage.load(tag);
        calamityCap.load(tag);
    }

    public void onClone(Player old, boolean isDeath) {
        if (player == null) {
            magicItemLevel = old.Calamity$Player.magicItemLevel;
        } else {
            adrenaline.onClone(old, isDeath);
            rage.onClone(old, isDeath);
            calamityCap.onClone(old, isDeath);
            VariableAttributeModifier.readOldValuesOfDeath(player, old);
            if (isDeath) GlobalCuriosStorage.addPlayerStorage(player);
            else GlobalCuriosStorage.fromOldDataClone(player, old);

            final Map<EventTypes<?>, List<MethodHandlerSorter>> curioEvents = old.Calamity$Player.curioEvents;
            if (curioEvents != null) DelayRunnable.nextTickRun(() -> EventTypes.removeBatch(player));
        }
    }

    public void logInServerInitialization() {
        rage.syncData();
        adrenaline.syncData();
        calamityCap.syncData();
        LifeFruit.setTexture(player);
        MagicItem.syncMagicLevel(this);
        GlobalCuriosStorage.addPlayerStorage(player);
    }

    @SuppressWarnings("unchecked")
    public <T> T getRuneData(IRuneReinforcement rune) {
        if (runeDatas == null) runeDatas = new Object2ObjectOpenHashMap<>(4);

        Object o = runeDatas.get(rune);
        if (o == null) runeDatas.put(rune, o = rune.addData());
        return (T) o;
    }

    public void addEvent(EventTypes<?> eventType, MethodHandlerSorter sorter) {
        if (curioEvents == null)
            curioEvents = new Object2ObjectOpenHashMap<>(EventTypes.getEventTypeTotal());

        curioEvents.compute(eventType, (key, events) -> {
            if (events == null) events = new ArrayList<>(16);
            events.add(sorter);
            events.sort(MethodHandlerSorter::compareTo);
            return events;
        });
    }

    public void removeEvent(EventTypes<?> type, MethodHandlerSorter sorter) {
        if (curioEvents != null) {
            curioEvents.computeIfPresent(type, (t, events) -> {
                events.remove(sorter);
                return events.isEmpty() ? null : events;
            });

            if (curioEvents.isEmpty()) curioEvents = null;
        }
    }

    public Map<EventTypes<?>, List<MethodHandlerSorter>> getActiveEvents() {
        return curioEvents;
    }

    //#Magic Expand
    public boolean usePotionMana(float consume) {
        if (automaticUsePotion && consume <= player.getAttributeValue(AttributeRegistry.MAX_MANA.get())) {
            Tuple<ItemStack, Integer> tuple = SupremeManaPotion.queryManaPotion(player);

            if (tuple != null) {
                tuple.getA().shrink(1);
                MobEffectRegistry.INSTANT_MANA.get().applyInstantenousEffect(null, null, player, tuple.getB(), 0);
                return data.getMana() >= consume;
            }
        }

        return false;
    }

    @SuppressWarnings("ConstantConditions")
    public void tryUseEnchantedStarfish() {
        VariableAttributeModifier.createOrIncrease(player, AttributeRegistry.MAX_MANA.get(), null,
            "EnchantedStarfish", 20, 200, AttributeModifier.Operation.ADDITION);
    }

    public boolean tryUseMagicItem(int level) {
        if (level > 7) {
            CalamityCurios.LOGGER.warn("Invalid magic item bit index");
        } else if ((magicItemLevel & 1 << level) == 0) {
            magicItemLevel = (byte) (magicItemLevel | 1 << level);
            UUID uuid = UUID.nameUUIDFromBytes("MagicItem".getBytes());
            VariableAttributeModifier.createOrIncrease(player, AttributeRegistry.MAX_MANA.get(), uuid,
                "MagicItem", 75, 600, AttributeModifier.Operation.ADDITION);
            VariableAttributeModifier.createOrIncrease(player, AttributeRegistry.SPELL_POWER.get(), uuid,
                "MagicItem", 0.05, 0.4, AttributeModifier.Operation.MULTIPLY_BASE);
            return true;
        }

        return false;
    }

    public boolean consumeMana(float mana) {
        if (data.getMana() >= mana || usePotionMana(mana)) {
            data.setMana(data.getMana() - mana);
            if (player != null) PacketDistributor.sendToPlayer(player, new SyncManaPacket(data));
            return true;
        }

        return false;
    }

    public void changeMana(float mana, boolean sync) {
        data.addMana(mana);
        if (sync) PacketDistributor.sendToPlayer(player, new SyncManaPacket(data));
    }

    //#Base
    public void canBeSeen(LivingEvent.LivingVisibilityEvent event) {
        if (invisible < 1f) event.modifyVisibility(invisible);
    }

    public byte getMagicItemLevel() {
        return magicItemLevel;
    }

    public void changeInvisible(float value) {
        invisible = Mth.clamp(invisible + value, 0f, 1f);
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    @OnlyIn(Dist.CLIENT)
    public void setClientMagicItemLevel(byte level) {
        if (player == null) magicItemLevel = level;
    }

    @OnlyIn(Dist.CLIENT)
    public void stopPlayerPostRender() {
        if (renderer != null) {
            renderer.onStop();
            renderer = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void startPlayerPostRender(IPlayerPostRenderer renderer) {
        if (this.renderer != null) stopPlayerPostRender();
        this.renderer = renderer;
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unchecked")
    public <T extends IPlayerPostRenderer> T getRenderer() {
        return (T) renderer;
    }
}
