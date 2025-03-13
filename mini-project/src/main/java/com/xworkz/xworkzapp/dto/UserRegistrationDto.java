package com.xworkz.xworkzapp.dto;

import lombok.*;

import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor

public class UserRegistrationDto {
    private Integer id;
    @Null(message = "The user name must not be null")
    @Size(min=3, max=10, message = "the name size must be 3 and less then 10")
    private String name;
    private Long phoneNumber;
    private String emailId;
    private String password;
    private String conformPassword;
    private String location;
    private String gender;
    private Integer age;
    private String date;
    private Integer failedAttempts = -1;
}
