package ds.assign.ring;

import java.io.*;
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
	Queue<String> pendingOperations=new LinkedList<>();

    public Peer(String hostname) {
	host   = hostname;
	Random rng = new Random() ;
	double lambda = 4.0/60.0;

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
	RequestGenerator requestGenerator = new RequestGenerator(args[0], peer.logger, peer.poissonProcess,Integer.parseInt(args[1]));
	new Thread(requestGenerator).start();
	new Thread(new Server(args[0], Integer.parseInt(args[1]), peer.logger)).start();

    }



}

class Server implements Runnable {
    String       host;
    int          port;
    ServerSocket server;
    Logger       logger;

    
    public Server(String host, int port, Logger logger) throws Exception {
	this.host   = host;
	this.port   = port;
	this.logger = logger;
        server = new ServerSocket(port, 1, InetAddress.getByName(host));

    }

    @Override
    public void run() {
	try {
	    logger.info("server: endpoint running at port " + port + " ...");
	    while(true ) {
		try {
		    Socket client = server.accept();
		    String clientAddress = client.getInetAddress().getHostAddress();
		    logger.info("server: new connection from " + clientAddress);

		    new Thread(new Connection(clientAddress, client, logger)).start();
		}catch(Exception e) {
		    e.printStackTrace();
		}    
	    }
	} catch (Exception e) {
	     e.printStackTrace();
	}
    }
}

class Connection implements Runnable {
    String clientAddress;
    Socket clientSocket;
    Logger logger;

    public Connection(String clientAddress, Socket clientSocket, Logger logger) {
	this.clientAddress = clientAddress;
	this.clientSocket  = clientSocket;
	this.logger        = logger;
    }

    @Override
    public void run() {
	/*
	 * prepare socket I/O channels
	 */
	try {
	    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));    
	    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
	
	    String command;
	    command = in.readLine();
	    logger.info("server: message from host " + clientAddress + "[command = " + command + "]");
	    /*
	     * parse command
	     */
	    Scanner sc = new Scanner(command);
	    String  op = sc.next();
	    double  x  = Double.parseDouble(sc.next());
	    double  y  = Double.parseDouble(sc.next());
	    double  result = 0.0; 
	    /*
	     * execute op
	     */
	    switch(op) {
	    case "add": result = x + y; break;
	    case "sub": result = x - y; break;
	    case "mul": result = x * y; break;
	    case "div": result = x / y; break;
	    }  
	    /*
	     * send result
	     */
		System.out.println(String.format("Result from command %s is: %f", command, result));


		out.flush();


		try {
			if (command == null) {
				clientSocket.close();
			} else {
				Socket socket = new Socket("localhost", clientSocket.getLocalPort() + 1);
				OutputStream outputStream = socket.getOutputStream();
				PrintWriter out2 = new PrintWriter(outputStream, true);
				out2.println(command);
				out2.flush();
				socket.close();
			}
			/*
			 * close connection
			 */


		}catch (java.net.ConnectException e){
			System.out.println("There are no more peers left to connect to.\n");
		}
	    clientSocket.close();
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }
}

