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

> 앞선 단원에서 AppConfig의 등장 배경과 스프링을 통해서 AppConfig를 기반으로 스프링 컨테이너를 생성해보았다.
>
> 이제, 스프링 컨테이너와 그 안에 의존관계를 만들어주기 위해서 만드는 스프링 빈에 대해서 정리해보겠다.

**스프링 컨테이너**

* 아래의 코드를 통해서 우리는 `AppConfig`라는 설정 클래스를 기반으로 스프링 컨테이너를 생성했다.

  ```java
  // 스프링 컨테이너 생성
  ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
  ```

* ApplicationContext

  * 스프링 컨테이너이다.
  * ApplicationContext는 인터페이스이다. -> 위에서 했던 것처럼 `@Configuration` 애노테이션 기반으로 스프링 컨테이너를 만들거나 XML 문서를 기반으로 만들 수 있다.
  * 스프링 컨테이너 생성의 예시는 애노테이션 기반으로 만든 스프링 컨테이너인데, `AnnotationConfigApplicationContext` 이름을 통해서 알 수 있다. 또한, 이는 `ApplicationContext`의 구현체이다.

**스프링 컨테이너 생성 과정**

1. AppConfig.class를 기반으로(구성정보로 하여) 스프링 컨테이너 생성![스크린샷 2023-08-12 오후 11.35.48](/Users/changbae/Library/Application Support/typora-user-images/스크린샷 2023-08-12 오후 11.35.48.png)
2. `@Bean` 애노테이션을 찾아서, <u>해당 메서드명을 빈 이름으로, 반환받은 객체를 빈 객체로</u>한 빈을 만들어 빈 저장소에 등록한다.(위의 그림의 빈 칸을 채운다 생각하자)
   * 이 때, 빈 이름은 `@Bean(name="anotherName")` 을 통해서 다른 이름으로 만들 수 있다.
   * 주의 - <u>빈 이름은 항상 다른 이름을 부여해야한다!</u>

3. 빈 의존 관계 설정

   * 설정 정보를 기반으로 의존관계를 주입한다.(DI; Dependency Injection)
   * 동적인 객체의존 관계를 스프링이 연결해주는 것이라 생각하자.(객체의 reference연결)
   * 자바 코드를 호출하는 것 같지만, 의존 관계연결은 그와 차이가 있다. -> 싱글톤 컨테이너에 정리할 예정

   => 실제로는 빈 생성하고 의존관계 주입하는 단계가 나누어져 있지만, 실제로는 호출하면서 의존관계 주입도 한 번에 처리

> 스프링 컨테이너를 생성하고, 스프링 빈을 저장하는 과정까지 정리했다.
>
> 그러면 이제, 저장한 스프링 빈을 확인하는 방법에 대해서 정리해보겠다.

**스프링 빈 조회**

* `getBean()`

  * `ac.getBean(빈 이름, 타입)` : 빈이름(key)으로부터 빈을 찾아 매개변수의 인자에 지정된 타입으로 빈 객체 반환
  * `ac.getBean(타입)` : 해당 타입의 빈을 찾아서 반환
    * 동일한 타입이 둘 이상일 때, 에러 발생 -> 빈 이름으로 지정해서 찾는다.
    * 에러 : `NoSuchBeanDefinitionException: No bean named 'xxxxx' available`
  * -> 조회 대상 타입 없을 시, 예외 발생

* `getBeansOfType()`

  * `ac.getBeansOfType(타입)` : 매개변수의 인자로 들어온 타입의 모든 빈 객체를 반환

* 상속관계에서의 조회

  * 부모 타입으로 조회시, 자식이 둘 이상 있으면, 중복 오류 -> 빈 이름으로 지정해서 찾는다
  * 특정하위 타입으로 지정해서 찾을 수도 있지만, 비추 -> "구체화에 의존하지 말라"
  * 부모 타입으로 모두 조회하게 되면(getBeansOfType()), 자식 객체가 모두 나옴. 특히 Object타입으로 조회 시, 스프링 빈에 등록된 모든 객체가 반환되게 된다. -> Object는 모든 클래스의 상위 클래스

* 예시

  ```java
  // 스프링 컨테이너 생성
  ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
  
  // ac.getBean(빈 이름, 타입)
  MemberService memberService = ac.getBean("memberService", MemberService.class);
  
  // ac.getBean(타입)
  MemberService memberService = ac.getBean(MemberService.class);
  
  // ac.getBeansOfType(타입) : key, value 꼴이기 때문에, Map구조로 받는다.
  Map<String, Object> beansOfType = ac.getBeansOfType(Object.class);
  
  // 아래는 키값을 순회하면서 해당하는 빈 객체를 출력
  for(String key : beansOfType.keySet()) {
      System.out.println("key = " + key + " value = " + beansOfType.get(key));
  }
  ```

