# Distributed Systems - Practical Assignment 2023/24

This repository contains the implementation of application prototypes for various scenarios in Distributed Systems.

## Scenarios Implemented

### 1. Mutual Exclusion with the Token Ring Algorithm

- Implemented a Token Ring Algorithm using Java Sockets
- Created a ring network with 5 peers distributed across different machines
- Peers generate requests for a server following a Poisson distribution
- Token-based approach for achieving mutual exclusion


### 2. Data Dissemination Using the Anti-Entropy Algorithm

- Utilized Anti-Entropy Algorithm for data dissemination among 6 peers
- Peers maintain a table of IPs/names of registered machines/peers
- Implemented random word generation and addition to a growing list
- Employed push-pull operations among peers at random instants

### 3. Basic Chat Application Using Totally Ordered Multicast

- Developed a chat application using Totally Ordered Multicast
- Implemented Lamport clocks for adequate message timestamping
- Peers send random messages using Poisson distribution
- Achieved consensus on the global order of messages received


## Directory Structure

The software is organized into three packages:

- `ds.assign.ring`
- `ds.assign.entropy`
- `ds.assign.chat`

Each package contains the relevant code for the corresponding scenario.

## Running the Examples

To compile and run the examples, follow these steps:

1. **Token Ring Algorithm:**
    - Navigate to `ds.assign.ring`
    - Compile and run the main class `Peer.java`
    ```
    $ javac Peer.java
    $ java ds/assign/ring/CalculatorMultiServer localhost 3000
    $java ds/assign/ring/Peer localhost 20000 localhost 20001 2> /dev/null 
    $java ds/assign/ring/Peer localhost 20001 localhost 20002 2> /dev/null 
    $java ds/assign/ring/Peer localhost 20002 localhost 20003 2> /dev/null 
    $java ds/assign/ring/Peer localhost 20003 localhost 20004 2> /dev/null 
    $java ds/assign/ring/Peer localhost 20004 localhost 20005 2> /dev/null 
    $java ds/assign/ring/Peer localhost 20005 localhost 20000 2> /dev/null 
    $java ds/assign/ring/Token localhost 20000 token
    
    ```

2. **Data Dissemination:**
    - Navigate to `ds.assign.entropy`
    - Compile and run the main class `Peer.java`
    ```
       $javac -Xlint ds/assign/entropy/*.java 
       $ java ds/assign/entropy/Peer localhost 20000 localhost 20001 localhost 20002 localhost 20003 2> /dev/null 
       $ java ds/assign/entropy/Peer localhost 20001 localhost 20000 2> /dev/null  
       $java ds/assign/entropy/Peer localhost 20002 localhost 20000 2> /dev/null  
       $java ds/assign/entropy/Peer localhost 20003 localhost 20000 2> /dev/null  
  
    ```

3. **Chat Application:**

    - The goal of exercise 3 was not achieved, only the lamport clock was implemented
    - Navigate to `ds.assign.chat`
    - Compile and run the main class `Peer.java`
  
  ```
$javac -Xlint ds/assign/chat/*.java 
       $ java ds/assign/chat/Peer localhost 20000 localhost 20001 localhost 20002 localhost 20003 2> /dev/null 
       $ java ds/assign/chat/Peer localhost 20001 localhost 20000 2> /dev/null  
       $java ds/assign/chat/Peer localhost 20002 localhost 20000 2> /dev/null  
       $java ds/assign/chat/Peer localhost 20003 localhost 20000 2> /dev/null  
   ```


