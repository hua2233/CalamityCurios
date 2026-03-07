package hua223.calamity.register.Items;

import net.minecraftforge.registries.RegistryObject;

public interface EnumRegister<E> {//extends Supplier<E>

    RegistryObject<E> getValue();

    default E get() {
        return getValue().get();
    }
}
