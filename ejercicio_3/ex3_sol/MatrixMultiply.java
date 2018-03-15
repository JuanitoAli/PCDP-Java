//package edu.coursera.parallel;

import static edu.rice.pcdp.PCDP.forseq2d;
import java.util.Random;
import java.lang.*;
/**
 * Wrapper class for implementing matrix multiply efficiently in parallel.
 */
public final class MatrixMultiply {
    private MatrixMultiply() {
    }

    public static void seqMatrixMultiply(final double[][] A, final double[][] B,
            final double[][] C, final int N) {
        forseq2d(0, N - 1, 0, N - 1, (i, j) -> {
            C[i][j] = 0.0;
            for (int k = 0; k < N; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        });
    }

    public static void parMatrixMultiply(final double[][] A, final double[][] B,
            final double[][] C, final int N) {
        /*
         * TODO Parallelize this outermost two-dimension sequential loop to
         * achieve performance improvement.
         */
        forall2dChunked(0, N - 1, 0, N - 1, 4, (i, j) -> {
            C[i][j] = 0.0;
            for (int k = 0; k < N; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        });
    }
    
    private static double[][] createMatrix(final int N) {
        final double[][] input = new double[N][N];
        final Random rand = new Random(314);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                input[i][j] = rand.nextInt(100);
            }
        }

        return input;
    }

    private static void checkResult(final double[][] ref, final double[][] output, final int N) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (ref[i][j] != output[i][j]){
                    String msg = "Error detected on cell (" + i + ", " + j + ")";
                    System.out.println(msg);
                }
            }
        }
    }
    
    private static void myTestHelper(final int N) {
        final double[][] A = createMatrix(N);
        final double[][] B = createMatrix(N);
        final double[][] C = new double[N][N];
        final double[][] refC = new double[N][N];

        final long seqStartTime = System.currentTimeMillis();
        seqMatrixMultiply(A, B, refC, N);
        final long seqEndTime = System.currentTimeMillis();

        final long parStartTime = System.currentTimeMillis();
        MatrixMultiply.parMatrixMultiply(A, B, C, N);
        final long parEndTime = System.currentTimeMillis();

        checkResult(refC, C, N);

        System.out.println("seq: " + (seqEndTime - seqStartTime));
        System.out.println("par: " + (parEndTime - seqEndTime));
    }
    
    public static void main(String args[]) {
        myTestHelper(521);
        
    }
}
