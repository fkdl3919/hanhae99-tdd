package io.hhplus.tdd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
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

}
