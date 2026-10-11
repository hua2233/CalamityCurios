package hua223.calamity.mixed;

public interface ISelfCast<T> {

    @SuppressWarnings("unchecked")
    default T cast() {
        return (T) this;
    }
}
