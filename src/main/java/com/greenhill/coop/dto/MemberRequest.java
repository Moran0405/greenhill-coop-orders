package com.greenhill.coop.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/** Payload for adding a new member (coordinator only). */
public class MemberRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    private String phone;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
