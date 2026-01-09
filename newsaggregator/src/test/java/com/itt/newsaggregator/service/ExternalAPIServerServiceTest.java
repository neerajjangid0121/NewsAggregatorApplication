package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.ExternalAPIServerDTO;
import com.itt.newsaggregator.entities.ExternalAPIDetails;
import com.itt.newsaggregator.repository.ExternalAPIRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class ExternalAPIServerServiceTest {
    @Mock private ExternalAPIRepository repository;
    private ExternalAPIServerService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ExternalAPIServerService(repository);
    }

    @Test
    @DisplayName("getAllServers returns DTO list")
    void getAllServers_ReturnsList() {
        ExternalAPIDetails details = new ExternalAPIDetails(1L, "key", "server", "url", true, LocalDateTime.now());
        Mockito.when(repository.findAll()).thenReturn(List.of(details));
        List<ExternalAPIServerDTO> result = service.getAllServers();
        assertEquals(1, result.size());
        assertEquals("server", result.get(0).getServerName());
    }

    @Test
    @DisplayName("updateApiKey returns true if server found")
    void updateApiKey_Success() {
        ExternalAPIDetails details = new ExternalAPIDetails(1L, "oldkey", "server", "url", true, LocalDateTime.now());
        Mockito.when(repository.findById("1")).thenReturn(Optional.of(details));
        Mockito.when(repository.save(any(ExternalAPIDetails.class))).thenReturn(details);
        boolean result = service.updateApiKey("1", "newkey");
        assertTrue(result);
        assertEquals("newkey", details.getApiKey());
    }

    @Test
    @DisplayName("updateApiKey returns false if server not found")
    void updateApiKey_NotFound() {
        Mockito.when(repository.findById("1")).thenReturn(Optional.empty());
        boolean result = service.updateApiKey("1", "newkey");
        assertFalse(result);
    }
}
