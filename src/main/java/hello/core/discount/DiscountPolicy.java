package hello.core.discount;

import hello.core.member.Member;

public interface DiscountPolicy {
    /**
     * @return 할인 대상 금액
     * 회원과 물건의 가격이 들어갔을 때, 할인 되는 돈 나온다.
     */

    int discount(Member member, int price);
}
