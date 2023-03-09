package ee.ciszewsj.c2java;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;

@Slf4j
@Configuration
public class StartProgram {
	public StartProgram(ApplicationProperties applicationProperties) throws FileNotFoundException, InterruptedException {
		log.info("READ : {} - {} - {}", applicationProperties.getMatrixA(), applicationProperties.getMatrixB(), applicationProperties.getNumberOfThreads());
		MatrixMulti matrixMulti = new MatrixMulti(applicationProperties.getMatrixA(), applicationProperties.getMatrixB(), applicationProperties.getNumberOfThreads());
		Matrix result = matrixMulti.calculateMatrix();
		log.info("Result A * B is : {}", MatrixMulti.print(result));
	}
}
