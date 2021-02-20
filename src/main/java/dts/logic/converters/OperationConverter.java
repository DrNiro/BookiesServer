package dts.logic.converters;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dts.dal.data.OperationEntity;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.subboundaries.Item;
import dts.logic.boundaries.subboundaries.ItemIdBoundary;
import dts.logic.boundaries.subboundaries.OperationIdBoundary;
import dts.logic.boundaries.subboundaries.User;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.util.Constants;

@Component
public class OperationConverter {
	private ObjectMapper jackson;

	@PostConstruct
	public void init() {
		this.jackson = new ObjectMapper();
	}
	
	public OperationEntity toEntity(OperationBoundary operationBoundary) {
		if (operationBoundary == null) {
			return null;
		}
		
		OperationEntity entity = new OperationEntity();
		
		if(operationBoundary.getOperationId() != null) {
			entity.setOperationId(operationBoundary.getOperationId().toString());
		}

		entity.setType(operationBoundary.getType());

		if(operationBoundary.getItem() != null && operationBoundary.getItem().getItemId() != null) {
			entity.setItem(operationBoundary.getItem().getItemId().toString());
		}

		entity.setCreatedTimestamp(operationBoundary.getCreatedTimestamp());
		
		if(operationBoundary.getInvokedBy() != null && operationBoundary.getInvokedBy().getUserId() != null) {
			entity.setInvokedBy(operationBoundary.getInvokedBy().getUserId().toString());			
		}

		entity.setOperationAttributes(toEntity(operationBoundary.getOperationAttributes()));

		return entity;

	}

	public OperationBoundary toBoundary(OperationEntity operationEntity) {
		if (operationEntity == null) {
			return null;
		}
		
		OperationBoundary boundary = new OperationBoundary();
		
		if (operationEntity.getOperationId() != null) {
			boundary.setOperationId(fromStringToBoundaryOperationId(operationEntity.getOperationId()));
		}
		
		boundary.setType(operationEntity.getType());

		if (operationEntity.getItem() != null) {
			boundary.setItem(fromStringToBoundaryItem(operationEntity.getItem()));
		}

		if (operationEntity.getInvokedBy() != null) {
			boundary.setInvokedBy(fromStringToBoundaryUser(operationEntity.getInvokedBy()));
		}
		
		boundary.setCreatedTimestamp(operationEntity.getCreatedTimestamp());

		boundary.setOperationAttributes(toBoundaryAsMap(operationEntity.getOperationAttributes()));

		return boundary;

	}

	private User fromStringToBoundaryUser(String stringId) {
		if (stringId != null) {
			String[] args = stringId.split(Constants.DELIMITER);
			return new User(new UserIdBoundary(args[0], args[1]));
		} else {
			return null;
		}
	}

	private OperationIdBoundary fromStringToBoundaryOperationId(String stringId) {
		if (stringId != null) {
			String[] args = stringId.split(Constants.DELIMITER);
			return new OperationIdBoundary(args[0], args[1]);
		} else {
			return null;
		}
	}
	private Item fromStringToBoundaryItem(String stringId) {
		if (stringId != null) {
			String[] args = stringId.split(Constants.DELIMITER);
			return new Item(new ItemIdBoundary(args[0], args[1]));
		} else {
			return null;
		}
	}
	
	// use JACKSON to store JSON String in the database
	private String toEntity(Map<String, Object> operationAttributes) {
		if (operationAttributes != null) {
			try {
				return this.jackson.writeValueAsString(operationAttributes);
			}catch (Exception e) {
				throw new RuntimeException(e);
			}
		}else {
			return "{}";
		}
	}
	
	// use JACKSON to store JSON String in the database
	@SuppressWarnings("unchecked")
	private Map<String, Object> toBoundaryAsMap(String operationAttributes) {
		if (operationAttributes != null) {
			try {
				return this.jackson.readValue(operationAttributes, Map.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}else {
			return new HashMap<>();
		}
	}


}
