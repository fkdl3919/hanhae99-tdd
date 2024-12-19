package io.hhplus.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.util.GlobalConcurrentControlMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Autowired
    private GlobalConcurrentControlMap controlMap;

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

    @Test
    @DisplayName("포인트 내역 조회 통합 테스트")
    public void pointSelectHistoriesIntegrationTest(){
        // given
        final long id = 1L;
        final long point = 500L;

        // id 가 1이고 기존 point가 500인 유저를 입력
        userPointTable.insertOrUpdate(id, point);

        // 반환할 포인트 내역을 입력
        // id 1 인 유저 2개의 내역
        pointHistoryTable.insert(id, 100, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(id, 100, TransactionType.USE, System.currentTimeMillis());

        // when
        List<PointHistory> returnPointHistories = pointService.selectHistories(id);

        // then
        // 조회한 포인트 내역에 대한 검증
        assertEquals(returnPointHistories.size(), 2);

    }

    /**
     * 동시성 테스트
     * 시나리오 1.
     * A 유저가 충전, 사용 서비스를 동시에 요청할 때
     */
    @Test
    @DisplayName("포인트 service 동시성 테스트")
    public void pointConcurrencyTest() throws ExecutionException, InterruptedException {
        // given
        final long id = 1L;
        final long point = 100L;

        final long usePoint = 100L;

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        Callable<UserPoint> charge = () -> pointService.charge(id, point);

        Callable<UserPoint> use = () -> pointService.use(id, usePoint);

        // when
        // A 유저가 5번의 충전 시도
        executorService.submit(charge);
        executorService.submit(charge);
        executorService.submit(charge);
        executorService.submit(charge);
        executorService.submit(charge);

        // globalMap의 Lock 클래스가 null이 아닌지 체크
        assertNotNull(controlMap.get(id));

        // A 유저가 1번의 사용 시도
        Future<UserPoint> useSubmit = executorService.submit(use);

        try {
            executorService.shutdown();
            executorService.awaitTermination(200, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        UserPoint expected = useSubmit.get();

        // then
        assertEquals(expected.point(), (point * 5) - usePoint);
    }

    /**
     * 동시성 테스트
     * 시나리오 2.
     * A,B,C 유저가 충전 서비스를 동시에 요청할 때
     */
    @Test
    @DisplayName("포인트 service 동시성 테스트")
    public void pointConcurrencyTest2() throws ExecutionException, InterruptedException {
        // given
        final long id = 1L;

        final long id2 = 2L;

        final long id3 = 3L;

        final long point = 100L;

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // user 1
        Callable<UserPoint> charge = () -> pointService.charge(id, point);

        // user 2
        Callable<UserPoint> charge2 = () -> pointService.charge(id2, point);

        // user 3
        Callable<UserPoint> charge3 = () -> pointService.charge(id3, point);

        // when
        // A 유저가 5번의 충전 시도
        Future<UserPoint> submit1 = executorService.submit(charge);
        Future<UserPoint> submit2 = executorService.submit(charge2);
        Future<UserPoint> submit3 = executorService.submit(charge3);

        try {
            executorService.shutdown();
            executorService.awaitTermination(200, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 각 유저의 포인트 충전 결과 값
        UserPoint expected = submit1.get();
        UserPoint expected2 = submit2.get();
        UserPoint expected3 = submit3.get();

        // then
        assertEquals(expected.point(), point);
        assertEquals(expected2.point(), point);
        assertEquals(expected3.point(), point);

    }

    /**
     * 동시성 테스트
     * 시나리오 3.
     * A 유저가 충전, 사용, 조회 를 동시에 요청했을 때 사용자는 순차적인 로직이 마무리 된 후 조회를 원할 경우
     */
    @Test
    @DisplayName("포인트 service 동시성 테스트")
    public void pointConcurrencyTest3() throws ExecutionException, InterruptedException {
        // given
        final long id = 1L;
        final long point = 100L;

        final long usePoint = 100L;

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        Callable<UserPoint> charge = () -> pointService.charge(id, point);

        Callable<UserPoint> use = () -> pointService.use(id, usePoint);

        Callable<UserPoint> selectPoint = () -> pointService.selectPoint(id);

        // when
        // A 유저가 5번의 충전 시도
        executorService.submit(charge);
        executorService.submit(charge);
        executorService.submit(charge);
        executorService.submit(charge);
        executorService.submit(charge);

        // globalMap의 Lock 클래스가 null이 아닌지 체크
        assertNotNull(controlMap.get(id));

        // A 유저가 1번의 사용 시도
        executorService.submit(use);

        // A 유저가 1번의 조회 시도
        Future<UserPoint> useSubmit = executorService.submit(selectPoint);

        try {
            executorService.shutdown();
            executorService.awaitTermination(200, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        UserPoint expected = useSubmit.get();

        // then
        assertEquals(expected.point(), (point * 5) - usePoint);
    }


}
