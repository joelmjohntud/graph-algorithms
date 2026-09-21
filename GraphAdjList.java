/*
Weighted undirected graph using Adjacency Linked Lists

contains three algorithms:
Depth First Traversal  (DF)
Breadth First Traversal (BF)
Prim's Minimum Spanning Tree (MST_Prim)

Reads two file formats:
Plain text: "V E" on line 1, then "u v weight" per edge
DIMACS .gr: comment lines 'c', problem line 'p sp V E',
edge lines 'a u v weight'
*/

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

class GraphAdjList {

    /*
    Node — one entry in an adjacency linked list
    Each node stores the neighbour vertex, the edge weight,
    and a pointer to the next node in the list
    */
    class Node {
        public int vert;   // the neighbour vertex number
        public int wgt;    // weight of the edge to that neighbour
        public Node next;  // next node in this vertex's linked list
    }

    /*
    V = number of vertices
    E = number of edges
    adj[] = array of adjacency list head
    */
    private int V, E;
    private Node[] adj;

    // sentinel node/ end of evrey list
    private Node z;

    //checks whether a vertex has been seen
    // 0 = not visited,  more than 0 = visit order number
    private int[] visited;

    //BF shortes distance from the source
    private int[] dist;

    //which vertex we came from
    private int[] parent;

    //d = discovery time for each vertex (DF)
    //f = finish time for each vertex
    //vertex is discovered when we first visit it (Grey)
    //vertex is finished when all its neighboursare explored (Black)
    private int[] d, f;

    //id = visit order counter for each each time we visit a new vertex
    //time = time counter that increments at discovery AND at finish
    private int id;
    private int time;


    /*
    Constructor
    Reads a graph from a text file and builds the adjacency list.
    detects the file format:
    wGraph1.txt
    First line  V E
    Each next line  u v weight
    for the read world example which i used USA-road-d.NY.gr
    DIMACS .gr format
    Lines starting with c are comments and to skip them
    Line starting with p gives p sp V E
    Lines starting with a give a u v weight
    */
    public GraphAdjList(String graphFile) throws IOException
    {
        int u, v;
        int e, wgt;
        Node t;
        boolean dimacs = false;   //flag for checking if its dimacs format or not

        FileReader fr     = new FileReader(graphFile);
        BufferedReader reader = new BufferedReader(fr);

        String splits = " +";    //split on one or more spaces
        String line   = reader.readLine();
        String[] parts;

        /*
        scan the header to find V and E
        We loop until we find the line that gives us V and E
        For plain files this is the very first line.
        For DIMACS files we skip c comment lines first
        */
        while(true)
        {
            if(line == null) break;// end of file
            line = line.trim();
            if(line.isEmpty()) {
                line = reader.readLine(); continue;
            }
            if(line.startsWith("c")) {
                dimacs = true;
                line = reader.readLine(); continue;
            }
            if(line.startsWith("p")) {// DIMACS problem line
                //"p sp V E"
                parts = line.split(splits);
                V = Integer.parseInt(parts[2]);
                E = Integer.parseInt(parts[3]);
                break;
            }
            //"V E"
            parts = line.split(splits);
            V = Integer.parseInt(parts[0]);
            E = Integer.parseInt(parts[1]);
            break;
        }

        System.out.println("Parts[] = " + V + " " + E
            + (dimacs ? " [DIMACS format]" : " [plain format]"));

        //sentinel node
        z      = new Node();
        z.next = z;     //z points to itself


        adj     = new Node[V+1];  // one list head per v
        visited = new int[V+1];   //flags
        dist    = new int[V+1];   //BF distance
        parent  = new int[V+1];   //tree parents

        d       = new int[V+1];   // DF discovery times
        f       = new int[V+1];   // DF finish times

        // initialise every adjacency list to z
        for(v = 1; v <= V; ++v)
            adj[v] = z;

        //read all edges
        System.out.println("Reading edges from text file");
        int count = 0;
        while(count < E && (line = reader.readLine()) != null)
        {
            line = line.trim();
            //skip blank lines and comment lines
            if(line.isEmpty() || line.startsWith("c")) continue;

            parts = line.split(splits);

            if(dimacs) {
                //DIMACS "a u v weight"
                if(!line.startsWith("a")) continue;
                u   = Integer.parseInt(parts[1]);
                v   = Integer.parseInt(parts[2]);
                wgt = Integer.parseInt(parts[3]);
            } else {
                //plain "u v weight"
                u   = Integer.parseInt(parts[0]);
                v   = Integer.parseInt(parts[1]);
                wgt = Integer.parseInt(parts[2]);
            }

            System.out.println("Edge " + toChar(u) + "--(" + wgt + ")--" + toChar(v));


            t = new Node();
            t.vert = v;  t.wgt = wgt;
            t.next = adj[u];   //new node --> old head
            adj[u] = t;        //new node = new head


            t = new Node();
            t.vert = u;  t.wgt = wgt;
            t.next = adj[v];
            adj[v] = t;

            count++;
        }
        reader.close();
    }


