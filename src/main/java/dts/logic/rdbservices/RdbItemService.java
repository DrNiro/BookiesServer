package dts.logic.rdbservices;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dts.dal.dao.IdGeneratorEntityDao;
import dts.dal.dao.ItemDao;
import dts.dal.dao.UserDao;
import dts.dal.data.IdGeneratorEntity;
import dts.dal.data.ItemEntity;
import dts.dal.data.UserEntity;
import dts.dal.data.UserRole;
import dts.logic.SearchAndBindItemService;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.subboundaries.ItemIdBoundary;
import dts.logic.converters.ItemConverter;
import dts.util.Constants;
import dts.util.ErrorMessages;
import dts.util.Functions;
import dts.util.annotation.Permissional;
import dts.util.exceptions.ItemNotFoundException;


@Service
public class RdbItemService implements SearchAndBindItemService {
	private String spaceName;
	private ItemDao itemDao;
	private ItemConverter itemConverter;
	private IdGeneratorEntityDao idGeneratorEntityDao;
	private UserDao userDao;

	@Autowired
	public RdbItemService(ItemDao itemDao, ItemConverter itemConverter, IdGeneratorEntityDao idGeneratorEntityDao, UserDao userDao) {
		this.itemDao = itemDao;
		this.itemConverter = itemConverter;
		this.idGeneratorEntityDao = idGeneratorEntityDao;
		this.userDao= userDao;
	}

	@Value("${spring.application.name:defultappname}")
	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

	@Override
	@Transactional
	@Permissional(rolesArray = {UserRole.MANAGER})
	public ItemBoundary create(String managerSpace, String managerEmail, ItemBoundary newItem) throws RuntimeException {
	//		validate item details
		if (managerSpace == null || managerEmail == null || newItem == null) {
			throw new NullPointerException("one of the input parameters is null");
		}
		
		if (newItem.getName() == null || newItem.getName() == "") {
			throw new RuntimeException("item name is null or empty");
		}

		if (newItem.getType() == null || newItem.getType() == "") {
			throw new RuntimeException("item type is null or empty");
		}

		if (!Functions.isValidEmail(managerEmail)) {
			throw new RuntimeException(managerEmail + " not a valid email");
		}

//		convert boundary to entity
		ItemEntity itemEntity = this.itemConverter.toEntity(newItem);

//		generate id
		IdGeneratorEntity idGeneratorEntity = new IdGeneratorEntity();
		idGeneratorEntity = this.idGeneratorEntityDao.save(idGeneratorEntity);
		Long newId = idGeneratorEntity.getId();
		this.idGeneratorEntityDao.deleteById(newId);

//		assign values to entity
		itemEntity.setItemId(this.spaceName + Constants.DELIMITER + newId);
		itemEntity.setCreatedTimestamp(new Date());
		itemEntity.setCreatedBy(managerSpace + Constants.DELIMITER + managerEmail);
		itemEntity.setChildren(new HashSet<>());
		itemEntity.setParents(new HashSet<>());
		
//		save entity to the db
		itemEntity = this.itemDao.save(itemEntity);

		return this.itemConverter.toBoundary(itemEntity);
	}

	@Override
	@Transactional
	@Permissional(rolesArray = {UserRole.MANAGER})
	public ItemBoundary update(String managerSpace, String managerEmail, String itemSpace, String itemId,
			ItemBoundary update) throws ItemNotFoundException {
//		validate item details
		if (update.getName() == null) {
			throw new ItemNotFoundException("item name is null");
		}

		if (update.getType() == null) {
			throw new ItemNotFoundException("item type is null");
		}

//		find original item from db.
		ItemEntity item = getExistingItemEntityOrThrow(itemSpace, itemId, ErrorMessages.ITEM_NOT_EXIST);

		ItemBoundary originalItem = this.itemConverter.toBoundary(item);

//		make sure the item keep its significant original attributes.
		update.setItemId(originalItem.getItemId());
		update.setCreatedBy(originalItem.getCreatedBy());
		update.setCreatedTimestamp(originalItem.getCreatedTimestamp());

//		if update values are null, keep original values
		if(update.getActive() == null) {
			update.setActive(originalItem.getActive());
		}
		if(update.getItemAttributes() == null) {
			update.setItemAttributes(originalItem.getItemAttributes());
		}
		if(update.getLocation() == null) {
			update.setLocation(originalItem.getLocation());
		}
		
//		convert updated item to entity.
		ItemEntity itemEntity = this.itemConverter.toEntity(update);
		itemEntity.setChildren(item.getChildren());
		itemEntity.setParents(item.getParents());
		
//		save updated item to db.
		itemEntity = this.itemDao.save(itemEntity);

		return this.itemConverter.toBoundary(itemEntity);
	}

//	never used because using pagination with default values.
	@Override
	@Transactional(readOnly = true)
	public List<ItemBoundary> getAll(String userSpace, String userEmail) { 
		return StreamSupport.stream(this.itemDao.findAll().spliterator(), false) // Iterable to Stream<ItemEntity>,
				.map(this.itemConverter::toBoundary) // Stream<ItemBoundary>
				.collect(Collectors.toList()); // List<ItemBoundary>
	}

