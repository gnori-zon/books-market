package org.gnori.booksmarket.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.gnori.booksmarket.core.files.S3FileStorage;
import org.gnori.booksmarket.feauture.attachments.AttachmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;

class BookIntegrationTest extends IntegrationTest {

	private final String basePath = "/api/v1/books";
	private final String attachmentBasePath = "/api/v1/attachments";
	private final String temporaryBucket = "tmp";
	private final String permanentBucket = "perm";

	@Autowired
	private AttachmentService attachmentService;

	@Autowired
	private S3FileStorage fileStorage;

	@Test
	void shouldReturnCreatedBookWithAttachments() throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final String coverAttachmentId = uploadCover();
		final String contentAttachmentId = uploadContent();

		final String name = "Book " + UUID.randomUUID();
		final String description = "Description " + UUID.randomUUID();
		final String language = "English";
		final LocalDate releaseDate = LocalDate.of(2024, 1, 15);

		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody(name, description, language, releaseDate,
								publisherId,
								Set.of(authorId), Set.of(genreId), coverAttachmentId,
								contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description").value(description))
				.andExpect(MockMvcResultMatchers.jsonPath("$.language.value").value(language))
				.andExpect(MockMvcResultMatchers.jsonPath("$.language.displayName").value(language))
				.andExpect(MockMvcResultMatchers.jsonPath("$.releaseDate")
						.value(releaseDate.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.publisher.id")
						.value(publisherId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.authors['%s'].id".formatted(authorId))
						.value(authorId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.genres['%s'].id".formatted(genreId))
						.value(genreId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.coverAttachmentId")
						.value(coverAttachmentId))
				.andExpect(MockMvcResultMatchers.jsonPath("$.contentAttachmentId")
						.value(contentAttachmentId))
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		assertAttachmentInPermanent(coverAttachmentId);
		assertAttachmentInPermanent(contentAttachmentId);

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.coverAttachmentId")
						.value(coverAttachmentId))
				.andExpect(MockMvcResultMatchers.jsonPath("$.contentAttachmentId")
						.value(contentAttachmentId));
	}

	@Test
	void shouldReturnExistBook() throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final String contentAttachmentId = uploadContent();

		final String name = "Book " + UUID.randomUUID();
		final String description = "Description " + UUID.randomUUID();
		final String language = "English";
		final LocalDate releaseDate = LocalDate.of(2024, 1, 15);

		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody(name, description, language, releaseDate,
								publisherId,
								Set.of(authorId), Set.of(genreId), null,
								contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description").value(description))
				.andExpect(MockMvcResultMatchers.jsonPath("$.language.value").value(language))
				.andExpect(MockMvcResultMatchers.jsonPath("$.releaseDate")
						.value(releaseDate.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.publisher.id")
						.value(publisherId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.authors['%s'].id".formatted(authorId))
						.value(authorId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.genres['%s'].id".formatted(genreId))
						.value(genreId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.coverAttachmentId").doesNotExist())
				.andExpect(MockMvcResultMatchers.jsonPath("$.contentAttachmentId")
						.value(contentAttachmentId));
	}

	@Test
	void shouldUpdateExistBook() throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final UUID newPublisherId = createPublisher();
		final UUID newAuthorId = createAuthor();
		final UUID newGenreId = createGenre();
		final String contentAttachmentId = uploadContent();

		final String name = "Book " + UUID.randomUUID();
		final String description = "Description " + UUID.randomUUID();
		final String language = "English";
		final LocalDate releaseDate = LocalDate.of(2024, 1, 15);

		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody(name, description, language, releaseDate,
								publisherId,
								Set.of(authorId), Set.of(genreId), null,
								contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		final String newName = "New " + name;
		final String newDescription = "New " + description;
		final String newLanguage = "Russian";
		final LocalDate newReleaseDate = LocalDate.of(2025, 6, 20);

		final ArrayNode patch = jsonMapper.createArrayNode();
		patch.add(replaceOpFrom("/name", TextNode.valueOf(newName)));
		patch.add(replaceOpFrom("/description", TextNode.valueOf(newDescription)));
		patch.add(replaceOpFrom("/language", TextNode.valueOf(newLanguage)));
		patch.add(replaceOpFrom("/releaseDate", TextNode.valueOf(newReleaseDate.toString())));
		patch.add(replaceOpFrom("/publisher", TextNode.valueOf(newPublisherId.toString())));
		patch.add(jsonMapper.createObjectNode()
				.put("op", "add")
				.put("path", "/authors/" + newAuthorId));
		patch.add(jsonMapper.createObjectNode()
				.put("op", "remove")
				.put("path", "/authors/" + authorId));
		patch.add(jsonMapper.createObjectNode()
				.put("op", "add")
				.put("path", "/genres/" + newGenreId));
		patch.add(jsonMapper.createObjectNode()
				.put("op", "remove")
				.put("path", "/genres/" + genreId));

		mockMvc.perform(
				MockMvcRequestBuilders.patch(basePath + "/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(patch.toPrettyString()))
				.andExpect(MockMvcResultMatchers.status().isOk());

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(id))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(newName))
				.andExpect(MockMvcResultMatchers.jsonPath("$.description").value(newDescription))
				.andExpect(MockMvcResultMatchers.jsonPath("$.language.value").value(newLanguage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.language.displayName").value(newLanguage))
				.andExpect(MockMvcResultMatchers.jsonPath("$.releaseDate")
						.value(newReleaseDate.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.publisher.id")
						.value(newPublisherId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.authors['%s'].id".formatted(newAuthorId))
						.value(newAuthorId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.authors['%s']".formatted(authorId))
						.doesNotExist())
				.andExpect(MockMvcResultMatchers.jsonPath("$.genres['%s'].id".formatted(newGenreId))
						.value(newGenreId.toString()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.genres['%s']".formatted(genreId))
						.doesNotExist());
	}

	@Test
	void shouldUpdateExistBookAttachments() throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final String oldCoverAttachmentId = uploadCover();
		final String oldContentAttachmentId = uploadContent();

		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Book " + UUID.randomUUID(), "Desc",
								"English",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(authorId), Set.of(genreId),
								oldCoverAttachmentId, oldContentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		assertAttachmentInPermanent(oldCoverAttachmentId);
		assertAttachmentInPermanent(oldContentAttachmentId);

		final String newCoverAttachmentId = uploadCover();
		final String newContentAttachmentId = uploadContent();

		final ArrayNode patch = jsonMapper.createArrayNode();
		patch.add(replaceOpFrom("/coverAttachmentId", TextNode.valueOf(newCoverAttachmentId)));
		patch.add(replaceOpFrom("/contentAttachmentId", TextNode.valueOf(newContentAttachmentId)));

		mockMvc.perform(
				MockMvcRequestBuilders.patch(basePath + "/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(patch.toPrettyString()))
				.andExpect(MockMvcResultMatchers.status().isOk());

		assertAttachmentInPermanent(newCoverAttachmentId);
		assertAttachmentInPermanent(newContentAttachmentId);
		assertAttachmentNotInPermanent(oldCoverAttachmentId);
		assertAttachmentNotInPermanent(oldContentAttachmentId);
	}

	@Test
	void shouldDeleteExistBookAndAttachments() throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final String coverAttachmentId = uploadCover();
		final String contentAttachmentId = uploadContent();

		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Book " + UUID.randomUUID(), "Desc",
								"English",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(authorId), Set.of(genreId),
								coverAttachmentId, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		assertAttachmentInPermanent(coverAttachmentId);
		assertAttachmentInPermanent(contentAttachmentId);

		mockMvc.perform(MockMvcRequestBuilders.delete(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isNoContent());

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isNotFound());

		assertAttachmentNotInPermanent(coverAttachmentId);
		assertAttachmentNotInPermanent(contentAttachmentId);
	}

	@ParameterizedTest(name = "{displayName}: {argumentsWithNames}")
	@MethodSource("invalidNameArgs")
	void shouldReturnErrorWhenCreateBookWithBlankName(String invalidName) throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final String contentAttachmentId = uploadContent();

		mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody(invalidName, "Description", "English",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(authorId), Set.of(genreId),
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	static Stream<Arguments> invalidNameArgs() {
		return Stream.of(
				Arguments.of((String) null),
				Arguments.of(""),
				Arguments.of("   "),
				Arguments.of("a".repeat(256)));
	}

	@ParameterizedTest(name = "{displayName}: {argumentsWithNames}")
	@MethodSource("invalidDescriptionArgs")
	void shouldReturnErrorWhenCreateBookWithBlankDescription(String invalidDescription) throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final String contentAttachmentId = uploadContent();

		mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", invalidDescription, "English",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(authorId), Set.of(genreId),
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	static Stream<Arguments> invalidDescriptionArgs() {
		return Stream.of(
				Arguments.of((String) null),
				Arguments.of(""),
				Arguments.of("   "),
				Arguments.of("a".repeat(32_768)));
	}

	@Test
	void shouldReturnErrorWhenCreateBookWithInvalidLanguage() throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final String contentAttachmentId = uploadContent();

		mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", "Description", "InvalidLanguage",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(authorId), Set.of(genreId),
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@ParameterizedTest(name = "{displayName}: {argumentsWithNames}")
	@MethodSource("invalidAuthorsSizeArgs")
	void shouldReturnErrorWhenCreateBookWithInvalidAuthorsSize(Set<UUID> authorIds) throws Exception {
		final UUID publisherId = createPublisher();
		final UUID genreId = createGenre();
		final String contentAttachmentId = uploadContent();

		mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", "Description", "English",
								LocalDate.of(2024, 1, 15), publisherId, authorIds,
								Set.of(genreId),
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	static Stream<Arguments> invalidAuthorsSizeArgs() {
		return Stream.of(
				Arguments.of(Set.of()),
				Arguments.of(new HashSet<>(Stream.generate(UUID::randomUUID).limit(11).toList())));
	}

	@ParameterizedTest(name = "{displayName}: {argumentsWithNames}")
	@MethodSource("invalidGenresSizeArgs")
	void shouldReturnErrorWhenCreateBookWithInvalidGenresSize(Set<UUID> genreIds) throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final String contentAttachmentId = uploadContent();

		mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", "Description", "English",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(authorId), genreIds,
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	static Stream<Arguments> invalidGenresSizeArgs() {
		return Stream.of(
				Arguments.of(Set.of()),
				Arguments.of(new HashSet<>(Stream.generate(UUID::randomUUID).limit(11).toList())));
	}

	@Test
	void shouldReturnErrorWhenCreateBookWithNotFoundPublisher() throws Exception {
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final String contentAttachmentId = uploadContent();

		mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", "Description", "English",
								LocalDate.of(2024, 1, 15), UUID.randomUUID(),
								Set.of(authorId), Set.of(genreId),
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	void shouldReturnErrorWhenCreateBookWithNotFoundAuthor() throws Exception {
		final UUID publisherId = createPublisher();
		final UUID genreId = createGenre();
		final String contentAttachmentId = uploadContent();

		mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", "Description", "English",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(UUID.randomUUID()), Set.of(genreId),
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	void shouldReturnErrorWhenCreateBookWithNotFoundGenre() throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final String contentAttachmentId = uploadContent();

		mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", "Description", "English",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(authorId), Set.of(UUID.randomUUID()),
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@ParameterizedTest(name = "{displayName}: {argumentsWithNames}")
	@MethodSource("invalidNameArgs")
	void shouldReturnErrorWhenUpdateBookWithBlankName(String invalidName) throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final String contentAttachmentId = uploadContent();

		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", "Description", "English",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(authorId), Set.of(genreId),
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		final JsonNode valueNode = invalidName == null
				? NullNode.getInstance()
				: TextNode.valueOf(invalidName);
		final ArrayNode patch = jsonMapper.createArrayNode();
		patch.add(replaceOpFrom("/name", valueNode));

		mockMvc.perform(
				MockMvcRequestBuilders.patch(basePath + "/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(patch.toPrettyString()))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Name"));
	}

	@ParameterizedTest(name = "{displayName}: {argumentsWithNames}")
	@MethodSource("invalidDescriptionArgs")
	void shouldReturnErrorWhenUpdateBookWithBlankDescription(String invalidDescription) throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final UUID genreId = createGenre();
		final String contentAttachmentId = uploadContent();

		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", "Description", "English",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(authorId), Set.of(genreId),
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);

		final JsonNode valueNode = invalidDescription == null
				? NullNode.getInstance()
				: TextNode.valueOf(invalidDescription);
		final ArrayNode patch = jsonMapper.createArrayNode();
		patch.add(replaceOpFrom("/description", valueNode));

		mockMvc.perform(
				MockMvcRequestBuilders.patch(basePath + "/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(patch.toPrettyString()))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());

		mockMvc.perform(MockMvcRequestBuilders.get(basePath + "/" + id))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Description"));
	}

	@Test
	void shouldReturnErrorWhenUpdateBookWithInvalidAuthorsSize() throws Exception {
		final UUID publisherId = createPublisher();
		final Set<UUID> authorIds = new HashSet<>();
		for (int i = 0; i < 10; i++) {
			authorIds.add(createAuthor());
		}
		final UUID genreId = createGenre();
		final String contentAttachmentId = uploadContent();

		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", "Description", "English",
								LocalDate.of(2024, 1, 15), publisherId, authorIds,
								Set.of(genreId),
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);
		final UUID newAuthorId = createAuthor();

		final ArrayNode patch = jsonMapper.createArrayNode();
		patch.add(jsonMapper.createObjectNode()
				.put("op", "add")
				.put("path", "/authors/" + newAuthorId));

		mockMvc.perform(
				MockMvcRequestBuilders.patch(basePath + "/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(patch.toPrettyString()))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void shouldReturnErrorWhenUpdateBookWithInvalidGenresSize() throws Exception {
		final UUID publisherId = createPublisher();
		final UUID authorId = createAuthor();
		final Set<UUID> genreIds = new HashSet<>();
		for (int i = 0; i < 10; i++) {
			genreIds.add(createGenre());
		}
		final String contentAttachmentId = uploadContent();

		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post(basePath)
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateBookBody("Name", "Description", "English",
								LocalDate.of(2024, 1, 15), publisherId,
								Set.of(authorId), genreIds,
								null, contentAttachmentId)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		final String id = parseIdFrom(response);
		final UUID newGenreId = createGenre();

		final ArrayNode patch = jsonMapper.createArrayNode();
		patch.add(jsonMapper.createObjectNode()
				.put("op", "add")
				.put("path", "/genres/" + newGenreId));

		mockMvc.perform(
				MockMvcRequestBuilders.patch(basePath + "/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(patch.toPrettyString()))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	private UUID createPublisher() throws Exception {
		final String name = "Publisher " + UUID.randomUUID();
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/v1/publishers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreatePublisherBody(name)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return UUID.fromString(parseIdFrom(response));
	}

	private UUID createAuthor() throws Exception {
		final String firstName = "First " + UUID.randomUUID();
		final String lastName = "Last " + UUID.randomUUID();
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/v1/authors")
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateAuthorBody(firstName, lastName)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return UUID.fromString(parseIdFrom(response));
	}

	private UUID createGenre() throws Exception {
		final String name = "Genre " + UUID.randomUUID();
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.post("/api/v1/genres")
						.contentType(MediaType.APPLICATION_JSON)
						.content(buildCreateGenreBody(name)))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return UUID.fromString(parseIdFrom(response));
	}

	private String uploadCover() throws Exception {
		final MockMultipartFile file = new MockMultipartFile(
				"file", "cover.jpg", "image/jpeg", new byte[] { 0x01, 0x02, 0x03 });
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.multipart(attachmentBasePath + "/temporary/cover")
						.file(file))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return parseIdFrom(response);
	}

	private String uploadContent() throws Exception {
		final MockMultipartFile file = new MockMultipartFile(
				"file", "content.pdf", "application/pdf", new byte[] { 0x04, 0x05, 0x06 });
		final String response = mockMvc.perform(
				MockMvcRequestBuilders.multipart(attachmentBasePath + "/temporary/content")
						.file(file))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return parseIdFrom(response);
	}

	private String buildCreateBookBody(String name, String description, String language,
			LocalDate releaseDate, UUID publisherId, Set<UUID> authorIds, Set<UUID> genreIds,
			String coverAttachmentId, String contentAttachmentId) {
		final var body = jsonMapper.createObjectNode();
		if (name != null) {
			body.put("name", name);
		}
		if (description != null) {
			body.put("description", description);
		}
		if (language != null) {
			body.put("language", language);
		}
		if (releaseDate != null) {
			body.put("releaseDate", releaseDate.toString());
		}
		if (publisherId != null) {
			body.put("publisherId", publisherId.toString());
		}
		if (authorIds != null) {
			final var array = jsonMapper.createArrayNode();
			authorIds.forEach(id -> array.add(id.toString()));
			body.set("authorIds", array);
		}
		if (genreIds != null) {
			final var array = jsonMapper.createArrayNode();
			genreIds.forEach(id -> array.add(id.toString()));
			body.set("genreIds", array);
		}
		if (coverAttachmentId != null) {
			body.put("coverAttachmentId", coverAttachmentId);
		}
		if (contentAttachmentId != null) {
			body.put("contentAttachmentId", contentAttachmentId);
		}
		return body.toPrettyString();
	}

	private String buildCreatePublisherBody(String name) {
		final var body = jsonMapper.createObjectNode();
		body.put("name", name);
		return body.toPrettyString();
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

	private String buildCreateGenreBody(String name) {
		final var body = jsonMapper.createObjectNode();
		body.put("name", name);
		return body.toPrettyString();
	}

	private void assertAttachmentInPermanent(String attachmentId) {
		assertTrue(fileStorage.exists(permanentBucket, unmask(attachmentId)),
				"Attachment should exist in permanent bucket");
	}

	private void assertAttachmentNotInPermanent(String attachmentId) {
		assertFalse(fileStorage.exists(permanentBucket, unmask(attachmentId)),
				"Attachment should not exist in permanent bucket");
	}

	private String unmask(String value) {
		return new String(Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8)),
				StandardCharsets.UTF_8);
	}
}
