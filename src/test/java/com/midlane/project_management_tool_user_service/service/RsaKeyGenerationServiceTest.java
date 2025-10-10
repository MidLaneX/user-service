package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.util.RsaKeyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RsaKeyGenerationService Unit Tests")
class RsaKeyGenerationServiceTest {

    @Mock
    private RsaKeyUtil rsaKeyUtil;

    @InjectMocks
    private RsaKeyGenerationService rsaKeyGenerationService;

    @TempDir
    Path tempDir;

    private KeyPair testKeyPair;
    private String testPrivateKeyPem;
    private String testPublicKeyPem;

    @BeforeEach
    void setUp() throws Exception {
        // Create test key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        testKeyPair = keyPairGenerator.generateKeyPair();
        
        testPrivateKeyPem = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...";
        testPublicKeyPem = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr5Z...";

        // Set the temporary directory as the key store path
        ReflectionTestUtils.setField(rsaKeyGenerationService, "keyStorePath", tempDir.toString());
    }

    @Test
    @DisplayName("Should generate new key pair when keys don't exist")
    void run_NoKeysExist_GeneratesNewKeyPair() throws Exception {
        // Given
        when(rsaKeyUtil.generateKeyPair()).thenReturn(testKeyPair);
        when(rsaKeyUtil.encodePrivateKey(testKeyPair.getPrivate())).thenReturn(testPrivateKeyPem);
        when(rsaKeyUtil.encodePublicKey(testKeyPair.getPublic())).thenReturn(testPublicKeyPem);

        // When
        rsaKeyGenerationService.run();

        // Then
        verify(rsaKeyUtil).generateKeyPair();
        verify(rsaKeyUtil).encodePrivateKey(testKeyPair.getPrivate());
        verify(rsaKeyUtil).encodePublicKey(testKeyPair.getPublic());

        // Verify files were created
        File privateKeyFile = new File(tempDir.toFile(), "private_key.pem");
        File publicKeyFile = new File(tempDir.toFile(), "public_key.pem");
        
        assertThat(privateKeyFile).exists();
        assertThat(publicKeyFile).exists();

        // Verify file contents
        String privateKeyContent = Files.readString(privateKeyFile.toPath());
        String publicKeyContent = Files.readString(publicKeyFile.toPath());
        
        assertThat(privateKeyContent).contains("-----BEGIN PRIVATE KEY-----");
        assertThat(privateKeyContent).contains("-----END PRIVATE KEY-----");
        assertThat(privateKeyContent).contains(testPrivateKeyPem);
        
        assertThat(publicKeyContent).contains("-----BEGIN PUBLIC KEY-----");
        assertThat(publicKeyContent).contains("-----END PUBLIC KEY-----");
        assertThat(publicKeyContent).contains(testPublicKeyPem);
    }

    @Test
    @DisplayName("Should not generate keys when they already exist")
    void run_KeysAlreadyExist_DoesNotGenerateNewKeys() throws Exception {
        // Given - Create existing key files
        File privateKeyFile = new File(tempDir.toFile(), "private_key.pem");
        File publicKeyFile = new File(tempDir.toFile(), "public_key.pem");
        
        Files.writeString(privateKeyFile.toPath(), "-----BEGIN PRIVATE KEY-----\nexisting_private_key\n-----END PRIVATE KEY-----\n");
        Files.writeString(publicKeyFile.toPath(), "-----BEGIN PUBLIC KEY-----\nexisting_public_key\n-----END PUBLIC KEY-----\n");

        // When
        rsaKeyGenerationService.run();

        // Then
        verify(rsaKeyUtil, never()).generateKeyPair();
        verify(rsaKeyUtil, never()).encodePrivateKey(any());
        verify(rsaKeyUtil, never()).encodePublicKey(any());

        // Verify files still exist with original content
        String privateKeyContent = Files.readString(privateKeyFile.toPath());
        String publicKeyContent = Files.readString(publicKeyFile.toPath());
        
        assertThat(privateKeyContent).contains("existing_private_key");
        assertThat(publicKeyContent).contains("existing_public_key");
    }