	@Override
	@Transactional(readOnly = true)
	@Permissional(rolesArray = {UserRole.MANAGER, UserRole.PLAYER})
	public ItemBoundary getSpecificItem(String userSpace, String userEmail, String itemSpace, String itemId) throws RuntimeException {
		UserEntity user = this.userDao.findById(userSpace + Constants.DELIMITER + userEmail).get();

		ItemEntity item = getExistingItemEntityOrThrow(itemSpace, itemId, ErrorMessages.ITEM_NOT_EXIST);
	
		if(user.getRole().name().equals(UserRole.PLAYER.name()) && !item.getActive()) {
			throw new ItemNotFoundException("inactive item. permissions: manager only.");
		}
		
		return this.itemConverter.toBoundary(item);
	}

	@Override
	@Transactional
	public void deleteAll(String adminSpace, String adminEmail) {
		this.itemDao.deleteAll();
	}

	@Override
	@Transactional
	@Permissional(rolesArray = {UserRole.MANAGER})
	public void bindItemToChild(String managerSpace, String managerEmail, String itemSpace, String itemId,
			ItemIdBoundary childItemId) throws RuntimeException {
//		get parent item
		ItemEntity parentItemEntity = getExistingItemEntityOrThrow(itemSpace, itemId,
				ErrorMessages.PARENT_ITEM_NOT_EXIST);

//		get child item
		ItemEntity childItemEntity = getExistingItemEntityOrThrow(childItemId.getSpace(), childItemId.getId(),
				ErrorMessages.CHILD_ITEM_NOT_EXIST);

//		bind items
		System.err.println("Child: " + childItemEntity.toString());
		System.err.println("Parent: " + parentItemEntity.toString());
		childItemEntity.addParent(parentItemEntity);
		parentItemEntity.addChild(childItemEntity);

//		save bind to db
		parentItemEntity = this.itemDao.save(parentItemEntity);
	}

//	
	@Override
	@Transactional(readOnly = true)
	public List<ItemBoundary> getAllChildren(String userSpace, String userEmail, String itemSpace, String itemId) {
		ItemEntity parentItemEntity = getExistingItemEntityOrThrow(itemSpace, itemId, ErrorMessages.PARENT_ITEM_NOT_EXIST);

		Set<ItemEntity> childrenEntities = parentItemEntity.getChildren();
		return childrenEntities.stream() // Stream<ItemEntity>
				.map(entity -> this.itemConverter.toBoundary(entity)) // Stream<ItemBoundary>
				.collect(Collectors.toList()); // List<ItemBoundary>
	}

	@Override
	@Transactional(readOnly = true)
	public List<ItemBoundary> getItemParents(String userSpace, String userEmail, String itemSpace, String itemId) {
		ItemEntity childItemEntity = getExistingItemEntityOrThrow(itemSpace, itemId,
				ErrorMessages.PARENT_ITEM_NOT_EXIST);

		Set<ItemEntity> parentEntities = childItemEntity.getParents();
		return parentEntities.stream() // Stream<ItemEntity>
				.map(this.itemConverter::toBoundary) // Stream<ItemBoundary>
				.collect(Collectors.toList()); // List<ItemBoundary>
	}

