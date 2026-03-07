package hua223.calamity.mixins;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ServerPlayer.class})
public class ServerPlayerMixin {
    @Unique
    public byte calamity$InactivationCount;
}