* 모든 빈 출력

  ```java
  // 스프링 빈 이름을 모두 가져오기
  String[] beanDefinitionNames = ac.getBeanDefinitionNames();
  // 하나씩 순회
  for (String beanDefinitionName : beanDefinitionNames) {
      Object bean = ac.getBean(beanDefinitionName);
      System.out.println("name=" + beanDefinitionName + " object=" + bean);
  }
  ```

  * `ac.getBeanDefinitionNames()` : 스프링에 등록된 모든 빈 이름을 조회
  * 결과적으로 빈 이름을 조회하고, 하나씩 순회하며 `ac.getBean()`을 통해서 객체를 찾고 출력

**BeanFactory & ApplicationContext**

* BeanFactory는 스프링 컨테이너의 최상위 인터페이스이자, 스프링 빈을 관리하고 조회하는 역할

* `getBean()` 제공

* ApplicationContext는 BeanFactory 기능을 모두 상속 받음. + 부가적인 기능을 추가함

  * ApplicationContext는 BeanFactory 기능을 상속받으면서 MessageSource, EnvironmentCapable, ApplicationEventPublisher, ResourceLoader을 상속받는다.
    * 일 예로, getBeansOfType() 메서드의 경우, ResourceLoader로부터 상속받은 다른 추상 클래스에 있는 메서드로, BeanFactory로 스프링 컨테이너를 생성 시, 테스트되지 않는 예제가 존재한다.
  * 부가기능
    * 메시지 소스를 활용한 국제화 기능
    * 환경 변수
    * 애플리케이션 이벤트
    * 편리한 리소스 조회

* BeanFactory, ApplicationContext 둘 다, 스프링 컨테이너다. 단지, 실제로 ApplicationContext를 주로 사용(부가 기능이 애플리케이션 제작에 거의 필수이기 때문에)

  ![스크린샷 2023-08-13 오전 12.27.13](/Users/changbae/Library/Application Support/typora-user-images/스크린샷 2023-08-13 오전 12.27.13.png)

**다양한 설정 형식 지원 - 자바코드, XML**

* 우리는 자바코드(AppConfig.class)를 통해서 설정을 하였다.

* 옛날 코드의 경우 XML을 통해서 설정정보를 만드는 경우가 많았다.

* `AnnotationCofigApplicationContext`, `GenericXmlApplicationContext`는 ApplicationContext를 상속받은 것이고, 각각 자바코드를 기반으로 설정 정보를 사용하거나 XML문서를 설정 정보로 활용한다.

* XML로 설정정보를 쓸 경우, 컴파일 없이 파일만 변경하면 빈 설정 정보를 변경할 수 있다는 장점이 있으나, 자바가 쓰기 더 편해서인지, 오늘날에는 자바 코드로 빈 설정정보를 설정하는 것이 대세이다.

