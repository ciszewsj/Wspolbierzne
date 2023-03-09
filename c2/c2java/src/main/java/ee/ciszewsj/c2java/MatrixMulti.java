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

	private final AtomicReference<Matrix> result;

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

		result = new AtomicReference<>(new Matrix(A.rows(), B.cols()));

	}

	public Matrix calculateMatrix() throws InterruptedException {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);

		log.debug("Create executor with number of threads : {}", executor.getCorePoolSize());

		multi(A, B);

		return result.get();
	}

	private void multi(Matrix A, Matrix B) throws InterruptedException {

		for (int r = 0; r < A.rows(); r++) {
			for (int c = 0; c < B.cols(); c++) {
				countPointFuture(r, c);
			}
		}
		executor.shutdown();
		if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
			executor.shutdownNow();
			throw new IllegalStateException("TOO MUCH TIME FOR CALCULATING");
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

	private void countPointFuture(final int rf, final int cf) {
		executor.execute(() -> {
			float s = 0;
			for (int k = 0; k < A.cols(); k++) {
				s += A.get(rf, k) * B.get(k, cf);
			}
			log.debug("COUNT r: {}, c: {}, s: {}", rf, cf, s);
			result.get().set(rf, cf, s);
		});
	}
}
