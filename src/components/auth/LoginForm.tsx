// src/components/auth/LoginForm.tsx
import React, { useState } from 'react';
const LoginForm = () => {
  const [loginMethod, setLoginMethod] = useState('');

  const handleOAuth2Login = async () => {
    try {
      // OAuth2 로그인 처리
      setLoginMethod('oauth2');
      // API 호출
    } catch (error) {
      console.error('OAuth2 로그인 실패:', error);
    }
  };

  const handleKeyCloakLogin = async () => {
    try {
      // KeyCloak 로그인 처리
      setLoginMethod('keycloak');
      // API 호출
    } catch (error) {
      console.error('KeyCloak 로그인 실패:', error);
    }
  };

  return (
    <div className="login-container">
      <h2>로그인 페이지</h2>
      <div className="login-buttons">
        <button onClick={handleOAuth2Login}>
          OAuth2를 사용한 인증
        </button>
        <button onClick={handleKeyCloakLogin}>
          KeyClock을 사용한 인증
        </button>
      </div>
      <div className="login-notice">
        <p>자체 회원가입 기능</p>
        <p>각 회원의 비밀번호는 해싱되어 저장</p>
      </div>
    </div>
  );
};
