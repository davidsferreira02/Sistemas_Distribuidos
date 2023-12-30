package ds.assign.chat;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Peer {
    String host;
    Logger logger;
    PoissonProcess poissonProcess;

    int lamportClock=0;


    public Peer(String hostname) {
        host = hostname;
        Random rng = new Random();
        double lambda = 10.0;


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
        Peer peer = new Peer(args[0]);
        System.out.printf("new peer @ host=%s\n", args[0]);
        Server server = new Server(args[0], Integer.parseInt(args[1]), peer.logger, args[2], Integer.parseInt(args[3]), peer.poissonProcess);
        new Thread(server).start();
        RequestGenerator requestGenerator = new RequestGenerator(args[0], peer.logger, peer.poissonProcess, Integer.parseInt(args[1]), server,peer);
        new Thread(requestGenerator).start();


    }

    private synchronized void incrementLamportClock(){
        lamportClock ++;

    }

    private synchronized int getLamportClock(){
        return lamportClock;
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

    String hostNext;
    int portNext;
    PoissonProcess poissonProcess;


    public Server(String host, int port, Logger logger, String hostNext, int portNext, PoissonProcess poissonProcess) throws Exception {
        this.host = host;
        this.port = port;
        this.logger = logger;
        server = new ServerSocket(port, 1, InetAddress.getByName(host));
        this.hostNext = hostNext;
        this.portNext = portNext;
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


    @Override
    public void run() {

        logger.info("server: endpoint running at port " + port + " ...");
        while (true) {
            try {

                Socket client = server.accept();

                ObjectInputStream in = new ObjectInputStream(client.getInputStream());

                Object obj = in.readObject();
                words3 = objectToQueue(obj);


                words2 = merge(words2, words3);


                System.out.println("Mensagem do client " + words2);


                String clientAddress = client.getInetAddress().getHostAddress();
                logger.info("server: new connection from " + clientAddress);

            } catch (IOException | ClassNotFoundException io) {
                io.printStackTrace();
            }

            synchronized (words) {
                while (!words.isEmpty()) {

                    String req = words.poll();
                    System.out.println("word " + req);
                    addWords2(req);
                    System.out.println("List of words " + words2);
                    System.out.flush();
                }
            }
            double interArrivalTime = poissonProcess.timeForNextEvent() * 1000 * 60;
            try {
                Thread.sleep((long) interArrivalTime);
                Socket socket = new Socket(hostNext, portNext);

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


