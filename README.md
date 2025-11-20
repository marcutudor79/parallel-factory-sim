# parallel-factory-sim
This is a Java project that starts from a single‑threaded example and is developed to be distributed across multiple threads. It employs multiple synchronization techniques in Java.

# Application structure
```
                                   +----------------------+
                                   |    Kafka Broker(s)   |
                                   |      (events)        |
                     +------------>+----------+-----------+<-----------------+
                     |                                                       |
                     | PRODUCE (factory events)      CONSUME (factory events)|
                     |                                                       v
+--------------------+        HTTP (control)           +-------------------------+
|   Spring Boot App  | <-------------------------------|   SimulatorApplication  |
|  (REST API layer)  |        (start/stop)             | (UI; consumes Kafka)    |
+---------+----------+                                 +-----------+-------------+
          |                                                        |
          | SOCKET INPUT / OUTPUT STREAM                           |
          v                                                        v
+--------------------+                                 +-------------------------+
| Persistence Server |                                 |  Local Model / Rendering |
| (filesystem store) |                                 +-------------------------+
+--------------------+
```

Role summary:
- Spring Boot App: Exposes REST endpoints (start/stop/retrieve). Publishes simulation updates and domain events to Kafka topics.
- Kafka: Decouples data flow. Topics (example): factory.state, simulation.events, command.requests.
- SimulatorApplication (Swing UI): Sends control commands via HTTP; subscribes to Kafka topics to receive near real‑time factory snapshots instead of (or in addition to) polling HTTP.
- Persistence Server: Continues to serve as socket-based persistence; may also consume Kafka to persist streamed snapshots.

[!(how-it-works)](/docs/how-it-works.mp4)

# Contents
- [How to setup project](#how-to-setup-for-the-project)
- [How to run the project](#how-to-run-this-project)

# How to setup for the project
1. Eclipse IDE 2025-09 from [here](https://www.eclipse.org/downloads/packages/release/2025-09/r/eclipse-ide-java-developers).
2. SpringTools 4.32.1.RELEASE (from Eclipse marketplace)
3. Java open-jdk 21
```
    sudo apt install openjdk-21-jdk
```

# How to run this project
1. Launch Eclipse IDE and import the project
2. Open fr.tp.inf112.projects.robotsim -> src/ dir
3. Run the SimulationApplication.java as 1 Java App
4. Run the SimulatorPersistenceServer.java as 1 Java App
5. Open fr.tp.slr201.projects.robotsim.service.simulation -> src/ dir
6. Run the Application.java as 8 Spring Boot App
7. Start the simulation and enjoy!
