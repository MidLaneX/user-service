package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.SocialUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialAuthService Unit Tests")
class SocialAuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SocialAuthService socialAuthService;

    private String testGoogleClientId = "test-google-client-id";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(socialAuthService, "googleClientId", testGoogleClientId);
    }

    @Test
    @DisplayName("Should get Google user info from ID token successfully")
    void getUserInfo_GoogleIdToken_Success() {
        // Given
        String provider = "google";
        String idToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature";
        
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("sub", "google123");
        tokenInfo.put("email", "test@gmail.com");
        tokenInfo.put("given_name", "John");
        tokenInfo.put("family_name", "Doe");
        tokenInfo.put("picture", "https://example.com/photo.jpg");
        tokenInfo.put("email_verified", true);
        tokenInfo.put("aud", testGoogleClientId);

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(tokenInfo));

        // When
        SocialUserInfo result = socialAuthService.getUserInfo(provider, idToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("google123");
        assertThat(result.getEmail()).isEqualTo("test@gmail.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getProfilePictureUrl()).isEqualTo("https://example.com/photo.jpg");
        assertThat(result.getProvider()).isEqualTo("google");
        assertThat(result.isEmailVerified()).isTrue();

        verify(restTemplate).getForEntity(contains("tokeninfo"), eq(Map.class));
    }

    @Test
    @DisplayName("Should get Google user info from access token successfully")
    void getUserInfo_GoogleAccessToken_Success() {
        // Given
        String provider = "google";
        String accessToken = "ya29.access_token";
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", "google123");
        userInfo.put("email", "test@gmail.com");
        userInfo.put("given_name", "John");
        userInfo.put("family_name", "Doe");
        userInfo.put("picture", "https://example.com/photo.jpg");
        userInfo.put("verified_email", true);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(userInfo));

        // When
        SocialUserInfo result = socialAuthService.getUserInfo(provider, accessToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("google123");
        assertThat(result.getEmail()).isEqualTo("test@gmail.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getProfilePictureUrl()).isEqualTo("https://example.com/photo.jpg");
        assertThat(result.getProvider()).isEqualTo("google");
        assertThat(result.isEmailVerified()).isTrue();

        verify(restTemplate).exchange(
                eq("https://www.googleapis.com/oauth2/v2/userinfo"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    @DisplayName("Should reject Google ID token with wrong audience")
    void getUserInfo_GoogleIdTokenWrongAudience_ThrowsException() {
        // Given
        String provider = "google";
        String idToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature";
        
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("sub", "google123");
        tokenInfo.put("email", "test@gmail.com");
        tokenInfo.put("aud", "wrong-client-id");

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(tokenInfo));

        // When & Then
        assertThatThrownBy(() -> socialAuthService.getUserInfo(provider, idToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ID token audience does not match client ID");
    }

    @Test
    @DisplayName("Should handle invalid Google ID token")
    void getUserInfo_InvalidGoogleIdToken_ThrowsException() {
        // Given
        String provider = "google";
        String idToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.signature";
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "invalid_token");
        errorResponse.put("error_description", "Invalid token");

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(errorResponse));

        // When & Then
        assertThatThrownBy(() -> socialAuthService.getUserInfo(provider, idToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid Google ID token");
    }

    @Test
    @DisplayName("Should get Facebook user info successfully")
    void getUserInfo_Facebook_Success() {
        // Given
        String provider = "facebook";
        String accessToken = "facebook_access_token";
        
        Map<String, Object> pictureData = new HashMap<>();
        pictureData.put("url", "https://facebook.com/photo.jpg");
        
        Map<String, Object> picture = new HashMap<>();
        picture.put("data", pictureData);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", "facebook123");
        userInfo.put("email", "test@facebook.com");
        userInfo.put("first_name", "Jane");
        userInfo.put("last_name", "Smith");
        userInfo.put("picture", picture);

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(userInfo));

        // When
        SocialUserInfo result = socialAuthService.getUserInfo(provider, accessToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("facebook123");
        assertThat(result.getEmail()).isEqualTo("test@facebook.com");
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getProfilePictureUrl()).isEqualTo("https://facebook.com/photo.jpg");
        assertThat(result.getProvider()).isEqualTo("facebook");
        assertThat(result.isEmailVerified()).isTrue();

        verify(restTemplate).getForEntity(contains("graph.facebook.com"), eq(Map.class));
    }

    @Test
    @DisplayName("Should handle Facebook user info without picture")
    void getUserInfo_FacebookNoPicture_Success() {
        // Given
        String provider = "facebook";
        String accessToken = "facebook_access_token";
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", "facebook123");
        userInfo.put("email", "test@facebook.com");
        userInfo.put("first_name", "Jane");
        userInfo.put("last_name", "Smith");

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(userInfo));

        // When
        SocialUserInfo result = socialAuthService.getUserInfo(provider, accessToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProfilePictureUrl()).isNull();
        assertThat(result.isEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("Should handle Facebook user info without email")
    void getUserInfo_FacebookNoEmail_Success() {
        // Given
        String provider = "facebook";
        String accessToken = "facebook_access_token";
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", "facebook123");
        userInfo.put("first_name", "Jane");
        userInfo.put("last_name", "Smith");

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(userInfo));

        // When
        SocialUserInfo result = socialAuthService.getUserInfo(provider, accessToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isNull();
        assertThat(result.isEmailVerified()).isFalse();
    }

    @Test
    @DisplayName("Should throw exception for unsupported provider")
    void getUserInfo_UnsupportedProvider_ThrowsException() {
        // Given
        String provider = "twitter";
        String accessToken = "twitter_access_token";

        // When & Then
        assertThatThrownBy(() -> socialAuthService.getUserInfo(provider, accessToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unsupported social provider: twitter");
    }

    @Test
    @DisplayName("Should handle Google API error")
    void getUserInfo_GoogleApiError_ThrowsException() {
        // Given
        String provider = "google";
        String accessToken = "invalid_access_token";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When & Then
        assertThatThrownBy(() -> socialAuthService.getUserInfo(provider, accessToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to get user info from Google access token");
    }

    @Test
    @DisplayName("Should handle Facebook API error")
    void getUserInfo_FacebookApiError_ThrowsException() {
        // Given
        String provider = "facebook";
        String accessToken = "invalid_access_token";

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("API Error"));

        // When & Then
        assertThatThrownBy(() -> socialAuthService.getUserInfo(provider, accessToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to get user info from Facebook");
    }

    @Test
    @DisplayName("Should handle null response from Google")
    void getUserInfo_GoogleNullResponse_ThrowsException() {
        // Given
        String provider = "google";
        String accessToken = "access_token";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));

        // When & Then
        assertThatThrownBy(() -> socialAuthService.getUserInfo(provider, accessToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to get user info from Google");
    }

    @Test
    @DisplayName("Should handle null response from Facebook")
    void getUserInfo_FacebookNullResponse_ThrowsException() {
        // Given
        String provider = "facebook";
        String accessToken = "access_token";

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));

        // When & Then
        assertThatThrownBy(() -> socialAuthService.getUserInfo(provider, accessToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to get user info from Facebook");
    }
}
