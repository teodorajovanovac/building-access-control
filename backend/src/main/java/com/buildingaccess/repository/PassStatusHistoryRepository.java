package com.buildingaccess.repository;

import com.buildingaccess.model.PassStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassStatusHistoryRepository extends JpaRepository<PassStatusHistory, Long> {

    List<PassStatusHistory> findByGatePassIdOrderByChangedAtDesc(Long gatePassId);
}
