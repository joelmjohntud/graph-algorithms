# Graph Algorithms (Java)

This was for my Algorithms & Data Structures module (CMPU2001) at TU Dublin. The task was to implement a graph from scratch and run four classic algorithms on it: DFS, BFS, Prim's MST and Kruskal's MST.

I didn't use any built-in graph or priority queue classes — everything's written from scratch so I actually understood what was happening underneath instead of just calling a library function.

- `GraphAdjList.java` - the graph itself (adjacency list, not a matrix), plus DFS, BFS, and Prim's MST using a binary min-heap I wrote myself
- `Kruskal.java` - Kruskal's MST, using union-find with path compression to check for cycles

I tested both MST algorithms on the same graph and they gave the same total weight (16), which was a good sign they were both actually correct. I also ran them on a real road network dataset (NY, ~264k vertices) just to see how they'd hold up outside a small hand-made example — full details are in the report.

## how to run it

```bash
cd src
javac GraphAdjList.java Kruskal.java

java GraphAdjList
Enter graph filename: ../data/Wgraph1.txt
Enter starting vertex number (12 for L): 12

java Kruskal
Enter graph filename: ../data/Wgraph1.txt
```

Vertices are numbers 1-13 when you type them in but get printed as letters A-M, just to match the diagrams in the report.

Joel Manoj John, TU Dublin BSc Computer Science, CMPU2001
