package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    // 충전할 amount 에 대한 valid 행위를 UserPoint 객체 내에 팩토리 메소드로 정의
    public void validAmount(long amount) {
        // 입력한 point가 0 일 경우
        if(amount == 0) {
            throw new IllegalArgumentException("포인트가 입력되지 않았습니다.");
        }

        // 입력한 amount가 유효한 범위내의 값이 아닌 경우 ( 0 < point <= 1000 )
        if(0 >= amount || amount > 1000) {
            throw new IllegalArgumentException("포인트는 0 초과 1000 이하 범위 내로 입력해 주세요.");
        }

    }

    public UserPoint addPoint (long amount) {
        return new UserPoint(this.id, this.point + amount, System.currentTimeMillis());
    }

}
