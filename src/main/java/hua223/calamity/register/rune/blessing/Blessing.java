package hua223.calamity.register.rune.blessing;

import hua223.calamity.events.EventTypes;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.register.rune.ArmorRune;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Blessing extends ArmorRune {
    public Blessing() {
        super(RegisterList.CALAMITY_ONE);
    }

    protected void addVoidTouched(ServerPlayer player, float value) {
        VoidTouched touched = (VoidTouched) CalamityItems.VOID_TOUCHED.get();
        float[] data = player.Calamity$Player.getRuneData(touched);
        data[0] += value;

        if (value > 0) {
            if ((data[0] -= value) == 0.01f) EventTypes.applyEvent(touched, player, true);
        } else if (data[0] < 0.01f) EventTypes.applyEvent(touched, player, false);
    }

    @OnlyIn(Dist.CLIENT)
    protected final void displayVoidTouchedValue(List<Component> components, float value) {
        displayAsAPercentage("void_touched", components, value);
    }

    @Override
    public @NotNull String getDescriptionId() {
        return "item.calamity_curios.blessing";
    }
}
