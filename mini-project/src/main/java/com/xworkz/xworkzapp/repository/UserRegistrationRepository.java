package com.xworkz.xworkzapp.repository;

import com.xworkz.xworkzapp.entity.UserRegistrationEntity;
import org.jboss.logging.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface UserRegistrationRepository {
    Boolean save(UserRegistrationEntity entity);
    UserRegistrationEntity checkAuthenticateUser(String emailId, String password);
    UserRegistrationEntity fetchEmail(String emailId);
    public Boolean saveUpdate(UserRegistrationEntity entity, boolean updatePassword);
    Optional<UserRegistrationEntity> findByEmail(String emailId);
    void updateFailedAttemps(String emailId);
    void resetFailedAttempts(UserRegistrationEntity user);
    void passwordUpdate(String emailId, String newPassword);
    public void resetPassword(String emailId, String newPassword);
    List<UserRegistrationEntity> findAll();
}