    //converts a vertex number to a letter
    private char toChar(int u)
    {
        return (char)(u + 64);
    }


    //display prints the full adjacency list
    //Shows each vertex and the chain of neighbours it connects to
    public void display()
    {
        int v;
        Node n;

        for(v = 1; v <= V; ++v) {
            System.out.print("\nadj[" + toChar(v) + "] ->");
            for(n = adj[v]; n != z; n = n.next)
                System.out.print(" |" + toChar(n.vert) + " | " + n.wgt + "| ->");
        }
        System.out.println("");
    }


    /*
    Depth First Traversal
    */
    public void DF(int s)
    {
        //resets counters and arrays
        id   = 0;
        time = 0;
        for(int v = 1; v <= V; ++v) {
            visited[v] = 0;   // not visited (White)
            parent[v]  = 0;   // means no parent
        }

        System.out.println("\nDepth First Graph Traversal");
        System.out.println("Starting with Vertex " + toChar(s));

        //start DFS from s
        dfVisit(0, s);

        //handle disconnected graphs
        //visit it from scratchs
        for(int v = 1; v <= V; ++v)
            if(visited[v] == 0)
                dfVisit(0, v);

        //prints the summary table of discovery/finish times
        System.out.println("\n\nVertex   Discovery  Finish  Parent");
        for(int v = 1; v <= V; ++v)
            System.out.println("  " + toChar(v) + "          " + d[v]
                + "         " + f[v] + "       " + toChar(parent[v]));

        System.out.print("\n\n");
    }


    /*
    dfVisit is the recursive helper for DF
    //
    // prev vertex it came from
    // v vertex it is currently visiting
    */
    private void dfVisit(int prev, int v)
    {
        Node t;

        visited[v] = ++id;    //mark v as visited which id gives visit order
        d[v]       = ++time;  //discovery time marked

        System.out.print("\n  DF visited vertex " + toChar(v)
            + " on edge " + toChar(prev) + "--" + toChar(v));

        //every neighbour of v
        for(t = adj[v]; t != z; t = t.next)
        {
            if(visited[t.vert] == 0)   //only recurse into unvisited neighbours
            {
                parent[t.vert] = v;
                dfVisit(v, t.vert);
            }
        }

        //all neighbours of v have been explored
        f[v] = ++time;    //marking the finish time
    }



    // BF Breadth First Traversal
    public void BF(int s)
    {
        int v, u;
        Node t;

        id = 0;   //reset visit counter

        //initialising all vertices as unvisited
        for(v = 1; v <= V; ++v) {
            visited[v] = 0;    //not seen
            dist[v]    = -1;   //-1 = not reached
            parent[v]  = 0;    // no parent
        }

        System.out.print("\nBreadth First Graph Traversal\n");
        System.out.println("Starting with Vertex " + toChar(s));

        //using  a Queue to explore level by level
        Queue<Integer> queue = new LinkedList<Integer>();

        //nqueueing the source vertex
        visited[s] = ++id;
        dist[s]    = 0;
        queue.add(s);

        while(!queue.isEmpty())
        {
            //dequeueing the next vertex to process
            v = queue.poll();

            System.out.print("\n  BF visited vertex " + toChar(v)
                + " on edge " + toChar(parent[v]) + "--" + toChar(v));

            //checking every neighbour of v
            for(t = adj[v]; t != z; t = t.next)
            {
                u = t.vert;

                if(visited[u] == 0)    //only processing unvisited neighbours
                {
                    visited[u] = ++id;          //discovered
                    dist[u]    = dist[v] + 1;   //one move further than v
                    parent[u]  = v;             // recording the tree edge
                    queue.add(u);               //enqueue for later
                }
            }
        }

        //printing the distance summary table
        System.out.println("\n\nVertex   Distance-from-" + toChar(s) + "   Parent");
        for(v = 1; v <= V; ++v)
            System.out.println("  " + toChar(v) + "              "
                + dist[v] + "             " + toChar(parent[v]));

        System.out.print("\n\n");
    }



