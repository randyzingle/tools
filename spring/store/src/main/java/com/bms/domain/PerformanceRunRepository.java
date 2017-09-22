package com.bms.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PerformanceRunRepository extends CrudRepository<ProducerPerformanceRun, String>{
	List<ProducerPerformanceRun> findByRunName(String runName);
}
