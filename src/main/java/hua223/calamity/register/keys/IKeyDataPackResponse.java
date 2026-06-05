package hua223.calamity.register.keys;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.packets.ApplyKeyEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;


/**
 * Key Data Pack Response Interface, Only In Client Send To Server Side
 * <p>
 * A callback interface for handling server-side data pack responses related to curios key abilities.
 * Classes implementing this interface can process response data from the server and manage
 * client-side key mapping interactions for curios items.
 * <p>
 * This interface provides methods for:
 * <ul>
 *   <li>Handling server responses through {@link #onServerResponse(ServerPlayer, CompoundTag)}</li>
 *   <li>Registering client-side key mappings via {@link #registerResponseKeyMapping()}</li>
 *   <li>Managing key codes and cooldown states</li>
 *   <li>Serializing and deserializing key ability data</li>
 * </ul>
 */
public interface IKeyDataPackResponse {
    /**
     * Server response callback method
     * <p>
     * Called when a server-side data pack response is received,
     * used to handle server response logic.
     *
     * @param player The server player that triggered the response, must not be null
     */
    void onServerResponse(ServerPlayer player, CompoundTag tag);

    /**
     * Registers the key mapping response handler for client-side interaction
     * <p>
     * This method creates a binding between the current data pack response instance
     * and its corresponding key mapping, enabling players to trigger server-side
     * actions through key presses on the client side.
     * <p>
     * Only executes in client environment to prevent server-side registration.
     */
    default void registerResponseKeyMapping() {
        if (FMLEnvironment.dist.isClient()) ClientInteraction.createCuriosKey(this);
    }

    /**
     * Gets the key code associated with this data pack response
     * <p>
     * Returns the numeric key code that represents the keyboard key
     * bound to this curios ability. This method is client-side only
     * as key handling is exclusively performed on the client.
     *
     * @return The key code value representing the bound keyboard key
     */
    @OnlyIn(Dist.CLIENT)
    int getKeyCode();


    /**
     * Detects whether the key ability is currently in cooling state
     * <p>
     * Checks if the curios ability associated with this key is on cooldown.
     * This method is client-side only and used to determine if the ability
     * can be triggered.
     *
     * @return true if the ability is in cooling state, false otherwise
     */

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    default boolean notInCooling(Minecraft minecraft) {
        return !minecraft.player.getCooldowns().isOnCooldown((Item) this);
    }


    /**
     * Checks whether the key ability can be accepted and triggered
     * <p>
     * Validates if the current client environment and game state allow
     * this curios key ability to be activated. This method is client-side
     * only and performs pre-condition checks before key event processing.
     *
     * @param minecraft The Minecraft client instance, must not be null
     * @return true if the key ability can be accepted, false otherwise
     */
    @OnlyIn(Dist.CLIENT)
    default boolean accept(Minecraft minecraft) {
        return true;
    }

    /**
     * Sets the key mapping state for the specified player
     * <p>
     * Sends a network packet to the client to enable or disable
     * the key mapping association for this curios ability.
     *
     * @param player The server player to whom the key mapping state will be sent, must not be null
     * @param enable true to enable the key mapping, false to disable it
     */
    default void setKeyMapping(ServerPlayer player, boolean enable) {
        NetMessages.sendToClient(new ApplyKeyEvent(this, enable), player);
    }

    /**
     * Gets the serialization stream for this key data pack response
     * <p>
     * Returns a CompoundTag containing the serialized data of this
     * curios key ability state. This data can be used for persistence
     * or network transmission.
     *
     * @return A CompoundTag containing the serialized data, or null if no data needs to be serialized
     */
    @OnlyIn(Dist.CLIENT)
    default CompoundTag getSerializationStream() {
        return null;
    }
}
