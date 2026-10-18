package com.syslab.service;

import com.syslab.model.BuildingBlock;
import com.syslab.model.BuildingBlock.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BuildingBlockService {

    private final Map<String, BuildingBlock> blocks;

    public BuildingBlockService() {
        this.blocks = buildAll().stream()
                .collect(Collectors.toMap(BuildingBlock::getId, Function.identity()));
    }

    public List<BuildingBlock> getAll() {
        return List.copyOf(blocks.values());
    }

    public Optional<BuildingBlock> getById(String id) {
        return Optional.ofNullable(blocks.get(id));
    }

    private List<BuildingBlock> buildAll() {
        return List.of(
            buildLoadBalancer(),
            buildCache(),
            buildCDN(),
            buildMessageQueue(),
            buildDatabaseSharding(),
            buildRateLimiter(),
            buildApiGateway(),
            buildServiceDiscovery(),
            buildConsistentHashing(),
            buildReplication()
        );
    }

    // ─────────────────────────────────────────────────────────────
    //  LOAD BALANCER
    // ─────────────────────────────────────────────────────────────
    private BuildingBlock buildLoadBalancer() {
        return BuildingBlock.builder()
            .id("load-balancer")
            .name("Load Balancer")
            .icon("⚖️")
            .category("Traffic Management")
            .tagline("Distribute traffic intelligently across servers")
            .description("A Load Balancer distributes incoming network traffic across multiple servers to ensure no single server bears too much demand, improving reliability and availability.")
            .keyPoints(List.of(
                "Horizontal scaling of backend servers",
                "Health checks to route around failures",
                "Algorithms: Round Robin, Least Connections, IP Hash",
                "Layer 4 (TCP) vs Layer 7 (HTTP) load balancing",
                "Session persistence / sticky sessions"
            ))
            .useCases(List.of("High-traffic web applications", "Microservices architectures", "Global traffic distribution"))
            .tradeoffs(List.of("Single point of failure (use redundant LBs)", "Added network hop", "Session state complexity"))
            .steps(List.of(
                AnimationStep.builder().order(1).title("Client sends request")
                    .description("User's browser sends HTTP request to the Load Balancer's public IP")
                    .nodes(List.of(
                        node("client", "Client", "client", "10%", "45%", true, null),
                        node("lb", "Load Balancer", "lb", "40%", "45%", false, null),
                        node("s1", "Server 1", "server", "70%", "20%", false, null),
                        node("s2", "Server 2", "server", "70%", "45%", false, null),
                        node("s3", "Server 3", "server", "70%", "70%", false, null)
                    ))
                    .edges(List.of(edge("client", "lb", "HTTP Request", "#00d4ff", true, "forward")))
                    .highlights(List.of("client", "lb")).durationMs(1500).build(),
                AnimationStep.builder().order(2).title("LB selects server (Round Robin)")
                    .description("The Load Balancer applies Round Robin algorithm to pick the least-recently-used server")
                    .nodes(List.of(
                        node("client", "Client", "client", "10%", "45%", false, null),
                        node("lb", "Load Balancer", "lb", "40%", "45%", true, "Selecting..."),
                        node("s1", "Server 1", "server", "70%", "20%", false, "CPU: 30%"),
                        node("s2", "Server 2", "server", "70%", "45%", true, "CPU: 25% ✓"),
                        node("s3", "Server 3", "server", "70%", "70%", false, "CPU: 40%")
                    ))
                    .edges(List.of()).highlights(List.of("lb", "s2")).durationMs(1500).build(),
                AnimationStep.builder().order(3).title("Request forwarded to Server 2")
                    .description("Load Balancer proxies the request to Server 2 with health checks passing")
                    .nodes(List.of(
                        node("client", "Client", "client", "10%", "45%", false, null),
                        node("lb", "Load Balancer", "lb", "40%", "45%", true, null),
                        node("s1", "Server 1", "server", "70%", "20%", false, "CPU: 30%"),
                        node("s2", "Server 2", "server", "70%", "45%", true, "Processing ⚡"),
                        node("s3", "Server 3", "server", "70%", "70%", false, "CPU: 40%")
                    ))
                    .edges(List.of(edge("lb", "s2", "Proxy", "#a855f7", true, "forward")))
                    .highlights(List.of("lb", "s2")).durationMs(1500).build(),
                AnimationStep.builder().order(4).title("Response returned to client")
                    .description("Server processes and returns response through the Load Balancer back to the client")
                    .nodes(List.of(
                        node("client", "Client", "client", "10%", "45%", true, "200 OK ✓"),
                        node("lb", "Load Balancer", "lb", "40%", "45%", false, null),
                        node("s1", "Server 1", "server", "70%", "20%", false, "CPU: 30%"),
                        node("s2", "Server 2", "server", "70%", "45%", false, "CPU: 26%"),
                        node("s3", "Server 3", "server", "70%", "70%", false, "CPU: 40%")
                    ))
                    .edges(List.of(
                        edge("s2", "lb", "Response", "#22c55e", true, "forward"),
                        edge("lb", "client", "200 OK", "#22c55e", true, "forward")
                    ))
                    .highlights(List.of("client")).durationMs(1500).build()
            ))
            .comparison(ComparisonView.builder()
                .title("Without vs With Load Balancer")
                .without(ScenarioPanel.builder().label("Single Server")
                    .steps(List.of(
                        AnimationStep.builder().order(1).title("All traffic hits one server")
                            .nodes(List.of(
                                node("c1","Client 1","client","10%","30%",true,null),
                                node("c2","Client 2","client","10%","50%",true,null),
                                node("c3","Client 3","client","10%","70%",true,null),
                                node("s1","Server (Overloaded)","server","60%","50%",true,"CPU: 100% 🔥")
                            ))
                            .edges(List.of(
                                edge("c1","s1","","#ef4444",true,"forward"),
                                edge("c2","s1","","#ef4444",true,"forward"),
                                edge("c3","s1","","#ef4444",true,"forward")
                            ))
                            .highlights(List.of("s1")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Latency", "2000ms", "bad"),
                        metric("Availability", "99%", "bad"),
                        metric("Throughput", "100 rps", "bad")
                    )).build())
                .with(ScenarioPanel.builder().label("With Load Balancer")
                    .steps(List.of(
                        AnimationStep.builder().order(1).title("Traffic distributed evenly")
                            .nodes(List.of(
                                node("c1","Client 1","client","10%","30%",true,null),
                                node("c2","Client 2","client","10%","50%",true,null),
                                node("c3","Client 3","client","10%","70%",true,null),
                                node("lb","Load Balancer","lb","40%","50%",true,null),
                                node("s1","Server 1","server","70%","25%",true,"CPU: 33%"),
                                node("s2","Server 2","server","70%","50%",true,"CPU: 33%"),
                                node("s3","Server 3","server","70%","75%",true,"CPU: 33%")
                            ))
                            .edges(List.of(
                                edge("c1","lb","","#00d4ff",true,"forward"),
                                edge("c2","lb","","#00d4ff",true,"forward"),
                                edge("c3","lb","","#00d4ff",true,"forward"),
                                edge("lb","s1","","#22c55e",true,"forward"),
                                edge("lb","s2","","#22c55e",true,"forward"),
                                edge("lb","s3","","#22c55e",true,"forward")
                            ))
                            .highlights(List.of("lb","s1","s2","s3")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Latency", "120ms", "good"),
                        metric("Availability", "99.99%", "good"),
                        metric("Throughput", "10,000 rps", "good")
                    )).build())
                .build())
            .codeExample(CodeExample.builder()
                .language("nginx")
                .title("NGINX Load Balancer Config")
                .snippet("""
                    upstream backend_servers {
                        # Round Robin (default)
                        server server1.example.com weight=3;
                        server server2.example.com weight=3;
                        server server3.example.com weight=3;
                        
                        # Health checks
                        keepalive 32;
                    }
                    
                    server {
                        listen 80;
                        location / {
                            proxy_pass http://backend_servers;
                            proxy_http_version 1.1;
                            proxy_set_header Connection "";
                        }
                    }
                    """)
                .build())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  CACHE
    // ─────────────────────────────────────────────────────────────
    private BuildingBlock buildCache() {
        return BuildingBlock.builder()
            .id("cache")
            .name("Cache")
            .icon("⚡")
            .category("Performance")
            .tagline("Serve data at the speed of memory")
            .description("Caching stores frequently accessed data in fast-access memory to reduce latency and database load. Redis and Memcached are popular in-memory caching solutions.")
            .keyPoints(List.of(
                "Cache-Aside (Lazy Loading) pattern",
                "Write-Through and Write-Back strategies",
                "Cache eviction: LRU, LFU, TTL",
                "Cache stampede / thundering herd prevention",
                "Hot key problem and solutions"
            ))
            .useCases(List.of("Session storage", "API response caching", "Leaderboards", "Rate limiting counters"))
            .tradeoffs(List.of("Cache invalidation complexity", "Memory cost", "Stale data risk", "Cold start problem"))
            .steps(List.of(
                AnimationStep.builder().order(1).title("Request arrives — check cache first")
                    .description("Application receives a request and checks the cache before hitting the database")
                    .nodes(List.of(
                        node("client","Client","client","5%","45%",true,null),
                        node("app","App Server","server","35%","45%",false,null),
                        node("cache","Cache (Redis)","cache","65%","25%",false,"Miss"),
                        node("db","Database","db","65%","70%",false,null)
                    ))
                    .edges(List.of(edge("client","app","GET /user/42","#00d4ff",true,"forward")))
                    .highlights(List.of("client","app")).durationMs(1500).build(),
                AnimationStep.builder().order(2).title("Cache MISS — fallback to DB")
                    .description("Data not in cache (cold start). App queries the database for the data")
                    .nodes(List.of(
                        node("client","Client","client","5%","45%",false,null),
                        node("app","App Server","server","35%","45%",true,null),
                        node("cache","Cache (Redis)","cache","65%","25%",true,"MISS ❌"),
                        node("db","Database","db","65%","70%",false,null)
                    ))
                    .edges(List.of(
                        edge("app","cache","lookup","#ef4444",true,"forward"),
                        edge("cache","app","null","#ef4444",true,"forward")
                    ))
                    .highlights(List.of("cache")).durationMs(1500).build(),
                AnimationStep.builder().order(3).title("DB query executed")
                    .description("Database returns data. App stores it in cache with TTL for future requests")
                    .nodes(List.of(
                        node("client","Client","client","5%","45%",false,null),
                        node("app","App Server","server","35%","45%",true,null),
                        node("cache","Cache (Redis)","cache","65%","25%",false,null),
                        node("db","Database","db","65%","70%",true,"Query: 450ms")
                    ))
                    .edges(List.of(
                        edge("app","db","SELECT * FROM users WHERE id=42","#a855f7",true,"forward"),
                        edge("db","app","user data","#22c55e",true,"forward"),
                        edge("app","cache","SET user:42 (TTL 5m)","#f59e0b",true,"forward")
                    ))
                    .highlights(List.of("db","cache")).durationMs(2000).build(),
                AnimationStep.builder().order(4).title("Next request — Cache HIT!")
                    .description("Second request for same key hits the cache. Served in microseconds, no DB hit!")
                    .nodes(List.of(
                        node("client","Client","client","5%","45%",true,"Response: 2ms ✓"),
                        node("app","App Server","server","35%","45%",false,null),
                        node("cache","Cache (Redis)","cache","65%","25%",true,"HIT ✅"),
                        node("db","Database","db","65%","70%",false,"Idle 😌")
                    ))
                    .edges(List.of(
                        edge("app","cache","GET user:42","#00d4ff",true,"forward"),
                        edge("cache","app","user data (2ms)","#22c55e",true,"forward"),
                        edge("app","client","200 OK","#22c55e",true,"forward")
                    ))
                    .highlights(List.of("cache","client")).durationMs(1500).build()
            ))
            .comparison(ComparisonView.builder()
                .title("Without vs With Cache")
                .without(ScenarioPanel.builder().label("No Cache")
                    .steps(List.of(
                        AnimationStep.builder().order(1).title("Every request hits DB")
                            .nodes(List.of(
                                node("c","Client","client","10%","50%",true,null),
                                node("a","App","server","45%","50%",true,null),
                                node("db","DB","db","80%","50%",true,"450ms/req")
                            ))
                            .edges(List.of(
                                edge("c","a","","#00d4ff",true,"forward"),
                                edge("a","db","","#ef4444",true,"forward"),
                                edge("db","a","","#ef4444",true,"backward")
                            ))
                            .highlights(List.of("db")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Avg Latency", "450ms", "bad"),
                        metric("DB Load", "100%", "bad"),
                        metric("Throughput", "200 rps", "bad")
                    )).build())
                .with(ScenarioPanel.builder().label("With Redis Cache")
                    .steps(List.of(
                        AnimationStep.builder().order(1).title("Cache absorbs 95% of reads")
                            .nodes(List.of(
                                node("c","Client","client","10%","50%",true,null),
                                node("a","App","server","40%","50%",true,null),
                                node("cache","Redis","cache","65%","25%",true,"HIT 95%"),
                                node("db","DB","db","65%","75%",false,"5% of reqs")
                            ))
                            .edges(List.of(
                                edge("c","a","","#00d4ff",true,"forward"),
                                edge("a","cache","","#22c55e",true,"forward"),
                                edge("cache","a","2ms","#22c55e",true,"forward")
                            ))
                            .highlights(List.of("cache")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Avg Latency", "2ms", "good"),
                        metric("DB Load", "5%", "good"),
                        metric("Throughput", "100,000 rps", "good")
                    )).build())
                .build())
            .codeExample(CodeExample.builder()
                .language("java")
                .title("Cache-Aside Pattern (Spring + Redis)")
                .snippet("""
                    @Service
                    public class UserService {
                        
                        @Autowired private RedisTemplate<String, User> redis;
                        @Autowired private UserRepository repo;
                        
                        public User getUser(Long id) {
                            String key = "user:" + id;
                            
                            // 1. Check cache first
                            User cached = redis.opsForValue().get(key);
                            if (cached != null) return cached;  // Cache HIT
                            
                            // 2. Cache MISS — query DB
                            User user = repo.findById(id).orElseThrow();
                            
                            // 3. Populate cache with TTL
                            redis.opsForValue().set(key, user, 5, MINUTES);
                            return user;
                        }
                    }
                    """)
                .build())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  CDN
    // ─────────────────────────────────────────────────────────────
    private BuildingBlock buildCDN() {
        return BuildingBlock.builder()
            .id("cdn")
            .name("CDN")
            .icon("🌐")
            .category("Performance")
            .tagline("Serve content from the edge, close to users")
            .description("A Content Delivery Network (CDN) is a geographically distributed network of servers that caches static content (images, JS, CSS, video) close to end users, dramatically reducing latency.")
            .keyPoints(List.of(
                "Edge PoPs (Points of Presence) distributed globally",
                "Push vs Pull CDN models",
                "Cache-Control headers and TTL management",
                "Dynamic content acceleration via TCP optimization",
                "DDoS mitigation as a side benefit"
            ))
            .useCases(List.of("Static asset delivery", "Video streaming", "Global SaaS products", "E-commerce image hosting"))
            .tradeoffs(List.of("Cache invalidation latency", "Cost at scale", "Compliance/data residency concerns"))
            .steps(List.of(
                AnimationStep.builder().order(1).title("User in Asia requests image")
                    .description("A user in Tokyo requests a large image hosted on an origin server in the US")
                    .nodes(List.of(
                        node("user","User (Tokyo)","client","5%","45%",true,null),
                        node("edge_asia","CDN Edge (Tokyo)","cdn","35%","25%",false,null),
                        node("edge_eu","CDN Edge (London)","cdn","35%","65%",false,null),
                        node("origin","Origin Server (US)","server","75%","45%",false,null)
                    ))
                    .edges(List.of(edge("user","edge_asia","GET /img/banner.jpg","#00d4ff",true,"forward")))
                    .highlights(List.of("user","edge_asia")).durationMs(1500).build(),
                AnimationStep.builder().order(2).title("CDN Edge: Cache MISS → Origin")
                    .description("Cache is cold; edge server fetches from origin across the ocean (300ms)")
                    .nodes(List.of(
                        node("user","User (Tokyo)","client","5%","45%",false,null),
                        node("edge_asia","CDN Edge (Tokyo)","cdn","35%","25%",true,"MISS ❌"),
                        node("edge_eu","CDN Edge (London)","cdn","35%","65%",false,null),
                        node("origin","Origin Server (US)","server","75%","45%",true,"Serving...")
                    ))
                    .edges(List.of(
                        edge("edge_asia","origin","fetch origin","#ef4444",true,"forward"),
                        edge("origin","edge_asia","banner.jpg (300ms)","#f59e0b",true,"forward")
                    ))
                    .highlights(List.of("edge_asia","origin")).durationMs(2000).build(),
                AnimationStep.builder().order(3).title("Edge caches & serves user")
                    .description("Edge caches the image (TTL 24h), serves user in 8ms instead of 300ms!")
                    .nodes(List.of(
                        node("user","User (Tokyo)","client","5%","45%",true,"8ms ✓"),
                        node("edge_asia","CDN Edge (Tokyo)","cdn","35%","25%",true,"Cached ✅"),
                        node("edge_eu","CDN Edge (London)","cdn","35%","65%",false,null),
                        node("origin","Origin Server (US)","server","75%","45%",false,"Idle")
                    ))
                    .edges(List.of(edge("edge_asia","user","banner.jpg (8ms)","#22c55e",true,"forward")))
                    .highlights(List.of("user","edge_asia")).durationMs(1500).build(),
                AnimationStep.builder().order(4).title("Next user gets instant response")
                    .description("All subsequent Tokyo users get the cached response. Origin gets zero load!")
                    .nodes(List.of(
                        node("user","User (Tokyo)","client","5%","45%",true,"Instant!"),
                        node("edge_asia","CDN Edge (Tokyo)","cdn","35%","25%",true,"HIT ✅"),
                        node("edge_eu","CDN Edge (London)","cdn","35%","65%",false,null),
                        node("origin","Origin Server (US)","server","75%","45%",false,"0 requests")
                    ))
                    .edges(List.of(
                        edge("user","edge_asia","GET /img/banner.jpg","#00d4ff",true,"forward"),
                        edge("edge_asia","user","cached (1ms)","#22c55e",true,"forward")
                    ))
                    .highlights(List.of("edge_asia")).durationMs(1500).build()
            ))
            .comparison(ComparisonView.builder()
                .title("Without vs With CDN")
                .without(ScenarioPanel.builder().label("No CDN")
                    .steps(List.of(AnimationStep.builder().order(1).title("All traffic hits origin")
                        .nodes(List.of(
                            node("u1","Tokyo User","client","5%","30%",true,null),
                            node("u2","London User","client","5%","70%",true,null),
                            node("origin","Origin (US)","server","75%","50%",true,"Overloaded")
                        ))
                        .edges(List.of(
                            edge("u1","origin","300ms","#ef4444",true,"forward"),
                            edge("u2","origin","200ms","#ef4444",true,"forward")
                        ))
                        .highlights(List.of("origin")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Tokyo Latency","300ms","bad"),
                        metric("London Latency","200ms","bad"),
                        metric("Origin Load","100%","bad")
                    )).build())
                .with(ScenarioPanel.builder().label("With CDN")
                    .steps(List.of(AnimationStep.builder().order(1).title("Edge serves locally")
                        .nodes(List.of(
                            node("u1","Tokyo User","client","5%","30%",true,null),
                            node("u2","London User","client","5%","70%",true,null),
                            node("e1","Edge Tokyo","cdn","40%","30%",true,"HIT"),
                            node("e2","Edge London","cdn","40%","70%",true,"HIT"),
                            node("origin","Origin (US)","server","80%","50%",false,"~5% traffic")
                        ))
                        .edges(List.of(
                            edge("u1","e1","1ms","#22c55e",true,"forward"),
                            edge("u2","e2","1ms","#22c55e",true,"forward")
                        ))
                        .highlights(List.of("e1","e2")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Tokyo Latency","8ms","good"),
                        metric("London Latency","5ms","good"),
                        metric("Origin Load","5%","good")
                    )).build())
                .build())
            .codeExample(CodeExample.builder()
                .language("http")
                .title("Cache-Control Headers for CDN")
                .snippet("""
                    # Static assets — cache 1 year, immutable
                    Cache-Control: public, max-age=31536000, immutable
                    # filename includes content hash: bundle.a3f9b2.js
                    
                    # HTML pages — revalidate every 5 minutes
                    Cache-Control: public, max-age=300, stale-while-revalidate=60
                    
                    # User-specific data — never cache on CDN
                    Cache-Control: private, no-store
                    Vary: Authorization
                    """)
                .build())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  MESSAGE QUEUE
    // ─────────────────────────────────────────────────────────────
    private BuildingBlock buildMessageQueue() {
        return BuildingBlock.builder()
            .id("message-queue")
            .name("Message Queue")
            .icon("📨")
            .category("Async & Decoupling")
            .tagline("Decouple services with async communication")
            .description("Message Queues enable asynchronous communication between services. Producers publish messages; consumers process them at their own pace. Kafka, RabbitMQ, and SQS are popular choices.")
            .keyPoints(List.of(
                "Producer-Consumer pattern decouples services",
                "At-most-once, At-least-once, Exactly-once delivery",
                "Dead Letter Queues for failed messages",
                "Topic-based pub/sub vs point-to-point queues",
                "Backpressure and flow control"
            ))
            .useCases(List.of("Order processing", "Email/notification dispatch", "Log aggregation", "Event sourcing"))
            .tradeoffs(List.of("Eventual consistency", "Message ordering complexity", "Infrastructure overhead"))
            .steps(List.of(
                AnimationStep.builder().order(1).title("Order service publishes event")
                    .description("When an order is placed, the Order Service publishes an 'OrderPlaced' event to the queue")
                    .nodes(List.of(
                        node("producer","Order Service","server","10%","45%",true,"New Order!"),
                        node("queue","Message Queue","queue","45%","45%",false,null),
                        node("email","Email Service","server","80%","20%",false,null),
                        node("inventory","Inventory Svc","server","80%","45%",false,null),
                        node("analytics","Analytics","server","80%","70%",false,null)
                    ))
                    .edges(List.of(edge("producer","queue","OrderPlaced event","#f59e0b",true,"forward")))
                    .highlights(List.of("producer","queue")).durationMs(1500).build(),
                AnimationStep.builder().order(2).title("Message queued & persisted")
                    .description("Queue persists the message durably. Producer is free — no need to wait for consumers")
                    .nodes(List.of(
                        node("producer","Order Service","server","10%","45%",false,"Done ✓ (free)"),
                        node("queue","Message Queue","queue","45%","45%",true,"1 message"),
                        node("email","Email Service","server","80%","20%",false,"Waiting..."),
                        node("inventory","Inventory Svc","server","80%","45%",false,"Waiting..."),
                        node("analytics","Analytics","server","80%","70%",false,"Waiting...")
                    ))
                    .edges(List.of()).highlights(List.of("queue")).durationMs(1500).build(),
                AnimationStep.builder().order(3).title("Consumers process independently")
                    .description("All subscribers receive the event and process it at their own pace, independently")
                    .nodes(List.of(
                        node("producer","Order Service","server","10%","45%",false,null),
                        node("queue","Message Queue","queue","45%","45%",true,"Dispatching"),
                        node("email","Email Service","server","80%","20%",true,"Sending email..."),
                        node("inventory","Inventory Svc","server","80%","45%",true,"Updating stock..."),
                        node("analytics","Analytics","server","80%","70%",true,"Recording...")
                    ))
                    .edges(List.of(
                        edge("queue","email","OrderPlaced","#a855f7",true,"forward"),
                        edge("queue","inventory","OrderPlaced","#a855f7",true,"forward"),
                        edge("queue","analytics","OrderPlaced","#a855f7",true,"forward")
                    ))
                    .highlights(List.of("queue","email","inventory","analytics")).durationMs(2000).build(),
                AnimationStep.builder().order(4).title("All services acknowledge")
                    .description("Each consumer sends an ACK. Message is deleted from queue only after all ACKs received")
                    .nodes(List.of(
                        node("producer","Order Service","server","10%","45%",false,null),
                        node("queue","Message Queue","queue","45%","45%",false,"0 pending ✓"),
                        node("email","Email Service","server","80%","20%",false,"Email sent ✓"),
                        node("inventory","Inventory Svc","server","80%","45%",false,"Stock updated ✓"),
                        node("analytics","Analytics","server","80%","70%",false,"Recorded ✓")
                    ))
                    .edges(List.of(
                        edge("email","queue","ACK","#22c55e",true,"forward"),
                        edge("inventory","queue","ACK","#22c55e",true,"forward"),
                        edge("analytics","queue","ACK","#22c55e",true,"forward")
                    ))
                    .highlights(List.of("email","inventory","analytics")).durationMs(1500).build()
            ))
            .comparison(ComparisonView.builder()
                .title("Synchronous vs Asynchronous (Queue)")
                .without(ScenarioPanel.builder().label("Synchronous")
                    .steps(List.of(AnimationStep.builder().order(1).title("API waits for all services")
                        .nodes(List.of(
                            node("api","API","server","5%","50%",true,"BLOCKED"),
                            node("e","Email","server","50%","20%",true,"2s..."),
                            node("i","Inventory","server","50%","50%",true,"1s..."),
                            node("a","Analytics","server","50%","80%",true,"500ms...")
                        ))
                        .edges(List.of(
                            edge("api","e","wait","#ef4444",true,"forward"),
                            edge("api","i","wait","#ef4444",true,"forward"),
                            edge("api","a","wait","#ef4444",true,"forward")
                        ))
                        .highlights(List.of("api")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("API Response Time","3.5s","bad"),
                        metric("Failure Impact","Total outage","bad"),
                        metric("Scalability","Tight coupling","bad")
                    )).build())
                .with(ScenarioPanel.builder().label("With Message Queue")
                    .steps(List.of(AnimationStep.builder().order(1).title("API publishes and returns")
                        .nodes(List.of(
                            node("api","API","server","5%","50%",true,"50ms ✓"),
                            node("q","Queue","queue","40%","50%",true,"Buffering"),
                            node("e","Email","server","80%","20%",false,"Async"),
                            node("i","Inventory","server","80%","50%",false,"Async"),
                            node("a","Analytics","server","80%","80%",false,"Async")
                        ))
                        .edges(List.of(
                            edge("api","q","publish","#22c55e",true,"forward")
                        ))
                        .highlights(List.of("api","q")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("API Response Time","50ms","good"),
                        metric("Failure Impact","Isolated","good"),
                        metric("Scalability","Independent scaling","good")
                    )).build())
                .build())
            .codeExample(CodeExample.builder()
                .language("java")
                .title("Kafka Producer & Consumer")
                .snippet("""
                    // Producer
                    @Service
                    public class OrderService {
                        @Autowired KafkaTemplate<String, OrderEvent> kafka;
                        
                        public void placeOrder(Order order) {
                            order = orderRepo.save(order);
                            kafka.send("order-events", new OrderEvent(order));
                            // Returns immediately — no waiting!
                        }
                    }
                    
                    // Consumer
                    @Component
                    public class EmailService {
                        @KafkaListener(topics = "order-events", groupId = "email-svc")
                        public void onOrderPlaced(OrderEvent event) {
                            emailClient.sendConfirmation(event.getUserEmail());
                        }
                    }
                    """)
                .build())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  DATABASE SHARDING
    // ─────────────────────────────────────────────────────────────
    private BuildingBlock buildDatabaseSharding() {
        return BuildingBlock.builder()
            .id("database-sharding")
            .name("Database Sharding")
            .icon("🗄️")
            .category("Scalability")
            .tagline("Horizontally partition data across multiple databases")
            .description("Sharding splits a large database into smaller, faster, more manageable pieces called shards. Each shard holds a subset of the total data, enabling horizontal scaling beyond a single machine's limits.")
            .keyPoints(List.of(
                "Horizontal partitioning by shard key",
                "Range-based, Hash-based, and Directory-based sharding",
                "Cross-shard queries are expensive — avoid them",
                "Hotspot shards and rebalancing challenges",
                "Consistent hashing for dynamic shard assignment"
            ))
            .useCases(List.of("Multi-billion row tables", "User data partitioning by region", "Time-series data"))
            .tradeoffs(List.of("Cross-shard JOINs become application logic", "Resharding is complex", "Hotspot risk"))
            .steps(List.of(
                AnimationStep.builder().order(1).title("Single DB becomes a bottleneck")
                    .description("With millions of users, a single database can't handle the read/write load")
                    .nodes(List.of(
                        node("app","App Servers","server","10%","45%",true,null),
                        node("db","Single DB","db","60%","45%",true,"500GB, overloaded 🔥")
                    ))
                    .edges(List.of(edge("app","db","all queries","#ef4444",true,"forward")))
                    .highlights(List.of("db")).durationMs(1500).build(),
                AnimationStep.builder().order(2).title("Shard key selection")
                    .description("Choose user_id as shard key. Hash it to determine which shard holds each user's data")
                    .nodes(List.of(
                        node("app","App Servers","server","10%","45%",true,null),
                        node("router","Shard Router","gateway","40%","45%",true,"hash(user_id) % 3"),
                        node("s0","Shard 0","db","75%","20%",false,"users 0-33M"),
                        node("s1","Shard 1","db","75%","45%",false,"users 33M-66M"),
                        node("s2","Shard 2","db","75%","70%",false,"users 66M-100M")
                    ))
                    .edges(List.of(edge("app","router","query user_id=42","#00d4ff",true,"forward")))
                    .highlights(List.of("router")).durationMs(1500).build(),
                AnimationStep.builder().order(3).title("Router directs to correct shard")
                    .description("hash(42) % 3 = 0, so the query goes to Shard 0. Other shards are untouched")
                    .nodes(List.of(
                        node("app","App Servers","server","10%","45%",false,null),
                        node("router","Shard Router","gateway","40%","45%",true,"→ Shard 0"),
                        node("s0","Shard 0","db","75%","20%",true,"Processing ⚡"),
                        node("s1","Shard 1","db","75%","45%",false,"Idle"),
                        node("s2","Shard 2","db","75%","70%",false,"Idle")
                    ))
                    .edges(List.of(edge("router","s0","routed query","#22c55e",true,"forward")))
                    .highlights(List.of("router","s0")).durationMs(1500).build(),
                AnimationStep.builder().order(4).title("Parallel writes across shards")
                    .description("Different users write to different shards simultaneously — true horizontal scale!")
                    .nodes(List.of(
                        node("app","App Servers","server","10%","45%",true,null),
                        node("router","Shard Router","gateway","40%","45%",true,null),
                        node("s0","Shard 0","db","75%","20%",true,"Writing user 42"),
                        node("s1","Shard 1","db","75%","45%",true,"Writing user 5M"),
                        node("s2","Shard 2","db","75%","70%",true,"Writing user 80M")
                    ))
                    .edges(List.of(
                        edge("router","s0","user 42","#22c55e",true,"forward"),
                        edge("router","s1","user 5M","#a855f7",true,"forward"),
                        edge("router","s2","user 80M","#f59e0b",true,"forward")
                    ))
                    .highlights(List.of("s0","s1","s2")).durationMs(1500).build()
            ))
            .comparison(ComparisonView.builder()
                .title("Single DB vs Sharded DB")
                .without(ScenarioPanel.builder().label("Single Database")
                    .steps(List.of(AnimationStep.builder().order(1).title("One DB handles everything")
                        .nodes(List.of(
                            node("a","App","server","10%","50%",true,null),
                            node("db","DB (500GB)","db","70%","50%",true,"MAXED OUT")
                        ))
                        .edges(List.of(edge("a","db","all load","#ef4444",true,"forward")))
                        .highlights(List.of("db")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Max Data Size","~2TB","bad"),
                        metric("Write Throughput","50k/s","bad"),
                        metric("Scale Method","Vertical only","bad")
                    )).build())
                .with(ScenarioPanel.builder().label("3-Shard Setup")
                    .steps(List.of(AnimationStep.builder().order(1).title("Load split 3 ways")
                        .nodes(List.of(
                            node("a","App","server","5%","50%",true,null),
                            node("r","Router","gateway","35%","50%",true,null),
                            node("s0","Shard 0","db","70%","25%",true,"167GB"),
                            node("s1","Shard 1","db","70%","50%",true,"167GB"),
                            node("s2","Shard 2","db","70%","75%",true,"167GB")
                        ))
                        .edges(List.of(
                            edge("a","r","","#00d4ff",true,"forward"),
                            edge("r","s0","","#22c55e",true,"forward"),
                            edge("r","s1","","#22c55e",true,"forward"),
                            edge("r","s2","","#22c55e",true,"forward")
                        ))
                        .highlights(List.of("s0","s1","s2")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Max Data Size","Unlimited (add shards)","good"),
                        metric("Write Throughput","150k/s","good"),
                        metric("Scale Method","Horizontal","good")
                    )).build())
                .build())
            .codeExample(CodeExample.builder()
                .language("java")
                .title("Shard Router Logic")
                .snippet("""
                    public class ShardRouter {
                        private final List<DataSource> shards;
                        
                        public DataSource getShardFor(long userId) {
                            int shardIndex = (int)(userId % shards.size());
                            return shards.get(shardIndex);
                        }
                        
                        public User findUser(long userId) {
                            // Route to correct shard
                            DataSource shard = getShardFor(userId);
                            JdbcTemplate jdbc = new JdbcTemplate(shard);
                            return jdbc.queryForObject(
                                "SELECT * FROM users WHERE id = ?",
                                userRowMapper, userId
                            );
                        }
                    }
                    """)
                .build())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  RATE LIMITER
    // ─────────────────────────────────────────────────────────────
    private BuildingBlock buildRateLimiter() {
        return BuildingBlock.builder()
            .id("rate-limiter")
            .name("Rate Limiter")
            .icon("🚦")
            .category("Resilience")
            .tagline("Control the flow of requests to protect your system")
            .description("Rate limiting controls how many requests a client can make in a time window, protecting APIs from abuse, DDoS, and overload. Common algorithms: Token Bucket, Leaky Bucket, Fixed Window, Sliding Window.")
            .keyPoints(List.of(
                "Token Bucket: allows bursts, refills at fixed rate",
                "Leaky Bucket: smooths output, no bursts",
                "Sliding Window: more accurate than fixed window",
                "Distributed rate limiting with Redis",
                "Per-user, per-IP, or per-API-key limiting"
            ))
            .useCases(List.of("Public APIs", "Login protection", "Payment APIs", "SMS/email sending"))
            .tradeoffs(List.of("False positives for legitimate traffic", "Redis dependency for distributed limiting", "Clock skew issues"))
            .steps(List.of(
                AnimationStep.builder().order(1).title("Token Bucket initialized")
                    .description("Each client gets a token bucket with capacity 5, refilling at 2 tokens/second")
                    .nodes(List.of(
                        node("client","Client","client","5%","45%",true,null),
                        node("rl","Rate Limiter","gateway","40%","45%",true,"Tokens: ●●●●● (5/5)"),
                        node("api","API","server","80%","45%",false,null)
                    ))
                    .edges(List.of()).highlights(List.of("rl")).durationMs(1500).build(),
                AnimationStep.builder().order(2).title("Request 1–3: Tokens consumed")
                    .description("Each allowed request consumes one token. Requests 1-3 pass through freely")
                    .nodes(List.of(
                        node("client","Client","client","5%","45%",true,"3 requests"),
                        node("rl","Rate Limiter","gateway","40%","45%",true,"Tokens: ●●○○○ (2/5)"),
                        node("api","API","server","80%","45%",true,"200 OK ✓")
                    ))
                    .edges(List.of(
                        edge("client","rl","requests","#00d4ff",true,"forward"),
                        edge("rl","api","allowed ✓","#22c55e",true,"forward")
                    ))
                    .highlights(List.of("client","rl","api")).durationMs(1500).build(),
                AnimationStep.builder().order(3).title("Request 6: Bucket empty — blocked!")
                    .description("Bucket is exhausted. Request 6 is rejected with HTTP 429 Too Many Requests")
                    .nodes(List.of(
                        node("client","Client","client","5%","45%",true,"429! ❌"),
                        node("rl","Rate Limiter","gateway","40%","45%",true,"Tokens: ○○○○○ (0/5)"),
                        node("api","API","server","80%","45%",false,"Protected 🛡️")
                    ))
                    .edges(List.of(
                        edge("client","rl","request 6","#ef4444",true,"forward"),
                        edge("rl","client","429 Too Many Requests","#ef4444",true,"forward")
                    ))
                    .highlights(List.of("rl")).durationMs(1500).build(),
                AnimationStep.builder().order(4).title("Tokens refill — access restored")
                    .description("After 1 second, 2 new tokens arrive. Client can make requests again")
                    .nodes(List.of(
                        node("client","Client","client","5%","45%",false,null),
                        node("rl","Rate Limiter","gateway","40%","45%",true,"Tokens: ●●○○○ (refilling...)"),
                        node("api","API","server","80%","45%",false,null)
                    ))
                    .edges(List.of()).highlights(List.of("rl")).durationMs(1500).build()
            ))
            .comparison(ComparisonView.builder()
                .title("Without vs With Rate Limiter")
                .without(ScenarioPanel.builder().label("No Rate Limit")
                    .steps(List.of(AnimationStep.builder().order(1).title("DDoS kills the service")
                        .nodes(List.of(
                            node("attacker","Attacker","client","5%","30%",true,"10k/s"),
                            node("legit","Legit User","client","5%","70%",false,"Blocked"),
                            node("api","API","server","70%","50%",true,"CRASHED 💥")
                        ))
                        .edges(List.of(edge("attacker","api","flood","#ef4444",true,"forward")))
                        .highlights(List.of("api")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("API Availability","0%","bad"),
                        metric("Legitimate Users","Affected","bad"),
                        metric("Cost","Unbounded","bad")
                    )).build())
                .with(ScenarioPanel.builder().label("With Rate Limiter")
                    .steps(List.of(AnimationStep.builder().order(1).title("Attacker blocked, user allowed")
                        .nodes(List.of(
                            node("attacker","Attacker","client","5%","30%",true,"Blocked 🚦"),
                            node("legit","Legit User","client","5%","70%",true,"Allowed ✓"),
                            node("rl","Rate Limiter","gateway","40%","50%",true,null),
                            node("api","API","server","80%","50%",true,"Healthy ✓")
                        ))
                        .edges(List.of(
                            edge("attacker","rl","flood","#ef4444",true,"forward"),
                            edge("rl","attacker","429","#ef4444",true,"forward"),
                            edge("legit","rl","normal","#00d4ff",true,"forward"),
                            edge("rl","api","allowed","#22c55e",true,"forward")
                        ))
                        .highlights(List.of("rl","api")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("API Availability","99.9%","good"),
                        metric("Legitimate Users","Unaffected","good"),
                        metric("Cost","Controlled","good")
                    )).build())
                .build())
            .codeExample(CodeExample.builder()
                .language("java")
                .title("Token Bucket Rate Limiter")
                .snippet("""
                    @Component
                    public class RateLimiter {
                        private final RedisTemplate<String, Long> redis;
                        private static final int CAPACITY = 100;
                        private static final int REFILL_RATE = 10; // tokens/sec
                        
                        public boolean allowRequest(String clientId) {
                            String key = "rate_limit:" + clientId;
                            Long tokens = redis.opsForValue().get(key);
                            
                            if (tokens == null) {
                                redis.opsForValue().set(key, (long) CAPACITY - 1,
                                    Duration.ofSeconds(60));
                                return true;
                            }
                            if (tokens > 0) {
                                redis.opsForValue().decrement(key);
                                return true;
                            }
                            return false; // 429
                        }
                    }
                    """)
                .build())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  API GATEWAY
    // ─────────────────────────────────────────────────────────────
    private BuildingBlock buildApiGateway() {
        return BuildingBlock.builder()
            .id("api-gateway")
            .name("API Gateway")
            .icon("🔀")
            .category("Traffic Management")
            .tagline("Single entry point for all microservice traffic")
            .description("An API Gateway acts as the front door for all client requests, routing them to the appropriate microservice. It handles cross-cutting concerns like auth, rate limiting, logging, and SSL termination centrally.")
            .keyPoints(List.of(
                "Single entry point — simplifies client code",
                "Authentication & authorization enforcement",
                "Request/response transformation",
                "SSL/TLS termination",
                "API versioning and canary deployments"
            ))
            .useCases(List.of("Microservices frontend", "Mobile API backends", "Partner API management"))
            .tradeoffs(List.of("Potential bottleneck", "Single point of failure", "Latency overhead"))
            .steps(List.of(
                AnimationStep.builder().order(1).title("Client sends API request")
                    .description("Mobile app sends a request. Without a gateway, it would need to know every service's address")
                    .nodes(List.of(
                        node("client","Mobile App","client","5%","45%",true,null),
                        node("gw","API Gateway","gateway","35%","45%",false,null),
                        node("auth","Auth Service","server","65%","15%",false,null),
                        node("user","User Service","server","65%","38%",false,null),
                        node("order","Order Service","server","65%","60%",false,null),
                        node("product","Product Service","server","65%","82%",false,null)
                    ))
                    .edges(List.of(edge("client","gw","GET /api/v1/orders","#00d4ff",true,"forward")))
                    .highlights(List.of("client","gw")).durationMs(1500).build(),
                AnimationStep.builder().order(2).title("Gateway authenticates request")
                    .description("Gateway validates the JWT token with Auth Service before routing the request")
                    .nodes(List.of(
                        node("client","Mobile App","client","5%","45%",false,null),
                        node("gw","API Gateway","gateway","35%","45%",true,"Validating JWT..."),
                        node("auth","Auth Service","server","65%","15%",true,"Valid ✓"),
                        node("user","User Service","server","65%","38%",false,null),
                        node("order","Order Service","server","65%","60%",false,null),
                        node("product","Product Service","server","65%","82%",false,null)
                    ))
                    .edges(List.of(
                        edge("gw","auth","validate token","#f59e0b",true,"forward"),
                        edge("auth","gw","valid ✓","#22c55e",true,"forward")
                    ))
                    .highlights(List.of("gw","auth")).durationMs(1500).build(),
                AnimationStep.builder().order(3).title("Gateway routes to Order Service")
                    .description("Based on the /orders path, Gateway routes to the Order microservice")
                    .nodes(List.of(
                        node("client","Mobile App","client","5%","45%",false,null),
                        node("gw","API Gateway","gateway","35%","45%",true,"Routing..."),
                        node("auth","Auth Service","server","65%","15%",false,null),
                        node("user","User Service","server","65%","38%",false,null),
                        node("order","Order Service","server","65%","60%",true,"Processing ⚡"),
                        node("product","Product Service","server","65%","82%",false,null)
                    ))
                    .edges(List.of(edge("gw","order","/orders → Order Svc","#a855f7",true,"forward")))
                    .highlights(List.of("gw","order")).durationMs(1500).build(),
                AnimationStep.builder().order(4).title("Response transformed & returned")
                    .description("Gateway transforms the internal response format and adds headers before returning to client")
                    .nodes(List.of(
                        node("client","Mobile App","client","5%","45%",true,"200 OK ✓"),
                        node("gw","API Gateway","gateway","35%","45%",true,"Transforming..."),
                        node("auth","Auth Service","server","65%","15%",false,null),
                        node("user","User Service","server","65%","38%",false,null),
                        node("order","Order Service","server","65%","60%",false,null),
                        node("product","Product Service","server","65%","82%",false,null)
                    ))
                    .edges(List.of(
                        edge("order","gw","raw data","#f59e0b",true,"forward"),
                        edge("gw","client","JSON response","#22c55e",true,"forward")
                    ))
                    .highlights(List.of("gw","client")).durationMs(1500).build()
            ))
            .comparison(ComparisonView.builder()
                .title("Direct Service Access vs API Gateway")
                .without(ScenarioPanel.builder().label("No Gateway")
                    .steps(List.of(AnimationStep.builder().order(1).title("Client knows every service")
                        .nodes(List.of(
                            node("c","Client","client","5%","50%",true,null),
                            node("s1","Auth Svc :8081","server","70%","20%",true,null),
                            node("s2","User Svc :8082","server","70%","45%",true,null),
                            node("s3","Order Svc :8083","server","70%","70%",true,null)
                        ))
                        .edges(List.of(
                            edge("c","s1","auth calls","#ef4444",true,"forward"),
                            edge("c","s2","user calls","#ef4444",true,"forward"),
                            edge("c","s3","order calls","#ef4444",true,"forward")
                        ))
                        .highlights(List.of("c")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Client Complexity","High","bad"),
                        metric("Security","Each service secures itself","bad"),
                        metric("Changes","Break all clients","bad")
                    )).build())
                .with(ScenarioPanel.builder().label("With API Gateway")
                    .steps(List.of(AnimationStep.builder().order(1).title("Client talks to one endpoint")
                        .nodes(List.of(
                            node("c","Client","client","5%","50%",true,null),
                            node("gw","Gateway :443","gateway","40%","50%",true,null),
                            node("s1","Auth Svc","server","75%","20%",false,null),
                            node("s2","User Svc","server","75%","50%",false,null),
                            node("s3","Order Svc","server","75%","80%",false,null)
                        ))
                        .edges(List.of(edge("c","gw","one endpoint","#22c55e",true,"forward")))
                        .highlights(List.of("gw")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Client Complexity","Low","good"),
                        metric("Security","Centralized","good"),
                        metric("Changes","Transparent to clients","good")
                    )).build())
                .build())
            .codeExample(CodeExample.builder()
                .language("yaml")
                .title("Spring Cloud Gateway Route Config")
                .snippet("""
                    spring:
                      cloud:
                        gateway:
                          routes:
                            - id: order-service
                              uri: lb://ORDER-SERVICE
                              predicates:
                                - Path=/api/v1/orders/**
                              filters:
                                - StripPrefix=2
                                - name: RequestRateLimiter
                                  args:
                                    redis-rate-limiter.replenishRate: 10
                                    redis-rate-limiter.burstCapacity: 20
                    """)
                .build())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  SERVICE DISCOVERY
    // ─────────────────────────────────────────────────────────────
    private BuildingBlock buildServiceDiscovery() {
        return BuildingBlock.builder()
            .id("service-discovery")
            .name("Service Discovery")
            .icon("🔍")
            .category("Microservices")
            .tagline("Automatically find and connect to services")
            .description("Service Discovery enables services to find each other dynamically without hardcoded addresses. Services register themselves in a registry (Consul, Eureka, etcd); clients query the registry to find available instances.")
            .keyPoints(List.of(
                "Client-side vs Server-side discovery",
                "Self-registration vs third-party registration",
                "Health check-based deregistration",
                "DNS-based vs API-based discovery",
                "Integration with load balancing (Ribbon, gRPC)"
            ))
            .useCases(List.of("Kubernetes pod-to-pod communication", "Auto-scaled microservices", "Blue-green deployments"))
            .tradeoffs(List.of("Registry becomes critical dependency", "Network partitions affect discovery", "Caching stale instances"))
            .steps(List.of(
                AnimationStep.builder().order(1).title("Service registers on startup")
                    .description("When Order Service starts, it registers its IP:Port and health check URL with the registry")
                    .nodes(List.of(
                        node("order","Order Service","server","10%","45%",true,"Starting up..."),
                        node("registry","Service Registry","gateway","50%","45%",false,"0 services"),
                        node("payment","Payment Service","server","85%","45%",false,null)
                    ))
                    .edges(List.of(edge("order","registry","REGISTER {ip:port, /health}","#22c55e",true,"forward")))
                    .highlights(List.of("order","registry")).durationMs(1500).build(),
                AnimationStep.builder().order(2).title("Registry stores & heartbeats")
                    .description("Registry records the instance. Services must send heartbeats every 30s or get deregistered")
                    .nodes(List.of(
                        node("order","Order Service","server","10%","45%",true,"❤️ heartbeat"),
                        node("registry","Service Registry","gateway","50%","45%",true,"order-svc: [{10.0.1.5:8080}]"),
                        node("payment","Payment Service","server","85%","45%",false,null)
                    ))
                    .edges(List.of(edge("order","registry","heartbeat","#f59e0b",true,"forward")))
                    .highlights(List.of("registry")).durationMs(1500).build(),
                AnimationStep.builder().order(3).title("Payment Service looks up Order")
                    .description("Payment Service needs to call Order Service but doesn't know its address — asks registry")
                    .nodes(List.of(
                        node("order","Order Service","server","10%","45%",false,null),
                        node("registry","Service Registry","gateway","50%","45%",true,null),
                        node("payment","Payment Service","server","85%","45%",true,"Need order-svc...")
                    ))
                    .edges(List.of(
                        edge("payment","registry","WHERE is order-svc?","#00d4ff",true,"forward"),
                        edge("registry","payment","[10.0.1.5:8080]","#22c55e",true,"forward")
                    ))
                    .highlights(List.of("registry","payment")).durationMs(1500).build(),
                AnimationStep.builder().order(4).title("Direct service-to-service call")
                    .description("Payment Service calls Order Service directly using the discovered address")
                    .nodes(List.of(
                        node("order","Order Service","server","10%","45%",true,"Processing"),
                        node("registry","Service Registry","gateway","50%","45%",false,null),
                        node("payment","Payment Service","server","85%","45%",true,"Got data ✓")
                    ))
                    .edges(List.of(edge("payment","order","HTTP call (direct)","#a855f7",true,"forward")))
                    .highlights(List.of("payment","order")).durationMs(1500).build()
            ))
            .comparison(ComparisonView.builder()
                .title("Hardcoded Config vs Service Discovery")
                .without(ScenarioPanel.builder().label("Static Config")
                    .steps(List.of(AnimationStep.builder().order(1).title("Config breaks on scale")
                        .nodes(List.of(
                            node("svc","Service A","server","10%","50%",true,"IP hardcoded"),
                            node("b1","Service B :8082","server","70%","30%",false,"Old address"),
                            node("b2","Service B :8083","server","70%","70%",true,"New instance — unknown!")
                        ))
                        .edges(List.of(edge("svc","b1","hardcoded IP","#ef4444",true,"forward")))
                        .highlights(List.of("svc")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Config updates","Manual","bad"),
                        metric("Auto-scaling","Not supported","bad"),
                        metric("Failure handling","Manual","bad")
                    )).build())
                .with(ScenarioPanel.builder().label("Service Discovery")
                    .steps(List.of(AnimationStep.builder().order(1).title("Dynamic address resolution")
                        .nodes(List.of(
                            node("svc","Service A","server","5%","50%",true,null),
                            node("reg","Registry","gateway","40%","50%",true,"2 instances"),
                            node("b1","Service B #1","server","75%","30%",true,"Registered"),
                            node("b2","Service B #2","server","75%","70%",true,"Registered")
                        ))
                        .edges(List.of(
                            edge("svc","reg","lookup","#00d4ff",true,"forward"),
                            edge("reg","svc","[b1, b2]","#22c55e",true,"forward")
                        ))
                        .highlights(List.of("reg")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Config updates","Automatic","good"),
                        metric("Auto-scaling","Seamless","good"),
                        metric("Failure handling","Auto-deregister","good")
                    )).build())
                .build())
            .codeExample(CodeExample.builder()
                .language("java")
                .title("Spring Eureka Client")
                .snippet("""
                    // Register service with Eureka
                    @SpringBootApplication
                    @EnableEurekaClient
                    public class OrderServiceApp {
                        public static void main(String[] args) {
                            SpringApplication.run(OrderServiceApp.class, args);
                        }
                    }
                    
                    // Discover and call another service
                    @Service
                    public class PaymentClient {
                        @Autowired
                        @LoadBalanced  // Client-side load balancing!
                        private RestTemplate restTemplate;
                        
                        public Payment charge(Order order) {
                            // Use service NAME, not IP:port
                            return restTemplate.postForObject(
                                "http://payment-service/api/charge",
                                order, Payment.class
                            );
                        }
                    }
                    """)
                .build())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  CONSISTENT HASHING
    // ─────────────────────────────────────────────────────────────
    private BuildingBlock buildConsistentHashing() {
        return BuildingBlock.builder()
            .id("consistent-hashing")
            .name("Consistent Hashing")
            .icon("🔄")
            .category("Distributed Systems")
            .tagline("Minimize data movement when nodes join or leave")
            .description("Consistent Hashing maps both data keys and nodes onto the same ring. When a node joins/leaves, only the data from its adjacent neighbor needs to move — not the entire dataset. Used in Cassandra, DynamoDB, and CDNs.")
            .keyPoints(List.of(
                "Hash ring with N virtual positions (0 to 2^32)",
                "Keys map clockwise to nearest node",
                "Only 1/N keys need to move on node addition",
                "Virtual nodes (vnodes) for better distribution",
                "Replication factor: replicate to next N clockwise nodes"
            ))
            .useCases(List.of("Distributed caches", "DHTs (BitTorrent, Chord)", "Database partitioning", "CDN routing"))
            .tradeoffs(List.of("Hot spots without vnodes", "Complexity vs simple modular hashing", "Load imbalance risk"))
            .steps(List.of(
                AnimationStep.builder().order(1).title("Hash ring with 3 nodes")
                    .description("Nodes A, B, C are placed on the ring by hashing their IDs. Keys map clockwise to nearest node")
                    .nodes(List.of(
                        node("ring","Hash Ring (0-360°)","gateway","40%","40%",true,"3 nodes"),
                        node("na","Node A (60°)","server","60%","15%",false,null),
                        node("nb","Node B (180°)","server","15%","75%",false,null),
                        node("nc","Node C (300°)","server","75%","75%",false,null)
                    ))
                    .edges(List.of()).highlights(List.of("ring")).durationMs(2000).build(),
                AnimationStep.builder().order(2).title("Key assignment — clockwise")
                    .description("Key 'user:42' hashes to position 90°. Clockwise from 90° is Node A (at 180°) — assigned!")
                    .nodes(List.of(
                        node("ring","Hash Ring","gateway","40%","40%",true,"key→90°"),
                        node("na","Node A (180°)","server","60%","15%",true,"user:42 ✓"),
                        node("nb","Node B (300°)","server","15%","75%",false,null),
                        node("nc","Node C (60°)","server","75%","75%",false,null)
                    ))
                    .edges(List.of(edge("ring","na","key 90° → Node A","#22c55e",true,"forward")))
                    .highlights(List.of("na")).durationMs(1500).build(),
                AnimationStep.builder().order(3).title("New node D joins the ring")
                    .description("Node D joins at position 120°. Only keys between 60°-120° need to move from A → D")
                    .nodes(List.of(
                        node("ring","Hash Ring","gateway","40%","40%",true,"4 nodes"),
                        node("na","Node A (180°)","server","60%","15%",false,"Lost some keys"),
                        node("nb","Node B (300°)","server","15%","75%",false,null),
                        node("nc","Node C (60°)","server","75%","75%",false,null),
                        node("nd","Node D (120°)","server","75%","15%",true,"New! Receiving...")
                    ))
                    .edges(List.of(edge("na","nd","migrate keys 60°-120°","#f59e0b",true,"forward")))
                    .highlights(List.of("nd","na")).durationMs(1500).build(),
                AnimationStep.builder().order(4).title("Only 1/N keys migrated!")
                    .description("Only the keys in the 60°-120° range moved. 75% of data stayed in place. Compare to modular hashing where 100% would move!")
                    .nodes(List.of(
                        node("ring","Hash Ring","gateway","40%","40%",true,"Balanced ✓"),
                        node("na","Node A","server","60%","15%",false,"Keys: 120°-180°"),
                        node("nb","Node B","server","15%","75%",false,"Keys: 180°-300°"),
                        node("nc","Node C","server","75%","75%",false,"Keys: 300°-60°"),
                        node("nd","Node D","server","75%","15%",true,"Keys: 60°-120°")
                    ))
                    .edges(List.of()).highlights(List.of("ring")).durationMs(1500).build()
            ))
            .comparison(ComparisonView.builder()
                .title("Modular Hashing vs Consistent Hashing")
                .without(ScenarioPanel.builder().label("Modular Hashing (key % N)")
                    .steps(List.of(AnimationStep.builder().order(1).title("Node added — 100% remapped")
                        .nodes(List.of(
                            node("n1","Node 1","server","30%","25%",true,"REMAPPING"),
                            node("n2","Node 2","server","70%","25%",true,"REMAPPING"),
                            node("n3","Node 3 (NEW)","server","50%","75%",true,"ALL keys change!")
                        ))
                        .edges(List.of(
                            edge("n1","n3","move 67% of keys","#ef4444",true,"forward"),
                            edge("n2","n3","move 67% of keys","#ef4444",true,"forward")
                        ))
                        .highlights(List.of("n1","n2")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Keys Migrated","~67% (100% remapped)","bad"),
                        metric("Cache Hit Rate","~0% during migration","bad"),
                        metric("Disruption","Full reshuffle","bad")
                    )).build())
                .with(ScenarioPanel.builder().label("Consistent Hashing")
                    .steps(List.of(AnimationStep.builder().order(1).title("Node added — 1/N keys move")
                        .nodes(List.of(
                            node("n1","Node 1","server","30%","25%",false,"Unchanged"),
                            node("n2","Node 2","server","70%","25%",false,"Unchanged"),
                            node("n3","Node 3 (NEW)","server","50%","75%",true,"~33% of keys")
                        ))
                        .edges(List.of(
                            edge("n1","n3","only adjacent keys","#22c55e",true,"forward")
                        ))
                        .highlights(List.of("n3")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Keys Migrated","~33% (1/N)","good"),
                        metric("Cache Hit Rate","~67% maintained","good"),
                        metric("Disruption","Minimal","good")
                    )).build())
                .build())
            .codeExample(CodeExample.builder()
                .language("java")
                .title("Consistent Hash Ring Implementation")
                .snippet("""
                    public class ConsistentHashRing {
                        private final TreeMap<Long, String> ring = new TreeMap<>();
                        private final int virtualNodes;
                        
                        public void addNode(String node) {
                            for (int i = 0; i < virtualNodes; i++) {
                                long hash = hash(node + "#" + i);
                                ring.put(hash, node);
                            }
                        }
                        
                        public String getNode(String key) {
                            if (ring.isEmpty()) return null;
                            long hash = hash(key);
                            // Clockwise lookup
                            Map.Entry<Long, String> entry =
                                ring.ceilingEntry(hash);
                            return entry != null
                                ? entry.getValue()
                                : ring.firstEntry().getValue(); // wrap around
                        }
                        
                        private long hash(String key) {
                            return Math.abs(key.hashCode());
                        }
                    }
                    """)
                .build())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  REPLICATION
    // ─────────────────────────────────────────────────────────────
    private BuildingBlock buildReplication() {
        return BuildingBlock.builder()
            .id("replication")
            .name("Replication")
            .icon("📋")
            .category("Reliability")
            .tagline("Keep copies of data for fault tolerance and read scaling")
            .description("Database replication maintains copies of data across multiple nodes. Primary-Replica is the most common pattern: Primary handles writes and replicates to replicas which serve reads. Enables high availability and read scaling.")
            .keyPoints(List.of(
                "Primary (Leader) handles all writes",
                "Replicas (Followers) serve read traffic",
                "Sync vs Async replication trade-offs",
                "Replication lag and read-your-writes consistency",
                "Automatic failover and leader election"
            ))
            .useCases(List.of("Read-heavy workloads", "Disaster recovery", "Multi-region deployments", "Analytics offloading"))
            .tradeoffs(List.of("Replication lag causes stale reads", "Write scaling still limited to primary", "Split-brain risk on failover"))
            .steps(List.of(
                AnimationStep.builder().order(1).title("Write goes to Primary")
                    .description("All writes are directed to the Primary node. Application never writes to replicas")
                    .nodes(List.of(
                        node("app","Application","server","5%","45%",true,null),
                        node("primary","Primary DB","db","40%","45%",false,null),
                        node("r1","Replica 1","db","75%","25%",false,null),
                        node("r2","Replica 2","db","75%","65%",false,null)
                    ))
                    .edges(List.of(edge("app","primary","WRITE: INSERT user...","#f59e0b",true,"forward")))
                    .highlights(List.of("app","primary")).durationMs(1500).build(),
                AnimationStep.builder().order(2).title("Primary replicates to replicas")
                    .description("Primary writes the change to its Write-Ahead Log (WAL) and streams it to replicas")
                    .nodes(List.of(
                        node("app","Application","server","5%","45%",false,null),
                        node("primary","Primary DB","db","40%","45%",true,"WAL streaming"),
                        node("r1","Replica 1","db","75%","25%",true,"Replicating..."),
                        node("r2","Replica 2","db","75%","65%",true,"Replicating...")
                    ))
                    .edges(List.of(
                        edge("primary","r1","WAL stream","#a855f7",true,"forward"),
                        edge("primary","r2","WAL stream","#a855f7",true,"forward")
                    ))
                    .highlights(List.of("primary","r1","r2")).durationMs(1500).build(),
                AnimationStep.builder().order(3).title("Reads served by Replicas")
                    .description("Read queries are load-balanced across replicas, offloading the primary for writes")
                    .nodes(List.of(
                        node("app","Application","server","5%","45%",true,null),
                        node("primary","Primary DB","db","40%","45%",false,"Writes only"),
                        node("r1","Replica 1","db","75%","25%",true,"READ ⚡"),
                        node("r2","Replica 2","db","75%","65%",true,"READ ⚡")
                    ))
                    .edges(List.of(
                        edge("app","r1","SELECT ...","#00d4ff",true,"forward"),
                        edge("r1","app","data","#22c55e",true,"forward")
                    ))
                    .highlights(List.of("r1","r2")).durationMs(1500).build(),
                AnimationStep.builder().order(4).title("Primary fails — replica promoted")
                    .description("Primary crashes. Automatic failover promotes Replica 1 to new Primary. Zero downtime!")
                    .nodes(List.of(
                        node("app","Application","server","5%","45%",true,null),
                        node("primary","Primary DB","db","40%","45%",false,"FAILED 💥"),
                        node("r1","Replica 1","db","75%","25%",true,"NEW PRIMARY ⭐"),
                        node("r2","Replica 2","db","75%","65%",false,"Following R1")
                    ))
                    .edges(List.of(
                        edge("app","r1","Redirect writes","#22c55e",true,"forward"),
                        edge("r1","r2","replicate","#a855f7",true,"forward")
                    ))
                    .highlights(List.of("r1")).durationMs(2000).build()
            ))
            .comparison(ComparisonView.builder()
                .title("No Replication vs Primary-Replica")
                .without(ScenarioPanel.builder().label("Single DB Node")
                    .steps(List.of(AnimationStep.builder().order(1).title("DB failure = total outage")
                        .nodes(List.of(
                            node("a","App","server","10%","50%",true,null),
                            node("db","DB (Single)","db","70%","50%",true,"FAILED 💥")
                        ))
                        .edges(List.of(edge("a","db","unreachable","#ef4444",true,"forward")))
                        .highlights(List.of("db")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Availability","~99.9% (8.7 hrs/yr)","bad"),
                        metric("Read Throughput","Limited to 1 node","bad"),
                        metric("Backup","Point-in-time snapshots only","bad")
                    )).build())
                .with(ScenarioPanel.builder().label("Primary + 2 Replicas")
                    .steps(List.of(AnimationStep.builder().order(1).title("Primary fails, replica takes over")
                        .nodes(List.of(
                            node("a","App","server","5%","50%",true,null),
                            node("p","Primary","db","40%","50%",false,"FAILED"),
                            node("r1","Replica 1 →New Primary","db","75%","30%",true,"Promoted ✓"),
                            node("r2","Replica 2","db","75%","70%",false,"Follower")
                        ))
                        .edges(List.of(edge("a","r1","failover","#22c55e",true,"forward")))
                        .highlights(List.of("r1")).durationMs(1500).build()
                    ))
                    .metrics(List.of(
                        metric("Availability","99.99% (52 min/yr)","good"),
                        metric("Read Throughput","3x (distributed)","good"),
                        metric("Backup","Continuous replication","good")
                    )).build())
                .build())
            .codeExample(CodeExample.builder()
                .language("yaml")
                .title("PostgreSQL Replication Config")
                .snippet("""
                    # primary postgresql.conf
                    wal_level = replica
                    max_wal_senders = 5
                    synchronous_commit = on  # sync replication
                    
                    # replica recovery.conf
                    standby_mode = on
                    primary_conninfo = 'host=primary port=5432 user=replicator'
                    trigger_file = '/tmp/promote_to_primary'
                    
                    # Spring DataSource routing
                    @Primary @Bean
                    DataSource writeDataSource() { return primaryDS(); }
                    
                    @Bean
                    DataSource readDataSource() { return replicaDS(); }
                    """)
                .build())
            .build();
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────
    private Node node(String id, String label, String type, String x, String y, boolean active, String metric) {
        return Node.builder().id(id).label(label).type(type).x(x).y(y).active(active).metric(metric).build();
    }

    private Edge edge(String from, String to, String label, String color, boolean animated, String direction) {
        return Edge.builder().from(from).to(to).label(label).color(color).animated(animated).direction(direction).build();
    }

    private Metric metric(String name, String value, String quality) {
        return Metric.builder().name(name).value(value).quality(quality).build();
    }
}
