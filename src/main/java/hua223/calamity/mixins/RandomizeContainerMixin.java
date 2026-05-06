package hua223.calamity.mixins;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.OutlineDetected;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class RandomizeContainerMixin extends BaseContainerBlockEntity {
    @Unique
    @OnlyIn(Dist.CLIENT)
    public boolean calamity$Detected = true;

    protected RandomizeContainerMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "tryLoadLootTable", at = @At(value = "RETURN", ordinal = 1))
    private void setDetected(CompoundTag tag, CallbackInfoReturnable<Boolean> cir) {
        NetMessages.sendToAllClient(new OutlineDetected(worldPosition));
    }

    @Inject(method = "unpackLootTable", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target =
        "Lnet/minecraft/world/level/storage/loot/LootTable;fill(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/storage/loot/LootParams;J)V"))
    private void setDetected(Player pPlayer, CallbackInfo ci) {
        NetMessages.sendToAllClient(new OutlineDetected(worldPosition));
    }
}
