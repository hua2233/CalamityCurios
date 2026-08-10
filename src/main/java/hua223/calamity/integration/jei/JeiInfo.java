package hua223.calamity.integration.jei;

import hua223.calamity.register.items.CalamityItems;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface JeiInfo {
    String text() default "";
    int line() default 1;
    CalamityItems item() default CalamityItems.CALAMITY;
    String[] zh_cn() default {};
}
