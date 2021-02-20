package dts.dal.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import dts.dal.data.UserEntity;

public interface UserDao extends PagingAndSortingRepository<UserEntity, String> {

}
