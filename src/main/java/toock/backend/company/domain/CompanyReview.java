package toock.backend.company.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(length = 50)
    private String field;

    @Column(length = 50)
    private String level;

    @Column
    private OffsetDateTime createdAt;

    @Column
    private LocalDate interviewedAt;

    @Column(length = 255)
    private String interviewFormat;

    @Column(length = 50)
    private String difficulty;

    @Lob
    @Column
    private String summary;

    @Builder
    public CompanyReview(Company company,
                        String field,
                        String level,
                        OffsetDateTime createdAt,
                        LocalDate interviewedAt,
                        String interviewFormat,
                        String difficulty,
                        String summary) {
        this.company = company;
        this.field = field;
        this.level = level;
        this.createdAt = createdAt;
        this.interviewedAt = interviewedAt;
        this.interviewFormat = interviewFormat;
        this.difficulty = difficulty;
        this.summary = summary;
    }
}


