package dts.dal.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class IdGeneratorEntity {
	private Long id;
	
	public IdGeneratorEntity() {
		
	}

	@Id	
	@GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	
}
