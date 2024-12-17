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

        // 조회한 유저와 충전 후 유저의 포인트가 같은지 검증
        UserPoint selectUser = userPointTable.selectById(id);
        assertEquals(selectUser.point(), returnUser.point());
        assertEquals(selectUser.id(), returnUser.id());

    }

    @Test
    @DisplayName("포인트 사용 통합 테스트")
    public void pointUseIntegrationTest(){
        // given
        final long id = 1L;
        final long point = 500L;

        // 사용할 포인트
        final long useAmount = 100L;

        // id 가 1이고 기존 point가 500인 유저를 입력
        userPointTable.insertOrUpdate(id, point);

        // when
        UserPoint returnUser = pointService.use(id, useAmount);

        // then
        assertEquals(returnUser.point(), point - useAmount);

        // 조회한 유저와 사용 후 유저의 포인트가 같은지 검증
        UserPoint selectUser = userPointTable.selectById(id);
        assertEquals(selectUser.point(), returnUser.point());
        assertEquals(selectUser.id(), returnUser.id());

    }

    @Test
    @DisplayName("포인트 조회 통합 테스트")
    public void pointSelectIntegrationTest(){
        // given
        final long id = 1L;
        final long point = 500L;

        // id 가 1이고 기존 point가 500인 유저를 입력
        userPointTable.insertOrUpdate(id, point);

        // when
        UserPoint returnUser = pointService.selectPoint(id);

        // then
        // 조회한 유저에 대한 검증
        assertEquals(returnUser.point(), point);
        assertEquals(returnUser.id(), id);

    }


}
