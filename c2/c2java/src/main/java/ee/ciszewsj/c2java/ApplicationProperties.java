package ee.ciszewsj.c2java;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ee.matrix")
public class ApplicationProperties {
	private String matrixA;
	private String matrixB;
	private Integer numberOfThreads;
}
