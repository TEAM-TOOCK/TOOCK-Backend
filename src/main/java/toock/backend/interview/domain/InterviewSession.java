package toock.backend.interview.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import toock.backend.company.domain.Company;
import toock.backend.member.domain.Member;

import java.time.OffsetDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InterviewFieldCategory field;

    @Column(length = 50)
    private String status;

    @Column
    private OffsetDateTime startedAt;

    @Column
    private OffsetDateTime completedAt;

    @Builder
    public InterviewSession(Member member,
                           Company company,
                            InterviewFieldCategory field,
                           String status,
                           OffsetDateTime startedAt,
                           OffsetDateTime completedAt) {
        this.member = member;
        this.company = company;
        this.field = field;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    // 면접 종료 시 호출될 메소드
    public void complete() {
        this.status = "COMPLETED";
        this.completedAt = OffsetDateTime.now();
    }
}


