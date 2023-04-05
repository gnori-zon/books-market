package org.gnori.booksmarket.api.controller;


import java.io.IOException;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.gnori.booksmarket.api.exception.BadRequestException;
import org.gnori.booksmarket.api.exception.InternalServerError;
import org.gnori.booksmarket.api.exception.NotFoundException;
import org.gnori.booksmarket.service.FileService;
import org.gnori.booksmarket.storage.dao.BinaryContentDao;
import org.gnori.booksmarket.storage.dao.BookDao;
import org.gnori.booksmarket.storage.entity.BinaryContentEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/binary-content")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BinaryContentController {

  BookDao bookDao;

  BinaryContentDao binaryContentDao;

  FileService fileService;


  @GetMapping("/{id}/image")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> getImage(
      @PathVariable Long id) {

    var binaryContent = binaryContentDao.findById(id);

    if(binaryContent.isEmpty() || binaryContent.get().getRaw() == null) {
      throw new NotFoundException(String.format("Binary content with id: %d does not exist",id));
    }
    var file = binaryContent.get();
    var type = file.getTypeImage();
    var bytes = file.getImage();

    var fileSystemResource = fileService.getFilesystemResource(bytes, "image_" + id);

    if(fileSystemResource==null) {
      throw new InternalServerError("InternalServerError");
    }

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(type))
        .header("Content-disposition","attachment;")
        .body(fileSystemResource);
    }

  @GetMapping("/{id}/raw")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<?> getRaw(
      @PathVariable Long id) {

    var binaryContent = binaryContentDao.findById(id);

    if(binaryContent.isEmpty() || binaryContent.get().getRaw() == null) {
      throw new NotFoundException(String.format("Binary content with id: %d does not exist",id));
    }
    var file = binaryContent.get();
    var type = file.getTypeRaw();
    var bytes = file.getRaw();

    var fileSystemResource = fileService.getFilesystemResource(bytes, "raw_" + id);

    if(fileSystemResource==null) {
      throw new InternalServerError("InternalServerError");
    }

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(type))
        .header("Content-disposition","attachment;")
        .body(fileSystemResource);
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  public void uploadBinaryContent(
      @RequestParam(name = "book_id") Long bookId,
      @RequestPart(required = false) Optional<MultipartFile> raw,
      @RequestPart(required = false) Optional<MultipartFile> image) throws IOException {

    if (raw.isEmpty() && image.isEmpty()) {
      throw new BadRequestException("Raw and image params is empty.");
    }

    var book = bookDao.findById(bookId);

    if (book.isEmpty()) {
      throw new NotFoundException(String.format("Book with id: %d does not exist", bookId));
    }

    var binaryContent = BinaryContentEntity.builder()
        .id((book.get().getId()))
        .build();

    if(raw.isPresent()) {
      var file = raw.get();

      var bytes = file.getBytes();
      var size = file.getSize() / 1_000_000D;

      binaryContent.setRaw(bytes);
      binaryContent.setTypeRaw(file.getContentType());
      binaryContent.setSizeRaw(size);
    }

    if(image.isPresent()) {
      var img = image.get();

      var bytes = img.getBytes();

      binaryContent.setImage(bytes);
      binaryContent.setTypeImage(img.getContentType());

    }

    binaryContentDao.saveAndFlush(binaryContent);

  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteBinaryContent(
      @PathVariable Long id) {

    if (!binaryContentDao.existsById(id)){
      throw new NotFoundException(String.format("Binary content with id: %d doesn't exist.", id));
    }

    binaryContentDao.deleteById(id);
  }


}
