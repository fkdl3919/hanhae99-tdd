package io.hhplus.tdd;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import java.awt.Point;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


/**
 * 컨트롤러에서 처리할 수 있는 예외 상황을 테스트
 * - requestbody가 존재하지 않거나, pathVariable이 존재하지 않거나 등
 */
@AutoConfigureMockMvc
@SpringBootTest
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointService pointService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * PATCH  /point/{id}/charge : 포인트를 충전한다.
     * 예외상항 설정
     * - pathVariable인 id가 유효한 값이 아닌 경우
     * - amount값이 존재하지 않는 경우
     */
    @Test
    @DisplayName("포인트 충전 e2e 통합 테스트")
    public void pointChargeE2ETest1() throws Exception {
        String url = "/charge";

        // given
        final long id = 1L;
        final long amount = 100L;

        // when
        // id가 유효한 값이 아닌 경우
        mockMvc.perform(patch("/point/tt" + url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(amount)))

            // then
            .andExpect(status().isBadRequest());

        // when
        // amount값이 존재하지 않는 경우
        mockMvc.perform(patch("/point/" + id + url)
                .contentType(MediaType.APPLICATION_JSON))

            // then
            .andExpect(status().isBadRequest());


        // when
        // 포인트 충전이 성공한 경우
        MvcResult mvcResult = mockMvc.perform(patch("/point/" + id + url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(amount)))
            .andExpect(status().isOk())
            .andReturn();

        UserPoint userPoint = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserPoint.class);

        // then
        assertEquals(amount, userPoint.point());
        assertEquals(id, userPoint.id());

    }

    /**
     * PATCH /point/{id}/use : 포인트를 사용한다.
     * 예외상항 설정
     * - pathVariable인 id가 유효한 값이 아닌 경우
     * - amount값이 존재하지 않는 경우
     */
    @Test
    @DisplayName("포인트 충전 e2e 통합 테스트")
    public void pointUseE2ETest1() throws Exception {
        String url = "/use";

        // given
        final long id = 1L;
        final long point = 200L;

        final long useAmount = 100L;

        // 사용될 유저를 미리 입력
        pointService.charge(id, point);

        // when
        // id가 유효한 값이 아닌 경우
        mockMvc.perform(patch("/point/tt" + url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(useAmount)))

            // then
            .andExpect(status().isBadRequest());

        // when
        // useAmount값이 존재하지 않는 경우
        mockMvc.perform(patch("/point/" + id + url)
                .contentType(MediaType.APPLICATION_JSON))

            // then
            .andExpect(status().isBadRequest());

        // when
        // 포인트 사용이 성공한 경우 사용된 포인트의 잔고를 확인
        MvcResult mvcResult = mockMvc.perform(patch("/point/" + id + url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(useAmount)))
            .andExpect(status().isOk())
            .andReturn();

        UserPoint userPoint = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserPoint.class);

        // then
        assertEquals( point - useAmount, userPoint.point());
        assertEquals(id, userPoint.id());

    }

    /**
     * GET /point/{id} : 포인트를 조회한다.
     * 예외상항 설정
     * - pathVariable인 id가 유효한 값이 아닌 경우
     */
    @Test
    @DisplayName("포인트 조회 e2e 통합 테스트")
    public void pointSelectE2ETest1() throws Exception {

        // given
        final long id = 1L;
        final long point = 200L;

        // 사용될 유저를 미리 입력
        pointService.charge(id, point);

        // when
        // id가 유효한 값이 아닌 경우
        mockMvc.perform(get("/point/tt"))

            // then
            .andExpect(status().isBadRequest());


        // when
        // 포인트 사용이 성공한 경우 사용된 포인트의 잔고를 확인
        MvcResult mvcResult = mockMvc.perform(get("/point/" + id ))
            .andExpect(status().isOk())
            .andReturn();

        UserPoint userPoint = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserPoint.class);

        // then
        assertEquals(point, userPoint.point());
        assertEquals(id, userPoint.id());

    }

    /**
     * GET /point/{id}/histories : 포인트 내역을 조회한다.
     * 예외상항 설정
     * - pathVariable인 id가 유효한 값이 아닌 경우
     */
    @Test
    @DisplayName("포인트 내역 조회 e2e 통합 테스트")
    public void pointSelectHistoriesE2ETest1() throws Exception {
        String url = "/histories";

        // given
        final long id = 1L;
        final long point = 200L;

        // 사용될 유저를 미리 입력
        pointService.charge(id, point);
        pointService.charge(id, point);

        // when
        // id가 유효한 값이 아닌 경우
        mockMvc.perform(get("/point/tt" + url))
            // then
            .andExpect(status().isBadRequest());


        // when
        // 포인트 사용이 성공한 경우 사용된 포인트의 잔고를 확인
        MvcResult mvcResult = mockMvc.perform(get("/point/" + id  + url ))
            .andExpect(status().isOk())
            .andReturn();

        List<PointHistory> userPointHistories = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<PointHistory>>() {});

        // then
        assertEquals(2, userPointHistories.size());

    }


}
