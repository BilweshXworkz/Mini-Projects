package com.xworkz.xworkzapp.service;

import com.xworkz.xworkzapp.dto.UserRegistrationDto;
import com.xworkz.xworkzapp.entity.UserRegistrationEntity;

public interface UserRegistrationService {
    String validAndSave(UserRegistrationDto dto);
    UserRegistrationEntity authenticateUser(String emailId, String password);
    public String validatePassword(String password);
    UserRegistrationDto fetchByEmail(String emailId);
    Boolean  updateUser(UserRegistrationDto dto);
    String updatePassword(String email, String newPassword);
}
