package hua223.calamity.register.config;

import com.electronwill.nightconfig.core.Config;
import hua223.calamity.main.AnnotationProcessor;
import hua223.calamity.main.CalamityCurios;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.jetbrains.annotations.ApiStatus;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

@ApiStatus.Internal
public class CalamityConfig {
    private CalamityConfig() {
    }

    private static ForgeConfigSpec.Builder server;

    @OnlyIn(Dist.CLIENT)
    private static ForgeConfigSpec.Builder client;

    static {
        //所有配置条目默认在其端配置下处理
        server = new ForgeConfigSpec.Builder()
            .comment("Server dist settings").push(ModConfig.Type.SERVER.name().toLowerCase(Locale.ROOT));
        server.comment("probability of transaction or drop items").push("drop");
        server.pop();
        server.comment("Whether to keep it during runtime. Deciding whether to enable hot updates or release memory")
            .define("RetentionPolicy", false);

        if (FMLEnvironment.dist.isClient()) {
            client = new ForgeConfigSpec.Builder()
                .comment("Client dist settings").push(ModConfig.Type.CLIENT.name().toLowerCase(Locale.ROOT));
            client.comment("Whether to keep it during runtime. Deciding whether to enable hot updates or release memory")
                .define("RetentionPolicy", false);
        }
    }

    public static void registerConfig(AnnotationProcessor annotationProcessor) {
        final Map<Class<?>, List<ForgeConfigSpec.ConfigValue<?>>> trace = new Object2ObjectOpenHashMap<>();

        annotationProcessor.addStartProcessingEntries(AutoConfig.class, processor -> {
            Class<?> clazz = processor.getDataClass();
            ModFileScanData.AnnotationData data = processor.getAnnotationData();
            ForgeConfigSpec.Builder builder = CalamityConfig.getBuilderOfSide(clazz);
            Field field = clazz.getField(data.memberName());
            Object value = field.get(null);
            ForgeConfigSpec.ConfigValue<?> v = null;
            if (data.annotationData().containsKey("template"))
                v = ConfigTemplate.valueOf(((ModAnnotation.EnumHolder) data.annotationData().get("template")).getValue()).handler(clazz, builder, value, processor);
            else if (data.annotationData().containsKey("functional") && (boolean) data.annotationData().get("functional")) {
                field.get(null);//Initialize fields for JVM to trigger registration logic
                ForgeConfigSpec.ConfigValue<?> values = getTrace(clazz, (String) data.annotationData().get("value"), trace);
                if (values != null) values.calamity$According[1] = field.getName();
                else CalamityCurios.LOGGER.error("在类 {} 中发现无效的函数配置字段 {}，找不到对应的注册的路径配置值对象", clazz.getSimpleName(), field.getName());
            } else {
                processor.analyzeDefaultData(AutoConfig.class);
                Class<?> type = field.getType();
                builder.comment(processor.parserArr("comment", String.class));

                String path = processor.parser("path");
                double[] defaultValue = processor.parser("defaultValue");
                boolean isRage = defaultValue.length == 2;
                if (!Modifier.isPublic(field.getModifiers()))
                    field.setAccessible(true);

                if (isRage) {
                    if (type == int.class)
                        v = builder.defineInRange(path, (Integer) value, defaultValue[0], defaultValue[1]);
                    else if (type == double.class)
                        v = builder.defineInRange(path, (Double) value, defaultValue[0], defaultValue[1]);
                    else if (type == float.class)
                        v = builder.defineInRange(path,  (Float) value, defaultValue[0], defaultValue[1]);
                    else if (type == boolean.class)
                        v = builder.define(path, (Boolean) value);
                } else v = builder.define(path, value);
            }

            if (v != null) v.calamity$According = new Object[] {clazz, field.getName()};
        });

        annotationProcessor.addPostProcessor(() -> CalamityConfig.register(annotationProcessor.context));
    }


    //函数配置由类加载时初始化，留下痕迹以供探查
    @SuppressWarnings("unchecked")
    private static ForgeConfigSpec.ConfigValue<?> getTrace(Class<?> targetClass, String path, Map<Class<?>, List<ForgeConfigSpec.ConfigValue<?>>> trace) throws Exception {
        if (trace.containsKey(targetClass)) {
            for (ForgeConfigSpec.ConfigValue<?> value : trace.get(targetClass))
                if (value.calamity$According.length > 2) {
                    List<String> paths = value.getPath();
                    if (paths.get(paths.size() - 1).equals(path)) return value;
                }
        } else {
            Field field = ForgeConfigSpec.Builder.class.getDeclaredField("values");;
            //Be sure to rebuild from the copied copy，
            //Otherwise, modifying the source fields held by Spec will cause the configuration files of various environments to be completely disordered
            List<ForgeConfigSpec.ConfigValue<?>> configs = new ArrayList<>((List<ForgeConfigSpec.ConfigValue<?>>) ReflectionUtil.getFieldValue(field, server));
            if (FMLEnvironment.dist.isClient()) configs.addAll((List<ForgeConfigSpec.ConfigValue<?>>) ReflectionUtil.getFieldValue(field, client));

            configs.removeIf(config ->
                config.calamity$According[0] == null || config.calamity$According.length < 2 || config.calamity$According[0] != targetClass);
            if (configs.isEmpty()) return null;
            else if (configs.size() == 1) return configs.get(0);
            else {
                trace.put(targetClass, configs);
                for (ForgeConfigSpec.ConfigValue<?> value : configs) {
                    List<String> paths = value.getPath();
                    if (paths.get(paths.size() - 1).equals(path)) return value;
                }
            }
        }

        return null;
    }


