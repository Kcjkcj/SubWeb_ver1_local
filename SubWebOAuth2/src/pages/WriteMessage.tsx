import React, { useState, useEffect } from 'react';
import Select from 'react-select';
import '../styles/Register.css';

interface FriendOption {
  value: number;
  label: string;
}
function getCsrfToken(){
    const cookieValue = document.cookie
    .split('; ')
    .find(row => row.startsWith('XSRF-TOKEN')) //쿠키 이름
    ?.split('=')[1];
    return cookieValue;
  }
const WriteMessagePage = () => {
  const [options, setOptions] = useState<FriendOption[]>([]);
  const [selectedFriend, setSelectedFriend] = useState<FriendOption | null>(null);
  const [messageBody, setMessageBody] = useState('');

  // 서버에서 친구 리스트 가져오기
  const fetchFriends = async () => {
    const token = localStorage.getItem('token');

    try {
      const response = await fetch('http://localhost:8080/profile/myFriends', {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        credentials:'include'
      });
      if (response.ok) {
        const data = await response.json();
        const friendOptions = data.map((friend: { accountId: number; accountName: string }) => ({
          value: friend.accountId,
          label: friend.accountName,
        }));
        setOptions(friendOptions); // 옵션 업데이트
      } else {
        alert('친구 리스트를 가져오지 못했습니다.');
      }
    } catch (error) {
      console.error('Error fetching friends:', error);
    }
  };

  // 메시지 보내기
  const sendMessage = async () => {
    const token = localStorage.getItem('token');
    const csrfToken = getCsrfToken();
    if (!selectedFriend) {
      alert('메시지를 보낼 친구를 선택하세요.');
      return;
    }
    try {
      const response = await fetch('http://localhost:8080/postMessage', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'X-XSRF-TOKEN':csrfToken || '',
          'Content-Type': 'application/json'
        },
        credentials:'include',
        body: JSON.stringify({
          receiveId: selectedFriend.value,
          messageBody,
        }),
      });
      if (response.ok) {
        alert('메시지 전송 성공');
        setMessageBody('');
        setSelectedFriend(null);
      } else {
        alert('메시지 전송 실패');
      }
    } catch (error) {
      console.error('Error sending message:', error);
    }
  };

  useEffect(() => {
    fetchFriends(); // 컴포넌트 마운트 시 친구 리스트 가져오기
  }, []);

  const handleChange = (option : FriendOption | null) => {
    setSelectedFriend(option)
  };
  return (
    <div className="message-container">
      <h1>메시지 보내기</h1>

      {/* 친구 선택 드롭다운 */}
      <Select
        options={options}
        onChange={handleChange}
        placeholder="친구를 선택하세요"
        isSearchable={true} // 검색 가능 설정
      />

      {/* 메시지 작성 텍스트 박스 */}
      <textarea
        value={messageBody}
        onChange={(e) => setMessageBody(e.target.value)}
        placeholder="메시지를 입력하세요"
        required
      />

      {/* 메시지 전송 버튼 */}
      <button onClick={sendMessage}>보내기</button>
    </div>
  );
};

export default WriteMessagePage;
