package hello.core.Order;

import hello.core.discount.DiscountPolicy;
import hello.core.member.Member;
import hello.core.member.MemberRepository;

public class OrderServiceImpl implements OrderService {
    // 1. private final MemberRepository memberRepository = new MemoryMemberRepository();
    // 1. private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    // 2. private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
    // ->구현객체 변경 -> but 클라이언트 코드가 변경되었다. 또한 아직 구현체에 의존하는 상태. 어떻게 해야 추상화에만 의존할 수 있을까
    private MemberRepository memberRepository;
    private DiscountPolicy discountPolicy;  // -> 이 자체만으로는 NPE(널 포인트 익셉션)이 난다. 누군가 구현객체를 '주입'해주어야 한다.

    // 생성자를 통해서 의존성을 주입받는다. from AppConfig
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);

        return new Order(memberId, itemName, itemPrice, discountPrice);
    }

    // 테스트 용도
    public MemberRepository getMemberRepository() {
        return memberRepository;
    }
}
