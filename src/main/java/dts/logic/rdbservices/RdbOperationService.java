package dts.logic.rdbservices;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dts.dal.dao.IdGeneratorEntityDao;
import dts.dal.dao.ItemDao;
import dts.dal.dao.OperationDao;
import dts.dal.data.IdGeneratorEntity;
import dts.dal.data.ItemEntity;
import dts.dal.data.OperationEntity;
import dts.dal.data.UserRole;
import dts.logic.OperationExecutor;
import dts.logic.SearchOperationsService;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.converters.OperationConverter;
import dts.util.Constants;
import dts.util.Functions;
import dts.util.annotation.Permissional;
import dts.util.exceptions.ItemNotFoundException;

@Service
public class RdbOperationService implements SearchOperationsService {
	private String spaceName;
	private OperationDao operationDao;
	private OperationConverter operationConverter;
	private IdGeneratorEntityDao idGeneratorEntityDao;
	private ItemDao itemDao;
	
	private ConfigurableApplicationContext appContext;
	
	@Autowired
	public RdbOperationService(OperationDao operationDao, OperationConverter operationConverter, IdGeneratorEntityDao idGeneratorEntityDao, ItemDao itemDao) {
		this.operationDao = operationDao;
		this.operationConverter = operationConverter;
		this.idGeneratorEntityDao = idGeneratorEntityDao;
		this.itemDao = itemDao;
	}
	
	@Autowired
	public void setAppContext(ConfigurableApplicationContext appContext) {
		this.appContext = appContext;
	}
	
	@Value("${spring.application.name:defultappname}")
	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

	@Override
	@Transactional
	@Permissional(rolesArray = {UserRole.PLAYER})
	public Object invokeOperation(OperationBoundary operation) {
//		validate operation details
		if (operation == null) {
			throw new NullPointerException("operation is null");
		}
		
		if(operation.getType() == null || operation.getType().equals("")) {
			throw new RuntimeException("operation type is null or empty");
		}
		
		if(operation.getInvokedBy() == null) {
			throw new NullPointerException("operation invokedBy is null");
		} else if(operation.getInvokedBy().getUserId() == null) {
			throw new NullPointerException("operation invokedBy userId is null");
		} else if(operation.getInvokedBy().getUserId().getEmail() == null) {
			throw new NullPointerException("operation invokedBy userId email is null");
		} else if(!Functions.isValidEmail(operation.getInvokedBy().getUserId().getEmail())) {
			throw new RuntimeException(operation.getInvokedBy().getUserId().getEmail() + " not a valid email");
		}

//		TODO: add operationAttribute that indicated if its an operation on a specific item or not.
		if(!(operation.getType().equals("createNewBook") || operation.getType().equals("findBooksInDistance") || operation.getType().equals("getAllUserBooks"))) {
			if(operation.getItem() == null) {
				throw new NullPointerException("item invoked operation on is null");
			} else if(operation.getItem().getItemId() == null) {
				throw new NullPointerException("item id invoked operation on is null");
			}
			
//		check if item exist in DB.
			ItemEntity invokedOnItem = this.itemDao.findById(operation.getItem().getItemId().getSpace() + Constants.DELIMITER + operation.getItem().getItemId().getId())
					.orElseThrow(() -> new ItemNotFoundException());
			if(!invokedOnItem.getActive()) {
				throw new RuntimeException("cant perform operations on inactive items.");
			}			
		}
		
//		convert boundary to entity
		OperationEntity operationEntity = this.operationConverter.toEntity(operation);
		
//		generate id
		IdGeneratorEntity idGeneratorEntity = new IdGeneratorEntity();
		idGeneratorEntity = this.idGeneratorEntityDao.save(idGeneratorEntity);
		Long newId = idGeneratorEntity.getId();
		this.idGeneratorEntityDao.deleteById(newId);
		
//		assign values to entity
		operationEntity.setOperationId(this.spaceName + Constants.DELIMITER + newId);
		operationEntity.setCreatedTimestamp(new Date());
	
//		perform actual operation
		Object result = executeOperation(operation);
		
//		save entity to the db
		operationEntity = this.operationDao.save(operationEntity);
		
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<OperationBoundary> getAllOperations(String adminSpace, String adminEmail) {
		return StreamSupport.stream(
				this.operationDao.findAll().spliterator(),  false)// Iterable to Stream<OperationEntity>,
			.map(entity -> this.operationConverter.toBoundary(entity)) // Stream<OperationBoundary>
			.collect(Collectors.toList()); // List<OperationBoundary>
	}

	@Override
	@Transactional
	public void deleteAllActions(String adminSpace, String adminEmail) {
		this.operationDao.deleteAll();		
	}
//	public void deleteAllActions(String adminSpace, String adminEmail) {
////		permission: admin only.
//		if(this.userDao.findById(adminSpace + Constants.DELIMITER + adminEmail).get().getRole().name().equals(UserRole.ADMIN.name())) {
//			this.operationDao.deleteAll();		
//		} else {
//			throw new RuntimeException("permissions: admin only.");
//		}
//	}

	@Override
	@Transactional(readOnly = true)
	public List<OperationBoundary> getAllOperations(String adminSpace, String adminEmail, int size, int page) {
		return this.operationDao.findAll(PageRequest.of(page, size, Direction.ASC, "invokedBy", "type", "operationId"))
				.getContent()
				.stream()
				.map(this.operationConverter::toBoundary)
				.collect(Collectors.toList());
	}
//	public List<OperationBoundary> getAllOperations(String adminSpace, String adminEmail, int size, int page) {
////		permission: admin only.
//		if(this.userDao.findById(adminSpace + Constants.DELIMITER + adminEmail).get().getRole().name().equals(UserRole.ADMIN.name())) {
//			return this.operationDao.findAll(PageRequest.of(page, size, Direction.ASC, "invokedBy", "type", "operationId"))
//					.getContent()
//					.stream()
//					.map(this.operationConverter::toBoundary)
//					.collect(Collectors.toList());
//		} else {
//			throw new RuntimeException("permissions: admin only.");
//		}
//	}
	
	
	private Object executeOperation(OperationBoundary operation) {
		String type = (operation.getType() != null) ? operation.getType() : "default";
		
		OperationExecutor executor = null;
		
		try {
			executor = this.appContext.getBean(type, OperationExecutor.class);
			return executor.executeOperation(operation);
		} catch (BeansException e) {
			throw new RuntimeException("invalid operation type.");
		}
	}
}
