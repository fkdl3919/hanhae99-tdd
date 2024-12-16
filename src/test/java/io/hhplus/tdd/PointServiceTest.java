package io.hhplus.tdd;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.assertj.core.api.Assertions;
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

        // when then
        // when 과 then 단계를 한번에 진행하여 검증
        assertThatThrownBy(() -> pointService.charge(id, amount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("해당 유저가 존재하지 않습니다.");
    }

}
