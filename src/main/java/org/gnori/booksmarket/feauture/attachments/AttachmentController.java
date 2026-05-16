package org.gnori.booksmarket.feauture.attachments;

import java.net.URI;

import org.gnori.booksmarket.core.exception.BaseHttpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {
	private final AttachmentService service;

	@PostMapping(value = "/temporary/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public SavedAttachmentDto saveTemporary(
			@PathVariable("type") String type,
			@RequestPart("file") MultipartFile file) {
		final Attachment attachment = switch (AttachmentTypeDto.from(type)) {
			case COVER -> new Attachment.Cover(file);
			case CONTENT -> new Attachment.Content(file);
			default -> throw BaseHttpException.invalidField("type", String.valueOf(type));
		};
		final String id = service.saveToTemporary(attachment);
		return new SavedAttachmentDto(id);
	}

	@GetMapping("/permanent/{id}")
	public ResponseEntity<Void> getPermanent(@PathVariable("id") String attachmentId) {
		final URI attachmentUri = service.getUrlForPermanent(attachmentId);
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(attachmentUri)
				.build();
	}
}
