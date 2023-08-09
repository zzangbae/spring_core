package hello.core.member;

public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository = new MemoryMemberRepository();

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
}
