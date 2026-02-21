import java.util.*;

public class TransitRouteEngin {
    static class Segment{
        String id, from, to;
        int time, cap;
        double fare;
        Segment(String i,String f,String t,int ti,int c,double fa){
            id=i;from=f;to=t;time=ti;cap=c;fare=fa;
        }
    }
    static class Edge {
        String to, segId;
        int time, originalCap;
        double fare;
        int remCap;  // Dynamic remaining capacity
        Edge(String t, String sid, int ti, int oc, double fa, int rc) {
            to = t; segId = sid; time = ti; originalCap = oc; fare = fa; remCap = rc;
        }
    }

    static class Route{
        String id;
        List<String> segs;
        Route(String i, List<String> s){
            id=i;segs=s;
        }
    }
    private static final double INF = Double.MAX_VALUE / 2;
    private static final double FARE_WEIGHT = 10.0;  // $1 equivalent to 10 minutes
    private static final double PENALTY_FACTOR = 1.0;  // Scale for capacity penalty

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of Segments (S), Routes (R), and OD pairs (P):");
        int S = sc.nextInt();
        int R = sc.nextInt();
        int P = sc.nextInt();
        Map<String,Segment> segMap = new HashMap<>();
        Set<String> allNodes = new HashSet<>();  // Collect all unique stops

        System.out.println("\nEnter details for " + S + " segments:");
        System.out.println("(Format: segmentId from to time capacity fare)");
        for(int i=0;i<S;i++){
            String sid=sc.next();
            String from=sc.next();
            String to=sc.next();
            int t=sc.nextInt();
            int c=sc.nextInt();
            double f=sc.nextDouble();
            segMap.put(sid,new Segment(sid,from,to,t,c,f));
            allNodes.add(from);
            allNodes.add(to);
        }
        // Build graph: adjacency list for Dijkstra (allows transfers)
        Map<String, List<Edge>> graph = new HashMap<>();
        Map<String, Integer> remCap = new HashMap<>();  // Remaining capacity per segId
        for (Map.Entry<String, Segment> entry : segMap.entrySet()) {
            Segment seg = entry.getValue();
            String from = seg.from;
            if (!graph.containsKey(from)) {
                graph.put(from, new ArrayList<>());
            }
            graph.get(from).add(new Edge(seg.to, seg.id, seg.time, seg.cap, seg.fare, seg.cap));
            remCap.put(seg.id, seg.cap);  // Initialize remaining cap
        }

        Map<String,Route> routes = new HashMap<>();
        System.out.println("\nEnter details for " + R + " routes:");
        System.out.println("(Format: routeId numOfSegments segmentId1 segmentId2 ...)");
        for(int i=0;i<R;i++){
            String rid = sc.next();
            int k=sc.nextInt();
            List<String> list=new ArrayList<>();
            for(int j=0;j<k;j++){
                list.add(sc.next());
            }
            routes.put(rid,new Route(rid,list));
        }

        List<String[]> ods = new ArrayList<>();
        List<Integer> pax = new ArrayList<>();

        System.out.println("\nEnter " + P + " Origin-Destination pairs:");
        System.out.println("(Format: origin destination passengers)");
        for(int i=0;i<P;i++){
            String o=sc.next();
            String d=sc.next();
            int p=sc.nextInt();
            ods.add(new String[]{o,d});
            pax.add(p);
            allNodes.add(o);  // Ensure OD nodes are in graph
            allNodes.add(d);
        }

