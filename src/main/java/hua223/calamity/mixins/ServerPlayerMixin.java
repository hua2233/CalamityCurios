package hua223.calamity.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerPlayer.class})
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveCalamityData(CompoundTag compound, CallbackInfo ci) {
        Calamity$Player.save(compound);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadCalamityData(CompoundTag compound, CallbackInfo ci) {
        Calamity$Player.load(compound);
    }

    @Unique
    public void calamity$SetInvulnerableTime(int time) {
        invulnerableTime += time;
        lastHurt = Integer.MAX_VALUE;
    }
}
