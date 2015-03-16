package tetris;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {

	private static int PORT = 8888;
	private static String HOST = "172.23.5.87";
	
	@SuppressWarnings({ "resource" })
	public static void main(String argv[]) throws Exception {
		try {
			System.out.println("starto!");
			
			Socket clientSocket = new Socket(HOST, PORT);
			ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());

			System.out.println("done init!");
			
			while (true) {
				Command newGame = (Command) inFromServer.readObject();
				System.out.println("got new job!");
				outToServer.writeObject(newGame.execute());
				System.out.println("done job!");
			}
		} catch (Exception closedSocket) {
			System.out.println("closed!");
			closedSocket.printStackTrace();
		}
	}
}