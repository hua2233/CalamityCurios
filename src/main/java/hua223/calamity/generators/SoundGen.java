package hua223.calamity.generators;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.sounds.CalamitySounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

import static hua223.calamity.main.CalamityCurios.MODID;

//I hate repetitive work
public class SoundGen extends SoundDefinitionsProvider {
    public SoundGen(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MODID, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        final StringBuilder builder = new StringBuilder();

        for (CalamitySounds value : CalamitySounds.values()) {
            ResourceLocation location = value.get().getLocation();
            String path = location.getPath();
            SoundDefinition definition = SoundDefinition.definition().subtitle(value.getLocationLang());

            if (path.charAt(path.length() - 2) == '-') {
                builder.append(path, 0, path.length() - 1);
                for (int i = 0; i <= path.charAt(path.length() - 1) - 48; i++)
                    definition.with(SoundDefinition.Sound.sound(CalamityCurios.ModResource(
                        builder.delete(builder.length() - 1, builder.length()).append(i).toString()), SoundDefinition.SoundType.SOUND).stream());
            } else definition.with(SoundDefinition.Sound.sound(location, SoundDefinition.SoundType.SOUND).stream());

            add(location, definition);
        }
    }
}
