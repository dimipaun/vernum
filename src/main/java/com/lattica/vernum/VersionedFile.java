package com.lattica.vernum;

import java.io.File;

import lombok.Getter;

@Getter
public class VersionedFile extends VersionedFilename {
	private File file;

	public VersionedFile(File file) {
		super (file.getName());
		
		this.file = file;
	}
}
