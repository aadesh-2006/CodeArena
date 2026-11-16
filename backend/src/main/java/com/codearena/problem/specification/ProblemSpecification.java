package com.codearena.problem.specification;

import com.codearena.problem.entity.Problem;
import com.codearena.problem.entity.ProblemDifficulty;
import com.codearena.problem.entity.ProblemStatus;
import com.codearena.problem.entity.Tag;
import com.codearena.problem.entity.Topic;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProblemSpecification {

    private ProblemSpecification() {
    }

    public static Specification<Problem> buildPublicDiscoverySpec(
            String search,
            ProblemDifficulty difficulty,
            String topicSlug,
            String tagSlug
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Security constraint: Only PUBLISHED problems are accessible in public discovery
            predicates.add(cb.equal(root.get("status"), ProblemStatus.PUBLISHED));

            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), searchPattern));
            }

            if (difficulty != null) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty));
            }

            if (topicSlug != null && !topicSlug.isBlank()) {
                Join<Problem, Topic> topicJoin = root.join("topics");
                predicates.add(cb.equal(cb.lower(topicJoin.get("slug")), topicSlug.trim().toLowerCase()));
                if (query != null) {
                    query.distinct(true);
                }
            }

            if (tagSlug != null && !tagSlug.isBlank()) {
                Join<Problem, Tag> tagJoin = root.join("tags");
                predicates.add(cb.equal(cb.lower(tagJoin.get("slug")), tagSlug.trim().toLowerCase()));
                if (query != null) {
                    query.distinct(true);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
