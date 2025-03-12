package com.xworkz.xworkzapp.service;

import com.xworkz.xworkzapp.dto.UserRegistrationDto;
import com.xworkz.xworkzapp.entity.UserRegistrationEntity;
import com.xworkz.xworkzapp.repository.UserRegistrationRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Repository
public class UserRegistrationServiceImp implements UserRegistrationService{

    @Autowired
    UserRegistrationRepository repository;

    private static final String SECRET_KEY = "MySecretKey12345";

    @Override
    public String validAndSave(UserRegistrationDto dto) {
        String error;

        List<UserRegistrationEntity> allUsers = repository.findAll();

        List<String> duplicateFields = new ArrayList<>();

        for (UserRegistrationEntity user : allUsers) {
            if (user.getName().equalsIgnoreCase(dto.getName())) duplicateFields.add("Name");
            if (user.getEmailId().equalsIgnoreCase(dto.getEmailId())) duplicateFields.add("Email ID");
            if (user.getPhoneNumber().equals(dto.getPhoneNumber())) duplicateFields.add("Phone Number");
        }

        if (!duplicateFields.isEmpty()) {
            return "User already exists with " + String.join(", ", duplicateFields);
        }

        String randomPassword = generateRandomNumber();

        if ((error = validatePassword(randomPassword)) != null) {
            return "Generated password is invalid: " + error;
        }

        System.out.println("Generated Password: " + randomPassword);

        String encryptedPassword = encryptPassword(randomPassword);

        UserRegistrationEntity entity = new UserRegistrationEntity();
        BeanUtils.copyProperties(dto, entity);
        System.out.println(entity.getPassword());
        entity.setPassword(encryptedPassword);

        entity.setFailedAttempts(dto.getFailedAttempts() != null ? dto.getFailedAttempts() : 0);
        repository.save(entity);
        sendAuthenticateEmail(dto.getEmailId(), randomPassword);
        return "Your account is created successfully. Use this password to log in: " + randomPassword;
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
//        String emailRegex = "^(?=.*[a-z])(?=.*\\d)(?=.*[@.])[a-z\\d@.]+@[a-z]+\\.[a-z]{2,6}$";
        String emailRegex = "^(?=.*[a-z])(?=.*\\d)(?=.*[@.])[a-z\\d@.]+@[a-z]+\\.[a-z]$";
        if (!email.matches(emailRegex)) {
            return "Invalid Email: Must contain a number, an uppercase letter, and a special character (@ or .)";
        }
        return null;
    }

    private String generateRandomNumber() {
        int num = 8;
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "@#$%^&+=!";
        String allChars = upperCase + lowerCase + digits + specialChars;

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        for (int i = 3; i < num; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        List<Character> passwordChars = password.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(passwordChars);

        return passwordChars.stream().map(String::valueOf).collect(Collectors.joining());
    }


    public String validatePassword(String password) {
        if (password == null || password.length() < 6)
            return "Password must be at least 6 characters long";

        String passwordRegex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$";
        if (!password.matches(passwordRegex)) {
            return "Password must contain at least one uppercase letter, one number, and one special character";
        }
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

        if (user.getFailedAttempts() != null && user.getFailedAttempts() == -1) {
            System.out.println("User needs to reset password.");
            return new UserRegistrationEntity(); // Return empty user to indicate reset
        }

        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            System.out.println("Account is locked until " + user.getAccountLockedUntil());
            return null;
        }

        String decryptedPassword = decryptPassword(user.getPassword());
        if (!password.equals(decryptedPassword)) {
            System.out.println("Invalid credentials");
            repository.updateFailedAttemps(emailId);
            return null;
        }

        repository.resetFailedAttempts(user);
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

    @Override
    public String resetPassword(String email, String currentPassword, String newPassword) {
        UserRegistrationEntity user = repository.fetchEmail(email);
        if (user == null) {
            return "User not found";
        }

        String decryptedPassword = decryptPassword(user.getPassword());

        if (!decryptedPassword.equals(currentPassword)) {
            return "Current password is incorrect.";
        }

        validatePassword(newPassword);

        String encryptedNewPassword = encryptPassword(newPassword);
        repository.resetPassword(email, encryptedNewPassword);

        return "Password reset successfully.";
    }

    @Override
    public String updatePassword(String emailId, String currentPassword, String newPassword) {
        Optional<UserRegistrationEntity> userOptional = repository.findByEmail(emailId);

        if (userOptional.isPresent()) {
            UserRegistrationEntity user = userOptional.get();

            String decryptedPassword = decryptPassword(user.getPassword());

            if (!decryptedPassword.equals(currentPassword)) {
                return "Current password is incorrect.";
            }

            String encryptedNewPassword = encryptPassword(newPassword);
            user.setPassword(encryptedNewPassword);

            repository.passwordUpdate(emailId, encryptedNewPassword);

            return "Password updated successfully.";
        } else {
            return "User not found with email: " + emailId;
        }
    }

    @Override
    public void deleteByEmailId(String emailId) {
        repository.deleteEmail(emailId);
        if(repository != null) {
            repository.deleteEmail(emailId);
            System.out.println("User profile with id " + emailId + " deleted successfully");
        } else {
            System.out.println("User profile with id " + emailId + " not found");
        }
    }

    public static void sendAuthenticateEmail(String emailId, String randomPassword) {

        final String username = "bilweshbinay1025@gmail.com";
        final String password = "lcdi pvao kojd mzhk";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("from@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse("to_username_a@gmail.com, to_username_b@yahoo.com")
            );
            message.setSubject("Testing Gmail TLS");
            message.setText("Dear Mail Crawler,"
                    + "\n\n Please do not spam my email!");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
