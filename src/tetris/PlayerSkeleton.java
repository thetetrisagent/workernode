package tetris;
public class PlayerSkeleton {
	
	public static final int NUM_FEATURES = 8;
	public static final boolean DEBUG_MODE = false;
	 
	/*
	 * BERTSEKAS FEATURES: 21
	 *  0 -  9 = Individual Column Heights
	 * 10 - 18 = Adjacent Column Height Difference
	 *      19 = Maximum Height
	 *      20 = Number of Holes
	 */
	private static final int ADJ_COL_OFFSET = 10;
	private static final int MAX_HEIGHT_INDEX = 19;
	private static final int NUM_HOLES_INDEX = 20;
	
	/*
	 * DELLACHERIE + CUSTOM FEATURES: 6 + 2
	 * 0 = Landing Height
	 * 1 = Eroded Piece Cells
	 * 2 = Row Transitions
	 * 3 = Column Transitions
	 * 4 = Number of Holes
	 * 5 = Board Wells
	 * 6 = Hole Depth
	 * 7 = Rows With Holes
	 */
	private static final int LANDING_INDEX = 0;
	private static final int ERODED_INDEX = 1;
	private static final int ROW_TRAN_INDEX = 2;
	private static final int COL_TRAN_INDEX = 3;
	private static final int HOLE_NUM_INDEX = 4;
	private static final int WELLS_INDEX = 5;
	private static final int HOLE_DEPTH_INDEX = 6;
	private static final int ROW_HOLE_INDEX = 7;
	
	private double[] weights;

