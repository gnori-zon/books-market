package org.gnori.booksmarket.e2e;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;

class AuthorIntegrationTest extends IntegrationTest {
	private final String basePath = "/api/v1/authors";

	@Test
	void shouldReturnCreatedAuthor() throws Exception {
		final String firstName = String.format("  some_firstName_%s     ", UUID.randomUUID());
		final String lastName = "author_lastName_" + UUID.randomUUID();
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateAuthorBody(firstName, lastName)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.first_name").value(firstName.trim()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.last_name").value(lastName.trim()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.first_name").value(firstName.trim()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.last_name").value(lastName.trim()));
	}

	@ParameterizedTest(name = "{displayName}: {argumentsWithNames}")
	@MethodSource("invalidCreateArgs")
	void shouldReturnErrorWhenCreateAuthorWithBlankField(String firstName, String lastName) throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateAuthorBody(firstName, lastName)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	static Stream<Arguments> invalidCreateArgs() {
		return Stream.of("", "   ", null).flatMap(invalidName -> Stream.of(
				Arguments.of(invalidName, UUID.randomUUID().toString()),
				Arguments.of(UUID.randomUUID().toString(), invalidName)));
	}

	@Test
	void shouldUpdateExistAuthor() throws Exception {
		final String oldFirstName = String.format("  some_firstName_%s     ", UUID.randomUUID());
		final String oldLastName = "author_lastName_" + UUID.randomUUID();
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateAuthorBody(oldFirstName, oldLastName)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);
		final String newFirstName = "new_" + oldFirstName;
		final String newLastName = "new_" + oldLastName;

		mockMvc.perform(
				MockMvcRequestBuilders.patch(basePath + "/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildUpdateAuthorBody(newFirstName, newLastName)))
				.andExpect(MockMvcResultMatchers.status().isOk());

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.first_name").value(newFirstName.trim()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.last_name").value(newLastName.trim()));
	}

	@ParameterizedTest(name = "{displayName}: {argumentsWithNames}")
	@MethodSource("invalidUpdateArgs")
	void shouldReturnErrorWhenUpdateAuthorWithBlankField(String newFirstName, String newLastName) throws Exception {
		final String oldFirstName = String.format("  some_firstName_%s     ", UUID.randomUUID());
		final String oldLastName = "author_lastName_" + UUID.randomUUID();
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateAuthorBody(oldFirstName, oldLastName)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		mockMvc.perform(
				MockMvcRequestBuilders.patch(basePath + "/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildUpdateAuthorBody(newFirstName, newLastName)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.first_name").value(oldFirstName.trim()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.last_name").value(oldLastName.trim()));

	}

	static Stream<Arguments> invalidUpdateArgs() {
		return Stream.of("", "   ", null).flatMap(invalidName -> Stream.of(
				Arguments.of(invalidName, UUID.randomUUID().toString()),
				Arguments.of(UUID.randomUUID().toString(), invalidName)));
	}

	private String buildUpdateAuthorBody(String newFirstName, String newLastName) {
		final var body = jsonMapper.createArrayNode();
		final JsonNode newFirstNameNode = newFirstName == null
				? NullNode.getInstance()
				: TextNode.valueOf(newFirstName);
		body.add(replaceOpFrom("/first_name", newFirstNameNode));
		final JsonNode newLastNameNode = newLastName == null
				? NullNode.getInstance()
				: TextNode.valueOf(newLastName);
		body.add(replaceOpFrom("/last_name", newLastNameNode));
		return body.toPrettyString();

	}

	@Test
	void shouldDeleteExistAuthor() throws Exception {
		final String oldFirstName = String.format("  some_firstName_%s     ", UUID.randomUUID());
		final String oldLastName = "author_lastName_" + UUID.randomUUID();
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateAuthorBody(oldFirstName, oldLastName)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		mockMvc.perform(MockMvcRequestBuilders.delete(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isNotFound());

	}

	private String buildCreateAuthorBody(String firstName, String lastName) {
		final var body = jsonMapper.createObjectNode();
		if (firstName != null) {
			body.put("first_name", firstName);
		}
		if (lastName != null) {
			body.put("last_name", lastName);
		}
		return body.toPrettyString();
	}
}
