/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.main;

import java.util.function.Function;

import com.example.func.FunctionalSpringBootTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.cloud.function.context.FunctionRegistration;
import org.springframework.cloud.function.context.FunctionType;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.Matchers.containsString;

import reactor.core.publisher.Mono;

/**
 * @author Dave Syer
 *
 */
@FunctionalSpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
public class AutowiredApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void ok() {
		client.post().uri("/").body(Mono.just("foo"), String.class).exchange()
				.expectStatus().isOk().expectBody(String.class).isEqualTo("FOO");
	}

	@Test
	public void bang() {
		client.post().uri("/").body(Mono.just("bang"), String.class).exchange()
				.expectStatus().is5xxServerError().expectBody(String.class)
				.value(containsString("status"));
	}

	@Test
	public void notFound() {
		client.get().uri("/").exchange().expectStatus().isEqualTo(404)
				.expectBody(String.class).isEqualTo(null);
	}

	@SpringBootConfiguration
	public static class TestApplication implements Function<String, String>,
			ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public String apply(String value) {
			if (value.equals("bang")) {
				throw new IllegalStateException("Planned");
			}
			return value.toUpperCase();
		}

		@Override
		public void initialize(GenericApplicationContext context) {
			context.registerBean(FunctionRegistration.class,
					() -> new FunctionRegistration<>(this, "function")
							.type(FunctionType.from(String.class).to(String.class)));
		}

	}

}