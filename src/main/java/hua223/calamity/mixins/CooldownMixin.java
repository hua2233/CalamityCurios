package hua223.calamity.mixins;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.ReduceCooldown;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(ItemCooldowns.class)
public class CooldownMixin {
    @Shadow @Final
    private Map<Item, ItemCooldowns.CooldownInstance> cooldowns;

    @Shadow
    private int tickCount;

    public void calamity$ReduceCooldown(Item item, int tick, @Nullable ServerPlayer player) {
        ItemCooldowns.CooldownInstance instance = cooldowns.get(item);
        if (instance.calamity$ReduceCooldown(tick) && player != null)
            NetMessages.sendToClient(new ReduceCooldown(item, tick), player);
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target =
        "Lnet/minecraft/world/item/ItemCooldowns$CooldownInstance;endTime:I", opcode = Opcodes.GETFIELD))
    private int getTickCount(ItemCooldowns.CooldownInstance instance) {
        return instance.calamity$GetEndTime();
    }

    /**
     * @author hua223
     * @reason Avoid query exceptions
     */
    @Overwrite
    public boolean isOnCooldown(Item item) {
        return cooldowns.containsKey(item);
    }

    /**
     * @author hua223
     * @reason Modify the scale of rendering
     */
    @Overwrite
    public float getCooldownPercent(Item item, float partialTicks) {
        ItemCooldowns.CooldownInstance instance = this.cooldowns.get(item);
        return instance == null ? 0f : instance.calamity$CalculatePercent(tickCount, partialTicks);
    }

    @Mixin(ItemCooldowns.CooldownInstance.class)
    private static class Instance {
        @Unique
        private int calamity$AccelerateTick;

        @Shadow @Final @Mutable int endTime;

        @Shadow @Final
        int startTime;


        public boolean calamity$ReduceCooldown(int tick) {
            if (calamity$GetEndTime() > 0) {
                calamity$AccelerateTick += tick;
                return calamity$GetEndTime() > 0;
            }

            return false;
        }

        public int calamity$GetEndTime() {
           return endTime - calamity$AccelerateTick;
        }

        public float calamity$CalculatePercent(int tickCount, float partialTick) {
            return Mth.clamp((calamity$GetEndTime() - tickCount - partialTick) / (endTime - startTime), 0.0F, 1.0F);
        }
    }
}
