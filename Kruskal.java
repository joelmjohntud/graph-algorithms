
// Kruskal's Minimum Spanning Tree algorithm

import java.io.*;
import java.util.Scanner;

class Kruskal {

    //stores one undirected weighted edge
    //u and v are the two end point vertex
    //wgt = edge weight
    class Edge {
        public int u, v, wgt;
    }

    //V = number of vertices
    //E = number of edges
    //edges = array holding all edges read
    private int V, E;
    private Edge[] edges;

    // Union-Find arrays
    private int[] parent;
    private int[] rank;


    // Constructor
    // Automatically detects plain or DIMACS format.
    public Kruskal(String graphFile) throws IOException
    {
        int u, v, wgt;
        boolean dimacs = false;   //flags if DIMACS format

        FileReader     fr     = new FileReader(graphFile);
        BufferedReader reader = new BufferedReader(fr);

        String   splits = " +";   //split on one or more spaces
        String   line   = reader.readLine();
        String[] parts;

        //scaning the header to find V and E
        while(true)
        {
            if(line == null) break;
            line = line.trim();
            if(line.isEmpty()) {
                line = reader.readLine(); continue;
            }
            if(line.startsWith("c")) {
                dimacs = true;
                line = reader.readLine(); continue;
            }
            if(line.startsWith("p")) {
                //"p sp V E"
                parts = line.split(splits);
                V = Integer.parseInt(parts[2]);
                E = Integer.parseInt(parts[3]);
                break;
            }
            //plain "V E"
            parts = line.split(splits);
            V = Integer.parseInt(parts[0]);
            E = Integer.parseInt(parts[1]);
            break;
        }

        System.out.println("Parts[] = " + V + " " + E
            + (dimacs ? " [DIMACS format]" : " [plain format]"));

        //allocating one slot per edge
        edges = new Edge[E];

        //reading all edges into the array
        System.out.println("Reading edges from text file");
        int count = 0;
        while(count < E && (line = reader.readLine()) != null)
        {
            line = line.trim();
            //skiping blank lines and comment lines
            if(line.isEmpty() || line.startsWith("c")) continue;

            parts = line.split(splits);

            if(dimacs) {
                // DIMACS edge line "a u v weight"
                if(!line.startsWith("a")) continue;
                u   = Integer.parseInt(parts[1]);
                v   = Integer.parseInt(parts[2]);
                wgt = Integer.parseInt(parts[3]);
            } else {
                // plain "u v weight"
                u   = Integer.parseInt(parts[0]);
                v   = Integer.parseInt(parts[1]);
                wgt = Integer.parseInt(parts[2]);
            }

            System.out.println("Edge " + toChar(u) + "--(" + wgt + ")--" + toChar(v));

            //storing  the edge in the array
            edges[count]     = new Edge();
            edges[count].u   = u;
            edges[count].v   = v;
            edges[count].wgt = wgt;
            count++;
        }
        reader.close();
    }


    //convert a vertex number to a letter
    private char toChar(int u)
    {
        return (char)(u + 64);
    }


    //initialise Union-Find
    private void makeSet()
    {
        parent = new int[V+1];
        rank   = new int[V+1];
        for(int v = 1; v <= V; ++v) {
            parent[v] = v;   //each vertex is its own root
            rank[v]   = 0;   //all trees start at height 0
        }
    }


    //find the root of the set containing vertex x
    private int find(int x)
    {
        if(parent[x] != x)
            //x is not the root then recurse up and compress the path
            parent[x] = find(parent[x]);

        return parent[x];   //return the root of x set
    }



    //merge the two sets containing vertices a and b
    private void union(int a, int b)
    {
        int ra = find(a);   //root of a component
        int rb = find(b);   //root of b component

        if(ra == rb) return;   //already in the same set

        //attaching the lower-rank tree under the higher-rank tree
        if(rank[ra] < rank[rb])
            parent[ra] = rb;          //ra tree goes under rb
        else if(rank[ra] > rank[rb])
            parent[rb] = ra;          //rb tree goes under ra
        else {
            parent[rb] = ra;          //same rank
            rank[ra]++;               //ra is now one level taller
        }
    }



    // sort edges by weight ascending
    // Uses the standard recursive QuickSort algorithm.
    // Average case O(E log E), worst case O(E^2) on sorted input
    private void quickSort(int lo, int hi)
    {
        if(lo >= hi) return;           //base case: subarray of size 0 or 1
        int p = partition(lo, hi);     //partition around a pivot
        quickSort(lo, p - 1);          //sort left half
        quickSort(p + 1, hi);          //sort right half
    }


    //helper for quickSort
    private int partition(int lo, int hi)
    {
        int  pivot = edges[hi].wgt;   //pivot = weight of last edge
        int  i     = lo - 1;          //i tracks the boundary of the left
        Edge tmp;

        for(int j = lo; j < hi; ++j)
        {
            if(edges[j].wgt <= pivot)
            {
                ++i;
                //swap edges[i] and edges[j] thus moving the small edge into the left
                tmp = edges[i]; edges[i] = edges[j]; edges[j] = tmp;
            }
        }
        //placing the pivot in its correct sorted position i+1
        tmp = edges[i+1]; edges[i+1] = edges[hi]; edges[hi] = tmp;
        return i + 1;
    }


    // main Kruskal algorithm

    public void MST_Kruskal()
    {
        makeSet();           //initialise Union-Find make each vertex its own set
        quickSort(0, E-1);  //sorting all edges by weight ascending order

        System.out.println("\nMST_Kruskal");
        System.out.println("Sorted edges:");
        for(Edge e : edges)
            System.out.println("  " + toChar(e.u) + " --(" + e.wgt + ")-- " + toChar(e.v));

        System.out.println("\nStep-by-step:");

        int mstWgt = 0;    //running total of MST edge weights
        int step   = 0;    //step counter for display
        int added  = 0;    //number of edges added to MST so far

        for(Edge e : edges)
        {
            //stoping as soon as it has a complete spanning tree
            if(added == V - 1) break;
            step++;

            //finding the root of each end points component
            int pu = find(e.u);
            int pv = find(e.v);

            System.out.println("\nStep " + step + ": "
                + toChar(e.u) + " --(" + e.wgt + ")-- " + toChar(e.v)
                + "   find(" + toChar(e.u) + ")=" + toChar(pu)
                + "  find(" + toChar(e.v) + ")=" + toChar(pv));

            if(pu != pv)
            {
                //different roots shows end points are in different components
                union(e.u, e.v);
                mstWgt += e.wgt;
                added++;
                System.out.println("  --> ADDED  (MST weight so far = " + mstWgt + ")");
            }
            else
            {
                //same root which means end points are already connected
                //adding this edge would create a cycle
                System.out.println("  --> REJECTED (same component, would form a cycle)");
            }
        }

        System.out.println("\n\nTotal MST Weight = " + mstWgt);
    }


    //Prompts the user to type the graph filename.
    public static void main(String[] args) throws IOException
    {
        Scanner sc = new Scanner(System.in);

        //prompting the user to enter the graph file name
        System.out.print("Enter graph filename: ");
        String fname = sc.nextLine().trim();

        Kruskal k = new Kruskal(fname);
        k.MST_Kruskal();
    }
}