        // For each OD, evaluate candidate routes (routes that begin at origin's node and end at destination's node)
        for(int i=0;i<P;i++){
            String origin = ods.get(i)[0];
            String dest = ods.get(i)[1];
            int passengers = pax.get(i);
            System.out.println("\nProcessing OD " + (i + 1) + ": " + origin + " -> " + dest);

            double[] dist = new double[allNodes.size()];  // But use Map for string keys
            Map<String, Double> distances = new HashMap<>();
            Map<String, String> prevNode = new HashMap<>();
            Map<String, String> prevSeg = new HashMap<>();  // To reconstruct segIds
            PriorityQueue<String> pq = new PriorityQueue<>(
                (a, b) -> Double.compare(distances.getOrDefault(a, INF), distances.getOrDefault(b, INF))
            );
            // Initialize
            for (String node : allNodes) {
                distances.put(node, INF);
            }
            distances.put(origin, 0.0);
            pq.offer(origin);
            while (!pq.isEmpty()) {
                String u = pq.poll();
                if (distances.get(u) == INF) continue;
                if (!graph.containsKey(u)) continue;
                for (Edge edge : graph.get(u)) {
                    String v = edge.to;
                    String segId = edge.segId;
                    int rc = remCap.getOrDefault(segId, 0);
                    // Capacity aware cost
                    double edgeCost;
                    if (rc <= 0) {
                        edgeCost = INF;  // Block full segments
                    } else {
                        double penalty = edge.fare * PENALTY_FACTOR * (edge.originalCap / (double) rc);
                        edgeCost = edge.time + (edge.fare * FARE_WEIGHT) + penalty;
                    }
                    double newDist = distances.get(u) + edgeCost;
                    if (newDist < distances.get(v)) {
                        distances.put(v, newDist);
                        prevNode.put(v, u);
                        prevSeg.put(v, segId);
                        pq.offer(v);
                    }
                }
            }
            if (distances.get(dest) == INF) {
                System.out.println("No path found between " + origin + " and " + dest);
                continue;
            }
            // Reconstruct path: list of segIds
            List<String> pathSegs = new ArrayList<>();
            String current = dest;
            while (current != null && !current.equals(origin)) {
                String segId = prevSeg.get(current);
                if (segId != null) {
                    pathSegs.add(0, segId);  // Reverse to get origin-to-dest order
                }
                current = prevNode.get(current);
            }
            if (pathSegs.isEmpty()) {
                System.out.println("No path found");
                continue;
            }
            // Compute metrics from path (using base values, not penalized costs)
            int totalTime = 0;
            double totalFare = 0.0;
            int minRemCap = Integer.MAX_VALUE;
            for (String segId : pathSegs) {
                Segment seg = segMap.get(segId);
                totalTime += seg.time;
                totalFare += seg.fare;
                int rc = remCap.get(segId);
                minRemCap = Math.min(minRemCap, rc);
            }
            totalFare = Math.round(totalFare * 100.0) / 100.0;  // Round to 2 decimals

            // Assign passengers (capacity scaling)
            int assigned = 0;
            if (minRemCap > 0) {
                assigned = Math.min(passengers, minRemCap);
                // Update remaining capacities
                for (String segId : pathSegs) {
                    remCap.put(segId, remCap.get(segId) - assigned);
                }
            }
            // For printing Output
            System.out.print("Chosen route:");
            for (String sid : pathSegs) {
                System.out.print(" " + sid);
            }
            System.out.println();
            System.out.println("Estimated time: " + totalTime + " minutes");
            System.out.println("Average fare: " + totalFare);
            System.out.println("Assigned passengers: " + assigned);
            System.out.println();
        }

        sc.close();

        System.out.println("What this does and improvements:");
        System.out.println("- Builds a graph from segments and uses Dijkstra to find capacity-aware shortest paths (allowing transfers) for OD pairs, combining time, fare, and dynamic penalties for full segments.");
        System.out.println("- Updates remaining capacities after assignments for greedy flow simulation.");
        System.out.println("- Improvements: Use min-cost max-flow for global optimization across all ODs; add transfer penalties (e.g., +5 min at nodes); integrate real-time data (e.g., via APIs); support multi-modal specifics (e.g., walking times).");
        System.out.println("- Add JUnit tests, input validation, BigDecimal for fares, and a web API (e.g., Spring Boot) for visualization.");
    }
}

/*
 input & output->
 Enter number of Segments (S), Routes (R), and OD pairs (P):
3 2 1

Enter details for 3 segments:
(Format: segmentId from to time capacity fare)
A O1 O2 10 100 5.0
B O2 O3 15 50 3.0
C O1 O3 30 200 10.0

Enter details for 2 routes:
(Format: routeId numOfSegments segmentId1 segmentId2 ...)
Route1 2 A B
Route2 1 C

Enter 1 Origin-Destination pairs:
(Format: origin destination passengers)
O1 O3 50

Processing OD 1: O1 -> O3
Chosen route: A B
Estimated time: 25 minutes
Average fare: 8.0
Assigned passengers: 50

What this does and improvements:
- Builds a graph from segments and uses Dijkstra to find capacity-aware shortest paths (allowing transfers) for OD pairs, combining time, fare, and dynamic penalties for full segments.
- Updates remaining capacities after assignments for greedy flow simulation.
- Improvements: Use min-cost max-flow for global optimization across all ODs; add transfer penalties (e.g., +5 min at nodes); integrate real-time data (e.g., via APIs); support multi-modal specifics (e.g., walking times).
- Add JUnit tests, input validation, BigDecimal for fares, and a web API (e.g., Spring Boot) for visualization.


 */

