package cz.expertkom.ju.L08HomeWorkKosikCz.services;

public class ResourcePages {
	
	private String nameTextFile;
	private String uri;
	
	public ResourcePages(String nameTextFile, String uri) {
		this.nameTextFile = nameTextFile;
		this.uri = uri;
	}
	
	public String getNameTextFile() {
		return nameTextFile;
	}

	public void setNameTextFile(String nameTextFile) {
		this.nameTextFile = nameTextFile;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	@Override
	public String toString() {
		return "ResourcePages [nameTextFile=" + nameTextFile + ", uri=" + uri + "]";
	}
	
	

}
