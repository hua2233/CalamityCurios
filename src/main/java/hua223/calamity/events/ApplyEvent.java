package hua223.calamity.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) //hua223
@Target(ElementType.METHOD)
public @interface ApplyEvent {
    int value() default 200;
}
