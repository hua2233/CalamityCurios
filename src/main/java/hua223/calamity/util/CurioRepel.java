package hua223.calamity.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CurioRepel {
    boolean isRoot() default false;

    Class<?> value() default CuriosConflictMap.class;
}
