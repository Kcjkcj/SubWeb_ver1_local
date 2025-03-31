package com.kcj.SubWebOAuth2.repository;

import com.kcj.SubWebOAuth2.entity.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Integer> {
    List<Comment> findByPost_PostId(int id);
}
