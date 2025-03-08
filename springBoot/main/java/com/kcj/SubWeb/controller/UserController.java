package com.kcj.SubWeb.controller;

import com.kcj.SubWeb.entity.Account;
import com.kcj.SubWeb.entity.Role;
import com.kcj.SubWeb.repository.AccountRepository;
import com.kcj.SubWeb.repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Account account){
        try{
            String hashPwd = passwordEncoder.encode(account.getAccountPwd());
            account.setAccountPwd(hashPwd);
            account.setCreateDt(new Date(System.currentTimeMillis()));
            Account savedAccount = accountRepository.save(account);
            Role role = new Role();
            role.setRole_name("ROLE_USER");
            role.setAccount(savedAccount);
            Role savedRole = roleRepository.save(role);

            if(savedAccount.getAccountId()>0 && savedRole.getRole_id()>0)
            {
                return ResponseEntity.status(HttpStatus.CREATED).
                        body("성공적으로 등록되었습니다.");
            }
            else
            {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                        body("등록에 실패하였습니다.");
            }
        }
        catch (Exception ex)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("예외가 발생하였습니다 : "+ex.getMessage());
        }
    }



    @RequestMapping("/user")
    public Account getAccountDetails(Authentication authentication)
    {
        Optional<Account> optionalAccount = accountRepository.findByEmail(authentication.getName());
        return optionalAccount.orElse(null);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request){
        return ResponseEntity.ok("");
    }
}
