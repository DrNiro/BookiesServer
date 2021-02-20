package dts.logic.opexecs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dts.dal.dao.ItemDao;
import dts.dal.data.ItemEntity;
import dts.logic.OperationExecutor;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.subboundaries.LocationBoundary;
import dts.logic.converters.ItemConverter;
import dts.util.Functions;

@Component("findBooksInDistance")
public class FindAllBooksInDistanceExecutor implements OperationExecutor {

	private ItemDao itemDao;
	private ItemConverter itemConverter;
	
	@Autowired
	public FindAllBooksInDistanceExecutor(ItemDao itemDao, ItemConverter itemConverter) {
		this.itemDao = itemDao;
		this.itemConverter = itemConverter;
	}
	
	@Override
	@Transactional
	public Object executeOperation(OperationBoundary operation) {
		// validate needed attributes
		Map<String, Object> operationAttr = operation.getOperationAttributes();
		if(!(operationAttr.containsKey("distance") && operationAttr.containsKey("myLocation"))) {
			throw new RuntimeException("Find books within distance operation must contain 'distance' and 'myLocation' attributes.");
		}
		if(!(operationAttr.containsKey("pageSize") && operationAttr.containsKey("pageOffset"))) {
			throw new RuntimeException("Find books within distance operation must contain 'pageSize' and 'pageOffset' attributes.");
		}
		
		double distance = (Double) operationAttr.get("distance");
		LocationBoundary location = (LocationBoundary) Functions.convertLinkedTreeToClass(operation.getOperationAttributes().get("myLocation"), LocationBoundary.class);
		int pageSize = (Integer) operationAttr.get("pageSize");
		int pageOffset = (Integer) operationAttr.get("pageOffset");
		
		List<ItemEntity> bookEntities = this.itemDao.findByLatBetweenAndLngBetweenAndTypeAndActive(location.getLat()-distance, 
																	     location.getLat()+distance, 
																	     location.getLng()-distance, 
																	     location.getLng()+distance, 
																	     "book", 
																	     true,
																	     PageRequest.of(pageOffset, pageSize, Direction.ASC, "lat", "lng", "itemId"));
		
		return bookEntities.stream()
				.map(this.itemConverter::toBoundary)
				.collect(Collectors.toList())
				.toArray(new ItemBoundary[0]);
	}
	
}
