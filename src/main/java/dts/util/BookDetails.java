package dts.util;

public class BookDetails {
	
	private String title;
	private String genre;
	private String abstractSummary;
	private String author;
	
	public BookDetails() {
		
	}

	public BookDetails(String title, String genre, String abstractSummary, String author) {
		super();
		setTitle(title);
		setGenre(genre);
		setAbstractSummary(abstractSummary);
		setAuthor(author);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getAbstractSummary() {
		return abstractSummary;
	}

	public void setAbstractSummary(String abstractSummary) {
		this.abstractSummary = abstractSummary;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	

}
