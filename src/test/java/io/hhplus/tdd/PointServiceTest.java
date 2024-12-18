package io.hhplus.tdd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    private PointService pointService; // 테스트 대상

    @Mock
    private UserPointTable userPointTable; // 테스트 대상의 의존성을 해결하기 위해, 예상된 결과를 반환하기 위하여 user table을 mock 으로 설정함

    @Mock
    private PointHistoryTable pointHistoryTable; // 테스트 대상의 의존성을 해결하기 위해, 예상된 결과를 반환하기 위하여 point table을 mock 으로 설정함


    /**
     * PATCH  /point/{id}/charge : 포인트를 충전한다.
     * 예외상항 설정
     * - 포인트 충전 시 입력한 id의 사용자가 존재하지 않는 경우
     */
    @Test
    @DisplayName("포인트 충전 시 입력한 id의 사용자가 존재하지 않는 경우")
    public void pointChargeTest1(){
        // given
        final long id = 1L;
        final long amount = 500L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 해당 유저가 존재하지 않음
        when(userPointTable.selectById(id)).thenReturn(null);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
           pointService.charge(id, amount);
        });

        // then
        assertEquals("입력한 유저가 존재하지 않습니다.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * PATCH  /point/{id}/charge : 포인트를 충전한다.
     * 예외상항 설정
     * - 포인트 충전 시 입력한 amount가 0 또는 입력되지 않았을 경우
     * - 포인트 기본값은 0 으로 설정
     */
    @Test
    @DisplayName("포인트 충전 시 입력한 amount 가 0 또는 입력되지 않았을 경우")
    public void pointChargeTest2(){
        // given
        final long id = 1L;
        final long amount = 0L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 원하는 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(UserPoint.empty(id));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(id, amount);
        });

        // then
        assertEquals("포인트가 입력되지 않았습니다.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * PATCH  /point/{id}/charge : 포인트를 충전한다.
     * 예외상항 설정
     * - 포인트 충전 시 입력한 amount가 유효한 범위내의 값이 아닌 경우 ( 0 < point <= 1000 )
     */
    @Test
    @DisplayName("포인트 충전 시 입력한 amount가 유효한 범위내의 값이 아닌 경우")
    public void pointChargeTest3(){
        // given
        final long id = 1L;
        final long amount = 1001L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 원하는 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(UserPoint.empty(id));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(id, amount);
        });

        // then
        assertEquals("포인트는 0 초과 1000 이하 범위 내로 입력해 주세요.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * PATCH  /point/{id}/charge : 포인트를 충전한다.
     *
     */
    @Test
    @DisplayName("포인트 충전 성공 테스트")
    public void pointChargeTest4(){
        // given
        final long id = 1L;
        final long amount = 500L;

        final long addAmount = 100L;

        // 조회 된 유저 정의
        UserPoint selectUser = new UserPoint(id, amount, System.currentTimeMillis());

        // 업데이트 된 유저 정의 ( 조회된 유저의 포인트 충전이 완료 된 )
        UserPoint updatedUser = new UserPoint(selectUser.id(), selectUser.point() + addAmount, System.currentTimeMillis());

        // stub
        // 주어진 id로 조회 된 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(selectUser);

        // 업데이트 된 유저 반환
        when(userPointTable.insertOrUpdate(updatedUser.id(), updatedUser.point())).thenReturn(updatedUser);

        // when
        UserPoint returnUser = pointService.charge(id, addAmount);

        // then
        // stub 으로 설정한 A와 B의 id, point 값이 같은 지 확인
        assertEquals(returnUser.id(), updatedUser.id());
        assertEquals(returnUser.point(), updatedUser.point());
        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(updatedUser.id(), updatedUser.point());

    }

    /**
     * PATCH /point/{id}/use : 포인트를 사용한다.
     * 예외상항 설정
     * - 포인트 사용 시 입력한 id의 사용자가 존재하지 않는 경우
     */
    @Test
    @DisplayName("포인트 사용 시 입력한 id의 사용자가 존재하지 않는 경우")
    public void pointUseTest1(){
        // given
        final long id = 1L;
        final long amount = 100L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 해당 유저가 존재하지 않음
        when(userPointTable.selectById(id)).thenReturn(null);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(id, amount);
        });

        // then
        assertEquals("입력한 유저가 존재하지 않습니다.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * PATCH /point/{id}/use : 포인트를 사용한다.
     * 예외상항 설정
     * - 포인트 사용 시 입력한 amount가 0 또는 입력되지 않았을 경우
     * - 포인트 기본값은 0 으로 설정
     */
    @Test
    @DisplayName("포인트 사용 시 입력한 amount 가 0 또는 입력되지 않았을 경우")
    public void pointUseTest2(){
        // given
        final long id = 1L;
        final long amount = 0L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 원하는 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(UserPoint.empty(id));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(id, amount);
        });

        // then
        assertEquals("포인트가 입력되지 않았습니다.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * PATCH /point/{id}/use : 포인트를 사용한다.
     * 예외상항 설정
     * - 포인트 사용 시 입력한 amount가 유효한 범위내의 값이 아닌 경우 ( 0 < point <= 1000 )
     */
    @Test
    @DisplayName("포인트 사용 시 입력한 amount가 유효한 범위내의 값이 아닌 경우")
    public void pointUseTest3(){
        // given
        final long id = 1L;
        final long amount = 1001L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 원하는 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(UserPoint.empty(id));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(id, amount);
        });

        // then
        assertEquals("포인트는 0 초과 1000 이하 범위 내로 입력해 주세요.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * PATCH /point/{id}/use : 포인트를 사용한다.
     * 예외상항 설정
     * - 포인트 사용 시 입력한 amount보다 조회된 user의 point잔고가 부족할 경우
     */
    @Test
    @DisplayName("포인트 사용 시 입력한 amount보다 조회된 user의 point잔고가 부족할 경우")
    public void pointUseTest4(){
        // given
        final long id = 1L;
        final long point = 100L;
        final long amount = 200L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 원하는 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(new UserPoint(id, point, System.currentTimeMillis()));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(id, amount);
        });

        // then
        assertEquals("사용하려는 포인트가 보유한 포인트를 초과하였습니다.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * PATCH /point/{id}/use : 포인트를 사용한다.
     *
     */
    @Test
    @DisplayName("포인트 사용 성공 테스트")
    public void pointUseTest5(){
        // given
        final long id = 1L;
        final long point = 500L;

        final long useAmount = 100L;

        // 조회 된 유저 정의
        UserPoint selectUser = new UserPoint(id, point, System.currentTimeMillis());

        // stub
        // 주어진 id로 조회 된 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(selectUser);

        // 업데이트 된 유저 정의 ( 조회된 유저의 포인트 사용이 완료 된 )
        UserPoint updatedUser = new UserPoint(selectUser.id(), selectUser.point() - useAmount, System.currentTimeMillis());

        // stub
        // 업데이트 된 유저 반환
        when(userPointTable.insertOrUpdate(updatedUser.id(), updatedUser.point())).thenReturn(updatedUser);

        // when
        UserPoint returnUser = pointService.use(id, useAmount);

        // then
        // stub 으로 설정한 A와 B의 id, point 값이 같은 지 확인
        assertEquals(returnUser.id(), updatedUser.id());
        assertEquals(returnUser.point(), updatedUser.point());
        verify(userPointTable, times(1)).selectById(id);
        verify(userPointTable, times(1)).insertOrUpdate(updatedUser.id(), updatedUser.point());

    }

    /**
     * GET /point/{id} : 포인트를 조회한다.
     * 예외상항 설정
     * - 포인트 조회 시 입력한 id의 사용자가 존재하지 않는 경우
     */
    @Test
    @DisplayName("포인트 조회 시 입력한 id의 사용자가 존재하지 않는 경우")
    public void pointSelectTest1(){
        // given
        final long id = 1L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 해당 유저가 존재하지 않음
        when(userPointTable.selectById(id)).thenReturn(null);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.selectPoint(id);
        });

        // then
        assertEquals("입력한 유저가 존재하지 않습니다.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * GET /point/{id} : 포인트를 조회한다.
     *
     */
    @Test
    @DisplayName("포인트 조회 성공 테스트")
    public void pointSelectTest2(){
        // given
        final long id = 1L;
        final long point = 500L;

        // 조회 된 유저 정의
        UserPoint selectUser = new UserPoint(id, point, System.currentTimeMillis());

        // stub
        // 주어진 id로 조회 된 유저를 반환
        when(userPointTable.selectById(id)).thenReturn(selectUser);

        // when
        UserPoint returnUser = pointService.selectPoint(id);

        // then
        // stub 으로 설정한 A와 B의 id, point 값이 같은 지 확인
        assertEquals(returnUser.id(), selectUser.id());
        assertEquals(returnUser.point(), selectUser.point());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * GET /point/{id}/histories : 포인트 내역을 조회한다.
     * 예외상항 설정
     * - 포인트 내역 조회 시 입력한 id의 사용자가 존재하지 않는 경우
     */
    @Test
    @DisplayName("포인트 내역 조회 시 입력한 id의 사용자가 존재하지 않는 경우")
    public void pointSelectHistoriesTest1(){
        // given
        final long id = 1L;

        // stub
        // 주어진 id로 유저를 검색 했을 때 해당 유저가 존재하지 않음
        when(userPointTable.selectById(id)).thenReturn(null);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.selectHistories(id);
        });

        // then
        assertEquals("입력한 유저가 존재하지 않습니다.", exception.getMessage());
        verify(userPointTable, times(1)).selectById(id);

    }

    /**
     * GET /point/{id}/histories : 포인트 내역을 조회한다.
     *
     */
    @Test
    @DisplayName("포인트 내역 조회 성공 테스트")
    public void pointSelectHistoriesTest2(){
        // given
        final long id = 1L;

        // 반환할 포인트 내역을 미리 정의
        // id 1 인 유저 2개의 내역
        List<PointHistory> pointHistories = Arrays.asList(
            new PointHistory(1, id, 100, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(2, id, 100, TransactionType.USE, System.currentTimeMillis())
        );

        // stub
        // 주어진 id로 조회 된 포인트 내역을 반환
        when(pointHistoryTable.selectAllByUserId(id)).thenReturn(pointHistories);

        // when
        List<PointHistory> returnPointHistories = pointService.selectHistories(id);

        // then
        // stub 으로 설정한 포인트 내역 검증
        assertEquals(returnPointHistories.size(), pointHistories.size());

        verify(userPointTable, times(1)).selectById(id);
        verify(pointHistoryTable, times(1)).selectAllByUserId(id);

    }

}
