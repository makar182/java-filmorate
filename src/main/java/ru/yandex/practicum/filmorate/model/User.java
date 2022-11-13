package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.enums.FriendshipStatus;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;

@Data
public class User {
    private Long id;
    @Email
    private String email;
    @NotNull
    @NotBlank
    private String login;
    private String name;
    @NotNull
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();
    private Map<Long, FriendshipStatus> friendsNew = new HashMap<>();

    public void addFriend(Long friendId) {
        friends.add(friendId);
    }

    public void deleteFriend(Long friendId) {
        friends.remove(friendId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
