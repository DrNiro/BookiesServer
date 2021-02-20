package dts.logic.opexecs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dts.dal.dao.UserDao;
import dts.dal.data.UserEntity;
import dts.logic.OperationExecutor;
import dts.logic.SearchAndBindItemService;
import dts.logic.SearchUsersService;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.boundaries.UserBoundary;
import dts.logic.boundaries.subboundaries.UserIdBoundary;
import dts.logic.boundaries.subboundaries.UserRoleBoundary;
import dts.logic.converters.UserConverter;
import dts.util.Constants;
import dts.util.Functions;
import dts.util.annotation.TempManagerPermmision;
import dts.util.exceptions.PermissionNotAuthorizedException;

@Component("deleteBook")
public class DeleteBookExecutor implements OperationExecutor{

	private SearchAndBindItemService itemService;
	private SearchUsersService userService;
	private UserDao userDao;
	private UserConverter userConverter;
	
	@Autowired
	public DeleteBookExecutor(SearchAndBindItemService itemService, SearchUsersService userService, UserDao userDao, UserConverter userConverter) {
		this.itemService = itemService;
		this.userService = userService;
		this.userDao = userDao;
		this.userConverter = userConverter;
	}
	
	@Override
	@Transactional
	@TempManagerPermmision
	public Object executeOperation(OperationBoundary operation) {
		// user is in database 100% if we got here, because every operation have been checked for user permission.
		UserEntity existingUserEntity = this.userDao.findById(operation.getInvokedBy().getUserId().getSpace() + Constants.DELIMITER + operation.getInvokedBy().getUserId().getEmail()).get();
		UserBoundary updateUserBoundary = this.userConverter.toBoundary(existingUserEntity);		
		
		// get item from db.
		ItemBoundary itemToDelete = this.itemService.getSpecificItem(operation.getInvokedBy().getUserId().getSpace(),
										 operation.getInvokedBy().getUserId().getEmail(),
										 operation.getItem().getItemId().getSpace(),
										 operation.getItem().getItemId().getId());
						
		// check if the updated user is the owner of the item
		if (itemToDelete.getType().equals("book") && !((UserIdBoundary) Functions.convertLinkedTreeToClass(itemToDelete.getItemAttributes().get("owner"), UserIdBoundary.class)).equals(updateUserBoundary.getUserId())) {			
			// only the owner of the item can delete it - throw exception
			throw new PermissionNotAuthorizedException("the user that delets the item must be it's owner");
		}

				
		// update inactive
		itemToDelete.setActive(false);
		
		this.itemService.update(operation.getInvokedBy().getUserId().getSpace(),
				 operation.getInvokedBy().getUserId().getEmail(),
				 operation.getItem().getItemId().getSpace(),
				 operation.getItem().getItemId().getId(),
				 itemToDelete);
		
		// revert user role to player.
		updateUserBoundary.setRole(UserRoleBoundary.PLAYER);
		this.userService.updateUser(updateUserBoundary.getUserId().getSpace(), updateUserBoundary.getUserId().getEmail(), updateUserBoundary);
		
		// TODO Auto-generated method stub
		return itemToDelete;
	}

}
