package hua223.calamity.register.keys;

import com.mojang.blaze3d.platform.InputConstants;
import hua223.calamity.integration.curios.item.GravistarSabaton;
import hua223.calamity.net.C2SPacket.*;
import hua223.calamity.net.NetMessages;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.ILongPressAvailable;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.EnumSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public enum ClientInteraction {
    RAGE_ACTIVE(new FunctionKey("rage_active",
        KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V) {
        @Override
        void onKeyDown() {
            NetMessages.sendToServer(new RageActive());
        }
    }),

    ADRENALINE_ACTIVE(new FunctionKey("adrenaline_active",
        KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z) {
        @Override
        void onKeyDown() {
            NetMessages.sendToServer(new AdrenalineActivate());
        }
    }),

    CRYSTALLIZATION(new FunctionKey("crystallization",
        KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C) {
        @Override
        void onKeyDown() {
            if (!mc.player.getCooldowns().isOnCooldown(CalamityItems.BLAZING_CORE.get()))
                NetMessages.sendToServer(new Crystallization());
        }
    }),

    SLAM(new FunctionKey("slam",
        KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X) {
        @Override
        void onKeyDown() {
            LocalPlayer player = mc.player;

            if (!player.isOnGround()) {
                ItemCooldowns cooldowns = player.getCooldowns();
                Item item = CalamityItems.GRAVISTAR_SABATON.get();
                if (!cooldowns.isOnCooldown(item)) {
                    GravistarSabaton.impact = true;
                    cooldowns.addCooldown(item, 300);
                }
            }
        }
    }),

    GRUESOME_EMINENCE(new FunctionKey("gruesome_eminence",
        KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G) {
        @Override
        void onKeyDown() {
            ItemCooldowns cooldowns = mc.player.getCooldowns();
            Item item = CalamityItems.GRUESOME_EMINENCE.get();
            if (!cooldowns.isOnCooldown(item)) {
                cooldowns.addCooldown(item, 1200);
                NetMessages.sendToServer(new EvilSpiritsC2S());
            }
        }
    }),

    ENCHANT(new FunctionKey("enchant",
        KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J) {
        @Override
        void onKeyDown() {
            NetMessages.sendToServer(new OpenEnchantGui());
        }
    }),

    INSIGNIA_FLY(new FunctionKey("insignia_fly",
        KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y) {
        @Override
        void onKeyDown() {
            LocalPlayer player = mc.player;
            ItemCooldowns cooldowns = player.getCooldowns();

            Item item = CalamityItems.ASCENDANT_INSIGNIA.get();
            if (cooldowns.isOnCooldown(item)) return;

            cooldowns.addCooldown(item, 800);
            player.playSound(CalamitySounds.ASCENDANT_ACTIVATE.get());
            NetMessages.sendToServer(new FlyInfinite());
            DelayRunnable.addRunTask(160, () -> player.playSound(CalamitySounds.ASCENDANT_OFF.get()));
        }
    }),

    SPECTRAL_TELEPORT(new FunctionKey("spectral_teleport",
        KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F) {
        @Override
        void onKeyDown() {
            NetMessages.sendToServer(new SpectralTeleport());
        }
    }),

    SPRINTING(new FunctionKey("sprinting",
        KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R) {
        @Override
        void onKeyDown() {
            NetMessages.sendToServer(new ApplySprint());
        }
    });

    public static final String KEY_GROUP = "key.category.calamity_curios";
    public static final String KEY_DESCRIPTION = "key.description.calamity_curios";
    private static final Set<ClientInteraction> KEYS = EnumSet.noneOf(ClientInteraction.class);
    private final FunctionKey key;
    ClientInteraction(FunctionKey key) {
        this.key = key;
    }

    public static void build(RegisterKeyMappingsEvent event) {
        ClientInteraction[] keys = ClientInteraction.values();
        for (ClientInteraction key : keys)
            event.register(key.key);
        mc = Minecraft.getInstance();
        mouse = mc.mouseHandler;
    }

    public static void applyOrDelete(String type, boolean isApply) {
        ClientInteraction key = ClientInteraction.valueOf(type);

        if (isApply) KEYS.add(key);
        else KEYS.remove(key);
    }

    public static void clear() {
        KEYS.clear();
    }

    public static void onKeyDown() {
        if (!KEYS.isEmpty()) {
            KEYS.stream()
                .filter(ClientInteraction::click)
                .findFirst()
                .ifPresent(ClientInteraction::down);
        }
    }

    public void down() {
        key.onKeyDown();
    }

    public boolean click() {
        return key.consumeClick();
    }

    private static int pressDuration;
    private static boolean isLongPressActive;
    private static Minecraft mc;
    private static MouseHandler mouse;

    public static boolean isLongPressActive() {
        return isLongPressActive;
    }

    public static void longPressToCheck() {
        if (!mouse.isLeftPressed()) {
            reSet();
            return;
        }

        LocalPlayer player = mc.player;
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

    public static void cancelResponse(InputEvent.InteractionKeyMappingTriggered event) {
        if ((isLongPressActive && !event.isPickBlock()) || mc.player.calamity$IsFreeze) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }
}

@OnlyIn(Dist.CLIENT)
abstract class FunctionKey extends KeyMapping {
    public FunctionKey(String description, IKeyConflictContext context, final InputConstants.Type type, final int code) {
        super(ClientInteraction.KEY_DESCRIPTION + "." + description, context, type, code, ClientInteraction.KEY_GROUP);
    }

    abstract void onKeyDown();
}
