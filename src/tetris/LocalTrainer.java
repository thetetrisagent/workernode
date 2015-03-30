package tetris;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import network.SampleVectorResult;
import tetris.StopWatch;

public class LocalTrainer {
	private static final int MAX_ITERATIONS = 80;
	private static final int NUM_SAMPLES = 100;
	private static final double SELECTION_RATIO = 0.1;
	private static final double NOISE_FACTOR = 0;
	private static final double NUM_EVALUATED = 30;
	private static final double[] CONSTANT_NOISE = new double[] {1.0,0.0};
	private static final double[] DECREASING_NOISE = new double[] {0.0,1.0};
	private static final String ITERATION_TIMES_CSV_FILENAME = "iterationTimes_%d.csv";
	private static final String TRAININGLOOP_TIMES_CSV_FILENAME = "trainingLoopTimes.csv";
	private static final String TRAINING_ONLY_ITERATION_TIMES_CSV_FILENAME = "trainingOnlyIterationTimes_%d.csv";
	
	public static void main(String[] args) {
		StopWatch trainingLoopWatch = new StopWatch();
		StopWatch iterationWatch = new StopWatch();
		double[] noiseController;
		int iteration = 0;
		Random r = new Random();
		double[] currMeanVector = new double[PlayerSkeleton.NUM_FEATURES];
		
		double[] currVarVector = new double[PlayerSkeleton.NUM_FEATURES];
		for (int i = 0; i < PlayerSkeleton.NUM_FEATURES; i++) {
			currVarVector[i] = 100;
		}
		
		for (int loop = 0; loop < 2; loop++) {
			trainingLoopWatch.reset();
			noiseController = setNoiseStatus(loop);
			while (iteration < MAX_ITERATIONS) {
				iterationWatch.reset();
				
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
					currVarVector[i] = newVariance + noiseController[0]*NOISE_FACTOR + noiseController[1]*getDecreasingNoise(iteration);
				}


				//Record Timings only training
				long sumRows = 0;
				for (int i = 0; i < sampleResults.length; i++) {
					sumRows += sampleResults[i].getResults();
				}
				long iterationTime = iterationWatch.getElapsedTime();
				recordTiming(String.format(TRAINING_ONLY_ITERATION_TIMES_CSV_FILENAME,loop),sumRows,iterationTime, iteration);
				
				//Evaluate Mean Weight
				double thirtyAvg = 0;
				for (int i = 0; i < NUM_EVALUATED; i++) {
					//Let them run
					State s = new State();
					PlayerSkeleton p = new PlayerSkeleton(currMeanVector);
					while(!s.hasLost()) {
						s.makeMove(p.pickMove(s,s.legalMoves()));
					}
					thirtyAvg += s.getRowsCleared();
				}
				sumRows += thirtyAvg;
				thirtyAvg /= NUM_EVALUATED;
				
				//Record Timings with evaluation
				iterationTime = iterationWatch.getElapsedTime();
				recordTiming(String.format(ITERATION_TIMES_CSV_FILENAME,loop),sumRows,iterationTime, iteration);
				
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
				System.out.println(iteration + " top: " + sampleResults[sampleResults.length-1].getResults());
				System.out.println(iteration + " bottom: " + sampleResults[sampleResults.length-(int)numSelected].getResults());
				System.out.println(iteration + " 30 Game Avg: " + thirtyAvg);
				
				iteration++;
			}

			long trainingLoopTime = trainingLoopWatch.getElapsedTime();
			recordTiming(TRAININGLOOP_TIMES_CSV_FILENAME,0,trainingLoopTime, iteration);
			
			//Reset
			currMeanVector = new double[PlayerSkeleton.NUM_FEATURES];
			currVarVector = new double[PlayerSkeleton.NUM_FEATURES];
			for (int i = 0; i < PlayerSkeleton.NUM_FEATURES; i++) {
				currVarVector[i] = 100;	
			}
			iteration = 0;
		}		
	}


	private static double[] setNoiseStatus(int loop) {
		//Set noise status
		if (loop%2 == 0) {
			//Constant Noise
			return CONSTANT_NOISE;
		} else {
			//Decreasing Noise
			return DECREASING_NOISE;
		}
	}
	
	private static double getDecreasingNoise(int iterations) {
		return Math.max(5-(iterations/10.0), 0);
	}
	

	private static void recordTiming(String filePath, long results, long iterationTime, int iterations) {
		try {
			FileWriter writer = new FileWriter(filePath,true);
			writer.append(""+ iterations + "," + results + "," + iterationTime);
			writer.append("\n");
		    writer.flush();
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
