package dts.logic.opexecs;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dts.logic.OperationExecutor;
import dts.logic.SearchAndBindItemService;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.subboundaries.LocationBoundary;
import dts.util.Constants;
import dts.util.Functions;
import dts.util.Swap;
import dts.util.annotation.TempManagerPermmision;

@Component("swapBook")
public class CreateSwapAndBindToItemExecutor implements OperationExecutor {

	private SearchAndBindItemService itemService;
	
	@Autowired
	public CreateSwapAndBindToItemExecutor(SearchAndBindItemService itemService) {
		this.itemService = itemService;
	}
	
	@Override
	@Transactional
	@TempManagerPermmision
	public Object executeOperation(OperationBoundary operation) {
//		create new item.
		Map<String, Object> operationAttr = operation.getOperationAttributes();
		if(!operationAttr.containsKey("swap")) {
			throw new RuntimeException("swapping operation attributes must contain a Swap object attribute.");
		}
		
		Swap rawSwap = (Swap) Functions.convertLinkedTreeToClass(operationAttr.get("swap"), Swap.class);
		System.err.println("rawSwap from op: " + rawSwap.toString());
		
		
		String name = (String) rawSwap.getSwapFrom().getEmail() + Constants.DELIMITER + (String) rawSwap.getSwapTo().getEmail();
		LocationBoundary location = rawSwap.getCurrentLocation();
		System.err.println("location from createSwap: " + location.toString() + " type: " + location.getClass().getSimpleName());
		Map<String, Object> swapAttr = new HashMap<>();
		swapAttr.put("fromUserId", rawSwap.getSwapFrom());
		swapAttr.put("toUserId", rawSwap.getSwapTo());
		
		ItemBoundary newSwap = new ItemBoundary("swap", name, true, location, swapAttr);
		System.err.println("from createSwap op (ItemBoundary - swap), BEFORE CREATE: " + newSwap.toString());
		newSwap = this.itemService.create(operation.getInvokedBy().getUserId().getSpace(), operation.getInvokedBy().getUserId().getEmail(), newSwap);
		System.err.println("from createSwap op (ItemBoundary - swap), AFTER CREATE: " + newSwap.toString());
		
		ItemBoundary itemSwapping = this.itemService.getSpecificItem(operation.getInvokedBy().getUserId().getSpace(),
				 operation.getInvokedBy().getUserId().getEmail(), 
				 rawSwap.getBookId().getSpace(), 
				 rawSwap.getBookId().getId());
		
		System.err.println("from createSwap op (ItemBoundary - itemSwapping): " + itemSwapping.toString());
		
//		update item attribute "owner" to new owner UserId.
		itemSwapping.putAttribute("owner", rawSwap.getSwapTo());
		itemSwapping = this.itemService.update(operation.getInvokedBy().getUserId().getSpace(),
											   operation.getInvokedBy().getUserId().getEmail(),
											   itemSwapping.getItemId().getSpace(),
											   itemSwapping.getItemId().getId(),
											   itemSwapping);
		
		System.err.println("from createSwap op (ItemBoundary - itemSwapping) AFTER UPDATE OWNER: " + itemSwapping.toString());
		
		this.itemService.bindItemToChild(operation.getInvokedBy().getUserId().getSpace(),
										 operation.getInvokedBy().getUserId().getEmail(), 
										 itemSwapping.getItemId().getSpace(), 
										 itemSwapping.getItemId().getId(), 
										 newSwap.getItemId());
		
		return newSwap;
	}
	

}
