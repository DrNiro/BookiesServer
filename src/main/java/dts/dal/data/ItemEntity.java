package dts.dal.data;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "ITEMS")
public class ItemEntity {
	private String itemId;
	private String type;
	private String name;
	private boolean active;
	private Date createdTimestamp;
	private String createdBy;
	private double lat;
	private double lng;
	private String itemAttributes;
	private Set<ItemEntity> parents;
	private Set<ItemEntity> children;
	
	public ItemEntity() {

	}
	
	public ItemEntity(String itemId, String type, String name, boolean active, Date createdTimestamp, String createdBy,
			double lat, double lng, String itemAttributes) {
		setItemId(itemId);
		setType(type);
		setName(name);
		setActive(active);
		setCreatedTimestamp(createdTimestamp);
		setCreatedBy(createdBy);
		setLat(lat);
		setLng(lng);
		setItemAttributes(itemAttributes);
	}
	
	@Id
	public String getItemId() {
		return itemId;
	}
	
	public void setItemId(String itemId) {
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
	
	public boolean getActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}
	
	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	@Lob
	public String getItemAttributes() {
		return itemAttributes;
	}
	
	public void setItemAttributes(String itemAttributes) {
		this.itemAttributes = itemAttributes;
	}

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
	public Set<ItemEntity> getParents() {
		return parents;
	}

	public void setParents(Set<ItemEntity> parents) {
		this.parents = parents;
	}

	@ManyToMany(mappedBy = "parents", fetch = FetchType.LAZY)
	public Set<ItemEntity> getChildren() {
		return children;
	}

	public void setChildren(Set<ItemEntity> children) {
		this.children = children;
	}

	public void addChild(ItemEntity childItemEntity) {
		this.children.add(childItemEntity);
	}
	
	public void addParent(ItemEntity parentItemEntity) {
		this.parents.add(parentItemEntity);
	}	
	
}
