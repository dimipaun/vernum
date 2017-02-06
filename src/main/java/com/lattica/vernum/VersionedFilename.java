package com.lattica.vernum;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = { "originalName" })
@ToString
public class VersionedFilename implements Comparable<VersionedFilename> {
	private FilenameParts parts;
	private final String originalName;

	public VersionedFilename(String filename) {
		this.parts = new FilenameParts(filename);
		this.originalName = parts.getOriginalName();
	}

	public boolean isOriginal() {
		return parts.getVersion() == null;
	}

	@Override
	public int compareTo(VersionedFilename o) {
		if (this.parts.getVersion() == null) {
			if (o.parts.getVersion() == null) {
				return this.originalName.compareTo(o.originalName);
			}
			return -1;
		}
		if (o.parts.getVersion() == null) {
			return -1;
		}
		return -this.parts.getVersion().compareTo(o.parts.getVersion());
	}
}