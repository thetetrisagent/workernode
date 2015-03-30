package network;

public class ClientMain {

	private static int PORT = 8888;
	private static String HOST = "localhost";
	
	@SuppressWarnings({ })
	public static void main(String argv[]) throws Exception {
		System.out.println("starto!");
		
		System.out.println("spawn training simulator!");
		SimulatorClient trainingClient = new SimulatorClient(HOST,PORT,"[Trainer]");
		
		System.out.println("spawn evaluating simulator!");
		SimulatorClient evaluatingClient = new SimulatorClient(HOST,PORT+1,"[Evaluator]");
		
		new Thread(trainingClient).start();
		new Thread(evaluatingClient).start();
	}
}