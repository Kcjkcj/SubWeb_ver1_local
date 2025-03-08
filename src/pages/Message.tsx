// src/pages/FriendRequestPage.tsx
import React,{useState, useEffect, useCallback} from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Message.css';
import axios from 'axios';

interface Message{
  messageId:number;
  sendId:number;
  receiveId:number;
  notice:boolean;
  requst:boolean;
  messageBody:string;
  createDt:string;
  sendUser:Account;
}

interface Account{
  accountName:string;
  email:string;
}



const MessagePage = () => {
  const navigate = useNavigate();
  const [messages, setMessages] = useState<Message[]>([]);
  const [messageType, setMessageType] = useState<string>('received');

    const fetchMessages = useCallback (async (type:string) => {
      const token = localStorage.getItem("token");
  
      if (!token) {
        console.log("No token found, redirecting to login");
        navigate("/login");
        return;
      }
  
      try {
        let url = "http://localhost:8080/getMessage";
        switch(type){
          case 'sent':
            url += "?sent";
            break;
          case 'notice':
            url += "?notice";
            break;
          case 'request':
            url += "?request";
            break;
          default:
        }
        const response = await axios.get<Message[]>(url, {
          
          headers: {Authorization: `Bearer ${token}`} //JWT을 보낼 때는 Bearer  접두사를 붙이는게 원칙 
        });
        setMessages(response.data);
      } catch (error) {
        console.error("Error fetching profile data:",error);
        if(axios.isAxiosError(error) && (error.response?.status === 403 || error.response?.status ===401 ))
        {
          console.log('Request headers:', {
            Authorization: `Bearer ${token}`
          });
          console.log("Access forbidden or unauthorized, redirecting to login");
          navigate("/login");
        }
      }
    }, [navigate]); //useCallback으로 의존성 설정해줌으로써 경고지움
  
    useEffect(() => {
      fetchMessages(messageType);
    }, [messageType, fetchMessages]); //이는 useEffect에 의존성을 넣어주는 것
   //useEffect 내부에서 navigate 사용 중이므로 의존성 배열에 넣어줘야 함

   function getCsrfToken(){
    const cookieValue = document.cookie
    .split('; ')
    .find(row => row.startsWith('XSRF-TOKEN')) //쿠키 이름
    ?.split('=')[1];
    return cookieValue;
  }

  const handleRequestResponse = async(message:Message, approve:boolean) => {
    const token = localStorage.getItem("token");
    try{
      const csrfToken = getCsrfToken();
      await fetch('http://localhost:8080/profile/request', {
        method : "POST",
        headers: {Authorization: `Bearer ${token}`,
        'X-XSRF-TOKEN':csrfToken || '',
        'Content-Type':'application/json'
      },
      credentials: 'include',
      body: JSON.stringify({
        message:message,
        approve:approve
      }),
      });
       fetchMessages(messageType); //JWT을 보낼 때는 Bearer  접두사를 붙이는게 원칙 
      } catch (error) {
        console.error("Error processing friend request:",error);
      }
    };
  

  return (
    <div className="friend-container">
      <button 
        onClick={() => navigate('/profile')}
      >
        나의 계정으로
      </button>

    <select value={messageType} onChange={(e)=> 
      setMessageType(e.target.value)}>
        <option value="received">내가 받은 메시지</option>
        <option value="sent">내가 보낸 메시지</option>
        <option value="notice">공지사항</option>
        <option value="request">친구 요청</option>
      </select>
    

      <div className="request-list">
        {messages.map((message) => (
          <li key={message.messageId} className="request-item">
            <div className="request-info">
              <div className="nickname">닉네임: {message.sendUser.accountName}</div>
              <div className="send-time">보낸 시간: {message.createDt}</div>
              <div className='message-body'>{message.messageBody}</div>
            </div>
            {messageType === 'request' && message.messageBody.includes('친구 요청') && (
            <div className="request-actions">
              <button className="action-btn accept" onClick={
                ()=>handleRequestResponse(message, true)}>승인</button>
              <button className="action-btn reject" onClick={
                ()=>handleRequestResponse(message, false)}>거절</button>
            </div>
            )}
          </li>
        ))}
            <button onClick={() => navigate("/friend_request")}>
            사용자 검색
          </button>
          <button onClick={() => navigate("/write_message")}>
            메시지 작성
          </button>
      </div>
    </div>
    
  );
};

export default MessagePage;
