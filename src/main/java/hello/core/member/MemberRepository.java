package hello.core.member;

// 인터페이스 -> 추상메서드
public interface MemberRepository {
    void save(Member member);
    Member findById(Long memberId);
}
