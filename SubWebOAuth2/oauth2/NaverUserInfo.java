package com.kcj.SubWebOAuth2.oauth2;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo{
    private final Map<String, Object> attributes;

    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return (String) response.get("id");
    }

    @Override
    public String getEmail() {
        Map<String,Object> response = (Map<String, Object>) attributes.get("response");
        if(response !=null){
            return (String)response.get("email");
        }
        return null;
    }

    public String getName() {
        Map<String,Object> response = (Map<String, Object>) attributes.get("response");
        if(response !=null){
            return (String) response.get("name");
        }
        return null;
    }
}
