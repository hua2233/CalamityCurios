package hua223.calamity.register.items;

import hua223.calamity.register.RegisterList;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

//Developer's Prayer
public class BrimstoneCrescentStaff extends StaffItem implements IPresetSpellContainer {
    public BrimstoneCrescentStaff() {
        super(RegisterList.ITEM_CALAMITY, new StaffTier(22.0F, -3F,
            new AttributeContainer(AttributeRegistry.BLOOD_SPELL_POWER, 0.7, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.SUMMON_DAMAGE, 0.2, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(AttributeRegistry.SPELL_POWER, 0.12, AttributeModifier.Operation.MULTIPLY_BASE)));
    }

    @Override
    public void initializeSpellContainer(ItemStack itemStack) {
        if (!ISpellContainer.isSpellContainer(itemStack)) {
            ISpellContainerMutable spellContainer = ISpellContainer.create(1, true, false).mutableCopy();
            spellContainer.addSpell(RegisterList.CRESCENT_STANCE.get(), 1, false);
            ISpellContainer.set(itemStack, spellContainer.toImmutable());
        }
    }
}
