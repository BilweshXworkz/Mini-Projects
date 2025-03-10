package com.xworkz.xworkzapp.service;

import com.xworkz.xworkzapp.dto.UserRegistrationDto;
import com.xworkz.xworkzapp.entity.UserRegistrationEntity;
import com.xworkz.xworkzapp.repository.UserRegistrationRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public class UserRegistrationServiceImp implements UserRegistrationService{

    @Autowired
    UserRegistrationRepository repository;

    private static final String SECRET_KEY = "MySecretKey12345";

    @Override
    public String validAndSave(UserRegistrationDto dto) {
        String error;

        if ((error = validateName(dto.getName())) != null) return error;
        if ((error = validatePhoneNumber(dto.getPhoneNumber())) != null) return error;
        if ((error = validateEmail(dto.getEmailId())) != null) return error;
        if ((error = validatePassword(dto.getPassword())) != null) return error;

        UserRegistrationEntity entity = new UserRegistrationEntity();
        BeanUtils.copyProperties(dto, entity);

        entity.setPassword(encryptPassword(dto.getPassword()));
        repository.save(entity);

        return null;
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) return "Name cannot be empty";
        name = name.trim();
        if (name.length() < 3) return "Name should be more than 3 characters";
        if (!name.matches(".*[A-Z].*")) return "Name must contain at least one uppercase letter";
        if (!name.matches("[A-Za-z ]+")) return "Name should not have any special characters or numbers";
        return null;
    }

    private String validatePhoneNumber(Long phoneNumber) {
        if (phoneNumber == null) return "Phone number cannot be null";
        String phno = String.valueOf(phoneNumber);
        if (phno.length() != 10) return "Phone number must be 10 digits.";
        char startDigit = phno.charAt(0);
        if (startDigit != '9' && startDigit != '8' && startDigit != '7') {
            return "Phone number should start with 9, 8, or 7";
        }
        return null;
    }

    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "Email cannot be empty";
        String emailRegex = "^(?=.*[a-z])(?=.*\\d)(?=.*[@.])[a-z\\d@.]+@[a-z]+\\.[a-z]{2,6}$";
        if (!email.matches(emailRegex)) {
            return "Invalid Email: Must contain a number, an uppercase letter, and a special character (@ or .)";
        }
        return null;
    }

    public String validatePassword(String password) {
        if (password == null || password.length() < 6)
            return "Password must be at least 6 characters long";

        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$";
        if (!password.matches(passwordRegex)) {
            return "Password must contain at least one uppercase letter, one number, and one special character";
        }
//        String encrypted = encryptPassword(password);
//        System.out.println("Encrypted Password: " + encrypted);

        return null;
    }

    private String encryptPassword(String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting password", e);
        }
    }

    private String decryptPassword(String encryptedPassword) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting password", e);
        }
    }

    @Override
    public UserRegistrationEntity authenticateUser(String emailId, String password) {
        UserRegistrationEntity user = repository.fetchEmail(emailId);
        if (user == null) {
            System.out.println("User not found");
            return null;
        }

        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            System.out.println("Account is locked Until " + user.getAccountLockedUntil());
            return null;
        }

        String decryptedPassword = decryptPassword(user.getPassword());
        if (!password.equals(decryptedPassword)) {
            System.out.println("Invalid credentials");
            repository.updateFailedAttemps(emailId); // Increment failed attempts
            return null;
        }

        repository.resetFailedAttempts(user); // Reset failed attempts on successful login
        return user;
    }

    @Override
    public UserRegistrationDto fetchByEmail(String emailId) {
        if (emailId != null){
            UserRegistrationDto dto = new UserRegistrationDto();
            UserRegistrationEntity entity = repository.fetchEmail(emailId);
            try {
                BeanUtils.copyProperties(entity, dto);
                return dto;
            } catch (BeansException e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }


    @Override
    public Boolean updateUser(UserRegistrationDto dto) {
        System.out.println(dto);
        if (validateName(dto.getName()) != null) return false;
        if (validatePhoneNumber(dto.getPhoneNumber()) != null) return false;
        if (validateEmail(dto.getEmailId()) != null) return false;

        boolean updatePassword = (dto.getPassword() != null && !dto.getPassword().trim().isEmpty());

        if (updatePassword) {
            if (validatePassword(dto.getPassword()) != null) return false;
        }

        UserRegistrationEntity entity = new UserRegistrationEntity();
        System.out.println("Moving to copy properties");
        BeanUtils.copyProperties(dto, entity);

        if (updatePassword) {
            entity.setPassword(encryptPassword(dto.getPassword()));
        }
        return repository.saveUpdate(entity, updatePassword);
    }

    @Override
    public String updatePassword(String emailId, String newPassword) {
        Optional<UserRegistrationEntity> userOptional = repository.findByEmail(emailId);
        if (userOptional.isPresent()){
            UserRegistrationEntity user = userOptional.get();

            user.setPassword(encryptPassword(newPassword));

            repository.saveUpdate(user, true);
            return "Password updated successfully.";
        } else {
            return "User not found with email: " + emailId;
        }
    }
}
