// src/pages/SignupPage.tsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/CreateSubculture.css';


const CreateSubculture = () => { //이미지 파일을 다룰 때는 JSON보다는 FormData가 더 효율적임
  const [title, setTitle] = useState(''); //JSON으로 하면 Base64로 인코딩하고 서버에서 디코딩 해야 함
  const [genre, setGenre] = useState('');
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const navigate = useNavigate();

  function getCsrfToken(){
    const cookieValue = document.cookie
    .split('; ')
    .find(row => row.startsWith('XSRF-TOKEN')) //쿠키 이름
    ?.split('=')[1];
    return cookieValue;
  }

  const handleCreate = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
        const csrftoken = getCsrfToken();
        const formData = new FormData();
        formData.append('title',title);
        formData.append('genre',genre);
        if(imageFile)
            formData.append('image',imageFile);


      const response = await fetch('http://localhost:8080/subculture', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'X-XSRF-TOKEN': csrftoken || '',
        },
        body: formData, 
        credentials : 'include'
      });
      if (response.ok) {
        alert('등록 성공');
        navigate('/');
      } else {
        alert('등록 실패');
      }
    } catch (error) {
      console.error('Register error:', error);
    }
  };


/*
  const validateForm = () => {
    const newErrors: Partial<SignupForm> = {};
    
    if (!formData.email) newErrors.email = '이메일을을 입력해주세요';
    if (!formData.password) newErrors.password = '비밀번호를 입력해주세요';
    if (formData.password !== formData.passwordConfirm) {
      newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다';
    }
    if (!formData.name) newErrors.name = '닉네임을 입력해주세요';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    try {
      // API 호출 로직 구현
      // 성공시 메인페이지로 리다이렉트
      navigate('/');
    } catch (error) {
      // 에러 처리
      console.error('회원가입 실패:', error);
    }
  };
  */

  const handleImageChange = (e:
    React.ChangeEvent<HTMLInputElement>) => {
        if(e.target.files && e.target.files[0]){
            setImageFile(e.target.files[0]);
            setImagePreview(URL.createObjectURL(e.target.files[0]));
        }
    };
  

  return (
<form onSubmit={handleCreate} className="create-subculture-container">
    <div className="create-subculture-box">
        <h1>작품 등록</h1>
        <div className="create-subculture-form">
            <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Title"
                required
            />
            <input
                type="text"
                value={genre}
                onChange={(e) => setGenre(e.target.value)}
                placeholder="Genre"
                required
            />
            <input
                type="file"
                onChange={handleImageChange}
                accept="image/*"
                required
            />
            {imagePreview && (
                <img
                    src={imagePreview}
                    alt="Preview"
                    className="image-preview"
                />
            )}
            <button type="submit" className="submit-btn">
                등록하기
            </button>
        </div>
    </div>
</form>

  );
};

export default CreateSubculture;