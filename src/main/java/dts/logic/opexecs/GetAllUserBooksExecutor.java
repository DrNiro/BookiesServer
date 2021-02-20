package dts.logic.opexecs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dts.dal.dao.ItemDao;
import dts.dal.data.ItemEntity;
import dts.logic.OperationExecutor;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.logic.converters.ItemConverter;
import dts.util.Functions;

@Component("getAllUserBooks")
public class GetAllUserBooksExecutor implements OperationExecutor {

	private ItemDao itemDao;
	private ItemConverter itemConverter;
	
	@Autowired
	public GetAllUserBooksExecutor(ItemDao itemDao, ItemConverter itemConverter) {
		this.itemDao = itemDao;
		this.itemConverter = itemConverter;
	}
	
	@Override
	@Transactional
	public Object executeOperation(OperationBoundary operation) {
		// validation
		Map<String, Object> operationAttr = operation.getOperationAttributes();
		if(!operationAttr.containsKey("owner")) {
			throw new RuntimeException("get all users books operation must contain 'owner' attribute.");
		}
		
		List<ItemEntity> myBooks = this.itemDao.findAllByTypeAndActiveTrue("book");
		
		return myBooks.stream()
				.map(itemConverter::toBoundary)
				.filter(boundary -> ((UserIdBoundary) Functions.convertLinkedTreeToClass(boundary.getItemAttributes().get("owner"), UserIdBoundary.class)).getEmail().equals(operation.getInvokedBy().getUserId().getEmail()))
				.collect(Collectors.toList())
				.toArray(new ItemBoundary[0]);
	}
}