    // MST_Prim (Prim's Minimum Spanning Tree algorithm)
    public void MST_Prim(int s)
    {
        int v, u, key, h;
        int[] mstParent = new int[V+1];   //MST parent of each vertex
        int[] hPos      = new int[V+1];   // position of vertex v in heap
        int[] dist2     = new int[V+1];   //current cheapest/smallest edge cost to v

        //initialising
        for(v = 0; v <= V; ++v) {
            dist2[v]     = Integer.MAX_VALUE;   //finity = not yet reachable
            mstParent[v] = 0;                   //no parent
            hPos[v]      = 0;                   //not positioned in heap yet
        }

        //the heap array holds vertex numbers
        //heap[1] is the vertex with the smallest distance
        int[] heap = new int[V+1];

        //starting point of the MST
        dist2[s] = 0;

        //load all vertices into the heap
        for(v = 1; v <= V; ++v)
            heap[v] = v;
        h = V;   //current heap size

        //rearrange so the vertex with smallest distance is at heap[1]
        //down from the middle downward
        for(v = h/2; v >= 1; --v)
            siftDown(heap, hPos, dist2, v, h);

        //recording the actual heap positions in hPos[] after rearranging
        for(v = 1; v <= V; ++v)
            hPos[heap[v]] = v;

        System.out.println("\nMST_Prim starting from vertex " + toChar(s));
        System.out.printf("%-8s %-8s %-8s%n", "Vertex", "Key", "Parent");

        int mstWgt = 0;   //running total of MST edge weights

        while(h > 0)
        {
            //the smallest cost
            u = heap[1];

            //record edge weight
            int edgeWgt = (u == s) ? 0 : dist2[u];
            mstWgt += edgeWgt;

            System.out.printf("  %c       %d       %c%n",
                toChar(u),
                edgeWgt,
                mstParent[u] == 0 ? '@' : toChar(mstParent[u]));

            //mark u as done
            dist2[u] = 0;

            //remove u from the heap:
            //replace with the last element and move it down
            heap[1]       = heap[h--];      //moving last element to root
            hPos[heap[1]] = 1;              //updating its position
            if(h > 0) siftDown(heap, hPos, dist2, 1, h);

            Node t;
            for(t = adj[u]; t != z; t = t.next)
            {
                v   = t.vert;    //neighbour vertex
                key = t.wgt;     //edge weight u->v

                //if edge is cheaper than v current best cost
                //and v is still in the heap
                if(key < dist2[v] && hPos[v] > 0)
                {
                    dist2[v]     = key;   // update v best cost
                    mstParent[v] = u;     // u is now v MST parent
                    //moving v up in the heap because its key decreased
                    siftUp(heap, hPos, dist2, hPos[v]);
                }
            }
        }

        //printing the final MST edge list
        System.out.println("\n MST edges:");
        for(v = 1; v <= V; ++v) {
            if(mstParent[v] != 0)
                System.out.println("  " + toChar(mstParent[v]) + " - " + toChar(v));
        }
        System.out.println("Total MST Weight = " + mstWgt);
    }



    //restore heap order by pushing an element down
    //k = position to start sifting from
    //n = current size of the heap
    private void siftDown(int[] heap, int[] hPos, int[] key, int k, int n)
    {
        int v = heap[k];   //vertex being sifted down
        int j;

        while(k <= n/2)    //stop when k has no children
        {
            j = 2 * k;     //j = left child of k

            //if right child exists and has a smaller key, use right child
            if(j < n && key[heap[j]] > key[heap[j+1]]) ++j;

            //if v key is already less than or equal to the smallest child, heap order is okay
            if(key[v] <= key[heap[j]]) break;

            //swap k with its smallest child and continue downward
            heap[k]       = heap[j];
            hPos[heap[k]] = k;    //accurate after the swap
            k = j;
        }
        //place v in final position
        heap[k] = v;
        hPos[v] = k;
    }



    //restores heap order by pushing an element up
    //k = position of the element to sift up
    private void siftUp(int[] heap, int[] hPos, int[] key, int k)
    {
        int v = heap[k];   //vertex being sifted up

        //while k is not the root && parent has a larger key
        while(k > 1 && key[heap[k/2]] > key[v])
        {
            heap[k]       = heap[k/2];   //move parent down to k
            hPos[heap[k]] = k;           //update for the moved vertex
            k             = k/2;         //move up to parent position
        }
        //place v in final position
        heap[k] = v;
        hPos[v] = k;
    }



    // Prompts the user to type the graph filename and start vertex
    public static void main(String[] args) throws IOException
    {
        Scanner sc = new Scanner(System.in);

        //prompting the user to enter the graph file name
        System.out.print("Enter graph filename: ");
        String fname = sc.nextLine().trim();

        //prompting the user to enter the starting vertex number
        System.out.print("Enter starting vertex number (12 for L): ");
        int s = sc.nextInt();

        // start timing and memory tracking
        long startTime  = System.currentTimeMillis();
        Runtime rt      = Runtime.getRuntime();
        rt.gc();   //garbage collect first so reading is clean
        long usedBefore = rt.totalMemory() - rt.freeMemory();

        GraphAdjList g = new GraphAdjList(fname);

        g.display();      //printing the adjacency list representation
        g.DF(s);          //running Depth First traversal
        g.BF(s);          //running Breadth First traversal
        g.MST_Prim(s);    //running Prim's MST

        //stop timing and print results
        long timeTaken = System.currentTimeMillis() - startTime;
        long memUsedMB = (rt.totalMemory() - rt.freeMemory() - usedBefore) / (1024 * 1024);

        System.out.println("==========================================");
        System.out.println("Time taken:  " + timeTaken + " ms");
        System.out.println("Memory used: " + memUsedMB + " MB");
        System.out.println("==========================================");
    }
}
