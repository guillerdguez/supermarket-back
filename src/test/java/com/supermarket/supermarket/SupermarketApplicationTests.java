package com.supermarket.supermarket;

import com.supermarket.supermarket.config.TestRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestRedisConfig.class)
@SpringBootTest
class SupermarketApplicationTests {

	@Test
	void contextLoads() {
	}

}
