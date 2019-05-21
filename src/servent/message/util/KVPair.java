package servent.message.util;

public class KVPair<K, V> {
    private K key;
    private V value;

    public KVPair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "KVPair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
