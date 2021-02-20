package dts.dal.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import dts.dal.data.OperationEntity;

public interface OperationDao extends PagingAndSortingRepository<OperationEntity, String>{

}
