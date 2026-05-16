package org.gnori.booksmarket.feauture.attachments;

import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AttachmentScheduleCleaner {

	private final AttachmentService service;

	@Scheduled(cron = "0 0 * * * ?")
	public void cleanAttachmentsByPreviousHours() {
		service.cleanTemporaryByHour(now -> now.minusHours(2));
	}
}
