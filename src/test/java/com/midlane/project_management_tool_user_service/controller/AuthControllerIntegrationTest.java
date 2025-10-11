package com.midlane.project_management_tool_user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.midlane.project_management_tool_user_service.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🎯 INTEGRATION TEST TUTORIAL: AuthController Register Endpoint
 *
 * This demonstrates the key concepts of integration testing:
 *
 * 1. FULL CONTEXT LOADING: Uses @SpringBootTest to load entire application
 * 2. HTTP LAYER TESTING: Tests actual REST endpoints with MockMvc
 * 3. JSON REQUEST/RESPONSE: Tests complete serialization/deserialization
 * 4. VALIDATION TESTING: Verifies request validation works end-to-end
 * 5. ERROR HANDLING: Tests how errors are properly formatted and returned
 *
 * Integration vs Unit Tests:
 * - Unit Test: Tests individual components in isolation with mocks
 * - Integration Test: Tests multiple components working together
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("📚 AuthController Registration Integration Test Tutorial")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 🔧 SETUP: MockMvc allows us to test Spring MVC controllers
     * without starting a full HTTP server
     */
    private MockMvc createMockMvc() {
        return MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .alwaysDo(print()) // 📝 This prints request/response for debugging
                .build();
    }

    /**
     * 🎯 TEST CASE 1: Happy Path - Valid Registration
     *
     * This test demonstrates:
     * - Building test data with builder pattern
     * - JSON serialization using ObjectMapper
     * - HTTP POST request simulation
     * - Response validation using JSONPath
     * - Multiple assertion types
     */
    @Test
    @DisplayName("✅ Should successfully register user with valid data")
    void registerUser_WithValidData_ShouldReturnAuthResponse() throws Exception {
        MockMvc mockMvc = createMockMvc();

        // 🏗️ ARRANGE: Prepare test data
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("integration.test@example.com")
                .password("SecurePassword123!")
                .phone("+1234567890")
                .build();

        // Convert Java object to JSON string
        String requestJson = objectMapper.writeValueAsString(registerRequest);
        System.out.println("📤 Request JSON: " + requestJson);

        // 🚀 ACT & ASSERT: Execute request and verify response
        mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .header("User-Agent", "Integration-Test/1.0"))

                // ✅ HTTP Status Validation
                .andExpect(status().isOk())

                // ✅ Content Type Validation
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // ✅ Response Structure Validation using JSONPath
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", greaterThan(0)))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.userEmail", is("integration.test@example.com")))

                // ✅ Data Type Validation
                .andExpect(jsonPath("$.userId", instanceOf(Number.class)))
                .andExpect(jsonPath("$.expiresIn", instanceOf(Number.class)));

        System.out.println("✅ Test passed: User registration successful!");
    }

    /**
     * 🎯 TEST CASE 2: Validation Error - Missing Email
     *
     * This demonstrates:
     * - Testing validation annotations (@NotBlank, etc.)
     * - Error response structure validation
     * - HTTP 400 Bad Request handling
     */
    @Test
    @DisplayName("❌ Should return 400 when email is missing")
    void registerUser_WithMissingEmail_ShouldReturn400() throws Exception {
        MockMvc mockMvc = createMockMvc();

        // 🏗️ ARRANGE: Create invalid request (missing email)
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .password("SecurePassword123!")
                .phone("+1234567890")
                // email is intentionally missing
                .build();

        String requestJson = objectMapper.writeValueAsString(invalidRequest);
        System.out.println("📤 Invalid Request JSON: " + requestJson);

        // 🚀 ACT & ASSERT: Expect validation error
        mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

                // ✅ Should return HTTP 400 Bad Request
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // ✅ Error response should have proper structure
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.message", containsStringIgnoringCase("email")));

        System.out.println("✅ Test passed: Validation error handled correctly!");
    }

    /**
     * 🎯 TEST CASE 3: Malformed JSON Error
     *
     * This demonstrates:
     * - Testing JSON parsing errors
     * - HTTP 400 Bad Request for malformed data
     */
    @Test
    @DisplayName("❌ Should handle malformed JSON gracefully")
    void registerUser_WithMalformedJson_ShouldReturn400() throws Exception {
        MockMvc mockMvc = createMockMvc();

        // 🏗️ ARRANGE: Create malformed JSON
        String malformedJson = "{ \"email\": \"test@example.com\", \"password\": }"; // Missing value
        System.out.println("📤 Malformed JSON: " + malformedJson);

        // 🚀 ACT & ASSERT: Should handle gracefully
        mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))

                .andExpect(status().isBadRequest());

        System.out.println("✅ Test passed: Malformed JSON handled correctly!");
    }

    /**
     * 🎯 TEST CASE 4: Content Type Error
     *
     * This demonstrates:
     * - Testing wrong Content-Type header
     * - HTTP 415 Unsupported Media Type
     */
    @Test
    @DisplayName("❌ Should return 415 for wrong content type")
    void registerUser_WithWrongContentType_ShouldReturn415() throws Exception {
        MockMvc mockMvc = createMockMvc();

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("SecurePassword123!")
                .phone("+1234567890")
                .build();

        String requestJson = objectMapper.writeValueAsString(registerRequest);

        // 🚀 ACT & ASSERT: Send with wrong content type
        mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.TEXT_PLAIN) // ❌ Wrong content type
                .content(requestJson))

                .andExpect(status().isUnsupportedMediaType());

        System.out.println("✅ Test passed: Wrong content type rejected!");
    }

    /**
     * 🎯 TEST CASE 5: HTTP Headers Validation
     *
     * This demonstrates:
     * - Testing HTTP response headers
     * - Verifying proper content type in response
     */
    @Test
    @DisplayName("✅ Should include proper response headers")
    void registerUser_ShouldIncludeProperHeaders() throws Exception {
        MockMvc mockMvc = createMockMvc();

        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("headers.test@example.com")
                .password("SecurePassword123!")
                .phone("+1234567890")
                .build();

        String requestJson = objectMapper.writeValueAsString(registerRequest);

        // 🚀 ACT & ASSERT: Verify response headers
        mockMvc.perform(post("/api/auth/initial/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))

                .andExpect(status().isOk())

                // ✅ Header validations
                .andExpect(header().string("Content-Type", containsString("application/json")))
                .andExpect(header().exists("Date"));

        System.out.println("✅ Test passed: Response headers are correct!");
    }
}

/**
 * 📚 INTEGRATION TESTING BEST PRACTICES SUMMARY:
 *
 * 1. 🎯 TEST REAL SCENARIOS: Test actual user workflows, not just individual methods
 *
 * 2. 🏗️ USE BUILDER PATTERN: Create test data with builders for readability
 *
 * 3. 📝 DESCRIPTIVE TEST NAMES: Use clear, behavior-driven test names
 *
 * 4. ✅ MULTIPLE ASSERTIONS: Test status, headers, and response body structure
 *
 * 5. 🔍 JSONPath VALIDATION: Use JSONPath expressions to validate JSON responses
 *
 * 6. ❌ TEST ERROR CASES: Include negative test cases for validation and errors
 *
 * 7. 🚀 SIMULATE REAL REQUESTS: Use proper HTTP headers and content types
 *
 * 8. 📊 VERIFY DATA FLOW: In real integration tests, check database state too
 *
 * 9. 🧹 CLEAN SETUP: Use @Transactional or manual cleanup between tests
 *
 * 10. 📈 COVERAGE: Test happy path, validation errors, and edge cases
 *
 * 🚀 HOW TO RUN THESE TESTS:
 * - Command Line: ./mvnw test -Dtest=AuthControllerIntegrationTest
 * - IDE: Right-click on class/method and "Run Test"
 * - Maven: mvn test (runs all tests)
 */
