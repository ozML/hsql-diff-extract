package de.ozml.hsqldiffextract.res;

public class ArgumentInfo {

	private String key;
	private String title;
	private String format;
	private String description;

	public ArgumentInfo() {}

	public ArgumentInfo(String key, String title, String format, String description) {
		this.key = key;
		this.title = title;
		this.format = format;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}