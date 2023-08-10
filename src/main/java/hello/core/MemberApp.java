package hello.core;

import hello.core.member.Grade;
import hello.core.member.Member;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;

/**
 * 나쁜 예시
 * 일반적으로 애플리케이션 내의 로직으로 테스트를 하는 것은 좋지 않다.
 * -> sout을 직접 찍으면서 하는 불편함
 * 테스트의 견고함 또한 부족할 수있다.
 */
public class MemberApp {
    public static void main(String[] args) {
        // 테스트로 쓰인 코드
//        MemberService memberService = new MemberServiceImpl();
//        Member member = new Member(1L, "memberA", Grade.VIP);
//        memberService.join(member);
//
//        Member findMember = memberService.findMember(1L);
//        System.out.println("new member = " + member.getName());
//        System.out.println("find member = " + findMember.getName());

        // 위에서 멤버서비스를 직접 만들었다면, 이제 AppConfig를 통해서 멤버서비스를 만들어서 진행
        AppConfig appConfig = new AppConfig();
        MemberService memberService = appConfig.memberService();
        Member member = new Member(1L, "memberA", Grade.VIP);
        memberService.join(member);

        Member findMember = memberService.findMember(1L);
        System.out.println("new member = " + member.getName());
        System.out.println("find member = " + findMember.getName());
    }
}
