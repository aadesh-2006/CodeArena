package com.codearena.admin.controller;

import com.codearena.auth.security.JwtTokenService;
import com.codearena.problem.dto.CreateProblemRequest;
import com.codearena.problem.dto.ProblemExampleDto;
import com.codearena.problem.dto.UpdateProblemRequest;
import com.codearena.problem.entity.*;
import com.codearena.problem.repository.ProblemRepository;
import com.codearena.problem.repository.TagRepository;
import com.codearena.problem.repository.TopicRepository;
import com.codearena.user.entity.User;
import com.codearena.user.entity.UserRole;
import com.codearena.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminProblemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    private String adminToken;
    private String userToken;
    private Topic savedTopic;
    private Tag savedTag;

    @BeforeEach
    void setUp() {
        problemRepository.deleteAll();
        topicRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(new User(
                "admin_super",
                "admin@codearena.dev",
                "Admin Boss",
                passwordEncoder.encode("AdminPass123!"),
                UserRole.ADMIN
        ));
        adminToken = jwtTokenService.generateToken(admin);

        User normalUser = userRepository.save(new User(
                "coder_alice",
                "alice@codearena.dev",
                "Alice",
                passwordEncoder.encode("UserPass123!"),
                UserRole.USER
        ));
        userToken = jwtTokenService.generateToken(normalUser);

        savedTopic = topicRepository.save(new Topic("Arrays", "arrays", "Arrays category"));
        savedTag = tagRepository.save(new Tag("Hash Table", "hash-table"));
    }

    @Test
    @DisplayName("POST /api/admin/problems - Admin can successfully create problem")
    void createProblem_asAdmin_shouldSucceed() throws Exception {
        CreateProblemRequest request = new CreateProblemRequest(
                "Two Sum",
                "two-sum",
                "Find indices that add up to target.",
                ProblemDifficulty.EASY,
                "2 <= nums.length <= 10^4",
                "Array nums, int target",
                "Indices [i, j]",
                "Use a map for O(n).",
                false,
                2000,
                256,
                ProblemType.ALGORITHMIC,
                Set.of(savedTopic.getId()),
                Set.of(savedTag.getId()),
                List.of(new ProblemExampleDto(null, "[2,7,11,15], 9", "[0,1]", "2 + 7 = 9", 1)),
                null
        );

        mockMvc.perform(post("/api/admin/problems")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("two-sum"))
                .andExpect(jsonPath("$.data.difficulty").value("EASY"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.topics[0].slug").value("arrays"))
                .andExpect(jsonPath("$.data.tags[0].slug").value("hash-table"))
                .andExpect(jsonPath("$.data.examples").isArray())
                .andExpect(jsonPath("$.data.testCases").doesNotExist()); // Private test cases never exposed

        assertThat(problemRepository.existsBySlug("two-sum")).isTrue();
    }

    @Test
    @DisplayName("POST /api/admin/problems - USER role is forbidden (403)")
    void createProblem_asUser_shouldBeForbidden() throws Exception {
        CreateProblemRequest request = new CreateProblemRequest(
                "Hack Problem",
                "hack-problem",
                "Statement",
                ProblemDifficulty.HARD,
                "Const",
                "In",
                "Out",
                null, false, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/admin/problems")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        assertThat(problemRepository.existsBySlug("hack-problem")).isFalse();
    }

    @Test
    @DisplayName("POST /api/admin/problems - Unauthenticated request is rejected (401)")
    void createProblem_unauthenticated_shouldBeUnauthorized() throws Exception {
        CreateProblemRequest request = new CreateProblemRequest(
                "Anonymous Problem",
                "anon-problem",
                "Statement",
                ProblemDifficulty.EASY,
                "Const",
                "In",
                "Out",
                null, false, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/admin/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/admin/problems/{id} - Admin can update problem details")
    void updateProblem_asAdmin_shouldSucceed() throws Exception {
        Problem problem = problemRepository.save(new Problem(
                "initial-slug",
                "Initial Title",
                "Initial Statement",
                ProblemDifficulty.EASY,
                "Const",
                "In",
                "Out"
        ));

        UpdateProblemRequest request = new UpdateProblemRequest(
                "Updated Title",
                "updated-slug",
                "Updated Statement",
                ProblemDifficulty.MEDIUM,
                "Updated Const",
                "Updated In",
                "Updated Out",
                "Editorial",
                true,
                1500,
                512,
                ProblemType.ALGORITHMIC,
                Set.of(savedTopic.getId()),
                null,
                List.of(new ProblemExampleDto(null, "sample in", "sample out", "sample expl", 1)),
                null
        );

        mockMvc.perform(put("/api/admin/problems/" + problem.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("updated-slug"))
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.difficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.data.timeLimitMs").value(1500))
                .andExpect(jsonPath("$.data.examples").isArray());

        Problem updated = problemRepository.findById(problem.getId()).orElseThrow();
        assertThat(updated.getSlug()).isEqualTo("updated-slug");
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("PUT /api/admin/problems/{id} - USER role is forbidden (403)")
    void updateProblem_asUser_shouldBeForbidden() throws Exception {
        Problem problem = problemRepository.save(new Problem("slug", "Title", "Stmt", ProblemDifficulty.EASY, "C", "I", "O"));

        UpdateProblemRequest request = new UpdateProblemRequest(
                "Hacked Title", "slug", "Stmt", ProblemDifficulty.HARD, "C", "I", "O",
                null, false, null, null, null, null, null, null, null
        );

        mockMvc.perform(put("/api/admin/problems/" + problem.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/problems/{id}/publish - Admin can publish complete problem")
    void publishProblem_asAdmin_shouldSucceed() throws Exception {
        Problem problem = new Problem("ready-problem", "Ready Title", "Statement", ProblemDifficulty.EASY, "Constraints", "Input", "Output");
        problem.addExample(new ProblemExample("in", "out", "expl", 1));
        Problem saved = problemRepository.save(problem);

        mockMvc.perform(post("/api/admin/problems/" + saved.getId() + "/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        Problem published = problemRepository.findById(saved.getId()).orElseThrow();
        assertThat(published.getStatus()).isEqualTo(ProblemStatus.PUBLISHED);
    }

    @Test
    @DisplayName("POST /api/admin/problems/{id}/publish - Fails when problem has no examples")
    void publishProblem_incompleteWithoutExamples_returnsBadRequest() throws Exception {
        Problem problem = problemRepository.save(new Problem("no-ex", "No Examples", "Statement", ProblemDifficulty.EASY, "Constraints", "Input", "Output"));

        mockMvc.perform(post("/api/admin/problems/" + problem.getId() + "/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot publish problem without at least one example test case."));

        Problem unchanged = problemRepository.findById(problem.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(ProblemStatus.DRAFT);
    }

    @Test
    @DisplayName("POST /api/admin/problems/{id}/archive - Admin can archive problem")
    void archiveProblem_asAdmin_shouldSucceed() throws Exception {
        Problem problem = new Problem("to-be-archived", "Archive Title", "Statement", ProblemDifficulty.EASY, "Constraints", "Input", "Output");
        problem.setStatus(ProblemStatus.PUBLISHED);
        Problem saved = problemRepository.save(problem);

        mockMvc.perform(post("/api/admin/problems/" + saved.getId() + "/archive")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));

        Problem archived = problemRepository.findById(saved.getId()).orElseThrow();
        assertThat(archived.getStatus()).isEqualTo(ProblemStatus.ARCHIVED);
    }

    @Test
    @DisplayName("POST /api/admin/problems - Duplicate slug returns 409 Conflict")
    void createProblem_duplicateSlug_returnsConflict() throws Exception {
        problemRepository.save(new Problem("existing-slug", "Title 1", "S", ProblemDifficulty.EASY, "C", "I", "O"));

        CreateProblemRequest request = new CreateProblemRequest(
                "Title 2",
                "existing-slug",
                "Statement",
                ProblemDifficulty.MEDIUM,
                "Constraints",
                "Input",
                "Output",
                null, false, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/admin/problems")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Problem with slug 'existing-slug' already exists"));
    }
}
