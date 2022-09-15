package com.serwisspolecznosciowy.Application.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(unique = true)
    @NotNull(message = "Username can not be null!")
    @Size(min = 3, max = 45)
    private String username;

    @NotNull(message = "Password can not be null!")
    @Size(min = 6, max = 64)
    private String password;

    @NonNull
    private LocalDateTime created;

    private LocalDateTime updated;

    @NonNull
    private String role;

    @NonNull
    private Boolean enabled;

    private String profilePicture;

}
