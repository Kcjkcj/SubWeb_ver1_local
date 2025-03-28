package com.kcj.SubWebOAuth2.dto;

import com.kcj.SubWebOAuth2.entity.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AddFriendRequest {
    private boolean approve;
    private Message message;
}
