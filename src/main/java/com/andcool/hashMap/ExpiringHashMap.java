package com.andcool.hashMap;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ExpiringHashMap<K, V> {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
    private final HashMap<K, Long> timestamps = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public long expirationTimeMillis;

    public ExpiringHashMap(long expirationTimeMillis) {
        this.expirationTimeMillis = expirationTimeMillis;
    }

    public void put(K key, V value) {
        long currentTime = System.currentTimeMillis();
        lock.lock();
        try {
            map.put(key, value);
            timestamps.put(key, currentTime);
        } finally {
            lock.unlock();
        }
    }

    public V get(K key) {
        long currentTime = System.currentTimeMillis();
        lock.lock();
        try {
            Long timestamp = timestamps.get(key);
            if (timestamp == null) {
                return null;
            }
            if (currentTime - timestamp > expirationTimeMillis) {
                map.remove(key);
                timestamps.remove(key);
                return null;
            }
            return map.get(key);
        } finally {
            lock.unlock();
        }
    }

    public void remove(K key) {
        lock.lock();
        try {
            timestamps.remove(key);
            map.remove(key);
        } finally {
            lock.unlock();
        }
    }
}