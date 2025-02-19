package com.yourfavspot.fraud.Controller;

import com.yourfavspot.fraud.Model.FraudCheckResponse;
import com.yourfavspot.fraud.Service.FraudCheckService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("api/v1/fraud-check")
public class FraudController {

    private FraudCheckService fraudCheckService;

    @GetMapping(path = "{userId}")
    public FraudCheckResponse isFraudster(@PathVariable("userId") Integer userId){
        boolean isFraudulentUser = fraudCheckService.isFraudulentUser(userId);
        log.info("fraud check request for user {}", userId);
        return new FraudCheckResponse(isFraudulentUser);
    }

}
