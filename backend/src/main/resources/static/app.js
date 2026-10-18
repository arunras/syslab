/* =====================================================
   SysLab — Main Application JavaScript
   Connects to Spring Boot API, drives animations
   ===================================================== */

const API = '/api/blocks';

// ── App state ──────────────────────────────────────────
let allBlocks = [];
let currentBlock = null;
let currentStep = 0;
let isPlaying = false;
let playTimer = null;
let activeTab = 'animation';

// ── Boot ──────────────────────────────────────────────
window.addEventListener('DOMContentLoaded', () => {
  initBackground();
  loadBlocks();
  initSearch();
});

// ── Background canvas ─────────────────────────────────
function initBackground() {
  const canvas = document.getElementById('bg-canvas');
  const ctx = canvas.getContext('2d');

  function resize() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  }
  resize();
  window.addEventListener('resize', resize);

  const COLS = 40, ROWS = 30;
  function draw() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const cw = canvas.width / COLS, rh = canvas.height / ROWS;
    ctx.strokeStyle = 'rgba(0,212,255,0.06)';
    ctx.lineWidth = 1;
    // Vertical
    for (let c = 0; c <= COLS; c++) {
      ctx.beginPath(); ctx.moveTo(c * cw, 0); ctx.lineTo(c * cw, canvas.height); ctx.stroke();
    }
    // Horizontal
    for (let r = 0; r <= ROWS; r++) {
      ctx.beginPath(); ctx.moveTo(0, r * rh); ctx.lineTo(canvas.width, r * rh); ctx.stroke();
    }
    // Glow dots at intersections (random sample)
    const t = Date.now() * 0.001;
    ctx.fillStyle = 'rgba(0,212,255,0.15)';
    for (let c = 0; c <= COLS; c += 5) {
      for (let r = 0; r <= ROWS; r += 5) {
        const pulse = 0.5 + 0.5 * Math.sin(t + c * 0.3 + r * 0.5);
        ctx.globalAlpha = pulse * 0.4;
        ctx.beginPath();
        ctx.arc(c * cw, r * rh, 2, 0, Math.PI * 2);
        ctx.fill();
      }
    }
    ctx.globalAlpha = 1;
    requestAnimationFrame(draw);
  }
  draw();
}

// ── Data Loading ───────────────────────────────────────
async function loadBlocks() {
  try {
    const res = await fetch(API);
    allBlocks = await res.json();
    renderSidebar();
    renderTopicGrid();
  } catch (err) {
    console.error('Failed to load blocks:', err);
    showError('Failed to connect to backend. Make sure Spring Boot is running on port 8080.');
  }
}

function showError(msg) {
  const w = document.getElementById('welcome-screen');
  w.innerHTML = `<div style="text-align:center;padding:60px;color:#ef4444">
    <div style="font-size:48px;margin-bottom:16px">⚠️</div>
    <h2 style="margin-bottom:12px">Connection Error</h2>
    <p style="color:#94a3b8">${msg}</p>
  </div>`;
}

// ── Sidebar ────────────────────────────────────────────
function renderSidebar() {
  const nav = document.getElementById('sidebar-nav');
  nav.innerHTML = allBlocks.map(b => `
    <div class="nav-item" id="nav-${b.id}" onclick="selectBlock('${b.id}')">
      <span class="nav-icon">${b.icon}</span>
      <div class="nav-text">
        <div class="nav-name">${b.name}</div>
        <div class="nav-category">${b.category}</div>
      </div>
    </div>
  `).join('');
  document.getElementById('stat-blocks').textContent = allBlocks.length;
}

// ── Topic grid (welcome) ───────────────────────────────
function renderTopicGrid() {
  const grid = document.getElementById('topic-grid');
  grid.innerHTML = allBlocks.map(b => `
    <div class="topic-card" onclick="selectBlock('${b.id}')">
      <div class="topic-card-icon">${b.icon}</div>
      <div class="topic-card-name">${b.name}</div>
      <div class="topic-card-cat">${b.category}</div>
    </div>
  `).join('');
}

