package dts.logic;

import java.util.List;

import dts.logic.boundaries.OperationBoundary;

public interface SearchOperationsService extends OperationsService {

	public List<OperationBoundary> getAllOperations(String adminSpace, String adminEmail, int size, int page);
	
}
