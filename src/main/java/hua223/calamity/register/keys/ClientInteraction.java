package hua223.calamity.register.keys;

import com.mojang.blaze3d.platform.InputConstants;
import hua223.calamity.net.DataPack;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.packets.ApplySprint;
import hua223.calamity.net.packets.ClientLongPressTrigger;
import hua223.calamity.net.packets.DataPackActive;
import hua223.calamity.net.packets.OpenEnchantGui;
import hua223.calamity.util.ILongPressAvailable;
import hua223.calamity.util.delaytask.DelayRunnable;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public abstract class ClientInteraction extends KeyMapping {
    private static final Int2ObjectArrayMap<ClientInteraction> FUNCTION_KEY = new Int2ObjectArrayMap<>();
    private static Minecraft minecraft;
    private boolean active;

    static {
        createSimpleDataPackKey("enchant", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, new OpenEnchantGui());

        createSimpleDataPackKey("sprinting", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, new ApplySprint());
        applyOrDelete(GLFW.GLFW_KEY_J, true);
    }

    @SuppressWarnings("ALL")
    private ClientInteraction(String description, IKeyConflictContext context, InputConstants.Type type, int code) {
        super(getSimpleId(description), context, type, code, "key.category.calamity_curios");
    }

    public static void createSimpleDataPackKey(String description, IKeyConflictContext context, InputConstants.Type type, int code, DataPack pack) {
        if (FUNCTION_KEY.put(code, new ClientInteraction(description, context, type, code) {
            @Override
            protected void onKeyDown() {
                NetMessages.sendToServer(pack);
            }
        }) != null) throw new IllegalArgumentException("Cannot create duplicate function keys!");
    }

    @Override
    public void setKey(InputConstants.@NotNull Key key) {
        if (!FUNCTION_KEY.containsKey(key.getValue())) super.setKey(key);
    }

    @Override
    public void setKeyModifierAndCode(@Nullable KeyModifier keyModifier, InputConstants.@NotNull Key keyCode) {
        FUNCTION_KEY.put(keyCode.getValue(), FUNCTION_KEY.remove(getKey().getValue()));
        super.setKeyModifierAndCode(keyModifier, keyCode);
    }

    public static String getSimpleId(String id) {
        return "key.calamity_curios." + id;
    }

    public static void createCuriosKey(IKeyDataPackResponse activatable) {
        if (!(activatable instanceof Item item) || FUNCTION_KEY.put(activatable.getKeyCode(), new ClientInteraction(
            item.getClass().getSimpleName().toLowerCase(), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, activatable.getKeyCode()) {
            @Override
            @SuppressWarnings("ConstantConditions")
            protected void onKeyDown() {
                if (activatable.accept(minecraft)) NetMessages.sendToServer(new DataPackActive(item));
            }
        }) != null) throw new IllegalArgumentException("Cannot create function handling key for this curio" + activatable.toString());
    }

    public static void build(RegisterKeyMappingsEvent event) {
        for (ClientInteraction key : FUNCTION_KEY.values()) event.register(key);
        minecraft = Minecraft.getInstance();
    }

    public static void applyOrDelete(int key, boolean isApply) {
        for (ClientInteraction k : FUNCTION_KEY.values()) {
            if (k.getDefaultKey().getValue() == key)
                k.active = isApply;
        }
    }

    public static void clear() {
        for (ClientInteraction key : FUNCTION_KEY.values())
            key.active = false;
    }

    public static void checkDown(int code) {
        ClientInteraction interaction = FUNCTION_KEY.get(code);
        if (interaction != null && interaction.consumeClick() && interaction.active)
            interaction.onKeyDown();
    }

    protected abstract void onKeyDown();

    //mouse
    private static int pressDuration;
    private static boolean isLongPressActive;

    public static boolean isLongPressActive() {
        return isLongPressActive;
    }

    @SuppressWarnings("ConstantConditions")
    public static void longPressResponse(InputEvent.InteractionKeyMappingTriggered event) {
        if ((isLongPressActive && !event.isPickBlock()) || minecraft.player.Calamity$Player.freeze) {
            event.setCanceled(true);
            event.setSwingHand(false);
        } else if (event.isAttack() && minecraft.player.getMainHandItem().getItem() instanceof ILongPressAvailable available) {
            final DataPack pack = new ClientLongPressTrigger();
            DelayRunnable.addUniqueLoopTask(() -> {
                //获取最新状态而不是缓存对象
                LocalPlayer player = minecraft.player;
                if (player == null || !minecraft.mouseHandler.isLeftPressed() ||
                    player.getMainHandItem().getItem() != available) {
                    isLongPressActive = false;
                    pressDuration = 0;
                    return true;
                }


                if (isLongPressActive) {
                    if (pressDuration >= available.maxDuration()) {
                        isLongPressActive = false;
                        pressDuration = 0;
                    } else if (available.isResponseTime(player, ++pressDuration)) {
                        available.onClientResponse(player);
                        NetMessages.sendToServer(pack);
                    }
                } else if (++pressDuration > 14) {
                    isLongPressActive = true;
                    pressDuration = 0;
                }

                return false;
            }, 1, ClientInteraction.class);
        }
    }
}
