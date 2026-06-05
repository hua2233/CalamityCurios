package hua223.calamity.util;

import hua223.calamity.capability.Adrenaline;
import hua223.calamity.capability.CalamityCap;
import hua223.calamity.capability.Rage;
import hua223.calamity.events.EventTypes;
import hua223.calamity.events.MethodHandlerSorter;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.mixed.ICalamityHeartType;
import hua223.calamity.register.Items.edible.LifeFruit;
import hua223.calamity.register.Items.edible.ManaPotion;
import hua223.calamity.register.gui.SpellType;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

//Calamity Player Expand
public final class CalamityPlayer {
    //#Base
    private final ServerPlayer player;

    //#Capability
    public final MagicData data;
    public final Adrenaline adrenaline;
    public final Rage rage;
    public final CalamityCap calamityCap;

    //#Curios Data
    public boolean canSprintingHit;
    public float extraFlyTime;
    public float flySpeedAmplifier;
    public float flyTimeAmplifier = 1f;
    private float invisible = 1f;
    public boolean hasRadianceEffect;
    public int azureAbyssFlag;
    public boolean noAttackCooling;
    public boolean fireImmune;
    public boolean cardDeck;
    private byte magicItemLeveL;
    public boolean automaticUsePotion;
    private SpellType.SpellData spellData;
    ObjectOpenHashSet<GlobalCuriosStorage.CuriosMemory> curiosStorage;
    private Map<EventTypes<?>, List<MethodHandlerSorter>> curioEvents;

    //#Client
    @OnlyIn(Dist.CLIENT)
    public ICalamityHeartType ASTR;
    @OnlyIn(Dist.CLIENT)
    private ICalamityHeartType renderHeart;
    @OnlyIn(Dist.CLIENT)
    public int astrAmount;
    @OnlyIn(Dist.CLIENT)
    public boolean freeze;
    @OnlyIn(Dist.CLIENT)
    public boolean sneakingSpeedBonus;
    @OnlyIn(Dist.CLIENT)
    public boolean fluidStand;
    @OnlyIn(Dist.CLIENT)
    public float jumpPower = 1f;

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
        magicItemLeveL = tag.getByte("MagicItemLeveL");
        adrenaline.save(tag);
        rage.save(tag);
        calamityCap.save(tag);
    }

    public void load(CompoundTag tag) {
        tag.putByte("MagicItemLeveL", magicItemLeveL);
        adrenaline.load(tag);
        rage.load(tag);
        calamityCap.load(tag);
    }

    public void onClone(Player old, boolean isDeath) {
        if (player == null) {
            renderHeart = old.Calamity$Player.renderHeart;
        } else {
            adrenaline.onClone(old, isDeath);
            rage.onClone(old, isDeath);
            calamityCap.onClone(old, isDeath);

            if (isDeath) {
                var events = old.Calamity$Player.curioEvents;
                if (events != null)
                    for (Map.Entry<EventTypes<?>, List<MethodHandlerSorter>> entry : events.entrySet())
                        entry.getKey().removeBatch(entry.getValue());
                addPlayerStorage();
                VariableAttributeModifier.readOldValuesOfDeath(player, old);
            }
        }
    }

    public void logInServerInitialization() {
        calamityCap.syncData();
        adrenaline.syncData();
        rage.syncData();
        LifeFruit.setTexture(player);
        addPlayerStorage();
    }


    private void addPlayerStorage() {
        //The Curios author defaults to not processing the first frame, possibly to prevent excessive noise, so manual registration is required here
        //If FirstTick is set to true when a player instance is created, it should be manually reset, such low frequency events are acceptable
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getEquippedCurios().getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ICuriosStorage storage)
                    storage.addToStorage(player);
            }
        });
    }

    public SpellType.SpellData getSpellData() {
        return spellData == null ? spellData = new SpellType.SpellData() : spellData;
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
    public boolean usePotionMana(float consume, boolean sync) {
        if (automaticUsePotion && consume <= player.getAttributeValue(AttributeRegistry.MAX_MANA.get())) {
            ItemStack[] manaPotions = player.getInventory().items.stream().filter(item -> item.getItem() instanceof ManaPotion)
                .sorted(Comparator.comparing(stack -> (ManaPotion) stack.getItem())).toArray(ItemStack[]::new);

            for (ItemStack stack : manaPotions) {
                ManaPotion potion = (ManaPotion) stack.getItem();
                for (int i = 0; i < stack.getCount(); i++) {
                    potion.apply(false, player);
                    stack.shrink(1);
                    if (data.getMana() >= consume) {
                        if (sync) PacketDistributor.sendToPlayer(player, new SyncManaPacket(data));
                        return true;
                    }
                }
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
        } else if ((magicItemLeveL & 1 << level) == 0) {
            magicItemLeveL = (byte) (magicItemLeveL | 1 << level);
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
        if (data.getMana() >= mana || usePotionMana(mana, false)) {
            data.setMana(data.getMana() - mana);
            if (player != null)
                PacketDistributor.sendToPlayer(player, new SyncManaPacket(data));
            return true;
        }

        return false;
    }

    //#Base
    public void changeMana(float mana, boolean sync) {
        data.addMana(mana);
        if (sync) PacketDistributor.sendToPlayer(player, new SyncManaPacket(data));
    }

    public void canBeSeen(LivingEvent.LivingVisibilityEvent event) {
        if (invisible < 1f) event.modifyVisibility(invisible);
    }

    public void changeInvisible(float value) {
        invisible = Mth.clamp(invisible + value, 0f, 1f);
    }

    //HEART
    @OnlyIn(Dist.CLIENT)
    public void setAstrHeart(int value) {
        astrAmount = value;
        if (value > 0 && ASTR == null)
            ASTR = createNewHeartType(70);
        else if (value <= 0) ASTR = null;
    }

    @OnlyIn(Dist.CLIENT)
    public void setFromLifeFruitLevel(int level) {
        renderHeart = createNewHeartType(106 + level * 36);
    }

    @OnlyIn(Dist.CLIENT)
    public ICalamityHeartType forPlayer(Player player) {
        Object type;
        if (player.hasEffect(MobEffects.POISON))  type = Gui.HeartType.POISIONED;
        else if (player.hasEffect(MobEffects.WITHER)) type = Gui.HeartType.WITHERED;
        else if (player.isFullyFrozen()) type = Gui.HeartType.FROZEN;
        else if (renderHeart != null) return renderHeart;
        else type = Gui.HeartType.NORMAL;

        return (ICalamityHeartType) type;
    }

    @OnlyIn(Dist.CLIENT)
    private static ICalamityHeartType createNewHeartType(int baseX) {
        return new ICalamityHeartType() {
            @Override
            public int calamity$GetX(boolean halfHeart, boolean renderHighlight) {
                int x = baseX;
                if (halfHeart) x += 9;
                if (renderHighlight) x += 18;
                return x ;
            }

            @Override
            public int calamity$GetY(int y) {
                return 18;
            }
        };
    }
}
