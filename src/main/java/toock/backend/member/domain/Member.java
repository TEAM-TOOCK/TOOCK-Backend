package toock.backend.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import toock.backend.interview.domain.InterviewFieldCategory;

@Slf4j
@Getter
@Entity
@Where(clause = "status = 'ACTIVATED'")
@SQLDelete(sql = "UPDATE member SET status = 'DELETED' WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Field field;

    @Enumerated(EnumType.STRING)
    @Column
    private InterviewFieldCategory interviewFieldCategory;


    @Column(length = 255)
    private String googleId;

    @Builder
    public Member(String email, String name, String username, Field field, String googleId, InterviewFieldCategory interviewFieldCategory) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.field = (field != null) ? field : Field.DEFAULT;
        this.googleId = googleId;
        this.status = Status.ACTIVATED;
        this.interviewFieldCategory = interviewFieldCategory;
    }

    public void updateProfile(Field field, InterviewFieldCategory interviewFieldCategory) {
        this.field = field;
        this.interviewFieldCategory = interviewFieldCategory;
    }
}

