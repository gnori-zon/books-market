package org.gnori.booksmarket.e2e;

import java.io.IOException;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Tag("e2e")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class IntegrationTest {

	@Autowired
	protected MockMvc mockMvc;
	protected final JsonMapper jsonMapper = JsonMapper.builder().build();

	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14.5")
			.withDatabaseName("testdb")
			.withUsername("test")
			.withPassword("test")
			.withReuse(true);

	static String MINIO_USER = "user";
	static String MINIO_PASSWORD = "password";

	static GenericContainer<?> minio = new GenericContainer<>("minio/minio:latest")
			.withEnv("MINIO_ROOT_USER", MINIO_USER)
			.withEnv("MINIO_ROOT_PASSWORD", MINIO_PASSWORD)
			.withReuse(true)
			.withCommand("server /data")
			.withExposedPorts(9000)
			.waitingFor(Wait.forHttp("/minio/health/live").forPort(9000));

	static {
		postgres.start();
		minio.start();
	}

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		registry.add("attachments.connection.url",
				() -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
		registry.add("attachments.connection.access-key", () -> MINIO_USER);
		registry.add("attachments.connection.secret-key", () -> MINIO_PASSWORD);
	}

	protected String parseIdFrom(String json) throws IOException {
		return jsonMapper.readTree(json).get("id").asText();
	}

	protected ObjectNode replaceOpFrom(String path, JsonNode value) {
		final var op = jsonMapper.createObjectNode();
		op.put("path", path);
		op.put("op", "replace");
		op.set("value", value);
		return op;
	}
}
