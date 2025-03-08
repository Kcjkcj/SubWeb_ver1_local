// src/pages/ProfilePage.tsx

import React, {useState, useEffect} from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Profile.css';
import axios from 'axios';

interface Account {
  accountId:number;
  email:string;
  createDt : string;
  accountName : string;
}


const ProfilePage = () => {
  const navigate = useNavigate();
  const [accountInfo, setAccountInfo] = useState<Account | null>(null);
  const [friendList, setFriendInfo] = useState<Account[]>([]);
  useEffect(() => {
    const fetchProfileData = async () => {
      const token = localStorage.getItem("token");
      if (!token) {
        console.log("No token found, redirecting to login");
        navigate("/login");
        return;
      }

      try {
        const response = await axios.get('http://localhost:8080/profile', {
          
          headers: {Authorization: `Bearer ${token}`} //JWT을 보낼 때는 Bearer  접두사를 붙이는게 원칙 
        });
        setAccountInfo(response.data);
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
    };
    fetchProfileData();
  }, [navigate]);
   //useEffect 내부에서 navigate 사용 중이므로 의존성 배열에 넣어줘야 함

   useEffect(() => {
    const fetchFriends = async () => {
      const token = localStorage.getItem("token");

      if (!token) {
        console.log("No token found, redirecting to login");
        navigate("/login");
        return;
      }

      try {
        const response = await axios.get<Account[]>('http://localhost:8080/profile/myFriends', {
          
          headers: {Authorization: `Bearer ${token}`} //JWT을 보낼 때는 Bearer  접두사를 붙이는게 원칙 
        });
        setFriendInfo(response.data);
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
    };
    fetchFriends();
  }, [navigate]);
   //useEffect 내부에서 navigate 사용 중이므로 의존성 배열에 넣어줘야 함


   
   function getCsrfToken(){
    const cookieValue = document.cookie
    .split('; ')
    .find(row => row.startsWith('XSRF-TOKEN')) //쿠키 이름
    ?.split('=')[1];
    return cookieValue;
  }

   const deleteFriend = async (friend: Account) => {
    //alert(JSON.stringify(friend,null,2));
    try {
      const csrfToken = getCsrfToken();
      //console.log(csrfToken);
      const response = await fetch('http://localhost:8080/profile/myFriends', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'X-XSRF-TOKEN':csrfToken || '',
          'Content-Type':'application/json'
          
        },
        credentials: 'include', //쿠키와 함께 요청을 보내기
        body: JSON.stringify({
          accountId:friend.accountId,
          accountName:friend.accountName,
          email:friend.email,
          createDt:friend.createDt
        }),
      });
  
      if (response.ok) {
        alert("해당 친구를 삭제하였습니다."); //서버에서 메시지를 받아서 하기에는 LogoutHandler를 만들어야 하는데 그렇게 유의미하지 않음
        setFriendInfo(prevfriends => prevfriends.filter(f => f.accountId !== friend.accountId)) //삭제할 대상이 되는 친구의 id는 필터링한다
      } else {
        alert("친구를 삭제하는데 문제가 발생하였습니다.");
        console.error('delete fail');
      }
    } catch (error) {
      console.error('Delete fail:', error);
    }
  };



  return (
    <div className="profile-container">
      <div className="profile-grid">
        <div className="profile-item main-page">
          <button onClick={() => navigate("/")}>메인 페이지</button>
        </div>
        <div className="profile-header">
          <h1>나의 정보</h1>
        </div>
      {
      accountInfo && (
          <div className="profile-info">
            <table className="profile-info-table">
              <tbody>
                <tr>
                  <td>이메일:</td>
                  <td>{accountInfo.email}</td>
                </tr>
                <tr>
                  <td>활동명:</td>
                  <td>
                    {accountInfo.accountName}
                  </td>
                </tr>
                <tr>
                  <td>가입일:</td>
                  <td>{accountInfo.createDt}</td>
                </tr>
              </tbody>
            </table>
          </div>
        )}

        {/* 친구 리스트 세로 스크롤 */}
        <div className="friend-slider">
          <h2>함께하는 친구들</h2>
          <div className="friend-list">
            <ul>
              {friendList.map((friend, index) => (
                <li key={index}>
                  <p>이름: {friend.accountName} </p>
                  <p>이메일: {friend.email}</p>
                  <p>가입일: {friend.createDt}</p>
                  <button className='delete-friend-btn'
                  onClick={() => {
                    if(window.confirm("정말로 삭제하시겠습니까?"))
                      { 
                        deleteFriend(friend)
                      };
                      
                    }}>
                    친구 삭제
                  </button>
                  <br></br>
                  </li>
              ))}
            </ul>
          </div>
        </div>

        <br></br>
        {/* 친구 요청 확인 버튼 */}
        <div className="profile-item verify">
          <button onClick={() => navigate("/message")}>
            메시지
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;