package dts.logic.boundaries;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dts.logic.boundaries.subboundaries.ItemIdBoundary;
import dts.logic.boundaries.subboundaries.LocationBoundary;
import dts.logic.boundaries.subboundaries.User;

public class ItemBoundary {
	private ItemIdBoundary itemId;
	private String type;
	private String name;
	private Boolean active;
	private Date createdTimestamp;
	private User createdBy;
	private LocationBoundary location;
	private Map<String, Object> itemAttributes;
	
	public ItemBoundary() {

	}
	
	public ItemBoundary(String type, String name, Boolean active, LocationBoundary location, Map<String, Object> itemAttributes) {
		setType(type);
		setName(name);
		setActive(active);	
		setLocation(location);
		setItemAttributes(itemAttributes);
	}

	public ItemBoundary(ItemIdBoundary itemId, String type, String name, Boolean active, Date createdTimeStamp,
			User createdBy, LocationBoundary location, Map<String, Object> itemAttributes) {
		setItemId(itemId);
		setType(type);
		setName(name);
		setActive(active);
		setCreatedTimestamp(createdTimeStamp);
		setCreatedBy(createdBy);
		setLocation(location);
		setItemAttributes(itemAttributes);
	}

	public ItemIdBoundary getItemId() {
		return itemId;
	}

	public void setItemId(ItemIdBoundary itemId) {
		this.itemId = itemId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimeStamp) {
		this.createdTimestamp = createdTimeStamp;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public LocationBoundary getLocation() {
		return location;
	}

	public void setLocation(LocationBoundary location) {
		this.location = location;
	}

	public Map<String, Object> getItemAttributes() {
		return itemAttributes;
	}

	public void setItemAttributes(Map<String, Object> itemAttributes) {
		this.itemAttributes = itemAttributes;
	}

	public void putAttribute(String key, Object value) {
		if(this.itemAttributes == null) {
			this.itemAttributes = new HashMap<>();
		}
		
		this.itemAttributes.put(key, value);
	}
	
	public void removeAttributeByKey(String key) {
		this.itemAttributes.remove(key);
	}
	
	@Override
	public String toString() {
		return "ItemBoundary [itemId=" + itemId + ", type=" + type + ", name=" + name + ", active=" + active
				+ ", createdTimestamp=" + createdTimestamp + ", createdBy=" + createdBy + ", location=" + location
				+ ", itemAttributes=" + itemAttributes + "]";
	}

	@Override
	public boolean equals(Object obj) {
		ItemBoundary item = null;
		if(obj.getClass() != ItemBoundary.class) {
			return false;
		} else {
			item = (ItemBoundary) obj;
			return (this.getItemId().getSpace() + this.getItemId().getId()).equals(item.getItemId().getSpace() + item.getItemId().getId());
		}
	}
	
	
}
