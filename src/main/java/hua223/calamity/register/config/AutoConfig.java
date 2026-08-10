package hua223.calamity.register.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置自动注册注解，标注于静态字段上，声明该字段的配置注册逻辑。
 * <p>
 * 支持三种注册模式：
 * <ul>
 *     <li>模板模式：通过 {@link #template()} 调用预设模板，无需额外操作</li>
 *     <li>简易模式：通过 {@link #path()} + {@link #defaultValue()} 声明路径与数值范围，
 *         字段值需调用 {@link CalamityConfig#value(Object)} 设置默认值并防止内联</li>
 *     <li>函数式模式：启用 {@link #functional()} 后，通过 {@link CalamityConfig#functionValue} 手动注册高自定义配置内容</li>
 * </ul>
 * 注解在启动阶段由 AnnotationProcessor 通过 ASM 扫描处理，生成 Forge 配置项并写入 TOML 文件，
 * 配置变更时通过 ModConfigEvent 监听并使用 Unsafe 实现字段热更新。
 *
 * @see CalamityConfig
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface AutoConfig {
    /**
     * 配置在 TOML 文件中的注册路径。
     * <p>
     * 函数式模式下，必须与手动调用 {@code define} 方法的第一参数一致；
     * 若存在父路径，取最后一个 {@code .} 之后的字符串。
     *
     * @return 配置注册路径
     */
    String path() default "";

    /**
     * 是否启用函数式声明模式。
     * <p>
     * 启用后需通过 {@link CalamityConfig#functionValue} 方法手动注册配置，
     * 同时必须声明 {@link #path()} 用于追踪绑定。
     *
     * @return 是否为函数式模式
     */
    boolean functional() default false;

    /**
     * 数值参数的取值范围，仅简易模式生效。
     * <p>
     * 数组长度为 2 时表示 {@code [min, max]} 范围约束；
     * 布尔值无需设置，直接设置字段值作为默认值即可。
     * 字段默认值需调用 {@link CalamityConfig#value(Object)} 设置并防止编译期内联。
     *
     * @return 取值范围数组
     */
    double[] defaultValue() default {};

    /**
     * 配置项的注释文本，仅简易模式生效，非必须。
     *
     * @return 注释文本数组
     */
    String[] comment() default "";

    /**
     * 预设配置模板，调用后无需额外操作。
     *
     * @return 配置模板枚举
     * @see CalamityConfig.ConfigTemplate
     */
    CalamityConfig.ConfigTemplate template() default CalamityConfig.ConfigTemplate.DROP;
}

