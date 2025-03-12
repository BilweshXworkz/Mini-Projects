package com.xworkz.xworkzapp.controller;

import com.xworkz.xworkzapp.dto.UserRegistrationDto;
import com.xworkz.xworkzapp.entity.UserRegistrationEntity;
import com.xworkz.xworkzapp.service.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@Component
@RequestMapping("/")
public class UserRegistrationController {

    @Autowired
    UserRegistrationService userRegistrationService;

    @RequestMapping ("signup")
    public String SignUp(){
        return "home.jsp";
    }

    @RequestMapping ("index")
    public String Index(){
        return "index.jsp";
    }

    @RequestMapping ("addUser")
    public String addUser(UserRegistrationDto dto, Model model){
        String errorMessage = userRegistrationService.validAndSave(dto);
        if (errorMessage != null){
            model.addAttribute("error", errorMessage);
            return "home.jsp";
        }
        return "response.jsp";
    }

    @RequestMapping ("signin")
    public String signIn(){
        return "signin.jsp";
    }

    @RequestMapping ("login")
    public ModelAndView loginUser(@RequestParam("emailId") String emailId,
                                  @RequestParam("password") String password){
        UserRegistrationEntity user = userRegistrationService.authenticateUser(emailId, password);

        if (user != null) {
            // If user exists but needs a password reset
            if (user.getEmailId() == null) {
                return new ModelAndView("resetPassword.jsp");
            }
            return new ModelAndView("welcome.jsp", "user", user);
        }
        return new ModelAndView("signin.jsp", "errorMessage", "Invalid Email or Password.");
    }

    @RequestMapping ("fetchByEmail")
    public String fetchByEmail(@RequestParam("emailId") String emailId, Model model){
        UserRegistrationDto dto = userRegistrationService.fetchByEmail(emailId);
        model.addAttribute("user", dto);
        return "update.jsp";
    }

    @RequestMapping ("update")
    public String updateAndSave(UserRegistrationDto dto, Model model){
        Boolean updateUser = userRegistrationService.updateUser(dto);
        if (updateUser){
            model.addAttribute("msg","User Data Updated Successfully");
        }else {
            model.addAttribute("msg", "User Data Not Updated");
        }
        return "update.jsp";
    }

    @RequestMapping("changePassword")
    public ResponseEntity<String> updatePassword(@RequestParam("emailId") String emailId,
                                                 @RequestParam("newPassword") String newPassword) {
        String response = userRegistrationService.updatePassword(emailId, newPassword);
        return ResponseEntity.ok(response);
    }

    @RequestMapping("resetPassword")
    public String resetPassword(@RequestParam("emailId") String email,
                                @RequestParam("password") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "New password and confirm password do not match.");
            return "forgetPassword";
        }

        String isUpdated = userRegistrationService.updatePassword(email, currentPassword, newPassword);

        if (isUpdated.equals("Current password is incorrect.") || isUpdated.startsWith("User not found")) {
            model.addAttribute("errorMessage", isUpdated);
            return "forgetPassword";
        }

        model.addAttribute("message", "Password successfully updated!");
        return "signin.jsp";
    }

    @RequestMapping("logout")
    public RedirectView logout(HttpServletRequest req){
        req.getSession().invalidate();
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(req.getContextPath());
        return redirectView;
    }

    @RequestMapping("deleteAccount")
    public RedirectView deleteByEmail(@RequestParam("emailId") String emailId, HttpServletRequest req){
        userRegistrationService.deleteByEmailId(emailId);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(req.getContextPath());
        return redirectView;
    }
}
