package hua223.calamity.register.config;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
//Configuration values are mapped by the underlying layer when changed，Prohibit manual modification
public class ClientConfigValue implements IClientConfig {
    @AutoConfig(comment = "The detection radius of the Omniscience potion's highlighting effect.",
        path = "detectionRadius", defaultValue = {1, 48})
    public static final double DETECTING_RADIUS = CalamityConfig.value(24);
//    @FunctionConfig("immuneEffects")
//    public static final List<? extends MobEffect> TEST = CalamityConfig.functionValue(
//        builder -> builder.comment("List of immune effects that shields can provide")
//            .defineList("immuneEffects", List.of(MobEffects.POISON), o -> true), null);
}
