package com.yourfavspot.fraud.Repository;

import com.yourfavspot.fraud.Model.FraudCheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudCheckHistoryRepository extends JpaRepository<FraudCheckHistory, Integer> {


}