    @Test
    @DisplayName("Should generate keys when only private key exists")
    void run_OnlyPrivateKeyExists_GeneratesNewKeyPair() throws Exception {
        // Given - Create only private key file
        File privateKeyFile = new File(tempDir.toFile(), "private_key.pem");
        Files.writeString(privateKeyFile.toPath(), "-----BEGIN PRIVATE KEY-----\nexisting_private_key\n-----END PRIVATE KEY-----\n");

        when(rsaKeyUtil.generateKeyPair()).thenReturn(testKeyPair);
        when(rsaKeyUtil.encodePrivateKey(testKeyPair.getPrivate())).thenReturn(testPrivateKeyPem);
        when(rsaKeyUtil.encodePublicKey(testKeyPair.getPublic())).thenReturn(testPublicKeyPem);

        // When
        rsaKeyGenerationService.run();

        // Then
        verify(rsaKeyUtil).generateKeyPair();
        verify(rsaKeyUtil).encodePrivateKey(testKeyPair.getPrivate());
        verify(rsaKeyUtil).encodePublicKey(testKeyPair.getPublic());

        // Verify both files exist with new content
        File publicKeyFile = new File(tempDir.toFile(), "public_key.pem");
        assertThat(publicKeyFile).exists();
        
        String privateKeyContent = Files.readString(privateKeyFile.toPath());
        String publicKeyContent = Files.readString(publicKeyFile.toPath());
        
        assertThat(privateKeyContent).contains(testPrivateKeyPem);
        assertThat(publicKeyContent).contains(testPublicKeyPem);
    }

    @Test
    @DisplayName("Should generate keys when only public key exists")
    void run_OnlyPublicKeyExists_GeneratesNewKeyPair() throws Exception {
        // Given - Create only public key file
        File publicKeyFile = new File(tempDir.toFile(), "public_key.pem");
        Files.writeString(publicKeyFile.toPath(), "-----BEGIN PUBLIC KEY-----\nexisting_public_key\n-----END PUBLIC KEY-----\n");

        when(rsaKeyUtil.generateKeyPair()).thenReturn(testKeyPair);
        when(rsaKeyUtil.encodePrivateKey(testKeyPair.getPrivate())).thenReturn(testPrivateKeyPem);
        when(rsaKeyUtil.encodePublicKey(testKeyPair.getPublic())).thenReturn(testPublicKeyPem);

        // When
        rsaKeyGenerationService.run();

        // Then
        verify(rsaKeyUtil).generateKeyPair();
        verify(rsaKeyUtil).encodePrivateKey(testKeyPair.getPrivate());
        verify(rsaKeyUtil).encodePublicKey(testKeyPair.getPublic());

        // Verify both files exist with new content
        File privateKeyFile = new File(tempDir.toFile(), "private_key.pem");
        assertThat(privateKeyFile).exists();
        
        String privateKeyContent = Files.readString(privateKeyFile.toPath());
        String publicKeyContent = Files.readString(publicKeyFile.toPath());
        
        assertThat(privateKeyContent).contains(testPrivateKeyPem);
        assertThat(publicKeyContent).contains(testPublicKeyPem);
    }

    @Test
    @DisplayName("Should create directory if it doesn't exist")
    void run_DirectoryDoesNotExist_CreatesDirectory() throws Exception {
        // Given - Set path to non-existent directory
        Path nonExistentDir = tempDir.resolve("new-keys-dir");
        ReflectionTestUtils.setField(rsaKeyGenerationService, "keyStorePath", nonExistentDir.toString());

        when(rsaKeyUtil.generateKeyPair()).thenReturn(testKeyPair);
        when(rsaKeyUtil.encodePrivateKey(testKeyPair.getPrivate())).thenReturn(testPrivateKeyPem);
        when(rsaKeyUtil.encodePublicKey(testKeyPair.getPublic())).thenReturn(testPublicKeyPem);

        // When
        rsaKeyGenerationService.run();

        // Then
        assertThat(nonExistentDir.toFile()).exists();
        assertThat(nonExistentDir.toFile()).isDirectory();

        File privateKeyFile = new File(nonExistentDir.toFile(), "private_key.pem");
        File publicKeyFile = new File(nonExistentDir.toFile(), "public_key.pem");
        
        assertThat(privateKeyFile).exists();
        assertThat(publicKeyFile).exists();
    }

    @Test
    @DisplayName("Should throw exception when key generation fails")
    void run_KeyGenerationFails_ThrowsException() throws Exception {
        // Given
        when(rsaKeyUtil.generateKeyPair()).thenThrow(new RuntimeException("Key generation failed"));

        // When & Then
        assertThatThrownBy(() -> rsaKeyGenerationService.run())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate RSA key pair");

        verify(rsaKeyUtil).generateKeyPair();
    }