    private static void register(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist.isClient()) {
            context.registerConfig(ModConfig.Type.CLIENT, client.build());
            client = null;
        }

        context.registerConfig(ModConfig.Type.SERVER, server.build());
        server = null;
    }

    //通过此方法设置默认值，同时保证字段不被内联
    public static <T> T value(T value) {
        return value;
    }

    public static <T, V> V functionValue(Function<ForgeConfigSpec.Builder, ForgeConfigSpec.ConfigValue<T>> function, Function<T, V> handle) {
        Class<?> caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        if (caller != null) function.apply(getBuilderOfSide(caller)).calamity$According = new Object[] {caller, null, handle};
        return null;
    }

    //You can define some constant values, which will also be updated
    //Constants should not be modifiable in code, as this ensures their security and allows them to change during configuration updates
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onLoadConfigInfo(ModConfigEvent event) {
        try {
            Unsafe unsafe = (Unsafe) ReflectionUtil.getFieldValue(Unsafe.class.getDeclaredField("theUnsafe"), null);
            ModConfig config = event.getConfig();
            boolean retain = false;//-153270840

            for (ForgeConfigSpec.ConfigValue<?> configValue : loadConfigValue(new ArrayList<>(), ((Config) ((ForgeConfigSpec)
                config.getSpec()).getValues().valueMap().get(config.getType().name().toLowerCase())).valueMap().values())) {
                //保留的资源定位
                if (configValue.calamity$According != null) {
                    Class<?> configClass = (Class<?>) configValue.calamity$According[0];
                    Object value = configValue.get();
                    Field field = configClass.getDeclaredField((String) configValue.calamity$According[1]);
                    Class<?> valueType = field.getType();
                    long offset = unsafe.staticFieldOffset(field);
                    if (valueType == double.class) unsafe.putDouble(configClass, offset, (Double) value);
                    else if (valueType == float.class)  unsafe.putFloat(configClass, offset, ((Double) value).floatValue());
                    else if (valueType == int.class) unsafe.putInt(configClass, offset, ((Double) value).intValue());
                    else if (valueType == boolean.class) unsafe.putBoolean(configClass, offset, (Boolean) value);
                    else unsafe.putObject(configClass, offset, configValue.calamity$According.length == 3 &&
                            configValue.calamity$According[2] != null ? ((Function) configValue.calamity$According[2]).apply(value) : value);
                } else {
                    List<String> path = configValue.getPath();
                    if (path.get(path.size() - 1).equals("RetentionPolicy"))
                        retain = (boolean) configValue.get();
                }
            }

            if (!retain) {
                Field containerF = config.getClass().getDeclaredField("container");
                containerF.setAccessible(true);
                Object container = containerF.get(config);
                Class<?> c = container.getClass().getSuperclass();
                Field configsF = c.getDeclaredField("configs");
                configsF.setAccessible(true);
                EnumMap<ModConfig.Type, ModConfig> map = (EnumMap<ModConfig.Type, ModConfig>) configsF.get(container);
                map.remove(config.getType());
                if (map.isEmpty()) {
                    Field configsH = c.getDeclaredField("configHandler");
                    configsH.setAccessible(true);
                    configsH.set(container, Optional.empty());
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static ForgeConfigSpec.Builder getBuilderOfSide(Class<?> clazz) {
        return FMLEnvironment.dist.isClient() && IClientConfig.class.isAssignableFrom(clazz) ? client : server;
    }

    private static List<ForgeConfigSpec.ConfigValue<?>> loadConfigValue(List<ForgeConfigSpec.ConfigValue<?>> allValues, Iterable<Object> configValues) {
        for (Object value : configValues) {
            if (value instanceof ForgeConfigSpec.ConfigValue<?> configValue)
                allValues.add(configValue);
            else if (value instanceof Config innerConfig) loadConfigValue(allValues, innerConfig.valueMap().values());
        }

        return allValues;
    }

    public enum ConfigTemplate {
        DROP {
            @Override
            protected ForgeConfigSpec.ConfigValue<?> handler(Class<?> dataClass, ForgeConfigSpec.Builder builder, Object value, AnnotationProcessor processor) {
                double probability = value instanceof Float ? (Float) value : (Double) value;
                return builder.defineInRange("drop." + dataClass.getSimpleName(), probability, 0, 1);
            }
        };

        protected abstract ForgeConfigSpec.ConfigValue<?> handler(Class<?> dataClass, ForgeConfigSpec.Builder builder, Object value, AnnotationProcessor processor);
    }
}
