## Spring Core

* 김영한t - 스프링핵심원리 - 기본편 강의를 듣고 공부한 것 정리

* 코드를 기반으로 리뷰를 작성한 것이고, 코드가 강의 진행에 따라서 업데이트 되기 때문에, 이 글을 보시는 독자분은 코드와 커밋메시지 기록을 보시면서 참고해주시길 바랍니다.

* commit convention

  ```
  단원명 : 핵심개념
  - 추가내용
  
  예시)
  스프링 핵심 원리 이해1 - 예제 만들기 : 구현과 역할을 분리하여 자바로 구현하기(스프링없이)
  ```

### 객체 지향 설계와 스프링



### 스프링 핵심 원리 이해1 - 예제 만들기

* 소프트웨어 개발 주기에 맞춰서 짧게 한 사이클을 진행함(예제)

  * 소프트웨어 개발 주기 : **요구사항 분석 -> 설계 -> 개발 -> 테스트**

* 설계과정에서 참고하는 자료

  * **도메인 협력 관계**
    * 기획자도 이해할 수 있는 내용의 자료이다.
    * 클라이언트 -> 회원서비스(회원가입, 회원조회) -> 회원저장소(메모리저장소, DB저장소, 외부 시스템)
  * **클래스 다이어그램**
    * 개발자용
    * 클래스와 인터페이스가 모두 나오는 다이어그램
    * **정적**인 다이어그램으로, 실제 서버 실행X 상황
  * **객체 다이어그램**
    * 실제 서버가 진행될 때, 어떤 객체가 참조되는 지를 확인하는 다이어그램
    * **동적**인 다이어그램으로, 소스코드가 실행되며 어떤 객체가 선택되었는지를 확인할 수 있음

* 회원, 주문 도메인의 요구사항에 맞게 개발을 진행함

* 테스트

  * app을 만들고, 메인 메서드를 만들어서 애플리케이션 로직을 통해서 테스트를 진행할 수는 있음

  * 하지만 sout을 활용하고, 눈으로 직접 값이 맞는지 확인해야 하는 등의 불폄함이 있음

    * 매우 큰 객체나 복잡한 계산은 직접 눈으로 확인하기 힘들 수 있다.

  * 따라서 Junit을 활용해야한다.

    * test코드는 src파일 밑에 test로 주어지게 됨(해당 파일은 실제 실행시 실행안됨)
    * 아래는 예시 코드 `@Test annotation`을 활용하고, Assertions라이브러리를 활용하여 객체나 값이 동일한지를 확인할 수 있다.

    ```java
    @Test
        void createOrder() {
            Long memberId = 1L;
            Member member = new Member(memberId, "memberA", Grade.VIP);
            memberService.join(member);
    
            Order order = orderService.createOrder(memberId, "itemA", 10000);
            Assertions.assertThat(order.getDiscountPrice()).isEqualTo(1000);
        }
    ```

    

### 스프링 핵심 원리 이해2 - 객체 지향 원리 적용

>  위의 예제에서 나타난 문제를 확인해보고, 스프링이 등장하게 된 배경을 파악하는 것이 목표

=> 우선, 결론은 AppConfig라는 '설정 클래스'를 만들어서 각 클래스에서 활용되는 의존 객체를 주입해줌을 통해서 DIP, SRP, OCP를 구현하였다.

=> 그리고, 스프링으로 전환해보았다.

---

* 자바 코드를 통해서 예제를 만들었던 것을 회귀해보면서 문제를 찾음

