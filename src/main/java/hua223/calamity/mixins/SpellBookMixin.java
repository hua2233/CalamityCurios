package hua223.calamity.mixins;

import hua223.calamity.register.RegisterList;
import io.redspace.ironsspellbooks.item.SpellBook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SpellBook.class)
public class SpellBookMixin {
    @Redirect(method = "<init>(ILio/redspace/ironsspellbooks/api/spells/SpellRarity;)V", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/world/item/Item$Properties;rarity(Lnet/minecraft/world/item/Rarity;)Lnet/minecraft/world/item/Item$Properties;"))
    private static Item.Properties reSetProperties(Item.Properties instance, Rarity rarity) {
        Item.Properties properties = RegisterList.getUniqueSettings();
        return properties == null ? instance.rarity(rarity) : properties;
    }
}