* 아래는 XML파일을 통해서 설정정보를 만드는 예시이고, XML파일을 보면 AppConfig클래스와 굉장히 유사하다는 것을 확인할 수 있다.

  * xml파일 - 참고로, xml파일같이 자바가 아닌 코드는 resources에 해당 파일을 위치시킨다.

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <beans xmlns="http://www.springframework.org/schema/beans"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
  
      <bean id="memberService" class="hello.core.member.MemberServiceImpl">
          <constructor-arg name="memberRepository" ref="memberRepository"/>
      </bean>
  
      <bean id="memberRepository" class="hello.core.member.MemoryMemberRepository"/>
  
      <bean id="orderService" class="hello.core.Order.OrderServiceImpl">
          <constructor-arg name="memberRepository" ref="memberRepository"/>
          <constructor-arg name="discountPolicy" ref="discountPolicy"/>
      </bean>
  
      <bean id="discountPolicy" class="hello.core.discount.RateDiscountPolicy"/>
  </beans>
  ```

  * xml을 기반으로 한 설정 파일 테스트

  ```java
  class XmlAppContext {
  
      @Test
      void xmlAppContext() {
          // 클래스 파일이 xml
          ApplicationContext ac = new GenericXmlApplicationContext("appConfig.xml");
          MemberService memberService = ac.getBean("memberService", MemberService.class);
          assertThat(memberService).isInstanceOf(MemberService.class);
      }
  }
  ```

![스크린샷 2023-08-13 오전 12.26.40](/Users/changbae/Library/Application Support/typora-user-images/스크린샷 2023-08-13 오전 12.26.40.png)

**BeanDefinition : 스프링 빈 설정 메타 정보**

* BeanDefinition : 설정 메타 정보 - 설정에 관한 여러 정보들을 알려주는 것
  * @Bean, <bean> 하나 당 메타 정보가 생성된다.
* BeanDefinition이라는 추상화 - 스프링이 위와 같이 다양한 설정 형식을 지원하도록 한다.
* 이 것 또한 마찬가지로 역할과 구현을 개념적으로 나눴다.
* 스프링 컨테이너가 오직 BeanDefinition만 알고, 자바코드인지, XML인지 몰라도 된다. 즉, BeanDefinition 인터페이스에 맞게만 코드가 짜여졌다면, 스프링 컨테이너에 설정정보로 추가할 수가 있다.(표준화)
* 스프링 컨테이너는 이 메타정보를 기반으로 스프링 빈을 생성

![스크린샷 2023-08-13 오전 12.31.01](/Users/changbae/Library/Application Support/typora-user-images/스크린샷 2023-08-13 오전 12.31.01.png)

* BeanDefinition에는 다양한 정보들이 있다.

  * BeanClassName, factoryBenName, Scope 등.
  * 이 정보를 바탕으로 스프링 컨테이너가 빈을 생성하는 것

* 아래는 빈 설정 메타 정보를 확인하는 코드다.

  ```java
  // 빈 이름을 받고, String으로 이루어진 배열에 저장
  String[] beanDefinitionNames = ac.getBeanDefinitionNames();
  // 빈 이름을 순회하며 설정 정보를 통해서 만든 빈을 경우, 빈 정보를 출력하도록 함
  for(String beanDefinitionName : beanDefinitionNames) {
      BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);
  
      if(beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION) {
          System.out.println("beanDefinitionName = " + beanDefinitionName + " beanDefinition = " + beanDefinition);
      }
  }
  /**
  아래는 결과
  beanDefinitionName = memberService beanDefinition = Generic bean: class [hello.core.member.MemberServiceImpl]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null; defined in class path resource [appConfig.xml]
  
  beanDefinitionName = memberRepository beanDefinition = Generic bean: class [hello.core.member.MemoryMemberRepository]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null; defined in class path resource [appConfig.xml]
  
  beanDefinitionName = orderService beanDefinition = Generic bean: class [hello.core.Order.OrderServiceImpl]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null; defined in class path resource [appConfig.xml]
  
  beanDefinitionName = discountPolicy beanDefinition = Generic bean: class [hello.core.discount.RateDiscountPolicy]; scope=; abstract=false; lazyInit=false; autowireMode=0; dependencyCheck=0; autowireCandidate=true; primary=false; factoryBeanName=null; factoryMethodName=null; initMethodName=null; destroyMethodName=null; defined in class path resource [appConfig.xml]
  */
  ```

> 스프링이 XML, 자바 코드 등의 다양한 형태의 설정 정보를 BeanDefinition으로 추상화해서 사용하는 것

> 정리
>
> 1. AppConfig라는 설정정보를 바탕으로 스프링 컨테이너를 생성했다. -> 이 과정에 대한 배경을 파악
>
>    * @Configuration 애노테이션을 통해서 해당 클래스를 바탕으로 스프링 컨테이너로 만들고자 했음
>    * @Bean 애노테이션 바탕으로 해당 메서드가 빈 정보임을 알림
>    * ApplicationContext 인터페이스, new AnnotationConfigApplicationContext(AppConfig.class); 를 통해서 스프링 컨테이너 생성
>
> 2. 스프링 빈이 생성되는 방법을 파악하고 조회하는 방법을 공부함
>
>    * 실제로는 조회는 잘하지 않는다고 한다.
>
> 3. BeanFactory와 ApplicationContext가 스프링 컨테이너이며, 어떤 차이점이 있는지 파악하였다.
>
>    * BeanFactory는 빈 관리 기능을 가지고 있음
>
>    * ApplicationContext는 BeanFactory뿐만 아니라 다른 인터페이스에서 추가적으로 상속을 받아 국제화, 환경변수, 애플리케이션 이벤트, 리소스 조회 등의 추가적인 기능을 가짐
>
> 4. 다양한 설정 파일(자바 코드, XML)을 통해서 스프링 컨테이너에 설정 정보를 생성할 수 있음을 파악하였다.
>
> 5. BeanDefinition을 통해서 스프링 컨테이너가 빈 메타 정보를 바탕으로 스프링 빈을 생성함을 확인할 수 있었다.



### 싱글톤 컨테이너



### 컴포넌트 스캔



### 의존관계 자동 주입



### 빈 생명주기 콜백



### 빈 스코프



