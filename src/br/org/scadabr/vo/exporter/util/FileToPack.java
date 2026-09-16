package br.org.scadamy.vo.exporter.util;

import java.io.File;

public class FileToPack {

	private String fileNameWhenPacked;
	private File file;

	public FileToPack(String fileNameWhenPacked, File file) {
		this.fileNameWhenPacked = fileNameWhenPacked;
		this.file = file;
	}

	public String getFileNameWhenPacked() {
		return fileNameWhenPacked;
	}

	public File getFile() {
		return file;
	}

}
