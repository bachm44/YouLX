package com.youlx.api.rest.user;

import com.youlx.domain.user.UserEdit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Getter
class UserEditDto {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String email;
    @NotNull
    @Pattern(regexp = "[+]{1}[0-9]{11,14}")
    private String phone;

    UserEdit toDomain() {
        return new UserEdit(firstName, lastName, email, phone);
    }
}
