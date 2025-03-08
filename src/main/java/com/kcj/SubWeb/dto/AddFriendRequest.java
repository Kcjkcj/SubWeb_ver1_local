package com.kcj.SubWeb.dto;

import com.kcj.SubWeb.entity.Message;
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
