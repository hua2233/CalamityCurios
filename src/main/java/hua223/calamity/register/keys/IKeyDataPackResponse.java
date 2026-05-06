package hua223.calamity.util;

import net.minecraft.server.level.ServerPlayer;

/**
 * Data Pack Response Interface
 * <p>
 * A callback interface for handling server-side data pack responses.
 * Classes implementing this interface can process response data from the server.
 */
public interface IDataPackResponse {
    /**
     * Server response callback method
     * <p>
     * Called when a server-side data pack response is received,
     * used to handle server response logic.
     *
     * @param player The server player that triggered the response, must not be null
     */
    void onServerResponse(ServerPlayer player);
}
