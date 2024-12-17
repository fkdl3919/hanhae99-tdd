package io.hhplus.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @Test
    @DisplayName("포인트 충전 통합 테스트")
    public void pointChargeIntegrationTest(){
        // given
        final long id = 1L;
        final long point = 500L;

        // 충전할 포인트
        final long addAmount = 100L;

        // id 가 1이고 기존 point가 500인 유저를 입력
        userPointTable.insertOrUpdate(id, point);

        // when
        UserPoint returnUser = pointService.charge(id, addAmount);

        // then
        assertEquals(returnUser.point(), point + addAmount);

    }

}
