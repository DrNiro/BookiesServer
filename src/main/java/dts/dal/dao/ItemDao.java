package dts.dal.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import dts.dal.data.ItemEntity;

public interface ItemDao extends PagingAndSortingRepository<ItemEntity, String> {

	List<ItemEntity> findByNameContaining(String name, Pageable arg0);
	List<ItemEntity> findByType(String type, Pageable arg0);
	List<ItemEntity> findByTypeAndActiveTrue(String type, Pageable arg0);
	List<ItemEntity> findByNameContainingAndActiveTrue(String name, Pageable arg0);

	
	List<ItemEntity> findAllByParents(ItemEntity parent, Pageable arg0); // find all item entities where parents set contains the parent (meaning all children). 
	List<ItemEntity> findAllByChildren(ItemEntity child, Pageable arg0); // find all item entities where children set contain the child (meaning all parents).
	List<ItemEntity> findAllByParentsAndActiveTrue(ItemEntity parent, Pageable arg0); // find all item entities where parents set contains the parent (meaning all children). 
	List<ItemEntity> findAllByChildrenAndActiveTrue(ItemEntity child, Pageable arg0); // find all item entities where children set contain the child (meaning all parents).

	List<ItemEntity> findAllByTypeAndActiveTrue(String type);
	
	List<ItemEntity> findByLatBetweenAndLngBetween(double latMin, double latMax, double lngMin, double lngMax, Pageable arg0);
	List<ItemEntity> findByLatBetweenAndLngBetweenAndActiveTrue(double latMin, double latMax, double lngMin, double lngMax, Pageable arg0);
	List<ItemEntity> findByLatBetweenAndLngBetweenAndTypeAndActive(double latMin, double latMax, double lngMin, double lngMax, String type, boolean active, Pageable arg0);
	
	List<ItemEntity> findByActiveTrue(Pageable arg0);

}
