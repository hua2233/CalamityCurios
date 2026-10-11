package hua223.calamity.mixins.client;

import hua223.calamity.mixed.IBakedGlyphAccessor;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedGlyph.class)
public interface BakedGlyphAccessor extends IBakedGlyphAccessor {
    @Accessor("u0")
    float calamity$GetU0();

    @Accessor("v0")
    float calamity$GetV0();

    @Accessor("u1")
    float calamity$GetU1();

    @Accessor("v1")
    float calamity$GetV1();

    @Accessor("left")
    float calamity$GetLeft();

    @Accessor("right")
    float calamity$GetRight();

    @Accessor("up")
    float calamity$GetUp();

    @Accessor("down")
    float calamity$GetDown();
}
