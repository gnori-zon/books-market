package org.gnori.booksmarket.api.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.InternalServerError;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.security.config.JwtService;
import org.gnori.booksmarket.service.FileService;
import org.gnori.booksmarket.storage.dao.BinaryContentDao;
import org.gnori.booksmarket.storage.dao.BookDao;
import org.gnori.booksmarket.storage.entity.BinaryContentEntity;
import org.gnori.booksmarket.storage.entity.BookEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;

@TestInstance(Lifecycle.PER_CLASS)
@SpringJUnitWebConfig
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = BinaryContentController.class)
class BinaryContentControllerTest {

  @MockBean
  BookDao bookDao;

  @MockBean
  BinaryContentDao binaryContentDao;

  @MockBean
  FileService fileService;

  @MockBean
  JwtService jwtService;

  @Autowired
  MockMvc mockMvc;

  private Long id;
  private BinaryContentEntity raw;

  @BeforeAll
  void init() {
    id = 1L;
    raw = BinaryContentEntity.builder()
        .id(id)
        .image(new byte[]{1,0,0,1})
        .raw(new byte[]{0,1,1,0})
        .sizeRaw(12D)
        .typeImage(MediaType.IMAGE_PNG_VALUE)
        .typeRaw(MediaType.APPLICATION_PDF_VALUE).build();
  }

  private static final String BINARY_CONTENT_URL = "/api/binary-content";

  @Test
  void getImageShouldReturnResponseEntityAndOk() throws Exception {

    var file = File.createTempFile("test","bin");
    file.deleteOnExit();

    when(binaryContentDao.findById(id)).thenReturn(Optional.of(raw));
    when(fileService.getFilesystemResource(raw.getImage(), "image_" + id)).thenReturn(new FileSystemResource(file));

    mockMvc.perform(get(BINARY_CONTENT_URL+"/"+id+"/image"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.parseMediaType(raw.getTypeImage())));

    Files.delete(Path.of(file.getPath()));
  }

  @Test
  void getImageShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    final String exceptionMessage = "Binary content with id: %d does not exist";

    when(binaryContentDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(get(BINARY_CONTENT_URL+"/"+id+"/image"))
        .andExpect(status().isNotFound())
        .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> Assertions.assertEquals(String.format(exceptionMessage,id),
                Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void getImageShouldThrowInternalServerErrorAndInternalServerError() throws Exception {

    final String exceptionMessage = "Internal server error";

    when(binaryContentDao.findById(id)).thenReturn(Optional.of(raw));
    when(fileService.getFilesystemResource(raw.getImage(), "image_"+id)).thenReturn(null);

    mockMvc.perform(get(BINARY_CONTENT_URL+"/"+id+"/image"))
        .andExpect(status().isInternalServerError())
        .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof InternalServerError))
        .andExpect(result -> Assertions.assertEquals(exceptionMessage,
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void getRawShouldReturnResponseEntityAndOk() throws Exception {

    var file = File.createTempFile("test","bin");
    file.deleteOnExit();

    when(binaryContentDao.findById(id)).thenReturn(Optional.of(raw));
    when(fileService.getFilesystemResource(raw.getRaw(), "raw_" + id)).thenReturn(new FileSystemResource(file));

    mockMvc.perform(get(BINARY_CONTENT_URL+"/"+id+"/raw"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.parseMediaType(raw.getTypeRaw())));

    Files.delete(Path.of(file.getPath()));
  }

  @Test
  void getRawShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    final String exceptionMessage = "Binary content with id: %d does not exist";

    when(binaryContentDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(get(BINARY_CONTENT_URL+"/"+id+"/raw"))
        .andExpect(status().isNotFound())
        .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> Assertions.assertEquals(String.format(exceptionMessage,id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void getRawShouldThrowInternalServerErrorAndInternalServerError() throws Exception {

    final String exceptionMessage = "Internal server error";

    when(binaryContentDao.findById(id)).thenReturn(Optional.of(raw));
    when(fileService.getFilesystemResource(raw.getImage(), "raw_"+id)).thenReturn(null);

    mockMvc.perform(get(BINARY_CONTENT_URL+"/"+id+"/raw"))
        .andExpect(status().isInternalServerError())
        .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof InternalServerError))
        .andExpect(result -> Assertions.assertEquals(exceptionMessage,
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void uploadBinaryContentShouldOk() throws Exception {

    var file1 = new MockMultipartFile("raw", raw.getRaw());
    var file2 = new MockMultipartFile("image", raw.getImage());

    var raw = BookEntity.builder().id(id).build();

    when(bookDao.findById(id)).thenReturn(Optional.of(raw));

    mockMvc.perform(multipart(BINARY_CONTENT_URL + "?book_id=" + id)
            .file(file1)
            .file(file2)
        .with(rq -> { rq.setMethod("PUT"); return rq; }))
        .andExpect(status().isOk());

    verify(binaryContentDao).saveAndFlush(Mockito.any());
  }

  @Test
  void uploadBinaryContentShouldThrowBadRequestExceptionAndBadRequest() throws Exception {

    final String exceptionMessage = "Raw and image params is empty.";

    mockMvc.perform(put(BINARY_CONTENT_URL + "?book_id=" + id))
        .andExpect(status().isBadRequest())
        .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof BadRequestException))
        .andExpect(result -> Assertions.assertEquals(exceptionMessage,
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void uploadBinaryContentShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    var file = new MockMultipartFile("raw",new byte[]{});

    final String exceptionMessage = "Book with id: %d does not exist";

    when(bookDao.findById(id)).thenReturn(Optional.empty());

    mockMvc.perform(multipart(BINARY_CONTENT_URL + "?book_id=" + id)
            .file(file)
            .with(rq -> { rq.setMethod("PUT"); return rq; }))
        .andExpect(status().isNotFound())
        .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> Assertions.assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));
  }

  @Test
  void deleteBinaryContentShouldReturnOk() throws Exception {

    when(binaryContentDao.existsById(id)).thenReturn(true);

    mockMvc.perform(delete(BINARY_CONTENT_URL + "/" + id))
        .andExpect(status().isOk());

    verify(binaryContentDao).deleteById(id);
  }

  @Test
  void deleteBinaryContentShouldThrowNotFoundExceptionAndNotFound() throws Exception {

    final String exceptionMessage = "Binary content with id: %d doesn't exist.";

    when(binaryContentDao.existsById(id)).thenReturn(false);

    mockMvc.perform(delete(BINARY_CONTENT_URL + "/" + id))
        .andExpect(status().isNotFound())
        .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof NotFoundException))
        .andExpect(result -> Assertions.assertEquals(String.format(exceptionMessage, id),
            Objects.requireNonNull(result.getResolvedException()).getMessage()));

    verify(binaryContentDao, never()).deleteById(id);
  }
}