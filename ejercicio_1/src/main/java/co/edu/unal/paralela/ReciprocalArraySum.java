// package co.edu.unal.paralela;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;
import java.util.Random;
import java.util.ArrayList;

/**
 * Clase que contiene los métodos para implementar la suma de los recíprocos de un arreglo usando paralelismo.
 */
public final class ReciprocalArraySum {

    /**
     * Constructor.
     */
    private ReciprocalArraySum() {
    }

    /**
     * Calcula secuencialmente la suma de valores recíprocos para un arreglo.
     *
     * @param input Arreglo de entrada
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double seqArraySum(final double[] input) {
        double sum = 0;

        // Calcula la suma de los recíprocos de los elementos del arreglo
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

    /**
     * calcula el tamaño de cada trozo o sección, de acuerdo con el número de secciones para crear
     * a través de un número dado de elementos.
     *
     * @param nChunks El número de secciones (chunks) para crear
     * @param nElements El número de elementos para dividir
     * @return El tamaño por defecto de la sección (chunk)
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Función techo entera
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Calcula el índice del elemento inclusivo donde la sección/trozo (chunk) inicia,
     * dado que hay cierto número de secciones/trozos (chunks).
     *
     * @param chunk la sección/trozo (chunk) para cacular la posición de inicio
     *              --> chunk counting starts in 0
     * @param nChunks Cantidad de seciiones/trozos (chunks) creados
     * @param nElements La cantidad de elementos de la sección/trozo que debe atravesarse
     * @return El indice inclusivo donde esta sección/trozo (chunk) inicia en el conjunto de
     *         nElements
     */
    private static int getChunkStartInclusive(final int chunk,
            final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Calcula el índice del elemento exclusivo que es proporcionado al final de la sección/trozo (chunk),
     * dado que hay cierto número de secciones/trozos (chunks).
     *
     * @param chunk LA sección para calcular donde termina
     * @param nChunks Cantidad de seciiones/trozos (chunks) creados
     * @param nElements La cantidad de elementos de la sección/trozo que debe atravesarse
     * @return El índice de terminación exclusivo para esta sección/trozo (chunk)
     */
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    /**
     * Este pedazo de clase puede ser completada para para implementar el cuerpo de cada tarea creada
     * para realizar la suma de los recíprocos del arreglo en paralelo.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {
        /**
         * Iniciar el índice para el recorrido transversal hecho por esta tarea.
         */
        private final int startIndexInclusive;
        /**
         * Concluir el índice para el recorrido transversal hecho por esta tarea.
         */
        private final int endIndexExclusive;
        /**
         * Arreglo de entrada para la suma de recíprocos.
         */
        private final double[] input;
        /**
         * Valor intermedio producido por esta tarea.
         */
        private double value;

        private final int SEQUENTIAL_THRESHOLD = 10;

        public double ans = 0;

        /**
         * Constructor.
         * @param setStartIndexInclusive establece el indice inicial para comenzar
         *        el recorrido trasversal.
         * @param setEndIndexExclusive establece el indice final para el recorrido trasversal.
         * @param setInput Valores de entrada
         */
        ReciprocalArraySumTask(final int setStartIndexInclusive,
                final int setEndIndexExclusive, final double[] setInput) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
        }

