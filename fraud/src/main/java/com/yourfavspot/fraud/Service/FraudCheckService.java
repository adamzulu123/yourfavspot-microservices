package com.yourfavspot.fraud.Service;

import com.yourfavspot.fraud.Model.FraudCheckHistory;
import com.yourfavspot.fraud.Repository.FraudCheckHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class FraudCheckService {

    private final FraudCheckHistoryRepository fraudcheckHistoryRepository;


    public boolean isFraudulentUser(Integer userId) {
        fraudcheckHistoryRepository.save(
                FraudCheckHistory.builder()
                        .userId(userId)
                        .isFraudster(false)
                        .createdAt(LocalDateTime.now())
                    .build()
        );
        return false; // todo: checking email and stuff like this
    }


}
