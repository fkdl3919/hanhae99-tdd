package io.hhplus.tdd;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import io.hhplus.tdd.database.UserPointTable;
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
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    /**
     * PATCH  /point/{id}/charge : 포인트를 충전한다.
     */
    @Test
    @DisplayName("포인트_충전_시_입력한_id의_사용자가_존재하지_않는_경우")
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
