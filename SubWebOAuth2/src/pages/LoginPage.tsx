// src/pages/LoginPage.tsx
import React, { useEffect, useState } from 'react';
import { FaGoogle, FaGithub } from 'react-icons/fa';
import { RiKakaoTalkFill } from 'react-icons/ri';
import { useNavigate } from 'react-router-dom';
import { JWT_HEADER } from '../constants/auth';
import axios from 'axios';
import '../styles/Login.css';

const LoginPage = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const GOOGLE_AUTH_URL = "http://localhost:8080/oauth2/authorization/google";
  const GITHUB_AUTH_URL =  "http://localhost:8080/oauth2/authorization/github";
  const KAKAO_AUTH_URL = "http://localhost:8080/oauth2/authorization/kakao";
  const NAVER_AUTH_URL =  "http://localhost:8080/oauth2/authorization/naver";

  function getCsrfToken(){
    const cookieValue = document.cookie
    .split('; ')
    .find(row => row.startsWith('XSRF-TOKEN')) //쿠키 이름
    ?.split('=')[1];
    return cookieValue;
  }

  const axiosInstance = axios.create({
    baseURL:'http://localhost:8080', 
    withCredentials:true //쿠키를 포함해서 요청을 보도록 설정
  });

  axiosInstance.interceptors.request.use(config => {
    config.headers['X-XSRF-TOKEN'] = getCsrfToken(); //X-XSRF-TOKEN 이라는 헤더
    return config;
  })

  axiosInstance.interceptors.response.use(
    (response) => {
      console.log('Response headers:', response.headers);
      return response;
    },
    (error) => {
      console.error('Response error:', error);
      return Promise.reject(error);
    }
  );

  const handleGoogleLogin = () =>{
    window.location.href = GOOGLE_AUTH_URL;
  }
  const handleGithubLogin = () =>{
    window.location.href = GITHUB_AUTH_URL;
  }
  const handleKakaoLogin = () =>{
    window.location.href = KAKAO_AUTH_URL;
  }
  const handleNaverLogin = () =>{
    window.location.href = NAVER_AUTH_URL;
  }


  

  const handleLogin = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      const response = await axiosInstance.get('/user', {
        headers: {
          'Authorization': 'Basic ' + btoa(email + ':' + password),
        },
      });
      if (response.status===200) {
        const token = response.headers['authorization'] || response.headers[JWT_HEADER];
        if(token){
          localStorage.setItem('token',token);
          console.log('Stored JWT:', token); //디버깅 용도
        }
        else {
          console.error("No token receive from server");
        }
        navigate('/');
      } else {
        alert('로그인 실패');
      }
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.error('Login error:', error.response?.data);
        console.error('Status:', error.response?.status);
      } else {
        console.error('Unexpected error:', error);
      }
      alert('로그인 중 오류가 발생했습니다.');
    }
  };
  useEffect(() => {
    const checkAuth = async () => {
      try {
        // 쿠키에서 JWT 자동 전송 확인
        const response = await axiosInstance.get('/api/auth/validate');
        if (response.status === 200 && response.data.authenticated) {
          navigate('/');
        }
      } catch (error) {
        console.error('인증 실패:', error);
      }
    };
    checkAuth();
  }, []);
  

  return (
    <div className="login-container">
      <div className="login-box">
      <button 
        className="back-button"
        onClick={() => navigate(`/`)}
      >
        메인화면으로
      </button>
        <h1>로그인</h1>
        <form onSubmit={handleLogin} className="login-form">
          <input
            type="text"
            name="username"
            placeholder="이메일"
            value={email}
            onChange={(e)=>setEmail(e.target.value)} aria-placeholder='Email' required />
          
          <input
            type="password"
            name="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)} aria-placeholder="Password" required />
          <button type="submit" className="login-btn">로그인</button>
        </form>
        
        <div className="divider">
          <span>또는</span>
        </div>

        <div className="oauth-buttons">
          <button className="oauth-btn google"
          onClick={handleGoogleLogin}>
            <FaGoogle /> Google
          </button>
          <button className="oauth-btn github"
          onClick={handleGithubLogin}>
            <FaGithub /> GitHub
          </button>
          <button className="oauth-btn kakao"
          onClick={handleKakaoLogin}>
            <RiKakaoTalkFill /> Kakao
          </button>
          <button
            className="oauth-btn naver"
            onClick={handleNaverLogin}
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              backgroundColor: "#03C75A", // 네이버 초록색
              color: "#FFFFFF",
              border: "none",
              borderRadius: "4px",
              cursor: "pointer",
            }}
          >
            <img
              src={`${process.env.PUBLIC_URL}/btnG_icon_square.png`}
              alt="Naver Logo"
              style={{
                width: "24px", // 로고 크기 조정
                height: "24px",
                marginRight: "8px",
              }}
            />
            Naver
          </button>

        </div>

        <div className="signup-section">
          <p>계정이 없으신가요?</p>
          <button className="signup-btn" onClick={() => navigate('/register')}>회원가입</button>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;

