package dts.dal.data;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Lob;


@Entity
@Table(name = "OPERATIONS")
public class OperationEntity {
	private String operationId;
	private String type;
	private String item;
	private Date createdTimestamp;
	private String invokedBy;
	private String operationAttributes;
	
	public OperationEntity() {
	
	}

	public OperationEntity(String operationId, String type, String item, Date createdTimestamp, String invokedBy,
			String operationAttributes) {
		setOperationId(operationId);
		setType(type);
		setItem(item);
		setCreatedTimestamp(createdTimestamp);
		setInvokedBy(invokedBy);
		setOperationAttributes(operationAttributes);
	}

	@Id
	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public String getInvokedBy() {
		return invokedBy;
	}

	public void setInvokedBy(String invokedBy) {
		this.invokedBy = invokedBy;
	}

	@Lob
	public String getOperationAttributes() {
		return operationAttributes;
	}

	public void setOperationAttributes(String operationAttributes) {
		this.operationAttributes = operationAttributes;
	}
	
}
