package tetris;

import java.util.Arrays;
import java.util.Random;

import network.SampleVectorResult;

public class LocalTrainer {
	private static final int MAX_ITERATIONS = 80;
	private static final int NUM_SAMPLES = 100;
	private static final double SELECTION_RATIO = 0.1;
	private static final double NOISE_FACTOR = 0;
	
	public static void main(String[] args) {
		int iteration = 0;
		Random r = new Random();
		double[] currMeanVector = new double[PlayerSkeleton.NUM_FEATURES];
//		currMeanVector[0] = -4.5;
//		currMeanVector[1] = 3.4;
//		currMeanVector[2] = -3.2;
//		currMeanVector[3] = -9.3;
//		currMeanVector[4] = -7.8;
//		currMeanVector[5] = -3.3;
		
		double[] currVarVector = new double[PlayerSkeleton.NUM_FEATURES];
		for (int i = 0; i < PlayerSkeleton.NUM_FEATURES; i++) {
			currVarVector[i] = 100;
		}
		
		while (iteration < MAX_ITERATIONS) {
			SampleVectorResult[] sampleResults = new SampleVectorResult[NUM_SAMPLES];
			
			//Generate Samples
			for (int i = 0; i < NUM_SAMPLES; i++) {
				//Generate new weight vector
				double[] newSampleWeight = new double[PlayerSkeleton.NUM_FEATURES];
				for (int j = 0; j < PlayerSkeleton.NUM_FEATURES; j++) {
					newSampleWeight[j] = (r.nextGaussian()*Math.sqrt(currVarVector[j]))+currMeanVector[j];	
				}
				
				//Let them run
				State s = new State();
				PlayerSkeleton p = new PlayerSkeleton(newSampleWeight);
				while(!s.hasLost()) {
					s.makeMove(p.pickMove(s,s.legalMoves()));
				}
				sampleResults[i] = new SampleVectorResult(newSampleWeight, s.getRowsCleared());
			}
			
			//Evaluate And Recalculate Mean & Variance
			Arrays.sort(sampleResults);
			double numSelected = NUM_SAMPLES*SELECTION_RATIO;
			for (int i = 0; i < PlayerSkeleton.NUM_FEATURES; i++) {
				double newMean = 0;
				double newVariance = 0;
				//Calculate New Mean
				for (int j = 0; j < numSelected; j++) {
					int curr = NUM_SAMPLES-1-j;
					newMean += sampleResults[curr].getWeightVector()[i];
				}
				newMean /= numSelected;
				currMeanVector[i] = newMean;
				
				//Calculate New Variance
				for (int j = 0; j < numSelected; j++) {
					int curr = NUM_SAMPLES-1-j;
					newVariance += Math.pow(newMean - sampleResults[curr].getWeightVector()[i],2);
				}
				newVariance /= numSelected;
				currVarVector[i] = newVariance + NOISE_FACTOR;
			}
			
			//Print Results
			System.out.print("Current Means are: ");
			for (int i = 0; i < PlayerSkeleton.NUM_FEATURES; i++) {
				System.out.print(currMeanVector[i] + ", ");
			}
			System.out.println();
			System.out.print("Current Vars are: ");
			for (int i = 0; i < PlayerSkeleton.NUM_FEATURES; i++) {
				System.out.print(currVarVector[i] + ", ");
			}
			System.out.println();
			System.out.println(iteration + ": " + sampleResults[sampleResults.length-(int)numSelected].getResults());
			
			iteration++;
		}
	}
	
}
