package ds.assign.entropy;

import ds.assign.entropy.PoissonProcess;
import ds.assign.entropy.RequestGenerator;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.ObjectInputStream;


public class Peer {
    String host;
    Logger logger;
    PoissonProcess poissonProcess;

    int port;
    List<PeerConnection> connections;


    public Peer(String hostname, int port,List<PeerConnection> connections) {
        host = hostname;
        this.port = port;
        Random rng = new Random();
        double lambda = 10.0;
        this.connections=connections;


        logger = Logger.getLogger("logfile");
        try {
            FileHandler handler = new FileHandler("./" + hostname + "_peer.log", true);
            logger.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        poissonProcess = new PoissonProcess(lambda, rng);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4 || (args.length - 2) % 2 != 0) {
            System.out.println("Uso: java Peer <host> <port> <peer1_host> <peer1_port> [<peer2_host> <peer2_port> ...]");
            return;
        }


        List<PeerConnection> connections = new ArrayList<>();
        for (int i = 2; i < args.length; i += 2) {
            String peerHost = args[i];
            int peerPort = Integer.parseInt(args[i + 1]);
            connections.add(new PeerConnection(peerHost, peerPort));
        }
        Peer peer = new Peer(args[0], Integer.parseInt(args[1]),connections);
        System.out.printf("new peer @ host=%s\n", args[0]);
        Server server = new Server(args[0], Integer.parseInt(args[1]), peer.logger, connections, peer.poissonProcess);
        new Thread(server).start();
        RequestGenerator requestGenerator = new RequestGenerator(args[0], peer.logger, peer.poissonProcess, Integer.parseInt(args[1]), server);
        new Thread(requestGenerator).start();

    }



}


class Server implements Runnable {
    String host;
    int port;
    ServerSocket server;
    Logger logger;

    Queue<String> words = new LinkedList<>();


    Set<String> words2 = new LinkedHashSet<>();


    Set<String> words3 = new LinkedHashSet<>();

    List<PeerConnection> neighbors;


    PoissonProcess poissonProcess;


    public Server(String host, int port, Logger logger, List<PeerConnection> neighbors, PoissonProcess poissonProcess) throws Exception {
        this.host = host;
        this.port = port;
        this.logger = logger;
        server = new ServerSocket(port, 1, InetAddress.getByName(host));
      this.neighbors=neighbors;
        this.poissonProcess = poissonProcess;


    }

    public void addWords(String operation) {
        words.add(operation);
    }

    public void addWords2(String operation) {
        if (!words2.contains(operation)) {
            words2.add(operation);
        }
    }

    public Set<String> objectToQueue(Object object) {
        if (object instanceof Set) {
            Set<String> receivedQueue = (Set<String>) object;
            return receivedQueue;
        }
        return null;
    }


    public Set<String> merge(Set<String> word2, Set<String> word3) {
        Set<String> set = new LinkedHashSet<>(word2);
        set.addAll(word3);
        return set;
    }


    public PeerConnection getRandomNeighbor() {
        if (neighbors.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int index = random.nextInt(neighbors.size());
        return neighbors.get(index);
    }


    @Override
    public void run() {

        logger.info("server: endpoint running at port " + port + " ...");
        while (true) {
            try {

                Socket client = server.accept();

                ObjectInputStream in = new ObjectInputStream(client.getInputStream());

                Object obj = in.readObject();
                words3 = objectToQueue(obj);
                System.out.println("Message from client " + words3);

                words2 = merge(words2, words3);





                String clientAddress = client.getInetAddress().getHostAddress();
                logger.info("server: new connection from " + clientAddress);

            } catch (IOException | ClassNotFoundException io) {
                io.printStackTrace();
            }

            synchronized (words) {
                while (!words.isEmpty()) {

                    String req = words.poll();
                    addWords2(req);
                    System.out.println("List of words " + words2);
                    System.out.flush();
                }
            }
            double interArrivalTime = poissonProcess.timeForNextEvent() * 1000 * 60;
            try {
                Thread.sleep((long) interArrivalTime);
                PeerConnection neighbor=getRandomNeighbor();
                Socket socket = new Socket(neighbor.getHost(), neighbor.getPort());

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());


                out.writeObject(words2);
                out.flush();
                socket.close();
            } catch (IOException io) {
                io.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


