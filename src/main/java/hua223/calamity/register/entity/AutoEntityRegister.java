package hua223.calamity.register.entity;

import net.minecraft.world.entity.MobCategory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface AutoEntityRegister {
    Class<?> renderClass() default AutoEntityRegister.class;

    String name() default "";

    MobCategory category() default MobCategory.MISC;

    float[] sized() default  {0.f, 0f};

    int trackingRange() default  -1;

    int updateInterval() default  -1;

    boolean noSummon() default true;

    boolean noSave() default true;

    boolean velocityUpdates() default true;
}
