package network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import tetris.PlayerSkeleton;
import tetris.State;

public class SimulatorClient implements Runnable {

	private String host;
	private int port;
	private ObjectOutputStream outToServer;
	private ObjectInputStream inFromServer;
	
	public SimulatorClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				connectToHost();
				System.out.println("done init!");
				
				while (true) {
					//Request for a new job
					Integer request = new Integer(0);
					outToServer.writeObject(request);
					double[] vector = (double[]) inFromServer.readObject();
					while (vector == null) {
						outToServer.writeObject(request);
						vector = (double[]) inFromServer.readObject();
					}
					
					System.out.println("Got New Job");
					outToServer.writeObject(executeGame(vector));
					System.out.println("Done Job");
				}
			} catch (Exception closedSocket) {
				System.out.println("closed!");
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
				System.out.println("SUMTIN WONG");
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

}