// ── Search ─────────────────────────────────────────────
function initSearch() {
  const input = document.getElementById('search-input');
  input.addEventListener('input', () => {
    const q = input.value.toLowerCase().trim();
    document.querySelectorAll('.nav-item').forEach(el => {
      const txt = el.textContent.toLowerCase();
      el.classList.toggle('hidden', q && !txt.includes(q));
    });
  });
}

// ── Select block ───────────────────────────────────────
async function selectBlock(id) {
  stopPlay();

  // Active nav highlight
  document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
  const navEl = document.getElementById('nav-' + id);
  if (navEl) { navEl.classList.add('active'); navEl.scrollIntoView({ block: 'nearest' }); }

  // Show block view, hide welcome
  document.getElementById('welcome-screen').classList.add('hidden');
  const view = document.getElementById('block-view');
  view.classList.remove('hidden');

  // Fetch full block data
  const res = await fetch(`${API}/${id}`);
  currentBlock = await res.json();
  currentStep = 0;
  activeTab = 'animation';

  // Populate header
  document.getElementById('bh-icon').textContent = currentBlock.icon;
  document.getElementById('bh-category').textContent = currentBlock.category;
  document.getElementById('bh-title').textContent = currentBlock.name;
  document.getElementById('bh-tagline').textContent = currentBlock.tagline;

  // Reset tabs
  switchTab('animation');

  // Render animation
  renderAnimation();

  // Render concepts
  renderConcepts();

  // Render code
  renderCode();

  // Render comparison (lazy)
}

// ── Tab switching ──────────────────────────────────────
function switchTab(tab) {
  activeTab = tab;
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.tab-content').forEach(p => p.classList.add('hidden'));
  document.getElementById('tab-' + tab)?.classList.add('active');
  document.getElementById('panel-' + tab)?.classList.remove('hidden');

  if (tab === 'comparison') renderComparison();
}

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.tab').forEach(t => {
    t.addEventListener('click', () => switchTab(t.dataset.tab));
  });
});

// ── Animation rendering ────────────────────────────────
function renderAnimation() {
  if (!currentBlock?.steps?.length) return;
  const steps = currentBlock.steps;

  // Step dots
  const dotsEl = document.getElementById('step-dots');
  dotsEl.innerHTML = steps.map((_, i) =>
    `<div class="step-dot ${i === 0 ? 'active' : ''}" onclick="goToStep(${i})"></div>`
  ).join('');

  renderStep(0);
}

function renderStep(idx) {
  if (!currentBlock?.steps) return;
  const steps = currentBlock.steps;
  if (idx < 0 || idx >= steps.length) return;
  currentStep = idx;

  const step = steps[idx];

  // Counter
  document.getElementById('step-counter').textContent = `Step ${idx + 1} of ${steps.length}`;

  // Step info
  document.getElementById('anim-step-title').textContent = step.title;
  document.getElementById('anim-step-desc').textContent = step.description;

  // Dots
  document.querySelectorAll('#step-dots .step-dot').forEach((d, i) => {
    d.classList.toggle('active', i === idx);
  });

  // Buttons
  document.getElementById('btn-prev').disabled = idx === 0;
  document.getElementById('btn-next').disabled = idx === steps.length - 1;

  // Render stage
  renderStage('anim-stage', 'anim-svg', step.nodes, step.edges);
}

