package com.kcj.SubWeb.controller;

import com.kcj.SubWeb.config.CustomUserDetails;
import com.kcj.SubWeb.config.SecurityService;
import com.kcj.SubWeb.entity.Account;
import com.kcj.SubWeb.entity.Message;
import com.kcj.SubWeb.repository.AccountRepository;
import com.kcj.SubWeb.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageController {
    private  final AccountRepository accountRepository;
    private final MessageRepository messageRepository;
    private final SecurityService securityService;

    @PostMapping("/postMessage")
    public ResponseEntity<String> postMessage(@RequestBody Message message, Authentication authentication)
    {
        try {
            if(((CustomUserDetails)authentication.getPrincipal()).getId() == message.getReceiveId())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                        body("자기 자신에게 메시지를 보낼 수 없습니다.");
            if(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
                message.setNotice(true);
            message.setCreateDt(new Date(System.currentTimeMillis()));
            message.setSendId(((CustomUserDetails)authentication.getPrincipal()).getId());
            Message savedMessage = messageRepository.save(message);
            if(savedMessage.getMessageId()>0)
                return ResponseEntity.status(HttpStatus.CREATED).
                        body("메시지가 전송되었습니다.");
            else
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                        body("메시지 전송에 실패하였습니다.");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("서버에 문제가 발생하였습니다." + e.getMessage());
        }

    }

    //친구요청 기능에서는 이미 친구가 되어있는 상대방에 대해서는 비활성화 시켜야 함
    
    @GetMapping(value = "/getMessage", params = "sent") //내가 보낸 메시지
    public List<Message> getMessageBySendId(@AuthenticationPrincipal UserDetails userDetails,
                                    Authentication authentication)
    {
        int id = ((CustomUserDetails)userDetails).getId();
        if(securityService.hasAccessToResource(id,authentication)) {
            return messageRepository.findBySendId(id);
        }
        else
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @GetMapping(value = "/getMessage") //내가 받은 메시지 //기본 메시지 함
    public List<Message> getMessageByReceiveId(@AuthenticationPrincipal UserDetails userDetails,
                                    Authentication authentication)
    {
        int id = ((CustomUserDetails)userDetails).getId();
        if(securityService.hasAccessToResource(id,authentication))
            return messageRepository.findByReceiveId(id);
        else
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @GetMapping(value = "/getMessage", params = "notice") //내가 받은 메시지
    public List<Message> getNotice(@AuthenticationPrincipal UserDetails userDetails,
                                    Authentication authentication)
    {
        int id = ((CustomUserDetails)userDetails).getId();
        if(securityService.hasAccessToResource(id,authentication))
            return messageRepository.findByReceiveIdAndNotice(id,true);
        else
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    @GetMapping(value = "/getMessage", params = "request") //내가 받은 메시지
    public List<Message> getRequest(@AuthenticationPrincipal UserDetails userDetails,
                                    Authentication authentication)
    {
        int id = ((CustomUserDetails)userDetails).getId();
        if(securityService.hasAccessToResource(id,authentication))
            return messageRepository.findByReceiveIdAndRequest(id,true);
        else 
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
