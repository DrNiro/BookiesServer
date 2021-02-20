package dts.logic;

import java.util.List;

import dts.logic.boundaries.OperationBoundary;

public interface OperationsService {

	public Object invokeOperation(OperationBoundary operation);
	public List<OperationBoundary> getAllOperations(String adminSpace, String adminEmail);
	public void deleteAllActions(String adminSpace, String adminEmail);
	
}
