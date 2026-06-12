package com.touchstone.compiler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.compile-job-poller.enabled=false")
class TouchstoneCompilerApplicationTests {

	@Test
	void contextLoads() {
	}

}
