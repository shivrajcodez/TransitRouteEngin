# 🚍 TransitRouteEngin – Java Transit Route Evaluator

A graph-based transit route evaluation system built in Java that optimizes routes based on **Time, Fare, and Capacity constraints** using a modified Dijkstra’s algorithm and greedy passenger assignment.

---

## 📌 Features
- Graph modeling using adjacency list
- Multi-factor route cost evaluation (Time + Fare + Capacity Penalty)
- Modified Dijkstra’s Algorithm
- Greedy passenger assignment under capacity constraints
- Console-based input/output system

---

## 🛠 Tech Stack
- Java (Core)
- Graph Data Structures
- Modified Dijkstra’s Algorithm
- Greedy Optimization
- File I/O

---

## 🧠 How It Works
1. Build a directed graph from transit segments.
2. Compute optimal path using custom-cost Dijkstra.
3. Assign passengers greedily.
4. Update remaining capacities.

---

## ⏱ Complexity Analysis
- Dijkstra’s Algorithm: **O((V + E) log V)**
- Passenger Assignment: **O(E)**
- Overall: Efficient for medium-scale transit networks.

---

## ▶️ How To Run

```bash
javac *.java
java TransitRouteEngin
📊 Example Output
Best Route: O1 → O2 → O3
Total Time: 25 min
Total Fare: ₹12
Passengers Assigned: 50

---


```markdown
## 🏗 Architecture Diagram
![Architecture](docs/architecture.png)
