package dts.logic.boundaries.subboundaries;

import dts.util.Constants;

public class ItemIdBoundary {

	private String space;
	private String id;
	
	public ItemIdBoundary() {
		
	}

	public ItemIdBoundary(String space, String id) {
		setSpace(space);
		setId(id);
	}

	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String toString() {
		return getSpace() + Constants.DELIMITER + getId();
	}		
}
