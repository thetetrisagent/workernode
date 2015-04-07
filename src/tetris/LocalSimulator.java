package tetris;

import tetris.State;
import tetris.TFrame;

public class LocalSimulator {
	
	private static final int DEBUG_PIECES = 9;

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		double[] weights = new double[PlayerSkeleton.NUM_FEATURES];
		weights[0] = -23.175;
		weights[1] = 123.055;
		weights[2] = -18.82;
		weights[3] = -3.666;
		weights[4] = -57.848;
		weights[5] = -38.703;
		weights[6] = -3.741;
		weights[7] = -80.036;
		
		PlayerSkeleton p = new PlayerSkeleton(weights);
		
		if (PlayerSkeleton.DEBUG_MODE) {
			for (int i = 0; i < DEBUG_PIECES; i++) {
				play(s, p);
			}
		} else {
			while(!s.hasLost()) {
				play(s, p);
			}
		}
		
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}

	private static void play(State s, PlayerSkeleton p) {
		s.makeMove(p.pickMove(s,s.legalMoves()));
		s.draw();
		s.drawNext(0,0);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void printField(int[][] field) {
		for (int row = (State.ROWS-1); row >= 0; row--) {
			for (int col = 0; col < State.COLS; col++) {
				System.out.print(field[row][col] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}
}
