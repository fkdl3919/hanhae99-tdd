package io.hhplus.tdd.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class GlobalConcurrentControlMap {

    /**
     * 어느 쓰레드에서 접근하더라도 같은 객체로 컨트롤하기 위해서 Spring Context의 Component로 설정
     * 동시성 제어를 하기 위해서 선언
     * - < user id, Lock > 형식으로 userId 별 Lock을 제공
     *
     * hashmap 은 동기화를 지원하지 않지만
     * ConcurrentHashMap 의 put 메소드는 동기화를 지원하기 때문에 사용 함
     */
    private final Map<Long, ReentrantLock> globalMap = new ConcurrentHashMap<>();

    /**
     * key 값을 userId 로 설정하여 userId 별 lock을 반환하도록
     *
     * userId로 get 시도 시 Lock이 존재하지 않으면 새로운 Lock을 생성해 반환
     * @param id
     * @return
     */
    public synchronized ReentrantLock get(Long id) {
        ReentrantLock reentrantLock = globalMap.get(id);
        if(reentrantLock == null){

            // 쓰레드 별 순차적인 공정성을 보장하기 위해서 fair 옵션 true로 설정
            reentrantLock = new ReentrantLock(true);
            this.put(id, reentrantLock);
        }

        return reentrantLock;
    }

    /**
     * key 값을 userId 로 설정하여 userId 별 lock을 관리
     *
     * 같은 user 별 같은 Lock을 갖기 위하여
     * @param id
     * @return
     */
    public void put(Long id, ReentrantLock lock) {
        globalMap.put(id, lock);
    }

    public void remove(Long id) {
        globalMap.remove(id);
    }

    /**
     * lock을 획득하기 위한 작업을 static 메소드로 선언, 사용할 lock과 최대 대기사간을 인수로 받는다.
     * @param lock
     * @param second
     */
    public static void tryLock(ReentrantLock lock, long second){
        try {
            if(!lock.tryLock(second, TimeUnit.SECONDS)) throw new InterruptedException();
        } catch (InterruptedException e) {
            throw new RuntimeException("연결시간이 초과되었습니다.");
        }
    }

}
