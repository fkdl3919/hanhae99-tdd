동시성 제어

> 동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.

1. 동시성 제어가 필요한 이유는 무엇일까? 에 대해 생각해 보자.
    - 동시성 제어가 필요한 이유는 **여러 쓰레드나 프로세스가 동일한 자원에 접근하거나 수정할 때 발생할 수 있는 충돌이나 데이터 불일치를 방지하기 위해서** 이다.
2. 필요한 이유를 알았다면 해당 과제에서 필요한 이유는 무엇일까? 알아보자
### 시나리오1
사용자 A, B, C 의 요청이 동시에 하나의 기능에 도달할 때
- 예를 들어
    - A,B,C 의 요청 -> 충전 기능

해당 시나리오에서는 동시성 제어가 필요할까? 를 생각해보면 필요하지 않다.
왜냐하면 사용자 A,B,C는 각자 본인이 갖고 있는 Point 자원은 사용자 고유자원이기 때문이다.
예로 A사용자가 충전중이라고 해서 B사용자가 충전할 때 A 사용자를 기다려야 할까?
논리적으로 맞지 않다.

### 시나리오2
사용자 A의 여러 요청이 동시에 여러 기능에 도달할 때
- 예를 들어
    - A, A, A 의 요청 -> 충전 기능, 사용 기능, 조회 기능

앞선 시나리오1에 대해 크게 달라진 점은 요청하는 사용자가 한명으로 줄었다는 것이다.
그렇다면 시나리오2 에 대해서는 동시성 제어가 필요할까? 를 논리적으로 생각해 본다면
사용자 A는 포인트 충전 -> 포인트 사용 -> 포인트 조회 에 대한 요청을 동시에 보냈을 때
사용자의 기대값은 충전이 완료되고 사용 완료 되었으며 그에 대한 포인트를 조회 받기 원할 것이다.
그렇기에 해당 시나리오는 사용자의 고유자원에 대한 접근으로 각 요청에 대해 동시성 제어가 필요하다.

### 해결 방안에 대한 시나리오
시나리오 2 에 대한 해결방안을 살펴보자면

해당 과제는 DB에 대한 접근이 없으므로 DB 수준에서 동시성 처리를 하는건 아니고
어플리케이션 수준에서 동시성 처리를 하려고 한다.

요구사항의 각 요청별로 사용자는 자신의 ID 를 포함하여 서버로 요청을 한다.
그러므로 동일한 ID(Long 타입) 에 대해서 만 동시성 처리를 한다.

### 여러가지 해결 방안

- 적용 전

```java
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

        pointHistoryTable.insert(updatedUser.id(), amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPointTable.insertOrUpdate(updatedUser.id(), updatedUser.point());
    }

```

#### synchronized 키워드 사용하는 방법
해당 키워드를 사용하여 다른 스레드의 접근을 막는 방법이다.
- 해당 과제의 문제를 보고 제일 먼저 떠올랐던 해결 방법이다. 하지만 그리 훌륭한 해결방안은 되지 못하였다. 그 이유를 살펴보면

```java
public synchronized void charge(String userId, Long point) { // .. }
```

synchronized 키워드를 적용 후
만약 사용자 A 와 B 가 동시에 충전 기능을 요청을 했다고 가정하자.
그렇다면 어떠한 문제 점이 발생 할 수 있을까?
1. A 와 B가 다루는 서로 다른 자원임에도 불구하고 B는 A의 충전이 끝날 때까지 기다려야 한다.

앞서 설명한 시나리오1에 포함되는 문제로 synchronized를 사용하면 스레드 간 세밀한 제어가 불가능하기 때문에 논리적으로 맞지 않는 부분이 발생한다.

#### ReentrantLock을 사용하는 방법
java.util.concurrent 패키지에서 제공하는 객체로 synchronized 보다 좀 더 세밀한 제어가 가능하다.
synchronized는 키워드로 적용되는 반면 ReentrantLock은 객체로 관리가 되며
다른 스레드가 이미 Lock을 가지고 있는 경우 일정시간 동안만 기다리거나, 아예 기다리지 않고 interrupt될 수 있다.
그리고 공정성을 지원한다.

### 해결 방법
앞서 두 개의 해결 방안 그리고 시나리오들을 기반한 결과 적절한 방법으로 택한 것은
ReentrantLock을 사용하는 방법이다.
해당 Lock 제어 방법은 객체로 관리가 되고 그렇다는 건 userId 별로 Lock 제어가 가능하다는 뜻이기에 해당 메소드를 다음과 같이 수정하였다.

```java

// 포인트충전 메소드
public UserPoint charge(long id, long amount){  
  
    ReentrantLock lock = globalConcurrentControlMap.get(id);  
  
    try {  
        if(!lock.tryLock(3, TimeUnit.SECONDS)) throw new InterruptedException();
    } catch (InterruptedException e) {  
        throw new RuntimeException(e);  
    }  
  
    try {  
        // ... 메서드 본문
    }finally {  
        System.out.println("충전 종료");  
        lock.unlock();  
    }  
}

@Component  
public class GlobalConcurrentControlMap {  
  
    private final Map<Long, ReentrantLock> globalMap = new HashMap<>();  
  
    public synchronized ReentrantLock get(Long id) {  
        ReentrantLock reentrantLock = globalMap.get(id);  
        if(reentrantLock == null){  
            reentrantLock = new ReentrantLock(true);  
            this.put(id, reentrantLock);  
        }  
  
        return reentrantLock;  
    }  
  
    public void put(Long id, ReentrantLock lock) {  
        globalMap.put(id, lock);  
    }  
  
    public void remove(Long id) {  
        globalMap.remove(id);  
    }  
}

```

