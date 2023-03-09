package ee.ciszewsj.c2java;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class MatrixMulti {

	private final int numberOfThreads;

	private final Matrix A;
	private final Matrix B;

	private final AtomicReference<Matrix> resultMatrix;

	private final AtomicReference<Float> resultSumOfMatrix;

	private final AtomicReference<Float> frobeniousOfMatrix;

	private ThreadPoolExecutor executor;

	public MatrixMulti(String pathA, String pathB, int numberOfThreads) throws FileNotFoundException {

		this.numberOfThreads = numberOfThreads;

		A = read(pathA);
		B = read(pathB);

		log.debug("READ A:");
		log.debug(print(A));

		log.debug("READ B:");
		log.debug(print(B));

		if (A.cols() != B.rows()) {
			log.error("A.cols == {} != B.rows == {}", A.cols(), B.rows());
		}

		resultMatrix = new AtomicReference<>(new Matrix(A.rows(), B.cols()));
		resultSumOfMatrix = new AtomicReference<>(0.0F);
		frobeniousOfMatrix = new AtomicReference<>(0.0F);

	}

	protected void generateThreads() {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);
		log.debug("Create executor with number of threads : {}", executor.getCorePoolSize());

	}

	protected void waitUntilThreadsStop() throws InterruptedException {
		executor.shutdown();
		if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
			executor.shutdownNow();
			throw new IllegalStateException("TOO MUCH TIME FOR CALCULATING");
		}
	}

	public Matrix getResultMatrix() {
		return resultMatrix.get();
	}

	public Float getResultSumOfMatrixElements() {
		return resultSumOfMatrix.get();
	}

	public Float getFrobeniousNormOfMatrix() {
		return frobeniousOfMatrix.get();
	}

	public void calculateMatrix() throws InterruptedException {
		generateThreads();
		doOperation(multiplyPointFuture());
		waitUntilThreadsStop();

		log.debug("Result matrix : {}", print(resultMatrix.get()));

		generateThreads();
		doOperation(countSumFuture());
		waitUntilThreadsStop();

		log.debug("Sum of matrix elements : {}", resultSumOfMatrix.get());

		generateThreads();
		doOperation(squareSumFuture());
		waitUntilThreadsStop();

		frobeniousOfMatrix.updateAndGet(aFloat -> (float) Math.sqrt(aFloat));

		log.debug("Frobenious norm of matrix : {}", frobeniousOfMatrix.get());
	}

	private void doOperation(RunnableMatrixCreator creator) {

		for (int r = 0; r < A.rows(); r++) {
			for (int c = 0; c < B.cols(); c++) {
				Runnable runnable = creator.create(r, c);
				executor.execute(runnable);
			}
		}

	}

	protected Matrix read(String name) throws FileNotFoundException {
		File f = new File(name);
		Scanner scanner = new Scanner(f).useLocale(Locale.ENGLISH);

		int rows = scanner.nextInt();
		int cols = scanner.nextInt();
		Matrix res = new Matrix(rows, cols);

		for (int r = 0; r < res.rows(); r++) {
			for (int c = 0; c < res.cols(); c++) {
				float a = scanner.nextFloat();
				res.set(r, c, a);
			}
		}
		return res;
	}

	public static String print(Matrix m) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\n");
		stringBuilder.append("[");
		stringBuilder.append("\n");
		for (int r = 0; r < m.rows(); r++) {

			for (int c = 0; c < m.cols(); c++) {
				stringBuilder.append(m.get(r, c));
				stringBuilder.append(" ");
			}
			stringBuilder.append("\n");

		}
		stringBuilder.append("]");
		stringBuilder.append("\n");
		return stringBuilder.toString();
	}

	private RunnableMatrixCreator multiplyPointFuture() {
		return (r, c) -> () -> {
			{
				float s = 0;
				for (int k = 0; k < A.cols(); k++) {
					s += A.get(r, k) * B.get(k, c);
				}
				log.debug("COUNT r: {}, c: {}, s: {}", r, c, s);
				resultMatrix.get().set(r, c, s);
			}
		};
	}

	private RunnableMatrixCreator countSumFuture() {
		return (r, c) -> () -> resultSumOfMatrix.updateAndGet(f -> resultMatrix.get().get(r, c) + f);
	}

	private RunnableMatrixCreator squareSumFuture() {
		return (r, c) -> () -> frobeniousOfMatrix.updateAndGet(f -> resultMatrix.get().get(r, c) * resultMatrix.get().get(r, c) + f);
	}
}