function renderStage(stageId, svgId, nodes, edges) {
  const stage = document.getElementById(stageId);
  const svg = document.getElementById(svgId);

  // Clear existing nodes (not the SVG)
  stage.querySelectorAll('.anim-node').forEach(n => n.remove());

  // Render nodes
  (nodes || []).forEach(n => {
    const el = document.createElement('div');
    el.className = `anim-node node-type-${n.type}`;
    el.id = `node-${stageId}-${n.id}`;
    el.style.left = n.x;
    el.style.top = n.y;
    el.innerHTML = `
      <div class="node-circle ${n.active ? 'active' : ''}"></div>
      <div class="node-info">
        <div class="node-label">${n.label}</div>
        ${n.metric ? `<div class="node-metric">${n.metric}</div>` : ''}
      </div>
    `;
    stage.appendChild(el);
  });

  // Render edges after nodes are placed
  requestAnimationFrame(() => {
    const svgNS = 'http://www.w3.org/2000/svg';
    // Clear old paths & labels (keep defs)
    svg.querySelectorAll('.anim-edge').forEach(e => e.remove());

    const stageRect = stage.getBoundingClientRect();

    (edges || []).forEach((edge, i) => {
      const fromEl = stage.querySelector(`[id$="-${edge.from}"] .node-circle`);
      const toEl   = stage.querySelector(`[id$="-${edge.to}"] .node-circle`);
      if (!fromEl || !toEl) return;

      const fr = fromEl.getBoundingClientRect();
      const tr = toEl.getBoundingClientRect();

      const x1 = fr.left + fr.width / 2 - stageRect.left;
      const y1 = fr.top + fr.height / 2 - stageRect.top;
      const x2 = tr.left + tr.width / 2 - stageRect.left;
      const y2 = tr.top + tr.height / 2 - stageRect.top;

      // Shorten to circle edge
      const dx = x2 - x1, dy = y2 - y1;
      const len = Math.sqrt(dx * dx + dy * dy);
      const r = 36;
      const ratio1 = r / len, ratio2 = r / len;
      const sx = x1 + dx * ratio1, sy = y1 + dy * ratio1;
      const ex = x2 - dx * ratio2, ey = y2 - dy * ratio2;

      // Slight curve
      const mx = (sx + ex) / 2 - dy * 0.08;
      const my = (sy + ey) / 2 + dx * 0.08;

      const g = document.createElementNS(svgNS, 'g');
      g.classList.add('anim-edge');

      // Arrow marker id based on color
      const markerSuffix = colorToMarker(edge.color, svgId);
      const path = document.createElementNS(svgNS, 'path');
      path.setAttribute('d', `M ${sx} ${sy} Q ${mx} ${my} ${ex} ${ey}`);
      path.setAttribute('stroke', edge.color || '#00d4ff');
      path.setAttribute('class', 'edge-path');
      path.setAttribute('marker-end', `url(#${markerSuffix})`);
      g.appendChild(path);

      // Label
      if (edge.label) {
        const text = document.createElementNS(svgNS, 'text');
        text.setAttribute('x', mx);
        text.setAttribute('y', my - 6);
        text.setAttribute('text-anchor', 'middle');
        text.classList.add('anim-edge-label');
        text.textContent = edge.label;
        g.appendChild(text);
      }

      svg.appendChild(g);
    });
  });
}

function colorToMarker(color, svgId) {
  const prefix = svgId === 'anim-svg' ? 'arrow' :
                 svgId === 'cmp-svg-without' ? 'cmp-arrow' + '-w' :
                 'cmp-arrow' + '-v';

  if (!color) return svgId === 'anim-svg' ? 'arrow-cyan' : prefix + '-cyan';
  if (color.includes('22c55e') || color.includes('green')) return prefix.replace('-w','').replace('-v','') + (svgId !== 'anim-svg' ? (svgId.includes('without') ? '-w' : '-v') : '') + '-green';

  const map = {
    '#00d4ff': 'cyan', '#22c55e': 'green', '#a855f7': 'purple',
    '#f59e0b': 'orange', '#ef4444': 'red'
  };
  const suffix = map[color] || 'cyan';
  if (svgId === 'anim-svg') return `arrow-${suffix}`;
  const side = svgId.includes('without') ? 'w' : 'v';
  return `cmp-arrow-${suffix}-${side}`;
}

// ── Playback ──────────────────────────────────────────
function nextStep() {
  if (!currentBlock) return;
  if (currentStep < currentBlock.steps.length - 1) renderStep(currentStep + 1);
  else stopPlay();
}

function prevStep() {
  if (!currentBlock) return;
  if (currentStep > 0) renderStep(currentStep - 1);
}

function goToStep(i) { renderStep(i); }

function togglePlay() {
  if (isPlaying) stopPlay();
  else startPlay();
}

