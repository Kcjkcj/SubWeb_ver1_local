// src/pages/FriendRequestPage.tsx
import React,{useState, useEffect, useCallback} from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Message.css';
import axios from 'axios';

interface SearchResult{
    accountId:number;
    nickname:string;
}

function getCsrfToken(){
    const cookieValue = document.cookie
    .split('; ')
    .find(row => row.startsWith('XSRF-TOKEN')) //쿠키 이름
    ?.split('=')[1];
    return cookieValue;
  }

const FriendRequestPage = () => {
    const [nickname, setNickname] = useState('');
    const [searchResult, setSearchResult] = useState<SearchResult[] | null>(null);
    const csrfToken = getCsrfToken();
    const searchFriend = async () => {
        const token = localStorage.getItem("token");
        const formData = new FormData();
        formData.append("nickname",nickname);
        try {
          const response = await fetch('http://localhost:8080/profile/search', {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
              'X-XSRF-TOKEN': csrfToken || ''
            },
            credentials: 'include',
            body: formData
          });
      
          if (response.ok) {
            const data = await response.json();
            setSearchResult(data);
          } else {
            setSearchResult(null); // 이전 검색 결과 초기화
            alert("해당 닉네임의 사용자를 찾을 수 없습니다.");
          }
        } catch (error) {
          setSearchResult(null); // 이전 검색 결과 초기화
          console.error("Error searching for friend:", error);
          alert("해당 닉네임의 사용자를 찾을 수 없습니다.");
        }
      };
      
      
  
      const sendFriendRequest = async (friendID: number) => {
        const token = localStorage.getItem("token");
        try {
            const response = await fetch('http://localhost:8080/postMessage', {
                method: "POST",
                headers: {
                  Authorization: `Bearer ${token}`,
                  'X-XSRF-TOKEN': csrfToken || '',
                   'Content-Type':'application/json'

                },
                credentials: 'include',
                body: JSON.stringify({
                    receiveId:friendID,
                    notice:false,
                    request:true,
                    messageBody:"친구 요청"
                })
          });
          if(response.ok)
          alert("친구 요청이 성공적으로 전송되었습니다.");
        } catch (error) {
          console.error("Error sending friend request:", error);
          alert("친구 요청 전송에 실패했습니다.");
        }
      };
      

  
return (
    <div className="friend-request-container">
      <input
        type="text"
        value={nickname}
        onChange={(e) => setNickname(e.target.value)}
        placeholder="닉네임을 입력하세요"
      />
      <button onClick={searchFriend}>검색</button>
      {searchResult && (
        <div className="search-result">
          {searchResult.map((result, index) => (
            <div key={index}>
              <p>닉네임: {result.nickname}</p>
              <button onClick={() => sendFriendRequest(result.accountId)}>친구 요청 보내기</button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
  
  };
  
export default FriendRequestPage;