package hua223.calamity.register.keys;

import com.mojang.blaze3d.platform.InputConstants;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.net.C2SPacket.*;
import hua223.calamity.net.NetMessages;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ILongPressAvailable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public abstract class ClientInteractions extends KeyMapping {
    //key
    private static final ObjectOpenHashSet<ClientInteractions> FUNCTION_KEY =
        CalamityHelp.createMappingSet(ClientInteractions.class);
    private static final List<ClientInteractions> ACTIVE_KEY = new ArrayList<>(16);
    private static Minecraft minecraft;

    static {
        createSimpleDataPackKey("enchant", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, new OpenEnchantGui());

        createSimpleDataPackKey("sprinting", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, new ApplySprint());
    }

    @SuppressWarnings("ALL")
    private ClientInteractions(String description, IKeyConflictContext context, InputConstants.Type type, int code) {
        super(getSimpleId(description), context, type, code, "key.category.calamity_curios");
    }

    @SuppressWarnings("ALL")
    private ClientInteractions(Item item, IKeyConflictContext context, InputConstants.Type type, int code) {
        super(getCurioKeyId(item), context, type, code, "key.category.calamity_curios");
    }

    public static void createSimpleDataPackKey(String description, IKeyConflictContext context, InputConstants.Type type, int code, C2S pack) {
        if (!FUNCTION_KEY.add(new ClientInteractions(description, context, type, code) {
            @Override
            void onKeyDown() {
                NetMessages.sendToServer(pack);
            }
        })) throw new IllegalArgumentException("Cannot create duplicate function keys!");
    }

    //Get key type of simple data pack。It will serve as both its translation key and query key
    public static String getSimpleId(String id) {
        return "key.description.calamity_curios." + id;
    }

    //Generally speaking, this is only used for a given translation key.
    //Please directly use the Curios Key itself to obtain it from the Set
    @SuppressWarnings("ConstantConditions")
    public static String getCurioKeyId(Item item) {
       return getSimpleId(ForgeRegistries.ITEMS.getKey(item).getPath());
    }

    public static void createCuriosKey(IKeyDataPackResponse activatable) {
        if (!(activatable instanceof BaseCurio curio) || !FUNCTION_KEY.add(
            new ClientInteractions(curio, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, activatable.getKeyCode()) {
                @Override
                @SuppressWarnings("ConstantConditions")
                void onKeyDown() {
                    if (activatable.detectCooling() && minecraft.player.getCooldowns().isOnCooldown(curio)) return;
                    if (!activatable.accept(minecraft)) NetMessages.sendToServer(new DataPackActive(curio));
                }

                @Override
                @SuppressWarnings("ALL")
                public boolean equals(Object obj) {
                    return curio.equals(obj);
                }

                @Override
                public int hashCode() {
                    return curio.hashCode();
                }
            }
        )) throw new IllegalArgumentException("Cannot create function handling key for this curio" + activatable.toString());
    }

    public static void build(RegisterKeyMappingsEvent event) {
        for (ClientInteractions key : FUNCTION_KEY) event.register(key);
        minecraft = Minecraft.getInstance();
    }

    public static void applyOrDelete(String id, Item item, boolean isApply) {
        Object key = item == null ? getSimpleId(id)  : item;
        ClientInteractions mapping = Objects.requireNonNull(FUNCTION_KEY.get(key));

        if (isApply) ACTIVE_KEY.add(mapping);
        else ACTIVE_KEY.remove(mapping);
    }

    public static void clear() {
        ACTIVE_KEY.clear();
    }

    public static void checkDown() {
        for (ClientInteractions clientInteractions : ACTIVE_KEY)
            if (clientInteractions.consumeClick()) {
                clientInteractions.onKeyDown();
                return;
            }
    }

    abstract void onKeyDown();

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    @SuppressWarnings("ALL")
    public boolean equals(Object obj) {
        return getName().equals(obj);
    }

    //mouse
    private static int pressDuration;
    private static boolean isLongPressActive;

    public static boolean isLongPressActive() {
        return isLongPressActive;
    }

    public static void longPressToCheck() {
        if (!minecraft.mouseHandler.isLeftPressed()) {
            reSet();
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null) {
            reSet();
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof ILongPressAvailable available)) {
            reSet();
            return;
        }

        if (isLongPressActive) {
            if (available.isResponseTime(player, ++pressDuration)) {
                available.onClientResponse(player, stack);
                NetMessages.sendToServer(new ClientLongPressTrigger());
            }
            if (pressDuration >= stack.getUseDuration()) reSet();
        } else if (++pressDuration > 14) {
            isLongPressActive = true;
            pressDuration = 0;
        }
    }

    private static void reSet() {
        isLongPressActive = false;
        pressDuration = 0;
    }

    @SuppressWarnings("ConstantConditions")
    public static void cancelResponse(InputEvent.InteractionKeyMappingTriggered event) {
        if ((isLongPressActive && !event.isPickBlock()) || minecraft.player.calamity$IsFreeze) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }
}
