package ru.itmo.java;

import java.util.Objects;

public class HashTable {
    static int HASH_INTERVAL = 13;
    static final int DEFAULT_INITIAL_CAPACITY = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.5f;
    static final int MAXIMUM_CAPACITY = 1 << 30;

    static class Entry {
        private Object key;
        private Object value;
        private boolean isReusable;

        Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
            this.isReusable = false;
        }

        public final Object getKey() {
            return key;
        }

        public final Object getValue() {
            return value;
        }

        public boolean isReusable() {
            return isReusable;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public void makeReusable() {
            key = null;
            value = null;
            isReusable = true;
        }
    }

    private int size;
    private int capacity;
    private float loadFactor;
    private int threshold;
    private Entry[] table;

    public HashTable() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public HashTable(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public HashTable(int initialCapacity, float loadFactor) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }

        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }

        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }

        this.size = 0;
        this.capacity = initialCapacity;
        this.loadFactor = loadFactor;
        this.threshold = (int) (capacity * loadFactor);
        this.table = new Entry[initialCapacity];
    }

    private int posMod(int x, int y) {
        return Math.floorMod(x, y);
    }

    Object put(Object key, Object value) {
        int hash = Objects.hashCode(key);
        int reusableBucketIndex = -1;

        int i = 0;
        int hashIndex = posMod(hash + HASH_INTERVAL * i, capacity);
        while (table[hashIndex] != null) {
            if (i > capacity) {
                throw new RuntimeException("Infinite collisions or no space to put one more entry");
            }

            if (reusableBucketIndex == -1 && table[hashIndex].isReusable()) {
                reusableBucketIndex = hashIndex;
            }

            if (Objects.equals(table[hashIndex].getKey(), key)) {
                Object oldValue = table[hashIndex].getValue();
                table[hashIndex].setValue(value);
                return oldValue;
            }

            i++;
            hashIndex = posMod(hash + HASH_INTERVAL * i, capacity);
        }

        int putBucketIndex = (reusableBucketIndex == -1) ? hashIndex
                                                         : reusableBucketIndex;
        table[putBucketIndex] = new Entry(key, value);

        if (++size > threshold) {
            resize();
        }

        return null;
    }

    Object get(Object key) {
        int hash = Objects.hashCode(key);

        int i = 0;
        int hashIndex = posMod(hash + HASH_INTERVAL * i, capacity);
        while (table[hashIndex] != null) {
            if (i > capacity) {
                return null;
            }

            if (Objects.equals(table[hashIndex].getKey(), key)) {
                return table[hashIndex].getValue();
            }

            i++;
            hashIndex = posMod(hash + HASH_INTERVAL * i, capacity);
        }

        return null;
    }

    Object remove(Object key) {
        int hash = Objects.hashCode(key);

        int i = 0;
        int hashIndex = posMod(hash + HASH_INTERVAL * i, capacity);
        while (table[hashIndex] != null) {
            if (i > capacity) {
                return null;
            }

            if (Objects.equals(table[hashIndex].getKey(), key)) {
                Object oldValue = table[hashIndex].getValue();
                table[hashIndex].makeReusable();
                --size;
                return oldValue;
            }

            i++;
            hashIndex = posMod(hash + HASH_INTERVAL * i, capacity);
        }

        return null;
    }

    int size() {
        return size;
    }

    private void resize() {
        int oldCapacity = capacity;
        Entry[] oldTable = table;

        int newCapacity = oldCapacity * 2;
        int newThreshold = (int) (capacity * loadFactor);
        Entry[] newTable = new Entry[newCapacity];

        capacity = newCapacity;
        threshold = newThreshold;
        table = newTable;
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Entry entry = oldTable[i];
            oldTable[i] = null;

            if (entry != null && !entry.isReusable()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }
}
