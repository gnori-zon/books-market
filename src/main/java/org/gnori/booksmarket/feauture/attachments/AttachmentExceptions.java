package org.gnori.booksmarket.feauture.attachments;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.stream.Collectors;

import org.gnori.booksmarket.core.exception.AppException;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AttachmentExceptions {

	@Getter
	@Accessors(fluent = true)
	@AllArgsConstructor
	public enum Type implements AppException.Type.Base {
		INVALID_FILE_TYPE("invalidFileType", HttpStatus.BAD_REQUEST),
		EXCEED_FILE_SIZE("exceedFileSize", HttpStatus.BAD_REQUEST);

		private final String key;
		private final HttpStatus status;

		private static final String ATTACHMENT_EXCEPTION_PATTERNS_BUNDLE = "AttachmentExceptionPatterns";

		@Override
		public String bundle() {
			return ATTACHMENT_EXCEPTION_PATTERNS_BUNDLE;
		}
	}

	public static AppException invalidCoverFileType(
			Collection<String> wantTypes,
			String gotType) {
		return new AppException(Type.INVALID_FILE_TYPE,
				new Object[] { "cover", wantTypes.stream().collect(Collectors.joining(",")), gotType });
	}

	public static AppException invalidContentFileType(
			Collection<String> wantTypes,
			String gotType) {
		return new AppException(Type.INVALID_FILE_TYPE, new Object[] { "content",
				wantTypes.stream().collect(Collectors.joining(",")), gotType });
	}

	public static AppException exceedCoverMaxSize(long wantSizeBytes, long gotBytesSize) {
		return new AppException(Type.EXCEED_FILE_SIZE, new Object[] { "cover", wantSizeBytes, gotBytesSize });
	}

	public static AppException exceedContentMaxSize(long wantSizeBytes, long gotBytesSize) {
		return new AppException(Type.EXCEED_FILE_SIZE, new Object[] { "content", wantSizeBytes, gotBytesSize });
	}
}
