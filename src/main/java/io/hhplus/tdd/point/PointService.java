package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.util.GlobalConcurrentControlMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable  userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final GlobalConcurrentControlMap globalConcurrentControlMap;

    public UserPoint charge(long id, long amount){

        ReentrantLock lock = globalConcurrentControlMap.get(id);

        GlobalConcurrentControlMap.tryLock(lock, 3);

        try {
            UserPoint userPoint = userPointTable.selectById(id);

            // 입력한 id의 유저가 존재하지 않을 시
            if(userPoint == null) {
                throw new IllegalArgumentException("입력한 유저가 존재하지 않습니다.");
            }

            // 입력받은 amount에 대해 검증
            userPoint.validAmount(amount);

            // 입력받은 amount 조회된 user에게 입력
            UserPoint updatedUser = userPoint.addPoint(amount);

            pointHistoryTable.insert(updatedUser.id(), amount, TransactionType.CHARGE, System.currentTimeMillis());

            return userPointTable.insertOrUpdate(updatedUser.id(), updatedUser.point());
        }finally {
            System.out.println("충전 종료");
            lock.unlock();
        }
    }

    public UserPoint use(long id, long amount){

        ReentrantLock lock = globalConcurrentControlMap.get(id);

        GlobalConcurrentControlMap.tryLock(lock, 3);

        try {
            UserPoint userPoint = userPointTable.selectById(id);

            // 입력한 id의 유저가 존재하지 않을 시
            if(userPoint == null) {
                throw new IllegalArgumentException("입력한 유저가 존재하지 않습니다.");
            }

            // 입력받은 amount에 대해 검증
            userPoint.validAmount(amount);

            // 입력받은 amount에 대한 보유 point 검증
            userPoint.validUsePoint(amount);

            // 입력받은 amount 조회된 user에게 입력
            UserPoint updatedUser = userPoint.usePoint(amount);

            pointHistoryTable.insert(updatedUser.id(), amount, TransactionType.USE, System.currentTimeMillis());

            return userPointTable.insertOrUpdate(updatedUser.id(), updatedUser.point());
        }finally {
            System.out.println("사용 종료");
            lock.unlock();
        }
    }

    public UserPoint selectPoint(long id){
        ReentrantLock lock = globalConcurrentControlMap.get(id);

        GlobalConcurrentControlMap.tryLock(lock, 3);

        try {
            UserPoint userPoint = userPointTable.selectById(id);

            // 입력한 id의 유저가 존재하지 않을 시
            if (userPoint == null) {
                throw new IllegalArgumentException("입력한 유저가 존재하지 않습니다.");
            }

            return userPoint;
        }finally {
            System.out.println("조회 종료");
            lock.unlock();
        }
    }

    public List<PointHistory> selectHistories(long id){
        ReentrantLock lock = globalConcurrentControlMap.get(id);

        GlobalConcurrentControlMap.tryLock(lock, 3);

        try {
            UserPoint userPoint = userPointTable.selectById(id);

            // 입력한 id의 유저가 존재하지 않을 시
            if (userPoint == null) {
                throw new IllegalArgumentException("입력한 유저가 존재하지 않습니다.");
            }

            return pointHistoryTable.selectAllByUserId(userPoint.id());
        }finally {
            System.out.println("내역 조회 종료");
            lock.unlock();
        }
    }

}
