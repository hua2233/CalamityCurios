package hua223.calamity.main;

import com.google.common.base.CaseFormat;
import cpw.mods.modlauncher.TransformingClassLoader;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.delaytask.DelayRunnable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

public class AnnotationProcessor {
    private final Map<Type, ResolveExceptionConsumer<AnnotationProcessor>>
        bootStartFunctions = new Object2ObjectOpenHashMap<>();
    private final List<Runnable> postProcessor = new ArrayList<>();
    private final List<Runnable> fmlSetup;
    private final EnumSet<ElementType> checkType;

    public final FMLJavaModLoadingContext context;
    public final boolean runData = FMLLoader.getLaunchHandler().isData();
    private final ModFileScanData scanData;
    public final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private Map<String, Object> defaultAnnotationParser;
    public final TransformingClassLoader modLoader;
    public final IEventBus modBus;
    public final IEventBus forgeBus;
    private ModFileScanData.AnnotationData annotationData;

    public AnnotationProcessor(final FMLJavaModLoadingContext context) {
        try {
            scanData = (ModFileScanData) ReflectionUtil.getFieldValue(
                FMLModContainer.class.getDeclaredField("scanResults"), context.getContainer());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        this.context = context;
        modBus = context.getModEventBus();
        forgeBus = MinecraftForge.EVENT_BUS;
        modLoader = (TransformingClassLoader) CalamityCurios.class.getClassLoader();
        checkType = EnumSet.of(ElementType.TYPE, ElementType.FIELD, ElementType.METHOD);
        List<Runnable> setUp = new ArrayList<>();
        fmlSetup = setUp;
        //除非你在本处理器内部addProcessor阶段方法，可以引用处理器本身。
        //不要在任何长期存在的lambda中引用它，这会导致处理器实例无法被GC
        DelayRunnable.currentTickEndRun(() -> {
            RegisterList.onFMLSetUp();
            for (Runnable runnable : setUp) runnable.run();
        });
    }

    public void addStartProcessingEntries(Class<? extends Annotation> target, ResolveExceptionConsumer<AnnotationProcessor> bootStartFunction) {
        bootStartFunctions.put(Type.getType(target), bootStartFunction);
    }

    public void addPostProcessor(Runnable runnable) {
        postProcessor.add(runnable);
    }

    public void onFmlSetup(Runnable runnable) {
        fmlSetup.add(runnable);
    }

    public void process() {
        CalamityCurios.LOGGER.info("Annotation processor start");
        long startTime = System.currentTimeMillis();

        try {
            for (ModFileScanData.AnnotationData annotation : scanData.getAnnotations()) {
                if (checkType.contains(annotation.targetType())) {
                    ResolveExceptionConsumer<AnnotationProcessor> functions = bootStartFunctions.get(annotation.annotationType());
                    if (functions != null) {
                        this.annotationData = annotation;
                        functions.resolveApply(this);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Runnable runnable : postProcessor) runnable.run();
        CalamityCurios.LOGGER.info("Annotation process complete in {} ms", System.currentTimeMillis() - startTime);
    }

    public final String getMethodName() {
        String full = annotationData.memberName();
        return full.substring(0, full.indexOf('('));
    }

    public final MethodType fromDescriptorGetType() {
        String full = annotationData.memberName();
        return MethodType.fromMethodDescriptorString(full.substring(full.indexOf('(')), modLoader);
    }

    public CalamityItems getItemEnum() throws ClassNotFoundException {
        Map<String, Object> data = annotationData.annotationData();
        return getItemEnum(data, getDataClass().getSimpleName());
    }

    public CalamityItems getItemEnum(Map<String, Object> data, String  simpleName) {
        String enumName = data.containsKey("item") ? ((ModAnnotation.EnumHolder) data.get("item")).getValue()
            : CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, simpleName);
        return CalamityItems.valueOf(enumName);
    }

    public ModFileScanData.AnnotationData getAnnotationData() {
        return annotationData;
    }

    public Class<?> getDataClass() throws ClassNotFoundException {
        return modLoader.loadClass(annotationData.clazz().getClassName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void analyzeDefaultData(Class<? extends Annotation> annotation) throws ClassNotFoundException {
        defaultAnnotationParser = annotationData.annotationData();

        for (Map.Entry<String, Object> entry : defaultAnnotationParser.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof ModAnnotation.EnumHolder holder) {
                String desc = holder.getDesc();
                Class aClass = modLoader.loadClass(desc.substring(1, desc.length() -1).replace('/', '.'));
                defaultAnnotationParser.put(entry.getKey(), Enum.valueOf(aClass, holder.getValue()));
            } else if (value instanceof Type type) {
                Class<?> t = modLoader.loadClass(type.getClassName());
                defaultAnnotationParser.put(entry.getKey(), t);
            }
        }

        Method[] methods = annotation.getDeclaredMethods();
        if (defaultAnnotationParser.size() != methods.length)
            for (Method method : methods) defaultAnnotationParser.computeIfAbsent(method.getName(), k -> method.getDefaultValue());
    }

    @SuppressWarnings("unchecked")
    public <T> T parser(String name) {
        return (T) defaultAnnotationParser.get(name);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public <T> T[] parserArr(String name, Class<T> type) {
        Object o = parser(name);
        return o instanceof ArrayList<?> list ? list.toArray((T[]) Array.newInstance(type, list.size())) : (T[]) o;
    }

    @FunctionalInterface
    public interface ResolveExceptionConsumer<T> {
        void accept(T t) throws Throwable;

        default void resolveApply(T t) {
            try {
                accept(t);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }
}
