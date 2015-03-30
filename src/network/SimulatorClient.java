package network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

import tetris.PlayerSkeleton;
import tetris.State;

public class SimulatorClient implements Runnable {

	private String host;
	private int port;
	private String logger;
	private ObjectOutputStream outToServer;
	private ObjectInputStream inFromServer;
	
	public SimulatorClient(String host, int port, String logger) {
		this.host = host;
		this.port = port;
		this.logger = logger;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				connectToHost();
				log("done init!");
				
				while (true) {
					//Request for a new job
					Integer request = new Integer(0);
					outToServer.writeObject(request);
					double[] vector = (double[]) inFromServer.readObject();
					log(Arrays.toString(vector));
					while (vector == null) {
						outToServer.writeObject(request);
						vector = (double[]) inFromServer.readObject();
					}
					log("got it alr" + Arrays.toString(vector));
					
					log("Got New Job");
					outToServer.writeObject(executeGame(vector));
					log("Done Job");
				}
			} catch (Exception closedSocket) {
				log("closed!");
				closedSocket.printStackTrace();
			}
		}
	}

	private void connectToHost() {
		Socket clientSocket;
		int timeoff = 1;
		while (true) {
			try {
				Thread.sleep(timeoff*500);
				clientSocket = new Socket(this.host, this.port);
				this.outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
				this.inFromServer = new ObjectInputStream(clientSocket.getInputStream());
				break;
			} catch (Exception e) {
				log("SUMTIN WONG");
				e.printStackTrace();
			}
			if (timeoff < 8) {
				timeoff *= 2;	
			}
		}
	}

	private static Object executeGame(double[] vector) {
		State s = new State();
		PlayerSkeleton p = new PlayerSkeleton(vector);
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
		}
		return new SampleVectorResult(vector, s.getRowsCleared());
	}
	
	private void log(String toLog) {
		System.out.println(this.logger+":"+toLog);
	}

}
