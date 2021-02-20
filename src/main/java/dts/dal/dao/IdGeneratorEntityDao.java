package dts.dal.dao;

import org.springframework.data.repository.CrudRepository;

import dts.dal.data.IdGeneratorEntity;

public interface IdGeneratorEntityDao extends CrudRepository<IdGeneratorEntity, Long> {

}
