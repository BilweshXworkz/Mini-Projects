package com.xworkz.xworkzapp.service;

import com.xworkz.xworkzapp.dto.UserRegistrationDto;
import com.xworkz.xworkzapp.entity.UserRegistrationEntity;

public interface UserRegistrationService {
    String validAndSave(UserRegistrationDto dto);
    UserRegistrationEntity authenticateUser(String emailId, String password);
    public String validatePassword(String password);
    UserRegistrationDto fetchByEmail(String emailId);
    Boolean  updateUser(UserRegistrationDto dto);
    public String updatePassword(String emailId, String newPassword);
    String resetPassword(String email, String currentPassword, String newPassword);
    public String updatePassword(String emailId,String currentPassword, String newPassword);
    void deleteByEmailId(String emailId);
}
