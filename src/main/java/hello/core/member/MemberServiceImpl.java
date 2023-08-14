package hello.core.member;

public class MemberServiceImpl implements MemberService{

    // private final MemberRepository memberRepository = new MemoryMemberRepository();
    private final MemberRepository memberRepository;    // 인터페이스(추상화)에만 의존하도록함

    // 생성자를 만들었다. -> 이제 MemberServiceImpl은 MemberRepository(인터페이스)에만 의존
    // 해당 클래스가 생성될 때, AppConfig에서 구현체를 집어넣어주게 된다.
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // Member객체를 저장소에 저장한다.
    @Override
    public void join(Member member) {
        memberRepository.save(member);
    }

    // 저장소 구현체로부터 값을 받아온다.
    @Override
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId);
    }

    // 테스트 용도
    public MemberRepository getMemberRepository() {
        return memberRepository;
    }
}
