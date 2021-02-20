package dts.logic.mockupservices;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;

import dts.dal.data.OperationEntity;
import dts.logic.OperationsService;
import dts.logic.boundaries.OperationBoundary;
import dts.logic.converters.OperationConverter;

//@Service
public class OperationServiceImplementation implements OperationsService {
	private String spaceName;
	private Map<String, OperationEntity> operationStore;
	private AtomicLong idGenerator;
	private OperationConverter operationConverter;

	@Value("${spring.application.name:2021a.hadar.bonavida}")
	public void setSpaceName(String helperName) {
		this.spaceName = helperName;
	}

	@Autowired
	public void setOperationConvertor(OperationConverter operationConverter) {
		this.operationConverter = operationConverter;
	}
	
	@PostConstruct
	public void init() {
		this.operationStore = Collections.synchronizedMap(new HashMap<>());
		this.idGenerator = new AtomicLong(1l);
	}

	@Override
	public Object invokeOperation(OperationBoundary operation) throws RuntimeException {
		if(operation.getInvokedBy() == null || operation.getInvokedBy().getUserId().getEmail() == null) {
			throw new RuntimeException("details of the user invoked are null");
		}
		
		operation.getOperationId().setSpace(this.spaceName);
		operation.getOperationId().setId("" + this.idGenerator.getAndIncrement()); //idGenerator++;
		operation.setCreatedTimestamp(new Date());
		
		OperationEntity operationEntity = this.operationConverter.toEntity(operation);
		
		// MOCKUP database store of the entity
		this.operationStore.put(operationEntity.getOperationId().toString(), operationEntity);
		
		return this.operationConverter.toBoundary(operationEntity);
	}

	@Override
	public List<OperationBoundary> getAllOperations(String adminSpace, String adminEmail) {
		return this.operationStore
				.values() // Collection<OperationEntity>
				.stream() // Stream<OperationEntity>
				.map(entity -> operationConverter.toBoundary(entity)
				) // Stream<OperationBoundary>
				.collect(Collectors.toList()); // List<OperationBoundary> 
	}

	@Override
	public void deleteAllActions(String adminSpace, String adminEmail) {
		this.operationStore.clear();
	}
}
