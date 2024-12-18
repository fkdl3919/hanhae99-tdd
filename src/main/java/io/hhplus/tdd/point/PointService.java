package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable  userPointTable;

    public UserPoint charge(long id, long amount){

        UserPoint userPoint = userPointTable.selectById(id);

        // 입력한 id의 유저가 존재하지 않을 시
        if(userPoint == null) {
            throw new IllegalArgumentException("입력한 유저가 존재하지 않습니다.");
        }

        // 입력받은 amount에 대해 검증
        userPoint.validAmount(amount);

        // 입력받은 amount 조회된 user에게 입력
        UserPoint updatedUser = userPoint.addPoint(amount);

        return userPointTable.insertOrUpdate(updatedUser.id(), updatedUser.point());
    }

    public UserPoint use(long id, long amount){

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

        return userPointTable.insertOrUpdate(updatedUser.id(), updatedUser.point());
    }

    public UserPoint selectPoint(long id){

        UserPoint userPoint = userPointTable.selectById(id);

        // 입력한 id의 유저가 존재하지 않을 시
        if(userPoint == null) {
            throw new IllegalArgumentException("입력한 유저가 존재하지 않습니다.");
        }

        return userPoint;
    }

    public List<UserPoint> selectHistories(long id){

        UserPoint userPoint = userPointTable.selectById(id);

        // 입력한 id의 유저가 존재하지 않을 시
        if(userPoint == null) {
            throw new IllegalArgumentException("입력한 유저가 존재하지 않습니다.");
        }

        return null;
    }


}
