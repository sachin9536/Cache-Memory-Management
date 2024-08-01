package CacheMemoryManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class CacheEntry {
    String key;
    String value;
    long timestamp;
    CacheEntry left;
    CacheEntry right;

    public CacheEntry(String key, String value) {
        this.key = key;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
        this.left = null;
        this.right = null;
    }
}

class SplayTreeCache {
    private int capacity;
    private CacheEntry root;
    private Map<String, CacheEntry> cacheMap;

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

            if (node.left == null) {
                return node;
            } else {
                return rightRotate(node);
            }
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

            if (node.right == null) {
                return node;
            } else {
                return leftRotate(node);
            }
        }
    }

    private CacheEntry find(CacheEntry node, String key) {
        if (node == null || node.key.equals(key)) {
            return node;
        }

        if (key.compareTo(node.key) < 0) {
            if (node.left == null) {
                return node;
            }
            return find(node.left, key);
        } else {
            if (node.right == null) {
                return node;
            }
            return find(node.right, key);
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
        if (node == null) {
            return null;
        }

        while (node.right != null) {
            node = node.right;
        }

        return node;
    }

    public String get(String key) {
        CacheEntry entry = find(root, key);
        if (entry != null && entry.key.equals(key)) {
            entry.timestamp = System.currentTimeMillis();
            root = splay(root, key);
            return entry.value;
        }
        return null;
    }

    public void put(String key, String value) {
        CacheEntry existingEntry = find(root, key);
        if (existingEntry != null && existingEntry.key.equals(key)) {
            existingEntry.value = value;
            existingEntry.timestamp = System.currentTimeMillis();
            root = splay(root, key);
            return;
        }

        if (cacheMap.size() >= capacity) {
            CacheEntry lruEntry = findLeastRecentlyUsed();
            root = erase(root, lruEntry.key);
        }

        root = insert(root, key, value);
        cacheMap.put(key, root);
    }

    public void remove(String key) {
        root = erase(root, key);
    }

    public void printCache() {
        System.out.println("Cache Contents:");
        printCacheHelper(root);
        System.out.println();
    }

    private void printCacheHelper(CacheEntry node) {
        if (node == null) {
            return;
        }

        printCacheHelper(node.left);
        System.out.println("Key: " + node.key + ", Value: " + node.value);
        printCacheHelper(node.right);
    }

    private CacheEntry findLeastRecentlyUsed() {
        CacheEntry[] lruEntry = new CacheEntry[1];
        long[] lruTime = new long[]{System.currentTimeMillis()};
        traverseTree(root, lruEntry, lruTime);
        return lruEntry[0];
    }

    private void traverseTree(CacheEntry node, CacheEntry[] lruEntry, long[] lruTime) {
        if (node == null) {
            return;
        }

        traverseTree(node.left, lruEntry, lruTime);

        if (node.timestamp < lruTime[0]) {
            lruEntry[0] = node;
            lruTime[0] = node.timestamp;
        }

        traverseTree(node.right, lruEntry, lruTime);
    }
}

public class main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the capacity of the cache: ");
        int capacity = scanner.nextInt();

        SplayTreeCache cache = new SplayTreeCache(capacity);

        char choice;
        do {
            System.out.println("\nSelect an option:");
            System.out.println("1. Add an entry");
            System.out.println("2. Retrieve an entry");
            System.out.println("3. Remove an entry");
            System.out.println("4. Print cache contents");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.next().charAt(0);

            switch (choice) {
                case '1': {
                    System.out.print("Enter key: ");
                    String key = scanner.next();
                    System.out.print("Enter value: ");
                    String value = scanner.next();
                    cache.put(key, value);
                    break;
                }
                case '2': {
                    System.out.print("Enter key: ");
                    String key = scanner.next();
                    String value = cache.get(key);
                    if (value != null) {
                        System.out.println("Value: " + value);
                    } else {
                        System.out.println("Entry not found in cache.");
                    }
                    break;
                }
                case '3': {
                    System.out.print("Enter key: ");
                    String key = scanner.next();
                    cache.remove(key);
                    System.out.println("Entry removed from cache.");
                    break;
                }
                case '4':
                    cache.printCache();
                    break;
                case '5':
                    System.out.println("Exiting program.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        } while (choice != '5');

        scanner.close();
    }
}

