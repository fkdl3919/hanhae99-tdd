package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
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

        // 입력한 point가 0 일 경우
        if(amount == 0) {
            throw new IllegalArgumentException("포인트가 입력되지 않았습니다.");
        }

        return null;
    }

}