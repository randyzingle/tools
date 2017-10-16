package com.sas.mkt.kafka.performance.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FlatRunRepository extends JpaRepository<FlatRun, String>{
	List<FlatRun> findByRunName(String runName);
}