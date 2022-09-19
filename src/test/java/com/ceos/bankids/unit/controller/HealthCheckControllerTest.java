package com.ceos.bankids.unit.controller;

import com.ceos.bankids.mapper.HealthCheckController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HealthCheckControllerTest {

    @Test
    @DisplayName("헬스 체크시, 결과 메시지 반환하는지 확인")
    public void testIfCheckHealthReturnHealthyMessage() {
        // given

        // when
        HealthCheckController healthCheckController = new HealthCheckController();
        String result = healthCheckController.healthCheck();

        // then
        Assertions.assertEquals("Healthy!!", result);
    }
}