charge 메소드 상단을 보면 userId 별로 Lock을 제어할 수 있도록 globalConcurrentControlMap 객체를 만들었다.

GlobalConcurrentControlMap 객체는 Component로 선언되었는데 이유는 Spring 서버가 단일 서버로 운영되고, 여러 쓰레드에서 접근하더라도 같은 상태를 보도록 Spring Context에서 하나의 Bean으로만 관리가 되도록 Component로 선언 하였다.

사용자는 userId 를 키값으로 globalConcurrentControlMap에서 Lock을 획득하고 없으면
`new ReentrantLock(true);` 키워드를 사용해 새로운 ReentrantLock을 생성한다.
ReentrantLock 생성 시 생성자 파라미터에 true로 입력하면 해당 Lock은 공정성을 보장해주도록 한다. (동시 요청들이 순차적으로 진행되기 위해서)

그리고 사용자는 해당 Lock의 lock을 취득할 때 최대 3초의 대기시간을 갖는다. 대기시간 동안 lock을 취득하지 못한다면 InterruptedException 이 발생하며 해당 메소드는 exception을 던진다.

사용자는 Lock을 정상적으로 취득 후 메소드가 종료 되면 해당 락을 unlock 상태로 변경한다.

### 문제 상황
위와 같은 해결방법으로 동시성 처리에 대한 해결은 했지만 한가지 과제가 남아있다.
바로 GlobalConcurrentControlMap 에 대한 관리가 안되고 있다는 점이다.
어느 문제점을 야기할 수 있는지 예상해 본다면
1. GlobalConcurrentControlMap 의 상태관리 Map은 HashMap이 사용되고 있어
   thread - safe 하지 않다는 점이다. 스레드 동기화를 지원하지 않아 여러 스레드가 동시에 접근하여 해당 map에 읽기/쓰기 작업이 수행된다면 데이터 무결성이 지켜지지 않을 수 있다.
3. 사용자가 제한적이지 않다는 점. 서로 다른 userId가 무수히 많을 수 있다는 점이다. 그런 점에서 봤을 때 GlobalConcurrentControlMap 의 상태관리 Map은 입력/조회 동작만 있을 뿐 제거 동작은 없다. 그래서 Map의 사이즈가 관리가 되지않아 Spring 서비스가 중단되지 않는 이상 무한하게 커질 수 있다.

### 문제 해결
- HashMap의 thread - safe 문제
    - 동기화를 지원하는 ConcurrentHashMap으로 변경
    - UserLock을 가져오는 GlobalConcurrentControlMap get 메소드에 synchronized 키워드 추가 - 동시에 get 메소드를 호출 할 경우에 대비하여
- GlobalConcurrentControlMap 의 Map 사이즈가 무수히 늘어날 수 있는 문제
    - ReetrantLock의 상태값을 가지는 UserLock 클래스 생성.
```java
public class UserLock {  
  
    private ReentrantLock lock;  
    private int counter;  
  
    public UserLock(ReentrantLock lock) {  
        this.lock = lock;  
        this.counter = 0;  
    }  
  
    public int getCounter() {  
        return this.counter;  
    }  
  
    public ReentrantLock getLock() {  
        return lock;  
    }  
  
    public synchronized void incrementCounter() {  
        counter++;  
    }  
  
    public synchronized void decrementCounter() {  
        counter--;  
    }  
  
    public synchronized boolean isUnused() {  
        return counter == 0;  
    }  
  
}
```

ReentrantLock과 참조 카운트를 관리하는 상태값을 한 객체로 관리함으로 써
해당 유저에 lock을 설정할 때 incrementCounter 호출,
해당 유저가 lock을 반납할 때 decrementCounter 을 호출 함으로 써
메소드가 종료됨과 동시에 isUnused 메소드를 활용하여 해당 Lock의 참조카운트가 0인지 아닌지 확인.
참조카운트가 0이라면 해당 유저의 GlobalConcurrentControlMap에서 관리되는 </ Long, UserLock> 은 remove 처리하여 더이상 사용되지 않는 Lock은 삭제 처리

lock 시도
```java
/**  
 * lock을 획득하기 위한 작업을 static 메소드로 선언, 사용할 lock과 최대 대기사간을 인수로 받는다.  
 * 사용하기 전 참조포인트를 + 해준다  
 * @param userLock  
 * @param second  
 */  
public static void tryLock(UserLock userLock, long second){  
    userLock.incrementCounter();  
    ReentrantLock lock = userLock.getLock();  
    try {  
        if(!lock.tryLock(second, TimeUnit.SECONDS)) throw new InterruptedException();  
    } catch (InterruptedException e) {  
        throw new RuntimeException("연결시간이 초과되었습니다.");  
    }  
}

```

lock 해제
```java
/**  
 * lock을 반납하기 위한 작업을 static 메소드로 선언, 반납할 lock을 인수로 받는다.  
 * 반납한 후 참조포인트를 - 해준다  
 * @param userLock  
 */  
public static void unLock(UserLock userLock){  
    ReentrantLock lock = userLock.getLock();  
    userLock.decrementCounter();  
    lock.unlock();  
}
```

사용하지 않는 Lock 에 대해선 remove 처리
```java
/**  
 * 사용하지 않는 Lock에 대해서 remove 처리  
 * @param id  
 */  
public void removeIfUnused(Long id) {  
    UserLock userLock = globalMap.get(id);  
    if(userLock.isUnused()){  
        globalMap.remove(id);  
    }  
}
```

### 테스트코드
```java
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

        // A유저에 대한 요청이 모두 끝난 후 globalMap에 A의 id 값이 남아있는지 검증
        assertNull(controlMap.getUserLock(id));
    }

```
