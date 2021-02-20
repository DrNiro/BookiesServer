package dts.logic;

import java.util.List;

import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.subboundaries.ItemIdBoundary;

public interface BindItemsService extends ItemsService {
	
	public void bindItemToChild(String managerSpace, String managerEmail, String itemSpace, String itemId, ItemIdBoundary childItemId);
	public List<ItemBoundary> getAllChildren(String userSpace, String userEmail, String itemSpace, String itemId);
	public List<ItemBoundary> getItemParents(String userSpace, String userEmail, String itemSpace, String itemId);

}



