public static double seqArraySum(double[] X) {
	long startTime = System.nanoTime();
	double sum = 0;
	for (int i=0; i < X.length; i++) {
		sum += 1/X[i];
	}
	long timeInNanos = System.nanoTime() - startTime;
	printResults("seqArraySum", timeInNanos, sum);
	// Tarea T0 espera por la tarea T1 (join) 
	return sum;
}

/**
 * <p>parArraySum.</p>
 * 
 * @param X una arreglo tipo double
 * @return sum of 1/X[i] for 0 <= i < X.length
*/
public static double parArraySum(double[] X) {
	long startTime = System.nanoTime();
	SumArray t = new SumArray(X, 0, X.length);
	ForkJoinPool.commonPool().invoke(t);
	double sum = t.ans;
	long timeInNanos = System.nanoTime() - startTime;
	printresults("parArraySum", timeInNanos, sum);
	return sum;
}

/**
 * <p>pmain</p>
 * 
 * @param argv an array of double
 */ 
public static void main(final String[] argv) {
	// Initialization 
	int n;
	if(argv.length !=0) {
		try {
			n = integer.parseInt(argv[0]);
			if(n <=0 ) {
				// valor incorrecto de n
				system.out.println(ERROR_MSG);
				n = DEFAULT_N;
			}
		} catch (Throwable e) {
			System.out.println(ERROR_MSG);
			n = DEFAULT_N;
		}
	} else { // argv.length == 0
		n = DEFAULT_N;
	}
	double[] X = new double[n]; 
	
	for(int i=0; i <n; i++) {
		X[i] = (i + 1);
	}
	
	// establece el número de 'workers' utilizados por ForKJoinPool.commonPool()
	System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "2");
	
	for (int numRun = 0; numRun < 5; numRun++) {
		System.out.printf("Run %d\n",numRun);
		seqArraySum(X);
		parArraySum(X);
	}
}
	
private static void printResults (String name, long timeInNanos, double sum) {
	system.out.printf(" %s completed in %8.3f milliseconds, with sum = %8.5f \n", name, timeInNanos / 1e6, sum);
}

private static class SumArray extends recursiveAction {
	static int SEQUENTIAL_THRESHOLD = 5;
	int lo;
	int hi;
	double arr[];
	double ans = 0;
	
	SumArray(double[] a, int l, int h) {
		lo =l;
		hi = h;
		arr = a;
	}
	
	protected void compute() {
		if (hi - lo <= SEQUENTIAL_THRESHOLD) {
			for (int = lo; i < hi; ++i)
				ans += 1 / arr[i];
		} else {
			SumArray left = new SumArray(arr, lo, (hi + lo) /2);
			SumArray right = new SumArray(arr, lo, (hi + lo) /2);
			left.fork;
			right.compute();
			left.join();
			ans = left.ans + right.ans;
		}
	} 
} 
