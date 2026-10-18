package com.syslab.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BuildingBlock {
    private String id;
    private String name;
    private String icon;
    private String category;
    private String tagline;
    private String description;
    private List<String> keyPoints;
    private List<AnimationStep> steps;
    private ComparisonView comparison;
    private List<String> useCases;
    private List<String> tradeoffs;
    private CodeExample codeExample;

    @Data
    @Builder
    public static class AnimationStep {
        private int order;
        private String title;
        private String description;
        private List<Node> nodes;
        private List<Edge> edges;
        private List<String> highlights;
        private long durationMs;
    }

    @Data
    @Builder
    public static class Node {
        private String id;
        private String label;
        private String type; // client, server, cache, db, queue, gateway, lb, cdn
        private String x;
        private String y;
        private boolean active;
        private String metric;
    }

    @Data
    @Builder
    public static class Edge {
        private String from;
        private String to;
        private String label;
        private String color;
        private boolean animated;
        private String direction; // forward, backward, bidirectional
    }

    @Data
    @Builder
    public static class ComparisonView {
        private String title;
        private ScenarioPanel without;
        private ScenarioPanel with;
    }

    @Data
    @Builder
    public static class ScenarioPanel {
        private String label;
        private List<AnimationStep> steps;
        private List<Metric> metrics;
    }

    @Data
    @Builder
    public static class Metric {
        private String name;
        private String value;
        private String quality; // good, bad, neutral
    }

    @Data
    @Builder
    public static class CodeExample {
        private String language;
        private String title;
        private String snippet;
    }
}