	public PlayerSkeleton(double[] weights) {
		this.weights = weights;
	}
	
	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int bestMove = 0;
		double bestMoveEval = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < legalMoves.length; i++) {
			// copy the initial State
			SimulatedState simState = new SimulatedState(s);
			
			simState.makeMove(legalMoves[i]);
			if (simState.hasLost()) {
				continue;
			}
			
			
			double nextLayerTotalEvaluation = 0;
			int[][] nextLayerLegalMoves = simState.legalMoves();
			// consider the moves 2 moves ahead
			for (int j = 0; j < nextLayerLegalMoves.length; j++) {
				SimulatedState nextLayerSimState = new SimulatedState(simState);
				
				Double nextLayerEvaluation = evaluateMove(nextLayerSimState, nextLayerLegalMoves[j]);
				nextLayerTotalEvaluation += nextLayerEvaluation;
	
			}
			// use the average evaluated score at the end of 2 moves
			
			double thisEvaluation = nextLayerTotalEvaluation / nextLayerLegalMoves.length;
			
			if (thisEvaluation > bestMoveEval) {
				bestMove = i;
				bestMoveEval = thisEvaluation;
			}
		}
		
		return bestMove;
	}
	
	
	private double evaluateMove(State s, int[] move) {
		int[] features = new int[NUM_FEATURES];
		int[][] field = simulateMove(s,move, features);
		

		if (field == null) {
			return -Double.MAX_VALUE / 500.0;
		}
		
		// Extracting features from field
		features[ROW_TRAN_INDEX] = extractRowTransitions(field);
		features[COL_TRAN_INDEX] = extractColTransitions(field);
		features[HOLE_NUM_INDEX] = extractHoles(field);
		features[WELLS_INDEX] = extractWells(field);
		features[HOLE_DEPTH_INDEX] = extractHoleDepths(field);
		features[ROW_HOLE_INDEX] = extractRowHoles(field);
		
		// Print features for debugging
		if (DEBUG_MODE) {
			LocalSimulator.printField(field);
			System.out.println("Landing: " + features[LANDING_INDEX]);
			System.out.println("Eroded: " + features[ERODED_INDEX]);
			System.out.println("Row Tran: " + features[ROW_TRAN_INDEX]);
			System.out.println("Col Tran: " + features[COL_TRAN_INDEX]);
			System.out.println("Holes: " + features[HOLE_NUM_INDEX]);
			System.out.println("Wells: " + features[WELLS_INDEX]);
			System.out.println("Hole Depth: " + features[HOLE_DEPTH_INDEX]);
			System.out.println("Row Holes: " + features[ROW_HOLE_INDEX]);
			System.out.println();
		}

		// Evaluating move 
		double evaluation = 0;
		for (int i = 0; i < NUM_FEATURES; i++) {
			evaluation += weights[i] * features[i];
		}
		return evaluation;
	}
	
	/**
	 * Calculates the height of a column in field
	 * @param field
	 * @param col
	 * @return height of column
	 */
	private int extractColumnHeight(int[][] field, int col) {
		for (int row = State.ROWS-1; row >= 0; row--) {
			if (field[row][col] > 0) {
				return row+1;
			}
		}
		return 0;
	}
	
	/**
	 * Calculates number of holes in pile
	 * @param field
	 * @return number of holes in pile
	 */
	private int extractHoles(int[][] field) {
		int holeCount = 0;
		boolean inPile = false;
		for (int col = 0; col < State.COLS; col++) {
			inPile = false;
			for (int row = State.ROWS-1; row >= 0; row--) {
				if (!inPile && field[row][col] > 0) {
					inPile = true;
				}
				if (inPile && field[row][col] == 0) {
					holeCount++;
				}
			}
		}
		return holeCount;
	}
	
	/**
	 * Calculates the horizontal full to empty or empty
	 * to full transitions between the cells of a field
	 * @param field
	 * @return sum of horizontal transitions
	 */
	private int extractRowTransitions(int[][] field) {
		int rowTransitions = 0;
		for (int row = 0; row < State.ROWS; row++) {
			if (field[row][0] == 0) {
				rowTransitions += 1;
			}
			for (int col = 0; col < (State.COLS-1); col++) {
				if (isTransition(field[row][col], field[row][col+1])) {
					rowTransitions++;
				}
			}
			if (field[row][State.COLS-1] == 0) {
				rowTransitions += 1;
			}
		}
		return rowTransitions;
	}
	
	/**
	 * Calculates the vertical full to empty or empty
	 * to full transitions between the cells of a field
	 * @param field
	 * @return sum of vertical transitions
	 */
	private int extractColTransitions(int[][] field) {
		int colTransitions = 0;
		for (int row = 0; row < (State.ROWS-2); row++) {
			if (field[row][0] == 0) {
				colTransitions++;
			}
			for (int col = 0; col < State.COLS; col++) {
				if (isTransition(field[row][col], field[row+1][col])) {
					colTransitions++;
				}
			}
			if (field[row][State.COLS - 1] == 0) {
				colTransitions++;
			}
		}
		return colTransitions;
	}
	
	/**
	 * Checks for transition between two cells
	 * @param cell1
	 * @param cell2
	 * @return true if two cells are in different states
	 */
	private boolean isTransition(int cell1, int cell2) {
		if (cell1 == 0 && cell2 != 0) return true;
		if (cell1 != 0 && cell2 == 0) return true;
		return false;
	}
	
	/**
	 * Calculates the sum of well depths given field
	 * @param field
	 * @return sum of well depths
	 */
	private int extractWells(int[][] field) {
		int sumWells = 0;
		for (int col = 0 ; col < State.COLS; col++) {
			sumWells += extractWell(field, col);
		}
		return sumWells;
	}
	
	/**
	 * A well is a succession of unoccupied cells in a column such as their
	 * left cells and right cells are both occupied.
	 * @param field
	 * @param col
	 * @return depth of well for given column
	 */
	private int extractWell(int[][] field, int col) {
		int wellDepth = 0;
		for (int row = (State.ROWS-1); row >= 0; row--) {
			if (field[row][col] != 0) {
				break; // bottom of well
			} else { // if cell is not empty
				if (col == 0) {
					if (field[row][col+1] != 0) {
						wellDepth++;
					}
				} else if (col == (State.COLS-1)) {
					if (field[row][col-1] != 0) {
						wellDepth++;
					}
				} else {
					if (field[row][col-1] != 0 && field[row][col+1] != 0) {
						wellDepth++;
					}
				}
			}
		}
		return wellDepth;
	}
	
	/**
	 * Calculates the sum of hole depths
	 * @param field
	 * @return sum of hole depths
	 */
	private int extractHoleDepths(int[][] field) {
		int sumHoleDepths = 0;
		for (int col = 0; col < State.COLS; col++) {
			sumHoleDepths += extractHoleDepth(field, col);
		}
		return sumHoleDepths;
	}
	
	/**
	 * The depth of a hole is measured as the sum of the number
	 * of full cells above each hole.
	 * @param field
	 * @param col
	 * @return hole depths given column
	 */
	private int extractHoleDepth(int[][] field, int col) {
		int sumHoleDepth = 0;
		int pileHeight = 0;
		boolean inPile = false;
		for (int row = (State.ROWS-1); row >=0; row--) {
			if (!inPile && field[row][col] > 0) {
				inPile = true;
				pileHeight = row;
			}
			if (inPile && field[row][col] == 0) {
				sumHoleDepth += pileHeight - row ;
			}
		}
		return sumHoleDepth;
	}
	
	/**
	 * Calculates the number of rows with holes
	 * @param field
	 * @return number of rows with holes
	 */
	private int extractRowHoles(int[][] field) {
		boolean inPile = false;
		int[] hasHole = new int[State.ROWS];
		for (int col = 0; col < State.COLS; col++) {
			inPile = false;
			for (int row = State.ROWS-1; row >= 0; row--) {
				if (!inPile && field[row][col] > 0) {
					inPile = true;
				}
				if (inPile && field[row][col] == 0) {
					hasHole[row] = 1;
				}
			}
		}
		int rowsWithHole = 0;
		for (int i = 0; i < hasHole.length; i++) {
			rowsWithHole += hasHole[i];
		}
		
		return rowsWithHole;
	}
	
	/**
	 * Simulates move while extracting the following features:
	 * 	column heights, landing height, eroded piece cells
	 * @param s
	 * @param move
	 * @return field after move is made
	 */
	private int[][] simulateMove(State s, int[] move, int[] features) {
		int[][][] pBottom = State.getpBottom();
		int[][][] pTop = State.getpTop();
		int[][] pWidth = State.getpWidth();
		int[][] pHeight = State.getpHeight();
		int[][] field = cloneField(s.getField());
		int[] top = s.getTop().clone();	// TODO: TAKE NOTE FOR OPTIMIZATION
		int nextPiece = s.getNextPiece();
		int orient = move[State.ORIENT];
		int slot = move[State.SLOT];
		int turn = s.getTurnNumber() + 1;
		
		// height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}
		
		//check if game ended
		if(height+pHeight[nextPiece][orient] >= State.ROWS) {
			return null;
		}
		
		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = turn;
			}
		}
		
		int rowsRemoved = 0;
		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < State.COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsRemoved++;
				//for each column
				for(int c = 0; c < State.COLS; c++) {
					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0) top[c]--;
				}
			}
		}
		
		// TODO: Include bricks removed using sets?
		features[LANDING_INDEX] = height+pHeight[nextPiece][orient];
		features[ERODED_INDEX] = rowsRemoved;
		
		return field;
	}
	
	protected static int[][] cloneField(int[][] field) {
		int [][] fieldClone = new int[field.length][];
		for(int i = 0; i < field.length; i++)
			fieldClone[i] = field[i].clone();
		return fieldClone;
	}
	

}