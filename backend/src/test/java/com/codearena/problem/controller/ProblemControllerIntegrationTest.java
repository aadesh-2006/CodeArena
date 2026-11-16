package com.codearena.problem.controller;

import com.codearena.problem.entity.*;
import com.codearena.problem.repository.ProblemRepository;
import com.codearena.problem.repository.TagRepository;
import com.codearena.problem.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProblemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TagRepository tagRepository;

    private Topic arraysTopic;
    private Topic graphsTopic;
    private Tag hashTag;
    private Tag treeTag;

    private Problem publishedProblem1;
    private Problem publishedProblem2;
    private Problem draftProblem;
    private Problem archivedProblem;

    @BeforeEach
    void setUp() {
        problemRepository.deleteAll();
        topicRepository.deleteAll();
        tagRepository.deleteAll();

        arraysTopic = topicRepository.save(new Topic("Arrays", "arrays", "Array manipulation problems"));
        graphsTopic = topicRepository.save(new Topic("Graphs", "graphs", "Graph algorithms"));

        hashTag = tagRepository.save(new Tag("Hash Table", "hash-table"));
        treeTag = tagRepository.save(new Tag("Binary Tree", "binary-tree"));

        // 1. Published problem with published editorial
        publishedProblem1 = new Problem(
                "two-sum",
                "Two Sum",
                "Find two numbers adding to target.",
                ProblemDifficulty.EASY,
                "2 <= nums.length <= 10^4",
                "Array nums, int target",
                "Indices [i, j]"
        );
        publishedProblem1.setEditorial("Use a hash map to track complements in O(n).");
        publishedProblem1.setEditorialPublished(true);
        publishedProblem1.setTimeLimitMs(2000);
        publishedProblem1.setMemoryLimitMb(256);
        publishedProblem1.setProblemType(ProblemType.ALGORITHMIC);
        publishedProblem1.setStatus(ProblemStatus.PUBLISHED);
        publishedProblem1.setTopics(Set.of(arraysTopic));
        publishedProblem1.setTags(Set.of(hashTag));

        publishedProblem1.addExample(new ProblemExample("nums = [2,7,11,15], target = 9", "[0,1]", "Because nums[0] + nums[1] == 9", 1));
        publishedProblem1.addHint(new ProblemHint("Can you use a hash map?", 1));
        publishedProblem1 = problemRepository.save(publishedProblem1);

        // 2. Published problem with UNPUBLISHED editorial
        publishedProblem2 = new Problem(
                "course-schedule",
                "Course Schedule",
                "Determine if you can finish all courses.",
                ProblemDifficulty.MEDIUM,
                "1 <= numCourses <= 2000",
                "int numCourses, int[][] prerequisites",
                "true or false"
        );
        publishedProblem2.setEditorial("Topological sort using Kahn's algorithm or DFS cycle detection.");
        publishedProblem2.setEditorialPublished(false);
        publishedProblem2.setTimeLimitMs(2000);
        publishedProblem2.setMemoryLimitMb(256);
        publishedProblem2.setProblemType(ProblemType.ALGORITHMIC);
        publishedProblem2.setStatus(ProblemStatus.PUBLISHED);
        publishedProblem2.setTopics(Set.of(graphsTopic));
        publishedProblem2 = problemRepository.save(publishedProblem2);

        // 3. Draft problem
        draftProblem = new Problem(
                "draft-enigma",
                "Draft Enigma",
                "Incomplete draft description.",
                ProblemDifficulty.HARD,
                "N <= 100",
                "int n",
                "int"
        );
        draftProblem.setEditorial("Secret draft editorial");
        draftProblem.setEditorialPublished(false);
        draftProblem.setTimeLimitMs(1000);
        draftProblem.setMemoryLimitMb(128);
        draftProblem.setProblemType(ProblemType.ALGORITHMIC);
        draftProblem.setStatus(ProblemStatus.DRAFT);
        draftProblem = problemRepository.save(draftProblem);

        // 4. Archived problem
        archivedProblem = new Problem(
                "archived-oldie",
                "Archived Oldie",
                "Old deprecated problem.",
                ProblemDifficulty.MEDIUM,
                "N <= 10",
                "int n",
                "int"
        );
        archivedProblem.setEditorial("Archived editorial");
        archivedProblem.setEditorialPublished(true);
        archivedProblem.setTimeLimitMs(1000);
        archivedProblem.setMemoryLimitMb(128);
        archivedProblem.setProblemType(ProblemType.ALGORITHMIC);
        archivedProblem.setStatus(ProblemStatus.ARCHIVED);
        archivedProblem = problemRepository.save(archivedProblem);
    }

    @Test
    @DisplayName("GET /api/problems - Anonymous user can retrieve published problems list")
    void getProblems_anonymous_returnsOnlyPublished() throws Exception {
        mockMvc.perform(get("/api/problems")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].slug", containsInAnyOrder("two-sum", "course-schedule")))
                .andExpect(jsonPath("$.data.content[*].slug", not(hasItem("draft-enigma"))))
                .andExpect(jsonPath("$.data.content[*].slug", not(hasItem("archived-oldie"))));
    }

    @Test
    @DisplayName("GET /api/problems - Search filter matches problem title case-insensitively")
    void getProblems_filterBySearch() throws Exception {
        mockMvc.perform(get("/api/problems")
                        .param("search", "two")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].slug").value("two-sum"))
                .andExpect(jsonPath("$.data.content[0].title").value("Two Sum"));
    }

    @Test
    @DisplayName("GET /api/problems - Filter by difficulty")
    void getProblems_filterByDifficulty() throws Exception {
        mockMvc.perform(get("/api/problems")
                        .param("difficulty", "EASY")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].slug").value("two-sum"))
                .andExpect(jsonPath("$.data.content[0].difficulty").value("EASY"));
    }

    @Test
    @DisplayName("GET /api/problems - Filter by topic slug")
    void getProblems_filterByTopic() throws Exception {
        mockMvc.perform(get("/api/problems")
                        .param("topic", "graphs")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].slug").value("course-schedule"));
    }

    @Test
    @DisplayName("GET /api/problems - Filter by tag slug")
    void getProblems_filterByTag() throws Exception {
        mockMvc.perform(get("/api/problems")
                        .param("tag", "hash-table")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].slug").value("two-sum"));
    }

    @Test
    @DisplayName("GET /api/problems - Reject invalid pagination parameters")
    void getProblems_invalidPagination_returns400() throws Exception {
        mockMvc.perform(get("/api/problems")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/problems")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/problems")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/problems - Reject invalid sort parameters")
    void getProblems_invalidSort_returns400() throws Exception {
        mockMvc.perform(get("/api/problems")
                        .param("sortBy", "unauthorizedField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/problems")
                        .param("sortDirection", "BACKWARDS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/problems/{slug} - Returns published problem details with published editorial")
    void getProblemBySlug_publishedWithEditorial_returns200() throws Exception {
        mockMvc.perform(get("/api/problems/two-sum")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Two Sum"))
                .andExpect(jsonPath("$.data.slug").value("two-sum"))
                .andExpect(jsonPath("$.data.difficulty").value("EASY"))
                .andExpect(jsonPath("$.data.editorial").value("Use a hash map to track complements in O(n)."))
                .andExpect(jsonPath("$.data.examples", hasSize(1)))
                .andExpect(jsonPath("$.data.hints", hasSize(1)))
                .andExpect(jsonPath("$.data.topics", hasSize(1)))
                .andExpect(jsonPath("$.data.tags", hasSize(1)))
                // Verify private evaluation test cases are NEVER exposed
                .andExpect(jsonPath("$.data.testCases").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/problems/{slug} - Editorial is null when editorialPublished is false")
    void getProblemBySlug_unpublishedEditorial_hidesEditorial() throws Exception {
        mockMvc.perform(get("/api/problems/course-schedule")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("course-schedule"))
                .andExpect(jsonPath("$.data.editorial").value(nullValue()));
    }

    @Test
    @DisplayName("GET /api/problems/{slug} - Returns 404 for DRAFT problem without leaking existence")
    void getProblemBySlug_draft_returns404() throws Exception {
        mockMvc.perform(get("/api/problems/draft-enigma")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Problem not found")));
    }

    @Test
    @DisplayName("GET /api/problems/{slug} - Returns 404 for ARCHIVED problem without leaking existence")
    void getProblemBySlug_archived_returns404() throws Exception {
        mockMvc.perform(get("/api/problems/archived-oldie")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Problem not found")));
    }

    @Test
    @DisplayName("GET /api/problems/{slug} - Returns 404 for nonexistent problem")
    void getProblemBySlug_nonexistent_returns404() throws Exception {
        mockMvc.perform(get("/api/problems/does-not-exist")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/topics - Returns distinct topics that have published problems")
    void getTopics_returnsPublishedTopics() throws Exception {
        mockMvc.perform(get("/api/topics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].slug", containsInAnyOrder("arrays", "graphs")));
    }

    @Test
    @DisplayName("GET /api/tags - Returns distinct tags that have published problems")
    void getTags_returnsPublishedTags() throws Exception {
        mockMvc.perform(get("/api/tags")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].slug").value("hash-table"));
    }

    @Test
    @DisplayName("GET /api/problems - Topic and Tag filters evaluate with strict AND semantics")
    void getProblems_filterByTopicAndTag_andSemantics() throws Exception {
        // publishedProblem1 has topic "arrays" AND tag "hash-table"
        // publishedProblem2 has topic "graphs" and NO tags
        // Matching both topic and tag should return ONLY publishedProblem1
        mockMvc.perform(get("/api/problems")
                        .param("topic", "arrays")
                        .param("tag", "hash-table")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].slug").value("two-sum"));

        // Searching for topic "graphs" with tag "hash-table" should return empty because no problem satisfies BOTH
        mockMvc.perform(get("/api/problems")
                        .param("topic", "graphs")
                        .param("tag", "hash-table")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/problems - Preserves requested sort order even when IDs are inverted")
    void getProblems_sortingPreservesOrderAcrossInClause() throws Exception {
        // Clean out existing and insert 3 problems with titles where natural sort order is opposite insertion order
        problemRepository.deleteAll();

        // 1. First inserted -> smallest ID
        Problem p1 = new Problem("zeta-task", "Zeta Task", "Statement", ProblemDifficulty.EASY, "c", "i", "o");
        p1.setStatus(ProblemStatus.PUBLISHED);
        problemRepository.save(p1);

        // 2. Second inserted -> middle ID
        Problem p2 = new Problem("beta-task", "Beta Task", "Statement", ProblemDifficulty.MEDIUM, "c", "i", "o");
        p2.setStatus(ProblemStatus.PUBLISHED);
        problemRepository.save(p2);

        // 3. Third inserted -> largest ID
        Problem p3 = new Problem("alpha-task", "Alpha Task", "Statement", ProblemDifficulty.HARD, "c", "i", "o");
        p3.setStatus(ProblemStatus.PUBLISHED);
        problemRepository.save(p3);

        // When requesting sortBy=title&sortDirection=ASC, expected order is Alpha, Beta, Zeta
        // Their IDs are: Alpha (highest), Beta (middle), Zeta (lowest)
        // SQL IN (...) typically returns [Zeta, Beta, Alpha] if not restored
        mockMvc.perform(get("/api/problems")
                        .param("sortBy", "title")
                        .param("sortDirection", "ASC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].slug").value("alpha-task"))
                .andExpect(jsonPath("$.data.content[1].slug").value("beta-task"))
                .andExpect(jsonPath("$.data.content[2].slug").value("zeta-task"));
    }

    @Test
    @DisplayName("GET /api/problems - Empty search results return valid pagination metadata")
    void getProblems_emptyResult_returnsCorrectPaginationMetadata() throws Exception {
        mockMvc.perform(get("/api/problems")
                        .param("search", "nonexistent_query_xyz")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.hasPrevious").value(false))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/topics and /api/tags - strictly hide draft-only, archived-only, and unassigned metadata")
    void getTopicsAndTags_hidesDraftOnlyArchivedOnlyAndUnassignedMetadata() throws Exception {
        // Create draft-only topic & tag
        Topic draftTopic = topicRepository.save(new Topic("Draft Only Topic", "draft-topic", "desc"));
        Tag draftTag = tagRepository.save(new Tag("Draft Only Tag", "draft-tag"));
        draftProblem.setTopics(Set.of(draftTopic));
        draftProblem.setTags(Set.of(draftTag));
        problemRepository.save(draftProblem);

        // Create archived-only topic & tag
        Topic archivedTopic = topicRepository.save(new Topic("Archived Only Topic", "archived-topic", "desc"));
        Tag archivedTag = tagRepository.save(new Tag("Archived Only Tag", "archived-tag"));
        archivedProblem.setTopics(Set.of(archivedTopic));
        archivedProblem.setTags(Set.of(archivedTag));
        problemRepository.save(archivedProblem);

        // Create unassigned topic & tag
        topicRepository.save(new Topic("Unassigned Topic", "unassigned-topic", "desc"));
        tagRepository.save(new Tag("Unassigned Tag", "unassigned-tag"));

        // Verify /api/topics only returns the published topics ("arrays" and "graphs")
        mockMvc.perform(get("/api/topics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].slug", containsInAnyOrder("arrays", "graphs")))
                .andExpect(jsonPath("$.data[*].slug", not(hasItem("draft-topic"))))
                .andExpect(jsonPath("$.data[*].slug", not(hasItem("archived-topic"))))
                .andExpect(jsonPath("$.data[*].slug", not(hasItem("unassigned-topic"))));

        // Verify /api/tags only returns published tags ("hash-table")
        mockMvc.perform(get("/api/tags")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].slug").value("hash-table"))
                .andExpect(jsonPath("$.data[*].slug", not(hasItem("draft-tag"))))
                .andExpect(jsonPath("$.data[*].slug", not(hasItem("archived-tag"))))
                .andExpect(jsonPath("$.data[*].slug", not(hasItem("unassigned-tag"))));
    }

    @Test
    @DisplayName("GET /api/problems - Problem with multiple topics and tags does not duplicate in pagination count or content")
    void getProblems_multipleJoinedTopicsAndTags_noDuplicateProblems() throws Exception {
        // Add more topics and tags to publishedProblem1
        Topic dpTopic = topicRepository.save(new Topic("Dynamic Programming", "dp", "desc"));
        Tag bitwiseTag = tagRepository.save(new Tag("Bitwise", "bitwise"));

        publishedProblem1.setTopics(Set.of(arraysTopic, dpTopic));
        publishedProblem1.setTags(Set.of(hashTag, bitwiseTag));
        problemRepository.save(publishedProblem1);

        // Filter by topic "arrays" where the problem also has another topic and multiple tags
        mockMvc.perform(get("/api/problems")
                        .param("topic", "arrays")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].slug").value("two-sum"))
                .andExpect(jsonPath("$.data.content[0].topics", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].tags", hasSize(2)));
    }
}
