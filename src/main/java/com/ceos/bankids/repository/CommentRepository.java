package com.ceos.bankids.repository;

import com.ceos.bankids.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    public void deleteAllByChallengeId(Long challengeId);
}
