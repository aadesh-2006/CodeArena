package com.codearena.problem.repository;

import com.codearena.problem.entity.Problem;
import com.codearena.problem.entity.ProblemDifficulty;
import com.codearena.problem.entity.ProblemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>, JpaSpecificationExecutor<Problem> {

    Optional<Problem> findBySlug(String slug);

    Optional<Problem> findBySlugAndStatus(String slug, ProblemStatus status);

    boolean existsBySlug(String slug);

    Page<Problem> findByStatus(ProblemStatus status, Pageable pageable);

    Page<Problem> findByStatusAndDifficulty(ProblemStatus status, ProblemDifficulty difficulty, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Problem p " +
           "LEFT JOIN FETCH p.topics " +
           "LEFT JOIN FETCH p.tags " +
           "WHERE p.slug = :slug")
    Optional<Problem> findWithTopicsAndTagsBySlug(@Param("slug") String slug);

    @Query("SELECT DISTINCT p FROM Problem p " +
           "LEFT JOIN FETCH p.topics " +
           "LEFT JOIN FETCH p.tags " +
           "WHERE p.slug = :slug AND p.status = :status")
    Optional<Problem> findWithTopicsAndTagsBySlugAndStatus(@Param("slug") String slug, @Param("status") ProblemStatus status);

    @Query("SELECT DISTINCT p FROM Problem p " +
           "LEFT JOIN FETCH p.topics " +
           "LEFT JOIN FETCH p.tags " +
           "WHERE p.id IN :ids")
    java.util.List<Problem> findWithTopicsAndTagsByIdIn(@Param("ids") java.util.Collection<Long> ids);

    @Query("SELECT DISTINCT p FROM Problem p " +
           "LEFT JOIN FETCH p.topics " +
           "WHERE p.id IN :ids")
    java.util.List<Problem> findWithTopicsByIdIn(@Param("ids") java.util.Collection<Long> ids);

    @Query("SELECT DISTINCT p FROM Problem p " +
           "LEFT JOIN FETCH p.tags " +
           "WHERE p.id IN :ids")
    java.util.List<Problem> findWithTagsByIdIn(@Param("ids") java.util.Collection<Long> ids);

    @Query("SELECT DISTINCT p FROM Problem p " +
           "LEFT JOIN FETCH p.topics " +
           "WHERE p.slug = :slug AND p.status = :status")
    Optional<Problem> findWithTopicsBySlugAndStatus(@Param("slug") String slug, @Param("status") ProblemStatus status);

    @Query("SELECT DISTINCT p FROM Problem p " +
           "LEFT JOIN FETCH p.tags " +
           "WHERE p.slug = :slug AND p.status = :status")
    Optional<Problem> findWithTagsBySlugAndStatus(@Param("slug") String slug, @Param("status") ProblemStatus status);
}
