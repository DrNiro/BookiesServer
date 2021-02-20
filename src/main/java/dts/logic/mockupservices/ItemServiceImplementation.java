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
import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Service;

import dts.dal.data.ItemEntity;
import dts.logic.ItemsService;
import dts.logic.boundaries.ItemBoundary;
import dts.logic.boundaries.subboundaries.ItemIdBoundary;
import dts.logic.converters.ItemConverter;

//@Service
public class ItemServiceImplementation implements ItemsService, CommandLineRunner {
	private String spaceName;
	private Map<String, ItemEntity> itemStore;
	private AtomicLong idGenerator;
	private ItemConverter itemConverter;
	
	@Value("${spring.application.name:defultappname}")
	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}
	
	@Autowired
	public void setItemConverter(ItemConverter itemConverter) {
		this.itemConverter = itemConverter;
	}
	
	@PostConstruct
	public void init() {
		this.itemStore = Collections.synchronizedMap(new HashMap<>()); // thread safe map
		this.idGenerator = new AtomicLong(1l);
	}
	
	@Override
	public void run(String... args) throws Exception {
		System.err.println(this.spaceName);
		
	}

	@Override
	public ItemBoundary create(String managerSpace, String managerEmail, ItemBoundary newItem) {		
		newItem.getItemId().setSpace(this.spaceName);
		newItem.getItemId().setId("" + this.idGenerator.getAndIncrement()); //idGenerator++;
		newItem.setCreatedTimestamp(new Date());
		newItem.getCreatedBy().getUserId().setSpace(managerSpace);
		newItem.getCreatedBy().getUserId().setEmail(managerEmail);
		
		ItemEntity itemEntity = this.itemConverter.toEntity(newItem);
		
		// MOCKUP database store of the entity
		this.itemStore.put(newItem.getItemId().toString(), itemEntity);
		
		return this.itemConverter.toBoundary(itemEntity);
	}

	@Override
	public ItemBoundary update(String managerSpace, String managerEmail, String itemSpace, String itemId, ItemBoundary update) throws RuntimeException {
		if (!this.itemStore.containsKey(new ItemIdBoundary(itemSpace, itemId).toString())) {
			throw new RuntimeException("item does not exist in the system");
		}
		
		ItemBoundary originalItem = this.itemConverter.toBoundary(this.itemStore.get(new ItemIdBoundary(itemSpace, itemId).toString()));

		update.setItemId(originalItem.getItemId());
		update.setCreatedBy(originalItem.getCreatedBy());
		update.setCreatedTimestamp(originalItem.getCreatedTimestamp());
				
		ItemEntity itemEntity = this.itemConverter.toEntity(update);
		
		// MOCKUP database store of the entity
		this.itemStore.put(update.getItemId().toString(), itemEntity);

		return this.itemConverter.toBoundary(itemEntity);
	}

	@Override
	public List<ItemBoundary> getAll(String userSpace, String userEmail) {
		return this.itemStore
				.values() // Collection<ItemEntity>
				.stream() // Stream<ItemEntity>
				.map(entity -> itemConverter.toBoundary(entity)
				) // Stream<ItemBoundary>
				.collect(Collectors.toList()); // List<ItemBoundary> 
	}

	@Override
	public ItemBoundary getSpecificItem(String userSpace, String userEmail, String itemSpace, String itemId) throws RuntimeException {
		if (!this.itemStore.containsKey(new ItemIdBoundary(itemSpace, itemId).toString())) {
			throw new RuntimeException("item does not exist in the system");
		}
		
		return this.itemConverter.toBoundary(this.itemStore.get(new ItemIdBoundary(itemSpace, itemId).toString()));
	}

	@Override
	public void deleteAll(String adminSpace, String adminEmail) {
		this.itemStore.clear();	
	}
 
}
 