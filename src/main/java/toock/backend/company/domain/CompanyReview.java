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

    // CSV: 'Job Field'
    @Column(length = 50)
    private String field;

    // CSV: 'Job Level'
    @Column(length = 50)
    private String level;

    // CSV: 'Date Posted'
    @Column
    private OffsetDateTime createdAt;

    // CSV: 'Interview Date'
    @Column
    private LocalDate interviewedAt;

    // CSV: 'Hiring Method'
    @Column(length = 255)
    private String interviewFormat;

    // CSV: 'Interview Difficulty'
    @Column(length = 50)
    private String difficulty;

    // CSV: 'Summary/Review'
    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;

    // CSV: 'Interview Path'
    @Column(length = 255)
    private String interviewPath;

    // CSV: 'Interview Questions'
    @Lob
    @Column(columnDefinition = "TEXT")
    private String interviewQuestions;

    // CSV: 'Interview Answer/Feeling'
    @Lob
    @Column(columnDefinition = "TEXT")
    private String interviewAnswer;

    // CSV: 'Announcement Period'
    @Column(length = 50)
    private String announcementPeriod;

    // CSV: 'Interview Result'
    @Column(length = 50)
    private String interviewResult;

    // CSV: 'Interview Experience'
    @Column(columnDefinition = "TEXT")
    private String interviewExperience;


    @Builder
    public CompanyReview(Company company,
                         String field,
                         String level,
                         OffsetDateTime createdAt,
                         LocalDate interviewedAt,
                         String interviewFormat,
                         String difficulty,
                         String summary,
                         String interviewPath,
                         String interviewQuestions,
                         String interviewAnswer,
                         String announcementPeriod,
                         String interviewResult,
                         String interviewExperience) {
        this.company = company;
        this.field = field;
        this.level = level;
        this.createdAt = createdAt;
        this.interviewedAt = interviewedAt;
        this.interviewFormat = interviewFormat;
        this.difficulty = difficulty;
        this.summary = summary;
        this.interviewPath = interviewPath;
        this.interviewQuestions = interviewQuestions;
        this.interviewAnswer = interviewAnswer;
        this.announcementPeriod = announcementPeriod;
        this.interviewResult = interviewResult;
        this.interviewExperience = interviewExperience;
    }
}