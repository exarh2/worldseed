package online.worldseed.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.worldseed.model.enums.RoleType;
import org.hibernate.annotations.ColumnTransformer;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "security_user")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class SecurityUserEntity {

    /** Уникальный идентификатор пользователя. */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    /** Логин пользователя (уникальный). */
    @Column(name = "login", nullable = false, length = 50)
    private String login;

    /** Хэш пароля пользователя. */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** Адрес электронной почты пользователя. */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /** Роль пользователя в системе. */
    @Enumerated(STRING)
    @Column(name = "role", nullable = false)
    @ColumnTransformer(
            read = "role::text",
            write = "?::role_type"
    )
    private RoleType role;

    /** Дата и время создания записи. */
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    /** Дата и время последнего изменения записи. */
    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private Instant lastModifiedDate;
}
