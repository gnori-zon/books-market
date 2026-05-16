package org.gnori.booksmarket.feauture.attachments;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttachmentTypeDto {
	COVER("cover"),
	CONTENT("content");

	private String value;

	private static final Map<String, AttachmentTypeDto> CASE_BY_VALUE = Arrays.stream(values())
			.collect(Collectors.toMap(type -> type.value().toLowerCase(), Function.identity()));

	@JsonCreator
	public static AttachmentTypeDto from(String value) {
		if (value == null) {
			return null;
		}
		return CASE_BY_VALUE.get(value.trim().toLowerCase());
	}

	@JsonValue
	public String value() {
		return value;
	}
}