	@Override
	@Transactional(readOnly = true)
	@Permissional(rolesArray = {UserRole.MANAGER, UserRole.PLAYER})
	public List<ItemBoundary> searchByNamePattern(String userSpace, String userEmail, String namePattern, int pageSize, int pageOffset) {
		UserEntity user = this.userDao.findById(userSpace + Constants.DELIMITER + userEmail).get();		
		Stream<ItemEntity> itemStream;
		
		if(user.getRole().name().equals(UserRole.PLAYER.name())) {
			itemStream = this.itemDao.findByNameContainingAndActiveTrue(namePattern, PageRequest.of(pageOffset, pageSize, Direction.DESC, "name", "type", "itemId"))
					.stream();		
		} else {
			itemStream = this.itemDao.findByNameContaining(namePattern, PageRequest.of(pageOffset, pageSize, Direction.DESC, "name", "type", "itemId"))
					.stream();		
		}
		
//			if userRole is player - filter out the inactive items.
//		itemStream = filterInactiveItemsForPlayerUser(itemStream, user);
		
		return itemStream.map(this.itemConverter::toBoundary)
				.collect(Collectors.toList());			
	}

	@Override
	@Transactional(readOnly = true)
	@Permissional(rolesArray = {UserRole.MANAGER, UserRole.PLAYER})
	public List<ItemBoundary> searchByType(String userSpace, String userEmail, String type, int pageSize, int pageOffset) {
		UserEntity user = this.userDao.findById(userSpace + Constants.DELIMITER + userEmail).get();
		Stream<ItemEntity> itemStream;
		
		if(user.getRole().name().equals(UserRole.PLAYER.name())) {
			itemStream = this.itemDao.findByTypeAndActiveTrue(type, PageRequest.of(pageOffset, pageSize, Direction.DESC, "type", "itemId"))
					.stream();
		} else {
			itemStream = this.itemDao.findByType(type, PageRequest.of(pageOffset, pageSize, Direction.DESC, "type", "itemId"))
					.stream();
		}
		
//			if userRole is player - filter out the inactive items.
//		itemStream = filterInactiveItemsForPlayerUser(itemStream, user);
		
		return itemStream.map(this.itemConverter::toBoundary)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Permissional(rolesArray = {UserRole.MANAGER, UserRole.PLAYER})
	public List<ItemBoundary> searchByLocation(String userSpace, String userEmail, String lat, String lng, String distance, int pageSize, int pageOffset) {
		UserEntity user = this.userDao.findById(userSpace + Constants.DELIMITER + userEmail).get();
		
		Stream<ItemEntity> itemStream;
		
		if(user.getRole().name().equals(UserRole.PLAYER.name())) {
			itemStream = this.itemDao.findByLatBetweenAndLngBetweenAndActiveTrue(Double.parseDouble(lat) - Double.parseDouble(distance), 
					   Double.parseDouble(lat) + Double.parseDouble(distance),
					   Double.parseDouble(lng) - Double.parseDouble(distance),
					   Double.parseDouble(lng) + Double.parseDouble(distance),
					   PageRequest.of(pageOffset, pageSize, Direction.ASC, "lat", "lng", "name", "itemId"))
							.stream();
		} else {
			itemStream = this.itemDao.findByLatBetweenAndLngBetween(Double.parseDouble(lat) - Double.parseDouble(distance), 
					   Double.parseDouble(lat) + Double.parseDouble(distance),
					   Double.parseDouble(lng) - Double.parseDouble(distance),
					   Double.parseDouble(lng) + Double.parseDouble(distance),
					   PageRequest.of(pageOffset, pageSize, Direction.ASC, "lat", "lng", "name", "itemId"))
							.stream();
		}

//			if userRole is player - filter out the inactive items.
//		itemStream = filterInactiveItemsForPlayerUser(itemStream, user);
		
		return itemStream.map(this.itemConverter::toBoundary)
				.collect(Collectors.toList());
	}

//	get all items using pagination.
	@Override
	@Transactional(readOnly = true)
	@Permissional(rolesArray = {UserRole.MANAGER, UserRole.PLAYER})
	public List<ItemBoundary> getAll(String userSpace, String userEmail, int pageSize, int pageOffset) {
		UserEntity user = this.userDao.findById(userSpace + Constants.DELIMITER + userEmail).get();
		
		
		Stream<ItemEntity> itemStream;
		
		if(user.getRole().name().equals(UserRole.PLAYER.name())) {
			itemStream = this.itemDao.findByActiveTrue(PageRequest.of(pageOffset, pageSize, Direction.DESC, "type", "name", "itemId"))
					.stream();		
		} else {
			itemStream = this.itemDao.findAll(PageRequest.of(pageOffset, pageSize, Direction.DESC, "type", "name", "itemId"))
					.getContent()
					.stream();		
		}
		
//			if userRole is player - filter out the inactive items.
//		itemStream = filterInactiveItemsForPlayerUser(itemStream, user);
		
		return itemStream.map(this.itemConverter::toBoundary)
				.collect(Collectors.toList());
	}

//	get all item's children with pagination
	@Override
	@Transactional(readOnly = true)
	@Permissional(rolesArray = {UserRole.MANAGER, UserRole.PLAYER})
	public List<ItemBoundary> getAllChildren(String userSpace, String userEmail, String itemSpace, String itemId,
			int pageSize, int pageOffset) {
		UserEntity user = this.userDao.findById(userSpace + Constants.DELIMITER + userEmail).get();
		ItemEntity parentItemEntity = getExistingItemEntityOrThrow(itemSpace, itemId, ErrorMessages.PARENT_ITEM_NOT_EXIST);
	
		Stream<ItemEntity> itemStream;

		if(user.getRole().name().equals(UserRole.PLAYER.name())) {
			itemStream = this.itemDao.findAllByParentsAndActiveTrue(parentItemEntity, PageRequest.of(pageOffset, pageSize, Direction.ASC, "itemId"))
					.stream();
		} else {
			itemStream = this.itemDao.findAllByParents(parentItemEntity, PageRequest.of(pageOffset, pageSize, Direction.ASC, "itemId"))
					.stream();
		}
		
//			if userRole is player - filter out the inactive items.
//		itemStream = filterInactiveItemsForPlayerUser(itemStream, user);

		return itemStream.map(this.itemConverter::toBoundary)
				.collect(Collectors.toList());
	}

//	get all item's parents with pagination
	@Override
	@Transactional(readOnly = true)
	@Permissional(rolesArray = {UserRole.MANAGER, UserRole.PLAYER})
	public List<ItemBoundary> getItemParents(String userSpace, String userEmail, String itemSpace, String itemId,
			int pageSize, int pageOffset) {
		UserEntity user = this.userDao.findById(userSpace + Constants.DELIMITER + userEmail).get();
		ItemEntity childItemEntity = getExistingItemEntityOrThrow(itemSpace, itemId,
				ErrorMessages.PARENT_ITEM_NOT_EXIST);
	
		Stream<ItemEntity> itemStream;
		
		if(user.getRole().name().equals(UserRole.PLAYER.name())) {
			itemStream = this.itemDao.findAllByChildrenAndActiveTrue(childItemEntity, PageRequest.of(pageOffset, pageSize, Direction.ASC, "itemId"))
					.stream();
		} else {
			itemStream = this.itemDao.findAllByChildren(childItemEntity, PageRequest.of(pageOffset, pageSize, Direction.ASC, "itemId"))
					.stream();
		}
				
//			if userRole is player - filter out the inactive items.
//		itemStream = filterInactiveItemsForPlayerUser(itemStream, user);
		
		return itemStream.map(this.itemConverter::toBoundary)
				.collect(Collectors.toList());
	}

	private ItemEntity getExistingItemEntityOrThrow(String itemSpace, String itemId, String errMsg)
			throws RuntimeException {
		Optional<ItemEntity> itemOption = this.itemDao.findById(itemSpace + Constants.DELIMITER + itemId);
		if (!itemOption.isPresent()) {
			throw new ItemNotFoundException(errMsg);
		}
		return itemOption.get();
	}

//	private Stream<ItemEntity> filterInactiveItemsForPlayerUser(Stream<ItemEntity> itemStream, UserEntity user) {
//		if(user.getRole().name().equals(UserRole.PLAYER.name())) {
//			return itemStream.filter(itemEntity -> itemEntity.getActive()); // filter out all items that are inactive
//		} else {
//			return itemStream;
//		}
//	}
	
}
