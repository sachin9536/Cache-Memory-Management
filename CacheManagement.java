package CacheMemoryManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

class CacheEntry {
    String key;
    String value;
    long timestamp;
    CacheEntry left;
    CacheEntry right;

    CacheEntry(String key, String value) {
        this.key = key;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }
}

class SplayTreeCache {
    private final int capacity;
    private CacheEntry root;
    private final Map<String, CacheEntry> cacheMap;
    private final Lock splayLock = new ReentrantLock();
    private final Condition operationsCompletedCondition = splayLock.newCondition();
    private boolean operationsCompleted = false;

    public SplayTreeCache(int capacity) {
        this.capacity = capacity;
        this.root = null;
        this.cacheMap = new HashMap<>();
    }

    private CacheEntry rightRotate(CacheEntry x) {
        CacheEntry y = x.left;
        x.left = y.right;
        y.right = x;
        return y;
    }

    private CacheEntry leftRotate(CacheEntry x) {
        CacheEntry y = x.right;
        x.right = y.left;
        y.left = x;
        return y;
    }

    private CacheEntry splay(CacheEntry node, String key) {
        if (node == null || node.key.equals(key)) {
            return node;
        }

        if (key.compareTo(node.key) < 0) {
            if (node.left == null) {
                return node;
            }

            if (key.compareTo(node.left.key) < 0) {
                node.left.left = splay(node.left.left, key);
                node = rightRotate(node);
            } else if (key.compareTo(node.left.key) > 0) {
                node.left.right = splay(node.left.right, key);
                if (node.left.right != null) {
                    node.left = leftRotate(node.left);
                }
            }

            return (node.left == null) ? node : rightRotate(node);
        } else {
            if (node.right == null) {
                return node;
            }

            if (key.compareTo(node.right.key) < 0) {
                node.right.left = splay(node.right.left, key);
                if (node.right.left != null) {
                    node.right = rightRotate(node.right);
                }
            } else if (key.compareTo(node.right.key) > 0) {
                node.right.right = splay(node.right.right, key);
                node = leftRotate(node);
            }

            return (node.right == null) ? node : leftRotate(node);
        }
    }

    private CacheEntry find(CacheEntry node, String key) {
        if (node == null || node.key.equals(key)) {
            return node;
        }

        if (key.compareTo(node.key) < 0) {
            return (node.left == null) ? node : find(node.left, key);
        } else {
            return (node.right == null) ? node : find(node.right, key);
        }
    }

    private CacheEntry insert(CacheEntry node, String key, String value) {
        if (node == null) {
            return new CacheEntry(key, value);
        }

        node = splay(node, key);

        if (node.key.equals(key)) {
            node.value = value;
            node.timestamp = System.currentTimeMillis();
            return node;
        }

        CacheEntry newNode = new CacheEntry(key, value);

        if (key.compareTo(node.key) < 0) {
            newNode.right = node;
            newNode.left = node.left;
            node.left = null;
        } else {
            newNode.left = node;
            newNode.right = node.right;
            node.right = null;
        }

        return newNode;
    }

    private CacheEntry erase(CacheEntry node, String key) {
        if (node == null) {
            return null;
        }

        node = splay(node, key);

        if (!node.key.equals(key)) {
            return node;
        }

        if (node.left == null) {
            root = node.right;
        } else {
            CacheEntry maxLeft = findMax(node.left);
            maxLeft.right = node.right;
            root = maxLeft;
        }

        cacheMap.remove(key);
        return root;
    }

    private CacheEntry findMax(CacheEntry node) {
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    public String get(String key) {
        splayLock.lock();
        try {
            CacheEntry entry = find(root, key);
            if (entry != null && entry.key.equals(key)) {
                entry.timestamp = System.currentTimeMillis();
                root = splay(root, key);
                return entry.value;
            }
            return null;
        } finally {
            splayLock.unlock();
        }
    }

    public void put(String key, String value) {
        splayLock.lock();
        try {
            CacheEntry existingEntry = find(root, key);
            if (existingEntry != null && existingEntry.key.equals(key)) {
                existingEntry.value = value;
                existingEntry.timestamp = System.currentTimeMillis();
                root = splay(root, key);
                return;
            }

            if (cacheMap.size() >= capacity) {
                CacheEntry lruEntry = findLeastRecentlyUsed(root);
                root = erase(root, lruEntry.key);
            }

            root = insert(root, key, value);
            cacheMap.put(key, root);
        } finally {
            splayLock.unlock();
        }
    }

    public void remove(String key) {
        splayLock.lock();
        try {
            root = erase(root, key);
        } finally {
            splayLock.unlock();
        }
    }

    public void printCache() {
        splayLock.lock();
        try {
            System.out.println("Cache Contents:");
            printCacheHelper(root);
            System.out.println();
        } finally {
            splayLock.unlock();
        }
    }

    private void printCacheHelper(CacheEntry node) {
        if (node == null) {
            return;
        }

        printCacheHelper(node.left);
        System.out.println("Key: " + node.key + ", Value: " + node.value);
        printCacheHelper(node.right);
    }

    private CacheEntry findLeastRecentlyUsed(CacheEntry node) {
        if (node == null) {
            return null;
        }

        CacheEntry lruEntry = null;
        long lruTime = System.currentTimeMillis();
        traverseTree(node, lruEntry, lruTime);

        return lruEntry;
    }

    private void traverseTree(CacheEntry node, CacheEntry lruEntry, long lruTime) {
        if (node == null) {
            return;
        }

        traverseTree(node.left, lruEntry, lruTime);

        if (node.timestamp < lruTime) {
            lruEntry = node;
            lruTime = node.timestamp;
        }

        traverseTree(node.right, lruEntry, lruTime);
    }
}

public class CacheManagement {
    public static void main(String[] args) {
        int capacity = 5;
        SplayTreeCache cache = new SplayTreeCache(capacity);

        // Function to add 3 random key-value pairs to the cache
        Runnable addThreeRandomPairs = () -> {
            for (int i = 0; i < 3; ++i) {
                String key = "key" + i;
                String value = "value" + i;

                cache.put(key, value);

                try {
                    Thread.sleep(500); // Simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            cache.splayLock.lock();
            try {
                cache.operationsCompleted = true;
                cache.operationsCompletedCondition.signal(); // Notify that both operations are completed
            } finally {
                cache.splayLock.unlock();
            }
        };

        // Function to add one more random key-value pair to the cache
        Runnable addOneRandomPair = () -> {
            String key = "key3";
            String value = "value3";

            cache.put(key, value);

            cache.splayLock.lock();
            try {
                cache.operationsCompleted = true;
                cache.operationsCompletedCondition.signal(); // Notify that both operations are completed
            } finally {
                cache.splayLock.unlock();
            }
        };

        // Create threads for the specified operations
        Thread thread1 = new Thread(addThreeRandomPairs);
        Thread thread2 = new Thread(addOneRandomPair);

        // Start threads
        thread1.start();
        thread2.start();

        // Wait for both threads to finish
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Wait until both operations are completed
        cache.splayLock.lock();
        try {
            while (!cache.operationsCompleted) {
                cache.operationsCompletedCondition.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            cache.splayLock.unlock();
        }

        // Print cache contents
        cache.printCache();

        System.out.println("Exiting program.");
    }
}
