package com.sas.mkt.kafka.performance.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProducerPropertyRepository extends JpaRepository<ProducerProperty, Long>{
//	List<ProducerProperty> findByRunName(String runName);
}