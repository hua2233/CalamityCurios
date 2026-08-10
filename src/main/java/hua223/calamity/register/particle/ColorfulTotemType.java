package hua223.calamity.register.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ColorfulTotemType extends FastParticleType<ColorfulTotemType.ColorfulTotemOptions> {
    public ColorfulTotemType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    protected ColorfulTotemOptions getInstance(Object... o) {
        return new ColorfulTotemOptions((int) o[0]);
    }

    @Override
    protected void getDeserializer(ArrayList<Class<?>> list) {
        list.add(int.class);
    }

    @OnlyIn(Dist.CLIENT)
    public static class ColorfulTotemProvider implements ParticleProvider<ColorfulTotemOptions> {
        private final SpriteSet sprites;

        @SuppressWarnings("deprecation")
        public ColorfulTotemProvider() {
            ResourceLocation location = BuiltInRegistries.PARTICLE_TYPE.getKey(ParticleTypes.TOTEM_OF_UNDYING);
            this.sprites = Minecraft.getInstance().particleEngine.spriteSets.get(location);
        }

        @Override
        public @Nullable Particle createParticle(@NotNull ColorfulTotemOptions colorfulTotemOptions, @NotNull ClientLevel clientLevel,
                                                 double v, double v1, double v2, double v3, double v4, double v5) {
            TotemParticle particle = new TotemParticle(clientLevel, v, v1, v2, v3, v4, v5, sprites);
            RandomSource source = clientLevel.getRandom();
            int color = colorfulTotemOptions.getColor(source);
            particle.setColor(adjustColor(FastColor.ARGB32.red(color), source),
                adjustColor(FastColor.ARGB32.green(color), source), adjustColor(FastColor.ARGB32.blue(color), source));
            return particle;
        }

        private float adjustColor(int color, RandomSource source) {
            float portion = color / 255f;
            //Maintain the original theme color to ensure it is not too flashy
            if (portion < .95f && source.nextFloat() > .45f)  {
                float change = 1f - portion;
                portion += (change - source.nextFloat() * (change * 2));
            }

            return portion;
        }
    }

    public static class ColorfulTotemOptions implements ParticleOptions {
        private final int[] color;

        //Server
        public ColorfulTotemOptions(int color) {
            this.color = new int[] {color};
        }

        //Client
        @OnlyIn(Dist.CLIENT)
        public ColorfulTotemOptions(int... color) {
            this.color = color;
        }

        @OnlyIn(Dist.CLIENT)
        public int getColor(RandomSource source) {
            return color.length == 1 ? color[0] : color[source.nextInt(0, color.length)];
        }

        @Override
        public @NotNull ColorfulTotemType getType() {
            return ParticleRegister.TOTEM.get();
        }

        @Override
        public void writeToNetwork(@NotNull FriendlyByteBuf friendlyByteBuf) {
            getType().toNetwork(friendlyByteBuf, color[0]);
        }

        @Override
        public @NotNull String writeToString() {
            return getType().toCommandString(color[0]);
        }
    }
}
