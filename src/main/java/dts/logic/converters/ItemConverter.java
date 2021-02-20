package dts.logic.converters;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dts.dal.data.ItemEntity;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.subboundaries.ItemIdBoundary;
import dts.logic.boundaries.subboundaries.LocationBoundary;
import dts.logic.boundaries.subboundaries.User;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.util.Constants;

@Component
public class ItemConverter {
	
	private ObjectMapper jackson;
	
	@PostConstruct
	public void init() {
		this.jackson = new ObjectMapper();
	}
	
	public ItemEntity toEntity (ItemBoundary itemBoundary) {
		if (itemBoundary == null) {
			return null;
		}
		
		ItemEntity itemEntity = new ItemEntity();
		
		if (itemBoundary.getItemId() != null) {
			itemEntity.setItemId(itemBoundary.getItemId().toString());
		}	
		
		itemEntity.setType(itemBoundary.getType());
		
		itemEntity.setName(itemBoundary.getName());
		
		if (itemBoundary.getActive() !=  null) {
			itemEntity.setActive(itemBoundary.getActive());
		}
		
		itemEntity.setCreatedTimestamp(itemBoundary.getCreatedTimestamp());

		if (itemBoundary.getCreatedBy() != null && itemBoundary.getCreatedBy().getUserId() != null) {
			itemEntity.setCreatedBy(itemBoundary.getCreatedBy().getUserId().toString());
		}
		
		if (itemBoundary.getLocation() != null && itemBoundary.getLocation().getLat() != null && itemBoundary.getLocation().getLng() != null) {
			itemEntity.setLat(itemBoundary.getLocation().getLat());
			itemEntity.setLng(itemBoundary.getLocation().getLng());
		}

		itemEntity.setItemAttributes(toEntity(itemBoundary.getItemAttributes()));
		
		return itemEntity;
	}
	
	public ItemBoundary toBoundary (ItemEntity itemEntity) {
		if (itemEntity == null) {
			return null;
		}
		
		ItemBoundary itemBoundary = new ItemBoundary();
		
		if (itemEntity.getItemId() != null) {
			itemBoundary.setItemId(createItemIdFromString(itemEntity.getItemId()));
		}

		itemBoundary.setType(itemEntity.getType());
		
		itemBoundary.setName(itemEntity.getName());
		
		itemBoundary.setActive(itemEntity.getActive());
		
		itemBoundary.setCreatedTimestamp(itemEntity.getCreatedTimestamp());
		
		if (itemEntity.getCreatedBy() != null) {
			itemBoundary.setCreatedBy(new User(createUserIdFromString(itemEntity.getCreatedBy())));
		}
		
		itemBoundary.setLocation(new LocationBoundary(itemEntity.getLat(), itemEntity.getLng()));
		
		itemBoundary.setItemAttributes(toBoundaryAsMap(itemEntity.getItemAttributes()));
		
		return itemBoundary;
	}
	
	private UserIdBoundary createUserIdFromString(String userIdAsString) {
		if (userIdAsString != null) {
			String[] args = userIdAsString.split(Constants.DELIMITER);
			return new UserIdBoundary(args[0], args[1]);
		} else {
			return null;
		}
	}
	
	private ItemIdBoundary createItemIdFromString(String itemIdAsString) {
		if (itemIdAsString != null) {
			String[] args = itemIdAsString.split(Constants.DELIMITER);
			return new ItemIdBoundary(args[0], args[1]);
		} else {
			return null;
		}
	}
	
//	private LocationBoundary createLocationBoundaryFromString(String LocationAsString) {
//		if (LocationAsString != null) {
//			String[] args = LocationAsString.split(Constants.DELIMITER);
//			return new LocationBoundary(Double.parseDouble(args[0]), Double.parseDouble(args[1]));
//		} else {
//			return null;
//		}
//	}

	// use JACKSON to store JSON String in the database
	private String toEntity(Map<String, Object> moreDetails) {
		if (moreDetails != null) {
			try {
				return this.jackson.writeValueAsString(moreDetails);
			}catch (Exception e) {
				throw new RuntimeException(e);
			}
		}else {
			return "{}";
		}
	}
	
	// use JACKSON to store JSON String in the database
	@SuppressWarnings("unchecked")
	private Map<String, Object> toBoundaryAsMap(String moreDetails) {
		if (moreDetails != null) {
			try {
				return this.jackson.readValue(moreDetails, Map.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}else {
			return new HashMap<>();
		}
	}
	
}
