package dts.util;

import java.util.Map;

public class Book {
	
	private BookDetails details;
	private String coverImage;
	private Float rating;
	private Condition condition;
	private Map<String, Object> moreDetails;
	
	public Book() {

	}	
	
	public Book(String coverImage, Float rating, Condition condition, Map<String, Object> moreDetails) {
		super();
		this.coverImage = coverImage;
		this.rating = rating;
		this.condition = condition;
		this.moreDetails = moreDetails;
	}

	public BookDetails getDetails() {
		return details;
	}

	public void setDetails(BookDetails details) {
		this.details = details;
	}

	public String getCoverImage() {
		return coverImage;
	}

	public void setCoverImage(String coverImage) {
		this.coverImage = coverImage;
	}

	public Float getRating() {
		return rating;
	}

	public void setRating(Float rating) {
		this.rating = rating;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public Map<String, Object> getMoreDetails() {
		return moreDetails;
	}

	public void setMoreDetails(Map<String, Object> moreDetails) {
		this.moreDetails = moreDetails;
	}
	
	
	
	
}
