package hua223.calamity.render;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;

import java.lang.reflect.Constructor;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public interface IPlayerPostRenderer {

    void render(RenderPlayerEvent.Post event);

    AbstractClientPlayer getOwner();

    default void onStop() {}


    @SuppressWarnings("unchecked")
    default void onlyThirdPersonRender(boolean showRightArm, boolean showLeftArm, boolean showRightItem, boolean showLeftItem) {
        try {
            Class<KeyframeAnimation> animationClass = KeyframeAnimation.class;
            Constructor<KeyframeAnimation> constructor =
                (Constructor<KeyframeAnimation>) animationClass.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            HashMap<?, ?> empty = new HashMap<>();
            KeyframeAnimation emptyKey = constructor.newInstance(0, 9999, 9999, true, 9999, empty, true, false, null, AnimationFormat.UNKNOWN, empty);
            KeyframeAnimationPlayer key = new KeyframeAnimationPlayer(emptyKey);
            key.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
            key.setFirstPersonConfiguration(new FirstPersonConfiguration(showRightArm, showLeftArm, showRightItem, showLeftItem));
            PlayerAnimationAccess.getPlayerAnimLayer(getOwner()).addAnimLayer(18, key);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    default void cancelThirdPersonRendering() {
        PlayerAnimationAccess.getPlayerAnimLayer(getOwner()).removeLayer(18);
    }
}
