package com.sas.mkt.kafka.performance.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProducerPerformanceRunRepository extends JpaRepository<ProducerPerformanceRun, String>{
	List<ProducerPerformanceRun> findByRunName(String runName);
}
