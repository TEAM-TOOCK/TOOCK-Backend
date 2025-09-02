package toock.backend.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

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

    // 소셜 로그인 전용: 패스워드 제거

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Field field;

    @Column(length = 255)
    private String googleId;

    @Builder
    public Member(String email, String name, String username, Field field, String googleId) {
        this.email = email;
        this.name = name;
        this.username = username;
        this.field = (field != null) ? field : Field.DEFAULT;
        this.googleId = googleId;
        this.status = Status.ACTIVATED;
    }
}

