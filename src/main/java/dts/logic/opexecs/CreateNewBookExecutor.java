package dts.logic.opexecs;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dts.logic.OperationExecutor;
import dts.logic.SearchAndBindItemService;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.subboundaries.LocationBoundary;
import dts.util.Functions;
import dts.util.annotation.TempManagerPermmision;

@Component("createNewBook")
public class CreateNewBookExecutor implements OperationExecutor {

	private SearchAndBindItemService itemService;
	
	@Autowired
	public CreateNewBookExecutor(SearchAndBindItemService itemService) {
		this.itemService = itemService;
	}
	
	@Override
	@Transactional
	@TempManagerPermmision
	public Object executeOperation(OperationBoundary operation) {
//		create new item.
		Map<String, Object> operationAttr = operation.getOperationAttributes();
		if(!(operationAttr.containsKey("bookName") && operationAttr.containsKey("bookLocation") && operationAttr.containsKey("bookAttributes"))) {
			throw new RuntimeException("create new book operation must contain the following attribute: bookName, bookLocation, bookAttributes.");
		}
		
		System.err.println("in createNewItem");
		
		String name = (String) operationAttr.get("bookName");
		LocationBoundary location = (LocationBoundary) Functions.convertLinkedTreeToClass(operationAttr.get("bookLocation"), LocationBoundary.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> bookAttr = (Map<String, Object>) operationAttr.get("bookAttributes");
		bookAttr.put("owner", operation.getInvokedBy().getUserId());
		bookAttr.put("offering", true);
		
		ItemBoundary newItem = new ItemBoundary(null, "book", name, true, null, null, location, bookAttr);
		newItem = this.itemService.create(operation.getInvokedBy().getUserId().getSpace(), operation.getInvokedBy().getUserId().getEmail(), newItem);
		
		return newItem;
	}

}