function startPlay() {
  isPlaying = true;
  document.getElementById('play-label').textContent = 'Pause';
  document.getElementById('play-icon').innerHTML = '<rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/>';

  function tick() {
    if (currentStep < currentBlock.steps.length - 1) {
      renderStep(currentStep + 1);
      const dur = currentBlock.steps[currentStep]?.durationMs || 2000;
      playTimer = setTimeout(tick, dur);
    } else {
      stopPlay();
    }
  }

  const dur = currentBlock.steps[currentStep]?.durationMs || 2000;
  playTimer = setTimeout(tick, dur);
}

function stopPlay() {
  isPlaying = false;
  clearTimeout(playTimer);
  document.getElementById('play-label').textContent = 'Play';
  document.getElementById('play-icon').innerHTML = '<polygon points="5,3 19,12 5,21"/>';
}

// ── Comparison ─────────────────────────────────────────
function renderComparison() {
  if (!currentBlock?.comparison) return;
  const cmp = currentBlock.comparison;

  document.getElementById('cmp-title').textContent = cmp.title;
  document.getElementById('cmp-without-label').textContent = cmp.without.label;
  document.getElementById('cmp-with-label').textContent = cmp.with.label;

  // Render both panels
  renderCompPanel('cmp-stage-without', 'cmp-svg-without', cmp.without);
  renderCompPanel('cmp-stage-with', 'cmp-svg-with', cmp.with);
}

function renderCompPanel(stageId, svgId, panel) {
  const step = panel.steps?.[0];
  if (step) renderStage(stageId, svgId, step.nodes, step.edges);

  const metricsEl = document.getElementById(stageId === 'cmp-stage-without' ? 'cmp-metrics-without' : 'cmp-metrics-with');
  metricsEl.innerHTML = (panel.metrics || []).map(m => `
    <div class="metric-row">
      <span class="metric-name">${m.name}</span>
      <span class="metric-value ${m.quality}">${m.value}</span>
    </div>
  `).join('');
}

// ── Concepts ───────────────────────────────────────────
function renderConcepts() {
  if (!currentBlock) return;
  document.getElementById('concept-desc').textContent = currentBlock.description;

  document.getElementById('concept-keypoints').innerHTML =
    (currentBlock.keyPoints || []).map(k => `<li>${k}</li>`).join('');

  document.getElementById('concept-usecases').innerHTML =
    (currentBlock.useCases || []).map(u => `<li>${u}</li>`).join('');

  document.getElementById('concept-tradeoffs').innerHTML =
    (currentBlock.tradeoffs || []).map(t => `<li>${t}</li>`).join('');
}

// ── Code ──────────────────────────────────────────────
function renderCode() {
  if (!currentBlock?.codeExample) return;
  const ex = currentBlock.codeExample;
  document.getElementById('code-title').textContent = ex.title;
  document.getElementById('code-lang').textContent = ex.language;
  document.getElementById('code-snippet').textContent = ex.snippet;
}

function copyCode() {
  const code = document.getElementById('code-snippet').textContent;
  navigator.clipboard.writeText(code).then(() => {
    const btn = document.getElementById('copy-btn');
    btn.classList.add('copied');
    btn.querySelector('span') ? btn.querySelector('span').textContent = 'Copied!' :
      btn.innerHTML = btn.innerHTML.replace('Copy', 'Copied! ✓');
    setTimeout(() => {
      btn.classList.remove('copied');
      btn.innerHTML = btn.innerHTML.replace('Copied! ✓', 'Copy');
    }, 2000);
  });
}

// ── Keyboard shortcuts ────────────────────────────────
window.addEventListener('keydown', e => {
  if (e.target.tagName === 'INPUT') return;
  if (e.key === 'ArrowRight' || e.key === 'l') nextStep();
  if (e.key === 'ArrowLeft' || e.key === 'h') prevStep();
  if (e.key === ' ') { e.preventDefault(); togglePlay(); }
  if (e.key === 'Escape') {
    document.getElementById('block-view').classList.add('hidden');
    document.getElementById('welcome-screen').classList.remove('hidden');
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    stopPlay();
  }
});
