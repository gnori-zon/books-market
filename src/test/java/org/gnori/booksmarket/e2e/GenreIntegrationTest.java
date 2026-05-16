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

class GenreIntegrationTest extends IntegrationTest {
	private final String basePath = "/api/v1/genres";

	@Test
	void shouldReturnCreatedGenre() throws Exception {
		final String name = "  some_genre_name_" + UUID.randomUUID() + "     ";
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateGenreBody(name)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name.trim().toLowerCase()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name.trim().toLowerCase()));
	}

	@ParameterizedTest(name = "{displayName}: {argumentsWithNames}")
	@MethodSource("invalidCreateArgs")
	void shouldReturnErrorWhenCreateGenreWithBlankField(String name) throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateGenreBody(name)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	static Stream<Arguments> invalidCreateArgs() {
		return Stream.of(
				Arguments.of(""),
				Arguments.of("   "),
				Arguments.of((String) null));
	}

	@Test
	void shouldUpdateExistGenre() throws Exception {
		final String oldName = "  some_genre_name_" + UUID.randomUUID() + "     ";
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateGenreBody(oldName)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);
		final String newName = "new_" + oldName;

		mockMvc.perform(
				MockMvcRequestBuilders.patch(basePath + "/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildUpdateGenreBody(newName)))
				.andExpect(MockMvcResultMatchers.status().isOk());

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name")
						.value(newName.trim().toLowerCase()));
	}

	@ParameterizedTest(name = "{displayName}: {argumentsWithNames}")
	@MethodSource("invalidUpdateArgs")
	void shouldReturnErrorWhenUpdateGenreWithBlankName(String newName) throws Exception {
		final String oldName = String.format("  some_name_%s     ", UUID.randomUUID());
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateGenreBody(oldName)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		mockMvc.perform(
				MockMvcRequestBuilders.patch(basePath + "/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildUpdateGenreBody(newName)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(oldName.trim()));

	}

	static Stream<Arguments> invalidUpdateArgs() {
		return Stream.of(Arguments.of(""), Arguments.of("   "), Arguments.of((String) null));
	}

	@Test
	void shouldDeleteExistGenre() throws Exception {
		final String name = "  some_genre_name_" + UUID.randomUUID() + "     ";
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateGenreBody(name)))
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

	private String buildCreateGenreBody(String name) {
		final var body = jsonMapper.createObjectNode();
		if (name != null) {
			body.put("name", name);
		}
		return body.toPrettyString();
	}

	private String buildUpdateGenreBody(String newName) {
		final var body = jsonMapper.createArrayNode();
		final JsonNode newNameNode = newName == null ? NullNode.getInstance() : TextNode.valueOf(newName);
		body.add(replaceOpFrom("/name", newNameNode));
		return body.toPrettyString();
	}
}
