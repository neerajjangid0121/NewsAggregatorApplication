package com.itt.newsaggregator.service;

import com.itt.newsaggregator.dto.ExternalAPIServerDTO;
import com.itt.newsaggregator.entities.ExternalAPIDetails;
import com.itt.newsaggregator.repository.ExternalAPIRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExternalAPIServerService {
    private final ExternalAPIRepository repository;

    public ExternalAPIServerService(ExternalAPIRepository repository) {
        this.repository = repository;
    }

    public List<ExternalAPIServerDTO> getAllServers() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public boolean updateApiKey(String id, String newApiKey) {
        Optional<ExternalAPIDetails> optionalServer = repository.findById(id);
        if (optionalServer.isEmpty()) return false;

        ExternalAPIDetails server = optionalServer.get();
        server.setApiKey(newApiKey);
        repository.save(server);
        return true;
    }

    private ExternalAPIServerDTO mapToDto(ExternalAPIDetails entity) {
        return new ExternalAPIServerDTO(
                entity.getId(),
                entity.getServerName(),
                entity.getApiKey(),
                entity.getUrl(),
                entity.isActive(),
                entity.getLastFetched()
        );
    }
}
