Cache Memory Management Using Splay Trees
 
This project implements a cache memory management system using Splay Trees as the underlying data structure. The system provides functionalities for adding, retrieving, and removing cache entries. The main.java file offers a basic command-line interface for interacting with the cache. The CacheManagement.java file demonstrates concurrent access to the cache using multiple threads.

Key Features
Splay Tree Implementation: Efficient data structure for cache operations, allowing for fast lookups and updates.
LRU (Least Recently Used) Eviction: Automatically removes the least recently used entry when the cache reaches capacity.
Thread Safety: The CacheManagement.java file incorporates thread safety using locks and condition variables for concurrent access.
Basic Command-Line Interface: The main.java file provides a simple interface for testing and demonstration purposes.
Project Structure
main.java: Contains the core logic for cache operations and a basic command-line interface.
CacheManagement.java: Demonstrates concurrent access to the cache using multiple threads.
How to Run
Compile the Java files:

Bash
javac main.java CacheManagement.java
Use code with caution.

Run the desired class:

Bash
java main  # For basic command-line interface
java CacheManagement  # For concurrent access demonstration
Use code with caution.

Usage
main.java:

Enter the cache capacity when prompted.
Use the menu options to add, retrieve, remove entries, or print the cache contents.
CacheManagement.java:

Demonstrates concurrent access to the cache by creating multiple threads.
Prints the final cache contents after all threads have finished.
