package com.itt.newsaggregator.controller;

import com.itt.newsaggregator.dto.ExternalAPIServerDTO;
import com.itt.newsaggregator.service.ExternalAPIServerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class ExternalAPIServerController {
    private final ExternalAPIServerService service;

    public ExternalAPIServerController(ExternalAPIServerService service) {
        this.service = service;
    }

    @GetMapping("/external-servers")
    public ResponseEntity<List<ExternalAPIServerDTO>> getAllExternalServers() {
        List<ExternalAPIServerDTO> servers = service.getAllServers();
        return ResponseEntity.ok(servers);
    }

    @PutMapping("/external-servers/{id}")
    public ResponseEntity<Void> updateApiKey(@PathVariable String id, @RequestBody ExternalAPIServerDTO dto) {
        boolean success = service.updateApiKey(id, dto.getApiKey());
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


}
