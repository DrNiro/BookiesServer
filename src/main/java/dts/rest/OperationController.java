package dts.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dts.logic.OperationsService;
import dts.logic.boundaries.OperationBoundary;

@RestController
public class OperationController {
	private OperationsService operationService;
	
	@Autowired
	public void setOperationService(OperationsService operationService) {
		this.operationService = operationService;
	}

	@RequestMapping(method = RequestMethod.POST, 
			path = "/dts/operations", 
			produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	  public Object invokeOperationOnItem(@RequestBody OperationBoundary op) {
		return this.operationService.invokeOperation(op);
	  }

}
