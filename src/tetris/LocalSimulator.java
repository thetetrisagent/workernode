package tetris;

import tetris.State;
import tetris.TFrame;

public class LocalSimulator {
	
	private static final int DEBUG_PIECES = 9;

	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		double[] weights = new double[PlayerSkeleton.NUM_FEATURES];
		weights[0] = -4.50;
		weights[1] = -3.42;
		weights[2] = -3.22;
		weights[3] = -9.34;
		weights[4] = -7.90;
		weights[5] = -3.39;
		weights[6] = 0;
		weights[7] = 0;
		
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