* 과연 DIP, SRP, OCP를 지키면서 코드를 짰을까? -> No!

  => 예제의 정액 할인 정책을 정률 할인 정책으로 바꾸는 과정을 지켜보자

  ```java
  // hello.core.order.OderServiceImpl
  
  // private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
  private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
  ```

  * 할인 정책을 바꾸기 위해서 OrderServiceImpl(클라이언트 코드)를 변경해야한다.
  * 즉, 현재는 클라이언트 코드가 인터페이스뿐만 아니라, 구현 객체에도 의존하고 있는 매우 안좋은 상태이다.
  * 그렇다면 어떻게 해야할까? -> 우선, DIP원칙 "추상화에 의존하라, 구체화에 의존하지 마라"에 맞춰서 아래와 같이 코드를 바꿔보겠다.

  ```java
  private DiscountPolicy discountPolicy;
  ```

  => 실행할 경우, NPE(널 포인트 익셉션)이 난다

  * 이는 당연하다. 인터페이스 참조 객체를 만들었지만, 참조 객체에 저장된 정보가 없으니 NULL인 상황이다.
  * 그렇다면 어떻게 할까? => 이때, 천재적인 사람이 외부에서 주입시켜줄 수 있는 방법을 고안하게 된다.

  1. 의존하는 객체의 생성자 만들기

     ```java
     private MemberRepository memberRepository;
     private DiscountPolicy discountPolicy;
     
     public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
             this.memberRepository = memberRepository;
             this.discountPolicy = discountPolicy;
         }
     ```

  2. AppConfig 클래스를 만든다. 이는 위의 객체에 구현객체를 집어넣어줄 설정 클래스이다.

     ```java
     // 인터페이스에 쓰일 구현객체를 지정하는 '공연 기획자'의 역할을 하는 클래스
     public class AppConfig {
         public MemberService memberService() {
             return new MemberServiceImpl(new MemoryMemberRepository());
         }
     
         public OrderService orderService() {
             return new OrderServiceImpl(
                     new MemoryMemberRepository(),
                     new FixDiscountPolicy());
         }
     }
     ```

  3. 그리고 아래와 같이 실행하자

     ```java
     public class OrderApp {
         public static void main(String[] args) {
             AppConfig appConfig = new AppConfig();
             MemberService memberService = appConfig.memberService();
             OrderService orderService = appConfig.orderService();
     
     
             Long memberId = 1L;
             Member member = new Member(1L, "memberA", Grade.VIP);
             memberService.join(member);
     
             Order order = orderService.createOrder(memberId, "itemA", 20000);
             System.out.println("order = " + order);
         }
     }
     ```

  * 자, 3번의 코드를 보면
    - AppConfig 객체가 만들어진다.
    - MemberService객체가 만들어질 때, AppConfig의 인스턴스로부터 만들어지며, 이 때, MemoryMemberRepository() 인스턴스가 생성된다.
    - 이 MemoryMemberRepository의 인스턴스는 다시 MemberService를 만들 때, MemberRepository의 참조변수 값에 들어가게 되어, 결국 앞서 보았던 NPE 문제가 해결되는 것이다.

* 이렇게 하면 무슨 장점이 있을까?

  1. 우선 배우는 연기에만, 기획자는 배우를 뽑는데에만 집중하면 되듯이 코드를 구현할 수 있다. -> **SRP**
     * AppConfig 클래스에서는 생성 및 주입의 역할만
     * 나머지 클래스에서는 실행의 역할만 하면된다
  2. **DIP**를 온전히 지킬 수 있다.
     * 일전과 다르게 할인정책이나, 저장방식을 변경하는 것이 클라이언트 코드를 변경하지 않아도 된다.
     * 필요한 클래스를 생성하고, 이를 AppConfig를 통해서 주입만 시켜주면 되는 것이다.
  3. **OCP**를 온전히 지킬 수 있다.
     * 클라이언트 코드 입장에서는 변경하지 않고도, 충분히 다른 객체에 의존할 수 있는 상황이 된다.
     * 즉, "수정에는 닫혀있고, 확장에는 열려있다"는 원칙을 지킬 수 있는 것이다.

  => 결론적으로, 객체지향법칙을 지킬 수 있는 코드를 짤 수 있다는 것이다.

* AppConfig를 리팩토링 -> 구현과 역할을 좀 더 잘볼 수 있도록 해보자

  ```java
  // 인터페이스에 쓰일 구현객체를 지정하는 '공연 기획자'의 역할을 하는 클래스
  public class AppConfig {
      public MemberService memberService() {
          return new MemberServiceImpl(memberRepository());
      }
  
      public OrderService orderService() {
          return new OrderServiceImpl(
                  memberRepository(),
                  discountPolicy());
      }
  
      // -> "저장소는 메모리 저장소 구현체를 적용중이구나!" : 역할과 구현을 확인할 수 있음
      public MemberRepository memberRepository() {
          return new MemoryMemberRepository();
      }
  
      // -> "할인정책은 정률 할인 구현체가 적용중이구나!" : 역할과 구현을 확인할 수 있음
      // 만약 다른 할인 정책을 적용하고자 한다면, 다른 할인정책 구현체를 만들고, 이 부분만 변경해주면 된다.
      public DiscountPolicy discountPolicy() {
          // (정액 할인 정책) return new FixDiscountPolicy();
          return new RateDiscountPolicy();    // 정률 할인 정책 적용***
      }
  }
  ```

  * MemberRepository에는 무엇을 썼는지

  * DiscountPolicy에는 어떤 정책을 적용했는지 파악하기 더 수월해진다.

  * 간단한 예제라 그렇지만, 실제로 엄청나게 많은 의존성 주입을 하는 상황이 있다고 생각해보자.

    -> 위와 같은 리펙토링은 역할과 구현클래스를 더 잘볼 수 있도록 한다.

  * 더해서, 할인정책의 변경은 이제, *** <- 이 표시가 있는 코드 부분만 고치면 되는 것이다.

    -> 클라이언트 코드를 다 찾아가서 바꿀 필요가 없다는 것이다!!!

**프레임워크 vs 라이브러리**

* 의존성 역전을 기준으로 나뉘며, 프레임워크의 경우, 내가 작성한 코드가 의존성을 제어하는 것이 아닌, 설정이 코드를 제어하게 된다.
  * ex) JUnit, VueJs
* 라이브러리는 내가 작성한 코드가 제어 흐름을 담당한다.
  * ex) React

