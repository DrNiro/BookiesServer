package dts.logic.opexecs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dts.logic.OperationExecutor;
import dts.logic.SearchAndBindItemService;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.OperationBoundary;
import dts.util.annotation.TempManagerPermmision;

@Component("toggleOffering")
public class ToggleBookOfferingStatusExecutor implements OperationExecutor {

	private SearchAndBindItemService itemService;
	
	@Autowired
	public ToggleBookOfferingStatusExecutor(SearchAndBindItemService itemService) {
		this.itemService = itemService;
	}
	
	@Override
	@Transactional
	@TempManagerPermmision
	public Object executeOperation(OperationBoundary operation) {		
		// get item from db
		ItemBoundary item = this.itemService.getSpecificItem(operation.getInvokedBy().getUserId().getSpace(), 
										 operation.getInvokedBy().getUserId().getEmail(), 
										 operation.getItem().getItemId().getSpace(), 
										 operation.getItem().getItemId().getId());
		
		// change offering attribute
		if(!item.getItemAttributes().containsKey("offering")) {
			throw new RuntimeException("book attribute must contain 'offering' key");
		}
		boolean offering = (boolean) item.getItemAttributes().get("offering");
		item.putAttribute("offering", !offering);
		
		// update item
		item = this.itemService.update(operation.getInvokedBy().getUserId().getSpace(),
							    operation.getInvokedBy().getUserId().getEmail(), 
							    operation.getItem().getItemId().getSpace(), 
							    operation.getItem().getItemId().getId(), 
							    item);
		
		return item;
	}
	
}
