package dts.logic.boundaries.subboundaries;

public class Item {
	private ItemIdBoundary itemId;
	
	public Item() {
		
	}
	
	public Item(ItemIdBoundary itemId) {
		setItemId(itemId);
	}

	public ItemIdBoundary getItemId() {
		return itemId;
	}

	public void setItemId(ItemIdBoundary itemId) {
		this.itemId = itemId;
	}
	
	
}