        /**
         * Adquiere el valor producido por esta tarea.
         * @return El valor producido por esta tarea
         */
        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {
            int lo = startIndexInclusive;
            int hi = endIndexExclusive;
            //System.out.println("[" + lo + ", " + hi + "]");
            if (hi - lo <= SEQUENTIAL_THRESHOLD) {
                for (int i = lo; i < hi; i++)
                    ans += 1 / input[i];
            } else {
                ReciprocalArraySumTask left =
                              new ReciprocalArraySumTask(lo, (hi + lo)/2, input);
                ReciprocalArraySumTask right =
                              new ReciprocalArraySumTask((hi + lo)/2, hi, input);

                invokeAll(left, right);
                //left.fork();
                //right.compute();
                //left.join();

                ans = left.ans + right.ans;
            }
	      }
    }

    /**
     * Para hacer: Modificar este método para calcular la misma suma de recíprocos como le realizada en
     * seqArraySum, pero utilizando dos tareas ejecutándose en paralelo dentro del framework ForkJoin de Java
     * Se puede asumir que el largo del arreglo de entrada
     * es igualmente divisible por 2.
     *
     * @param input Arreglo de entrada
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double parArraySum(final double[] input) {
        assert input.length % 2 == 0;

        double sum = 0;
        int nElements = input.length;
        final int CHUNKS = 2;

        // Note that chunk counting begins in 0...
        ReciprocalArraySumTask sumTask1 =
                new ReciprocalArraySumTask(
                    getChunkStartInclusive(0, CHUNKS, nElements),
                    getChunkEndExclusive(0, CHUNKS, nElements),
                    input
                );

        ReciprocalArraySumTask sumTask2 =
                new ReciprocalArraySumTask(
                    getChunkStartInclusive(1, CHUNKS, nElements),
                    getChunkEndExclusive(1, CHUNKS, nElements),
                    input
                );


        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(sumTask1);
        pool.invoke(sumTask2);

        sum = sumTask1.ans + sumTask2.ans;
        return sum;
    }

    /**
     * Para hacer: extender el trabajo hecho para implementar parArraySum que permita utilizar un número establecido
     * de tareas para calcular la suma del arreglo recíproco.
     * getChunkStartInclusive y getChunkEndExclusive pueden ser útiles para cacular
     * el rango de elementos indice que pertenecen a cada sección/trozo (chunk).
     *
     * @param input Arreglo de entrada
     * @param numTasks El número de tareas para crear
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double parManyTaskArraySum(final double[] input,
            final int numTasks) {
        ForkJoinPool pool = new ForkJoinPool();
        double sum = 0;

        for(int i = 0; i < numTasks; ++i) {
            ReciprocalArraySumTask task =
                  new ReciprocalArraySumTask(
                        getChunkStartInclusive(i, numTasks, input.length),
                        getChunkEndExclusive(i, numTasks, input.length),
                        input
                  );
            pool.invoke(task);
            sum += task.ans;
        }
        return sum;
    }


/*
    private static double[] createArray(final int N) {
        final double[] input = new double[N];
        final Random rand = new Random(314);

        for (int i = 0; i < N; i++) {
            input[i] = rand.nextInt(100);
            // No se permiten valores en cero en el arreglo de entrada para evitar la división por cero
            if (input[i] == 0.0) {
                i--;
            }
        }

        return input;
    }


    private static double parTestHelper(final int N, final boolean useManyTaskVersion, final int ntasks) {
        // Crea un arreglo de entrada de manera aleatoria
        final double[] input = createArray(N);
        // Utilza una version secuencial para calcular el resultado correcto
        final double correct = seqArraySum(input);
        // Utiliza la implementación paralela para calcular el resultado
        double sum;
        if (useManyTaskVersion) {
            sum = ReciprocalArraySum.parManyTaskArraySum(input, ntasks);
        } else {
            // assert ntasks == 2;
            sum = ReciprocalArraySum.parArraySum(input);
        }
        final double err = Math.abs(sum - correct);
        // Asegura que la salida esperada sea la producida
        final String errMsg = String.format("No concuerda el resultado para N = %d, valor esperado = %f, valor calculado = %f, error " +
                "absoluto = %f", N, correct, sum, err);

        if (!(err < 1E-2))
            System.out.println(errMsg);



        int REPEATS = 10;

        final long seqStartTime = System.currentTimeMillis();
        for (int r = 0; r < REPEATS; r++) {
            seqArraySum(input);
        }
        final long seqEndTime = System.currentTimeMillis();

        final long parStartTime = System.currentTimeMillis();
        for (int r = 0; r < REPEATS; r++) {
            if (useManyTaskVersion) {
                ReciprocalArraySum.parManyTaskArraySum(input, ntasks);
            } else {
                assert ntasks == 2;
                ReciprocalArraySum.parArraySum(input);
            }
        }
        final long parEndTime = System.currentTimeMillis();

        final long seqTime = (seqEndTime - seqStartTime) / REPEATS;
        final long parTime = (parEndTime - parStartTime) / REPEATS;

        return (double)seqTime / (double)parTime;
    }

    public static void main(String[] args) {
        System.out.println(parTestHelper(100000000, true, 10));
    }
*/
}
