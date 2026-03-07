package hua223.calamity.register.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Locale;

//The product that I am lazy about, please make sure to use it in the correct order, and the order type must be consistent
public abstract class FastParticleType<T extends ParticleOptions> extends ParticleType<T> {
    private Class<?>[] types;

    public FastParticleType(boolean overrideLimiter) {
        super(overrideLimiter, null);

        deserializer = new ParticleOptions.Deserializer<>() {
            @Override
            public T fromCommand(ParticleType<T> particleType, StringReader reader) throws CommandSyntaxException {
                final Class<?>[] types = getTypes();

                Object[] args = new Object[types.length];
                for (int i = 0; i < types.length; i++) {
                    reader.expect(' ');
                    Class<?> c = types[i];
                    if (c == int.class) args[i] = reader.readInt();
                    else if (c == float.class) args[i] = reader.readFloat();
                    else if (c == boolean.class) args[i] = reader.readBoolean();
                    else if (c == String.class) args[i] = reader.readString();
                    else {
                        MutableComponent message = Component.literal("Unsupported parameter format type!");
                        throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
                    }
                }

                return getInstance(args);
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public T fromNetwork(ParticleType<T> particleType, FriendlyByteBuf buf) {
                final Class<?>[] types = getTypes();

                Object[] args = new Object[types.length];
                for (int i = 0; i < types.length; i++) {
                    Class<?> c = types[i];
                    if (c == int.class) args[i] = buf.readVarInt();
                    else if (c == float.class) args[i] = buf.readFloat();
                    else if (c == boolean.class) args[i] = buf.readBoolean();
                    else if (c == String.class) args[i] = buf.readUtf();
                    else throw new IllegalStateException("Unsupported parameter format type!");
                }

                return getInstance(args);
            }
        };
    }

    //this is usually not used
    @Override
    public Codec<T> codec() {
        return null;
    }

    void toNetwork(FriendlyByteBuf buf, Object... values) {
        final Class<?>[] types = getTypes();

        for (int i = 0; i < types.length; i++) {
            Class<?> c = types[i];
            if (c == int.class) buf.writeVarInt((Integer) values[i]);
            else if (c == float.class) buf.writeFloat((Float) values[i]);
            else if (c == boolean.class) buf.writeBoolean((Boolean) values[i]);
            else if (c == String.class) buf.writeUtf((String) values[i]);
            else throw new IllegalStateException("Unsupported parameter format type!");
        }
    }

    String toCommandString(Object... values) {
        StringBuilder builder = new StringBuilder(32);
        builder.append("%s ");
        for (Class<?> c : getTypes()) {
            if (c == int.class) builder.append("%d ");
            else if (c == float.class) builder.append("%.2f ");
            else if (c == boolean.class) builder.append("%b ");
            else if (c == String.class) builder.append("%s ");
            else throw new IllegalStateException("Unsupported parameter format type!");
        }

        Object[] finalArr = new Object[values.length + 1];
        finalArr[0] = this;
        System.arraycopy(values, 0, finalArr, 1, values.length);
        return String.format(Locale.ROOT, builder.deleteCharAt(builder.length() - 1).toString(), finalArr);
    }

    private Class<?>[] getTypes() {
        if (types == null) {
            ArrayList<Class<?>> deserializerInfo = new ArrayList<>();
            getDeserializer(deserializerInfo);
            types = deserializerInfo.toArray(Class<?>[]::new);
            DelayRunnable.addRunTask(40, () -> types = null);
        }

        return types;
    }

    protected abstract T getInstance(Object... o);

    protected abstract void getDeserializer(ArrayList<Class<?>> list);
}