    @Test
    @DisplayName("Should get public key from file successfully")
    void getPublicKeyFromFile_Success() throws Exception {
        // Given - Create public key file
        File publicKeyFile = new File(tempDir.toFile(), "public_key.pem");
        String keyContent = "-----BEGIN PUBLIC KEY-----\n" + testPublicKeyPem + "\n-----END PUBLIC KEY-----\n";
        Files.writeString(publicKeyFile.toPath(), keyContent);

        // When
        String result = rsaKeyGenerationService.getPublicKeyFromFile();

        // Then
        assertThat(result).isEqualTo(testPublicKeyPem);
        assertThat(result).doesNotContain("-----BEGIN PUBLIC KEY-----");
        assertThat(result).doesNotContain("-----END PUBLIC KEY-----");
        assertThat(result).doesNotContain("\n");
        assertThat(result).doesNotContain(" ");
    }

    @Test
    @DisplayName("Should get private key from file successfully")
    void getPrivateKeyFromFile_Success() throws Exception {
        // Given - Create private key file
        File privateKeyFile = new File(tempDir.toFile(), "private_key.pem");
        String keyContent = "-----BEGIN PRIVATE KEY-----\n" + testPrivateKeyPem + "\n-----END PRIVATE KEY-----\n";
        Files.writeString(privateKeyFile.toPath(), keyContent);

        // When
        String result = rsaKeyGenerationService.getPrivateKeyFromFile();

        // Then
        assertThat(result).isEqualTo(testPrivateKeyPem);
        assertThat(result).doesNotContain("-----BEGIN PRIVATE KEY-----");
        assertThat(result).doesNotContain("-----END PRIVATE KEY-----");
        assertThat(result).doesNotContain("\n");
        assertThat(result).doesNotContain(" ");
    }

    @Test
    @DisplayName("Should throw IOException when public key file doesn't exist")
    void getPublicKeyFromFile_FileNotFound_ThrowsIOException() {
        // Given - No public key file exists

        // When & Then
        assertThatThrownBy(() -> rsaKeyGenerationService.getPublicKeyFromFile())
                .isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Should throw IOException when private key file doesn't exist")
    void getPrivateKeyFromFile_FileNotFound_ThrowsIOException() {
        // Given - No private key file exists

        // When & Then
        assertThatThrownBy(() -> rsaKeyGenerationService.getPrivateKeyFromFile())
                .isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Should handle public key file with extra whitespace")
    void getPublicKeyFromFile_WithExtraWhitespace_Success() throws Exception {
        // Given - Create public key file with extra whitespace
        File publicKeyFile = new File(tempDir.toFile(), "public_key.pem");
        String keyContent = "-----BEGIN PUBLIC KEY-----\n  " + testPublicKeyPem + "  \n  \n-----END PUBLIC KEY-----\n";
        Files.writeString(publicKeyFile.toPath(), keyContent);

        // When
        String result = rsaKeyGenerationService.getPublicKeyFromFile();

        // Then
        assertThat(result).isEqualTo(testPublicKeyPem);
        assertThat(result).doesNotContain(" ");
        assertThat(result).doesNotContain("\n");
        assertThat(result).doesNotContain("\t");
    }

    @Test
    @DisplayName("Should handle private key file with extra whitespace")
    void getPrivateKeyFromFile_WithExtraWhitespace_Success() throws Exception {
        // Given - Create private key file with extra whitespace
        File privateKeyFile = new File(tempDir.toFile(), "private_key.pem");
        String keyContent = "-----BEGIN PRIVATE KEY-----\n  " + testPrivateKeyPem + "  \n  \n-----END PRIVATE KEY-----\n";
        Files.writeString(privateKeyFile.toPath(), keyContent);

        // When
        String result = rsaKeyGenerationService.getPrivateKeyFromFile();

        // Then
        assertThat(result).isEqualTo(testPrivateKeyPem);
        assertThat(result).doesNotContain(" ");
        assertThat(result).doesNotContain("\n");
        assertThat(result).doesNotContain("\t");
    }

    @Test
    @DisplayName("Should throw exception when directory creation fails")
    void run_DirectoryCreationFails_ThrowsException() throws Exception {
        // Given - Set path to a location where directory creation will fail
        // Create a file with the same name as the directory we want to create
        Path conflictingFile = tempDir.resolve("conflicting-name");
        Files.createFile(conflictingFile);
        ReflectionTestUtils.setField(rsaKeyGenerationService, "keyStorePath", conflictingFile.toString());

        // When & Then
        assertThatThrownBy(() -> rsaKeyGenerationService.run())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate RSA key pair");
    }
}
