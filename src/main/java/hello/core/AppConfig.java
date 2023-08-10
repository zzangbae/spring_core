package hello.core;

import hello.core.discount.DiscountPolicy;
import hello.core.discount.FixDiscountPolicy;
import hello.core.Order.OrderService;
import hello.core.Order.OrderServiceImpl;
import hello.core.discount.RateDiscountPolicy;
import hello.core.member.MemberRepository;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import hello.core.member.MemoryMemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 인터페이스에 쓰일 구현객체를 지정하는 '공연 기획자'의 역할을 하는 클래스 -> IoC컨테이너 혹은 DI 컨테이너
@Configuration
public class AppConfig {

    @Bean
    public MemberService memberService() {
        // return new MemberServiceImpl(new MemoryMemberRepository());
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(
                // new MemoryMemberRepository(),
                memberRepository(),
                // new FixDiscountPolicy());
                discountPolicy());
    }

    // -> "저장소는 메모리 저장소 구현체를 적용중이구나!" : 역할과 구현을 확인할 수 있음
    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    // -> "할인정책은 정률 할인 구현체가 적용중이구나!" : 역할과 구현을 확인할 수 있음
    // 만약 다른 할인 정책을 적용하고자 한다면, 다른 할인정책 구현체를 만들고, 이 부분만 변경해주면 된다.
    @Bean
    public DiscountPolicy discountPolicy() {
        // (정액 할인 정책) return new FixDiscountPolicy();
        return new RateDiscountPolicy();    // 정률 할인 정책 적용
    }
}

/**
 * 역할과 구현 클래스가 한눈에 들어온다. 애플리케이션이 어떻게 구성되는지 파악할 수 있음
 */