**클래스 다이어그램 vs 객체 다이어그램**

* 클래스 다이어그램
  * 정적인 다이어그램
  * IntelliJ의 다이어그램을 통해서 확인할 수 있다(추상화 의존)
  * 추상화 의존이기 때문에 어떤 인터페이스에 의존하는지는 확인이 가능하나, 실제로 어떤 구현체에 의존하는지는 확인이 불가능하다.(IoC, DIP)
* 객체 다이어그램
  * 동적인 다이어그램
  * 애플리케이션 실행 시점(런 타임)에 외부에서 어떤 구현체를 생성하고 클라이언트에 전달해서 어떤 의존관계를 가지는지 보이는 다이어그램
  * **의존관계주입을 사용하면 정적인 클래스 의존관계를 변경하지 않고(DIP, IoC), 동적인 객체 인스턴스 의존관계를 쉽게 변경할 수 있다.**

**IoC 컨테이너, DI 컨테이너**

* 우리는 위에서 AppConfig클래스를 보았다.

* 객체를 생성하고 관리하며 의존관계를 연결해주는 것을 의미한다.

* DI(Dependency Injection)에 초점을 맞춰 DI 컨테이너라고 부르는 것

  -> IoC의 경우 프로그래밍의 다른 분야에서도 쓰이므로 더 명확한 표현이기도 하다.

* 어셈블러라고도 불림 -> 구현체를 가져와서 조립하기 때문에
* 오브젝트 팩토리 라고도 불림 -> 인스턴스를 생성해서 주입하는 곳이므로



**스프링 적용**

* 아래 코드는 AppConfig에 스프링 기반으로 변경한 것이다.

  ```java
  // IoC컨테이너 혹은 DI 컨테이너
  @Configuration	// 해당 클래스에 설정을 구성한다는 뜻
  public class AppConfig {
  
      @Bean	// 각 메서드에 붙여줌 -> 스프링 컨테이너에 스프링 빈으로 등록됨
      public MemberService memberService() {
          return new MemberServiceImpl(memberRepository());
      }
  
      @Bean
      public OrderService orderService() {
          return new OrderServiceImpl(
                  memberRepository(),
                  discountPolicy());
      }
  
      @Bean
      public MemberRepository memberRepository() {
          return new MemoryMemberRepository();
      }
  
      @Bean
      public DiscountPolicy discountPolicy() {
          return new RateDiscountPolicy();
      }
  }
  ```

* 이제 스프링 컨테이너를 MemberApp, OrderApp에 적용

  ```java
  public class MemberApp {
      public static void main(String[] args) {
  			
          ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
          MemberService memberService = applicationContext.getBean("memberService", MemberService.class);
  
          Member member = new Member(1L, "memberA", Grade.VIP);
          memberService.join(member);
  
          Member findMember = memberService.findMember(1L);
          System.out.println("new member = " + member.getName());
          System.out.println("find member = " + findMember.getName());
  
      }
  }
  ```

  * `ApplicationContext` : 스프링 컨테이너

  * 기존에는 개발자가 직접 AppConfig를 만들고 구성하여 DI를 했지만, 이제 스프링 컨테이너를 통해서 함

  * `@Configuration` : 해당 클래스를 설정 정보로 사용

  * `@Bean` : 스프링 컨테이너에 해당 어노테이션이 붙은 메서드를 모두 호출하여 반환한 객체를 스프링 컨테이너에 등록

    -> 이렇게 스프링 컨테이너에 등록된 객체를 **스프링 빈**이라고 함

    -> 스프링 빈은 `@Bean`이 붙은 메서드 명을 스프링 빈의 이름으로 사용

  * 이제 AppConfig에서 조회하는 것이 아닌, ApplicationContext의 인스턴스에 `.getBean()`메서드를 통해서 접근

    ```java
    MemberService memberService = applicationContext.getBean("memberService", MemberService.class);	// 메서드(빈 이름, 반환 클래스)
    ```

  => 복잡해진 것 같다. 그래도 왜 쓰는 걸까? : 이것을 앞으로 공부해볼 예정

>  **정리**
>
> 1. 자바를 통해서 다형성에 맞춰서 애플리케이션을 만들어보았다. -> DIP, OCP 부족으로 객체지향의 장점을 이용할 수 없음
>
> 2. AppConfig의 도입으로 구성 영역과 사용 영역을 나눴다. -> DIP, OCP, SRP 만족
>
>    -> 이는 스프링이 나오게 된 계기. DI 컨테이너이자, 앞으로 스프링이 적용되어 스프링 컨테이너로 발전
>
> 3. 모든 프로젝트에 적용 가능한 프레임워크의 도입. Spring
>
> 4. 앞으로는 Spring 컨테이너를 통해서 얻게 될 장점을 알아볼 것이다.



### 스프링 컨테이너와 스프링 빈



### 싱글톤 컨테이너



### 컴포넌트 스캔



### 의존관계 자동 주입



### 빈 생명주기 콜백



### 빈 스코프



