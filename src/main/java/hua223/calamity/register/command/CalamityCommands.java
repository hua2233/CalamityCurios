package hua223.calamity.register.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hua223.calamity.main.CalamityCurios.MODID;

public class CalamityCommands {
    private static final Logger log = LoggerFactory.getLogger(CalamityCommands.class);

    public static void commandInit(IEventBus bus) {
        DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES
            = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, MODID);

//        ARGUMENT_TYPES.register(MODID, () -> ArgumentTypeInfos.registerByClass(CalamityArgument.class,
//            SingletonArgumentInfo.contextFree(CalamityArgument::getBlackInstance)));

        ARGUMENT_TYPES.register(bus);
        MinecraftForge.EVENT_BUS.addListener(CalamityCommands::register);
    }

    private static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext buildContext = event.getBuildContext();

//        LiteralArgumentBuilder<CommandSourceStack> root =
//            Commands.literal("calamity").requires(player -> player.hasPermission(2));

//        root.then(Commands.argument("capability", CalamityArgument.getBlackInstance())
//            .then(Commands.literal("set")
//                .then(Commands.argument("value", IntegerArgumentType.integer(0))
//                    .executes(context -> {
//                        ServerPlayer player = context.getSource().getPlayer();
//                        if (player == null) return 2;
//                        int value = context.getArgument("value", Integer.class);
//
//                        if (CalamityArgument.getType(context, "capability").equals("magic")) {
//                            CalamityCapProvider.MAGIC.getCapabilityFrom(player).ifPresent(magic -> {
//                                magic.setTempValue(value, player);
//                                NetMessages.sendToClient(new MagicDataSync(magic.getValue()), player);
//                            });
//                        } else player.getCapabilityFrom(CalamityCapProvider.RAGE.getCapabilityFrom()).ifPresent(rage -> rage.setRageValue(value, player));
//
//                        return 1;
//                    })))
//            .then(Commands.literal("start")
//                .then(Commands.argument("value", BoolArgumentType.bool())
//                    .executes(context -> {
//                        ServerPlayer player = context.getSource().getPlayer();
//                        if (player == null) return 2;
//                        boolean is = BoolArgumentType.getBool(context, "value");
//
//                        if (CalamityArgument.getType(context, "capability").equals("magic")) {
//                            Magic.setEnabledState(player, is);
//                        } else player.getCapabilityFrom(CalamityCapProvider.RAGE.getCapabilityFrom()).ifPresent(rage -> rage.setEnabled(is, player));
//                        return 1;
//                    })))
//        );

//        dispatcher.register(root);
    }
}
