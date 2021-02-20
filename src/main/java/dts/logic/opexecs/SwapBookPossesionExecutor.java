package dts.logic.opexecs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dts.logic.OperationExecutor;
import dts.logic.SearchAndBindItemService;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.util.Functions;
import dts.util.annotation.TempManagerPermmision;
import dts.util.exceptions.PermissionNotAuthorizedException;

@Component("swapBookPossesion")
public class SwapBookPossesionExecutor implements OperationExecutor {

	private SearchAndBindItemService itemService;
	
	@Autowired
	public SwapBookPossesionExecutor(SearchAndBindItemService itemService) {
		this.itemService = itemService;
	}
	
	@Override
	@Transactional
	@TempManagerPermmision
	public Object executeOperation(OperationBoundary operation) {
		ItemBoundary itemSwapping = this.itemService.getSpecificItem(operation.getInvokedBy().getUserId().getSpace(),
																	 operation.getInvokedBy().getUserId().getEmail(), 
																	 operation.getItem().getItemId().getSpace(), 
																	 operation.getItem().getItemId().getId());
		
		

		if (itemSwapping.getType().equals("book") && !((UserIdBoundary) Functions.convertLinkedTreeToClass(itemSwapping.getItemAttributes().get("owner"), UserIdBoundary.class)).equals(operation.getInvokedBy().getUserId())) {			
			// only the owner of the item can delete it - throw exception
			throw new PermissionNotAuthorizedException("the user that swaps the item must be it's owner");
		}

//		check if 'newOwnerUserId' key exist in operationAttributes.
		if(!operation.getOperationAttributes().containsKey("newOwnerUserId")) {
			throw new RuntimeException("this operation must contain a key 'newOwnerUserId'");
		}
		if(operation.getOperationAttributes().get("newOwnerUserId") == null) {
			throw new RuntimeException("UserIdBoundary in 'newOwnerUserId' attribute of this operation is null");
		}
				
		UserIdBoundary newOwnerUserId = (UserIdBoundary) Functions.convertLinkedTreeToClass(operation.getOperationAttributes().get("newOwnerUserId"), UserIdBoundary.class);
		
		if(newOwnerUserId.getSpace() == null || newOwnerUserId.getEmail() == null) {
			throw new RuntimeException("newOwnerUserId's space or email attribute are null");
		}

//		update item attribute "owner" to new owner UserId.
		itemSwapping.putAttribute("owner", newOwnerUserId);
		itemSwapping = this.itemService.update(operation.getInvokedBy().getUserId().getSpace(),
											   operation.getInvokedBy().getUserId().getEmail(),
											   itemSwapping.getItemId().getSpace(),
											   itemSwapping.getItemId().getId(),
											   itemSwapping);
		
		return itemSwapping;
	}

}
