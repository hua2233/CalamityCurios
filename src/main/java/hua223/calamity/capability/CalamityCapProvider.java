package hua223.calamity.capability;

import hua223.calamity.mixed.ICalamityMagicExpand;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

//我实在不想为每一个能力去创建一个专属而又几乎相同的Provider，尽管这与我的初衷相违背，但确实方便了不少
public final class CalamityCapProvider<Cap extends BaseCap<?>> implements ICapabilitySerializable<CompoundTag> /* Cloneable */ {
//    @Deprecated(since = "The native magic system has been replaced by ISS")
//    public static final CalamityCapProvider<Magic> MAGIC =
//        createTemplate(Magic::new, CapabilityManager.get(new CapabilityToken<>() {}));

    public static final CalamityCapProvider<Adrenaline> ADRENALINE =
        createTemplate(Adrenaline::new, CapabilityManager.get(new CapabilityToken<>() {}));

    public static final CalamityCapProvider<Rage> RAGE =
        createTemplate(Rage::new, CapabilityManager.get(new CapabilityToken<>() {}));

    public static final CalamityCapProvider<CalamityCap> CALAMITY =
        createTemplate(CalamityCap::new, CapabilityManager.get(new CapabilityToken<>() {}));


    private final Supplier<Cap> supplier;
    private final Capability<Cap> capability;
    private final Cap type;
    private final LazyOptional<Cap> optional;

    private CalamityCapProvider(@Nullable Supplier<Cap> supplier, @Nullable Cap type, Capability<Cap> capability) {
        this.supplier = supplier;
        this.type = type;
        this.capability = capability;
        this.optional = type != null ? LazyOptional.of(() -> type) : null;
    }

    public static <T extends BaseCap<?>> CalamityCapProvider<T> createTemplate(Supplier<T> supplier, Capability<T> capability) {
        return new CalamityCapProvider<>(supplier, null, capability);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == capability) return optional.cast();
        return LazyOptional.empty();
    }

    public Capability<Cap> getCapabilityType() {
        return capability;
    }

    public LazyOptional<Cap> getCapabilityFrom(@NotNull ICapabilityProvider holder) {
        return holder.getCapability(capability);
    }

    public static void safetyRunCalamityMagic(@NotNull ICapabilityProvider holder, Consumer<ICalamityMagicExpand> expand) {
        LazyOptional<MagicData> optional = holder.getCapability(PlayerMagicProvider.PLAYER_MAGIC);
        if (optional.isPresent()) expand.accept((ICalamityMagicExpand) optional.orElseGet(null));
    }

    @Override
    @SuppressWarnings("ALL")
    public CalamityCapProvider<Cap> clone() {
        return new CalamityCapProvider<>(null, supplier.get(), capability);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        type.save(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        type.load(tag);
    }
}
