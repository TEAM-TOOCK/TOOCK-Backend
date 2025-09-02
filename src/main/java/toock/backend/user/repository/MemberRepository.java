package toock.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toock.backend.user.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}