package hua223.calamity.mixed;

public interface ISelfCast<T> {
    default T cast() {
        return (T) this;
    }
}
