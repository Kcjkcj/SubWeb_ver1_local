// src/pages/ContentPage.tsx
import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import '../styles/Content.css';
import { useEffect, useState } from 'react';
import axios from 'axios';

interface Post{
  postId:number;
  title:string;
  postBody:string;
  writeUser:Account;
  notice:boolean;
  createDt:string;
}

interface Comments{
  commentId:number;
  commentBody:string;
  createDt:string;
  post:Post;
  writeUser:Account;
}

interface Account{
  accountName:string;
  email:string;
}

const ContentPage = () => {
  const navigate = useNavigate();
  const [comments, setComments] = useState<Comments[]>([]);
  const [commentBody,setCommentBody ] = useState('');
  const location = useLocation();
  const subID = Number(localStorage.getItem('subID'));
  const {Post} = location.state || {};
  const token = localStorage.getItem("token");

  function getCsrfToken(){
    const cookieValue = document.cookie
    .split('; ')
    .find(row => row.startsWith('XSRF-TOKEN')) //쿠키 이름
    ?.split('=')[1];
    return cookieValue;
  }

    const handleWriteComment = async (e: React.FormEvent<HTMLFormElement>) => {
      e.preventDefault();
      const csrfToken = getCsrfToken();
      if(token === null)
      {
        alert("로그인 후 시도하여 주십시오");
        navigate('/login');
      }
      try {
        const response = await fetch('http://localhost:8080/board/content', {
          method: "POST",
          headers: {
            'Authorization': `Bearer ${token}`,
            'X-XSRF-TOKEN':csrfToken || '',
            'Content-Type': 'application/json',
          },
          credentials:'include',
          body: JSON.stringify({
            commentBody:commentBody,
            postId:Post.postId
          }),
        });
        if (response.ok) {
        } else {
          alert('댓글작성 실패');
        }
      } catch (error) {
        console.error('comment error:', error);
      }
    };

  useEffect(() => {
    const fetchPosts = async () => {

      try {
        const response = await axios.get<Comments[]>(`http://localhost:8080/board/content?post_id=${Post.postId}`, {
          
        });
        setComments(response.data);
      } catch (error) {
        console.error("Error fetching profile data:",error);
      }
    };
    fetchPosts();
  }, [Post, navigate]);


  return (
    <div className="content-container">
      <button 
        className="back-button"
        onClick={() => navigate(`/board?SubID=${subID}`)}
      >
        게시판으로
      </button>
      <div className="content-box">
        <div className="content-header">
          <div className="title-row">
            <span>글 제목: {Post.title}</span>
            <span>작성자: {Post.writeUser.accountName}</span>
          </div>
        </div>

        <div className="content-body">
          <p>글 내용:</p>
          <div dangerouslySetInnerHTML={{__html:Post.postBody}}></div>
        </div>
      </div>

      
      <div className="comment-section">
        <h3>댓글</h3>
        <div className="comment-list">
            {comments.map((comment)=>
          <div key={comment.commentId} className="comment-item">     
            <p>{comment.writeUser.accountName} : {comment.commentBody}</p>     
          </div>
           )}   
        </div>
          <form onSubmit={handleWriteComment}>
            <input type="text" value={commentBody} onChange={(e) => setCommentBody(e.target.value)} placeholder="Name" required />
          <button type="submit">작성</button>
    </form>
      </div>
    </div>
  );
};

export default ContentPage;
