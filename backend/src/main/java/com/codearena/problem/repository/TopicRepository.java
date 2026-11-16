package com.codearena.problem.repository;

import com.codearena.problem.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findBySlug(String slug);
    Optional<Topic> findByName(String name);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);

    @Query("SELECT DISTINCT t FROM Problem p " +
           "JOIN p.topics t " +
           "WHERE p.status = com.codearena.problem.entity.ProblemStatus.PUBLISHED " +
           "ORDER BY t.name ASC")
    List<Topic> findDistinctTopicsForPublishedProblems();
}
