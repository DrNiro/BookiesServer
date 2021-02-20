package dts.logic;

import java.util.List;

import dts.logic.boundaries.ItemBoundary;

public interface SearchAndBindItemService extends BindItemsService {
	
	public List<ItemBoundary> getAll(String userSpace, String userEmail, int pageSize, int pageOffset);
	
	public List<ItemBoundary> getAllChildren(String userSpace, String userEmail, String itemSpace, String itemId, int pageSize, int pageOffset);
	public List<ItemBoundary> getItemParents(String userSpace, String userEmail, String itemSpace, String itemId, int pageSize, int pageOffset);
	
	public List<ItemBoundary> searchByNamePattern(String userSpace, String userEmail, String namePattern, int pageSize, int pageOffset);
	public List<ItemBoundary> searchByType(String userSpace, String userEmail, String type, int pageSize, int pageOffset);
	public List<ItemBoundary> searchByLocation(String userSpace, String userEmail, String lat, String lng, String distance, int pageSize, int pageOffset);

}
