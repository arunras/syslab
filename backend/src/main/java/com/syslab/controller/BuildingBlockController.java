package com.syslab.controller;

import com.syslab.model.BuildingBlock;
import com.syslab.service.BuildingBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BuildingBlockController {

    private final BuildingBlockService service;

    @GetMapping
    public List<Map<String, Object>> getAllBlocks() {
        return service.getAll().stream()
            .map(b -> Map.<String, Object>of(
                "id", b.getId(),
                "name", b.getName(),
                "icon", b.getIcon(),
                "category", b.getCategory(),
                "tagline", b.getTagline()
            ))
            .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuildingBlock> getBlock(@PathVariable String id) {
        return service.getById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return service.getAll().stream()
            .map(BuildingBlock::getCategory)
            .distinct()
            .sorted()
            .toList();
    }
}
