package dts.util;

import dts.logic.boundaries.subboundaries.ItemIdBoundary;
import dts.logic.boundaries.subboundaries.LocationBoundary;
import dts.logic.boundaries.subboundaries.UserIdBoundary;

public class Swap {

    private ItemIdBoundary bookId;
    private UserIdBoundary swapFrom;
    private UserIdBoundary swapTo;
    private LocationBoundary currentLocation;

    public Swap() {

    }

    public Swap(ItemIdBoundary bookId, UserIdBoundary swapFrom, UserIdBoundary swapTo, LocationBoundary currentLocation) {
        this.bookId = bookId;
        this.swapFrom = swapFrom;
        this.swapTo = swapTo;
        this.currentLocation = currentLocation;
    }

    public ItemIdBoundary getBookId() {
        return bookId;
    }

    public void setBookId(ItemIdBoundary bookId) {
        this.bookId = bookId;
    }

    public UserIdBoundary getSwapFrom() {
        return swapFrom;
    }

    public void setSwapFrom(UserIdBoundary swapFrom) {
        this.swapFrom = swapFrom;
    }

    public UserIdBoundary getSwapTo() {
        return swapTo;
    }

    public void setSwapTo(UserIdBoundary swapTo) {
        this.swapTo = swapTo;
    }

    public LocationBoundary getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(LocationBoundary currentLocation) {
        this.currentLocation = currentLocation;
    }

	@Override
	public String toString() {
		return "Swap [bookId=" + bookId + ", swapFrom=" + swapFrom + ", swapTo=" + swapTo + ", currentLocation="
				+ currentLocation + "]";
	}
    
    
}
