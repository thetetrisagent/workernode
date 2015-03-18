package network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import tetris.PlayerSkeleton;
import tetris.State;

public class SimulatorClient implements Runnable {

	private String host;
	private int port;
	
	public SimulatorClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	@SuppressWarnings("resource")
	@Override
	public void run() {
		try {
			Socket clientSocket = new Socket(this.host, this.port);
			ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());

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

	private static Object executeGame(double[] vector) {
		State s = new State();
		PlayerSkeleton p = new PlayerSkeleton(vector);
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
		}
		return new SampleVectorResult(vector, s.getRowsCleared());
	}

}
