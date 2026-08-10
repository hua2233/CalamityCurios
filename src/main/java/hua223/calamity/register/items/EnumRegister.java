package hua223.calamity.register.items;

import net.minecraftforge.registries.RegistryObject;

public interface EnumRegister<E> {//extends Supplier<E>

    RegistryObject<E> getValue();

    default E get() {
        return getValue().get();
    }
}
