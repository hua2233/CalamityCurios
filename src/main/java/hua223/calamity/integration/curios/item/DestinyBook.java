package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.DeathListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.render.Item.DestinyBookRender;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

//I have tried my best to put high consumption manipulation at the end of the processing
public class DestinyBook extends BaseCurio implements ICuriosStorage {
    public DestinyBook(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!player.isLocalPlayer()) {
            ItemStack stack = player.getItemInHand(usedHand);
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                SpawnEggItem egg = fromNbtGetSaveEgg(tag);
                if (egg != null) {
                    player.getInventory().add(egg.getDefaultInstance());
                    tag.remove("egg");
                    tag.remove("DamageUp");
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.fail(player.getItemInHand(usedHand));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            Tuple<float[], UUID[]> storage = getPair(player);
            SpawnEggItem egg = fromNbtGetSaveEgg(tag);
            if (egg != null) storage.getB()[0] = UUID.nameUUIDFromBytes(egg.getType(null).toString().getBytes());
            if (tag.contains("DamageUp")) {
                storage.getA()[0] = tag.getFloat("DamageUp");
                tag.remove("DamageUp");
            }
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        float[] storage = getCount(player);
        if (storage[0] > 0) stack.getTag().putFloat("DamageUp", storage[0]);
        syncHealth(player);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid,
            "destiny", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        UUID captureType = getFirstUUID(listener.player);
        if (captureType != null && captureType.equals(UUID.nameUUIDFromBytes(
            listener.entity.getType().toString().getBytes())))
            listener.amplifier += getCount(listener.player)[0];
    }

    @ApplyEvent
    @SuppressWarnings("ConstantConditions")
    public final void onDeath(DeathListener listener) {
        if (!listener.isPlayerDeath && listener.player.getRandom().nextFloat() <= 0.02f) {
            EntityType<?> entityType = listener.entity.getType();
            Tuple<float[], UUID[]> storage = getPair(listener.player);
            UUID record = storage.getB()[0];
            UUID type = UUID.nameUUIDFromBytes(entityType.toString().getBytes());
            if (record == null) {
                Item spawnEggItem = ForgeSpawnEggItem.fromEntityType(entityType);
                if (spawnEggItem != null) {
                    storage.getB()[0] = type;
                    CalamityHelp.safeUpdateItemInSlot(this, listener.entity, stack ->
                        stack.getOrCreateTag().putString("egg", ForgeRegistries.ITEMS.getKey(spawnEggItem).toString()));
                }
            } else if (type.equals(record)) storage.getA()[0] += 0.005f;
        }
    }

    private static SpawnEggItem fromNbtGetSaveEgg(CompoundTag tag) {
        if (tag.contains("egg")) {
            Item item = ForgeRegistries.ITEMS.getValue(CalamityCurios.resource(tag.getString("egg")));
            if (item instanceof SpawnEggItem eggItem) return eggItem;
        }

        return null;
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new DestinyBookRender();
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public ItemStack getOverlayStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("egg") ? ForgeRegistries.ITEMS.getValue(
            CalamityCurios.resource(tag.getString("egg"))).getDefaultInstance() : null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("destiny", 2).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.getTranslatable("destiny", 3).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("destiny", 1).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
