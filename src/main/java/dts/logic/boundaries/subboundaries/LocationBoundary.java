package dts.logic.boundaries.subboundaries;

import dts.util.Constants;

public class LocationBoundary {

	private Double lat;
	private Double lng;
	
	public LocationBoundary() {
		
	}
	
	public LocationBoundary(Double lat, Double lng) {
		setLat(lat);
		setLng(lng);
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	@Override
	public String toString() {
		return getLat() + Constants.DELIMITER + getLng();
	}
	
	
	
}
