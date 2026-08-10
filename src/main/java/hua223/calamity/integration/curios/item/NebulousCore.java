package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.entity.projectiles.Nebula;
import hua223.calamity.register.particle.ColorfulTotemType;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import hua223.calamity.util.ICuriosStorage;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@CurioRepel(EclipseMirror.class)
public class NebulousCore extends BaseCurio implements ICuriosStorage, IDataPackResponse {
    public NebulousCore(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        getMemory(player).putTypeStorage(new ObjectOpenHashSet<UUID>());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        ObjectOpenHashSet<UUID> set = getMemory(player).getTypeStorage(ObjectOpenHashSet.class);
        if (!set.isEmpty()) {
            ServerLevel level = player.serverLevel();
            for (UUID uuid : set) {
                Entity entity = level.getEntity(uuid);
                if (entity != null && entity.isAlive()) entity.discard();
            }
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier("nebulous_core", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onPlayerTick(Player player) {
        var memory = getMemory(player);
        if (memory.count[0]++ >= 20) {
            memory.count[0] = 0;
            ObjectOpenHashSet<UUID> set = memory.getTypeStorage(ObjectOpenHashSet.class);
            ServerLevel level = (ServerLevel) player.level();

            set.removeIf(uuid -> {
                Entity entity = level.getEntity(uuid);
                return entity == null || !entity.isAlive();
            });

            if (set.size() < 10) {
                UUID id = Nebula.spawnAroundPlayer(player);
                if (id != null) set.add(id);
            }
        }
    }

    @ApplyEvent(100)
    public final void onDeath(DeathListener listener) {
        if (listener.canceledPlayerDeathIfNotCooldowns(this, .7f, 1800, 5636095, 16733695, 5592575, 11141290, 43690))
            listener.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 3));
    }

    @Override
    public Class<?>[] defineStorageType() {
        return new Class[] {ObjectOpenHashSet.class};
    }

    @Override
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public void onClientResponse(CompoundTag tag) {
        Minecraft minecraft = Minecraft.getInstance();
        int[] totem = tag.getIntArray("totem");
        ClientLevel level = minecraft.level;
        Entity entity = level.getEntity(totem[0]);
        if (entity != null) {
            int[] colors = new int[totem.length - 2];
            ParticleOptions data;
            if (colors.length != 0) {
                System.arraycopy(totem, 2, colors, 0, colors.length);
                data = new ColorfulTotemType.ColorfulTotemOptions(colors);
            } else data = ParticleTypes.TOTEM_OF_UNDYING;

            minecraft.particleEngine.createTrackingEmitter(entity, data, 30);
            level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
            if (entity == minecraft.player)
                minecraft.gameRenderer.displayItemActivation(BuiltInRegistries.ITEM.byId(totem[1]).getDefaultInstance());
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.AQUA, "nebulous_core", 1, 2, 3);
        return tooltips;
    }
}
