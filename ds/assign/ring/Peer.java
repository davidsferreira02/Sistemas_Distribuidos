package ds.assign.ring;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;



public class Peer {
    String host;
    Logger logger;
	PoissonProcess poissonProcess;





    public Peer(String hostname) {
	host   = hostname;
	Random rng = new Random() ;
	double lambda = 4.0;


	logger = Logger.getLogger("logfile");
	try {
	    FileHandler handler = new FileHandler("./" + hostname + "_peer.log", true);
	    logger.addHandler(handler);
	    SimpleFormatter formatter = new SimpleFormatter();	
	    handler.setFormatter(formatter);	
	} catch ( Exception e ) {
	     e.printStackTrace();
	}
	poissonProcess=new PoissonProcess(lambda,rng);
    }

    public static void main(String[] args) throws Exception {
	Peer peer = new Peer(args[0]);
	System.out.printf("new peer @ host=%s\n", args[0]);
    Server server=new Server(args[0], Integer.parseInt(args[1]), peer.logger);
	new Thread(server).start();
	RequestGenerator requestGenerator = new RequestGenerator(args[0], peer.logger, peer.poissonProcess,Integer.parseInt(args[1]),server);
	new Thread(requestGenerator).start();

    }




}
class Server implements Runnable {
    String       host;
    int          port;
    ServerSocket server;
    Logger       logger;

	Queue<String> operations = new LinkedList<>();
	private boolean hasToken = false;
	private final Object tokenLock = new Object();


   public Server(String host, int port, Logger logger) throws Exception {
	this.host   = host;
	this.port   = port;
	this.logger = logger;
        server = new ServerSocket(port, 1, InetAddress.getByName(host));



    }
	public void addOperations(String operation) {
		operations.add(operation);
	}

    @Override
    public void run() {
	try {
	    logger.info("server: endpoint running at port " + port + " ...");
	    while(true) {
		try {

		    Socket client = server.accept();

			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String receivedToken = in.readLine();


         if(receivedToken.equals("token")){
		    String clientAddress = client.getInetAddress().getHostAddress();
		    logger.info("server: new connection from " + clientAddress);
			synchronized (operations) {

				while (!operations.isEmpty()) {
					String req = operations.poll();
					Socket socket = new Socket("localhost", 8080);
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in2 = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					out.println(req);
					out.flush();
					String result = in2.readLine();


					System.out.println("Result from operation " + req + " = "  + result);

					socket.close();
				}
			}
				try {
					Socket socket = new Socket("localhost", port + 1);
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

					// Envia o token para o próximo peer
					out.println("token");
					out.flush();
					socket.close();
				}
				catch(ConnectException e) {
					System.err.println("Não foi possível conectar-se ao servidor na porta seguinte\n");
				}
			}
		}catch(Exception e) {
		    e.printStackTrace();
		}
	    }
	} catch (Exception e) {
	     e.printStackTrace();
	}

    }
}


