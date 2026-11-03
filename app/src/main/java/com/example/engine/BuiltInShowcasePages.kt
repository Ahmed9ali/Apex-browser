package com.example.engine

data class SpeedDialDestination(
    val title: String,
    val subtitle: String,
    val url: String,
    val badge: String
)

object BuiltInShowcasePages {

    val speedDialItems = listOf(
        SpeedDialDestination(
            title = "AI News Pulse",
            subtitle = "Live DOM feed for Nano Agents",
            url = "apex://news",
            badge = "AI Demo"
        ),
        SpeedDialDestination(
            title = "Apex Gear Store",
            subtitle = "Product grid for Retriever",
            url = "apex://store",
            badge = "Scraper"
        ),
        SpeedDialDestination(
            title = "Hacker News",
            subtitle = "news.ycombinator.com",
            url = "https://news.ycombinator.com",
            badge = "Tech"
        ),
        SpeedDialDestination(
            title = "Wikipedia",
            subtitle = "en.wikipedia.org",
            url = "https://en.wikipedia.org",
            badge = "Wiki"
        ),
        SpeedDialDestination(
            title = "DuckDuckGo",
            subtitle = "duckduckgo.com",
            url = "https://duckduckgo.com",
            badge = "Private"
        ),
        SpeedDialDestination(
            title = "GitHub Trending",
            subtitle = "github.com/trending",
            url = "https://github.com/trending",
            badge = "Code"
        )
    )

    fun resolveHtml(url: String): Pair<String, String>? {
        return when (url.lowercase().trim()) {
            "apex://news" -> "https://news.apex.browser/" to AI_NEWS_HTML
            "apex://store" -> "https://store.apex.browser/" to GEAR_STORE_HTML
            else -> null
        }
    }

    private const val AI_NEWS_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>Apex AI &amp; Privacy Pulse — Top Stories Today</title>
<style>
  :root { color-scheme: light dark; }
  body {
    font-family: system-ui, -apple-system, sans-serif;
    margin: 0; padding: 20px 16px 96px;
    background: #0d1321; color: #eef2ff;
    line-height: 1.55;
  }
  header {
    padding: 18px; border-radius: 22px;
    background: linear-gradient(135deg, #1b2a4a, #132238);
    border: 1px solid rgba(120, 170, 255, 0.22);
    margin-bottom: 18px;
  }
  .kicker { font-size: 12px; font-weight: 700; text-transform: uppercase; color: #64dfdf; letter-spacing: 0.08em; }
  h1 { margin: 6px 0 6px; font-size: 24px; line-height: 1.25; }
  .filter-bar { display: flex; gap: 8px; flex-wrap: wrap; margin: 14px 0 18px; }
  button.topic-btn {
    background: #1e2f4f; color: #dbeafe; border: 1px solid #3b5998;
    border-radius: 999px; padding: 8px 14px; font-weight: 600; font-size: 13px; cursor: pointer;
  }
  button.topic-btn.active { background: #3b82f6; color: #ffffff; border-color: #60a5fa; }
  article.story-card {
    background: #151f33; border-radius: 20px; padding: 18px; margin-bottom: 14px;
    border: 1px solid rgba(255,255,255,0.08);
  }
  article.story-card h2 { margin: 4px 0 8px; font-size: 18px; color: #93c5fd; }
  article.story-card p { margin: 0 0 10px; font-size: 14px; color: #cbd5e1; }
  .meta { display: flex; justify-content: space-between; font-size: 12px; color: #94a3b8; }
  .price, .score { font-weight: 700; color: #5eead4; }
  a { color: #60a5fa; text-decoration: none; }
</style>
</head>
<body>
  <header>
    <div class="kicker">Live Curated Briefing</div>
    <h1>Top AI &amp; Web Architecture Breakthroughs Today</h1>
    <div style="font-size:13px;color:#94a3b8;">Zero-tracker reader edition • Try Nano Agent or Retriever on this page</div>
  </header>

  <div class="filter-bar">
    <button class="topic-btn active" onclick="filterTopic('all', this)">All Briefings</button>
    <button class="topic-btn" onclick="filterTopic('agents', this)">On-Device Agents</button>
    <button class="topic-btn" onclick="filterTopic('privacy', this)">Browser Privacy</button>
  </div>

  <main id="feed">
    <article class="story-card" data-topic="agents" data-product="story-1">
      <div class="meta"><span>#1 • On-Device AI</span><span class="score">984 pts</span></div>
      <h2><a href="https://en.wikipedia.org/wiki/Intelligent_agent">1. Sub-4B Local Browser Agents Surpass Cloud Latency Benchmarks by 4.2x</a></h2>
      <p>Researchers demonstrated a three-stage Planner, Navigator, and Validator multi-agent loop executing DOM reasoning directly inside mobile WebViews with zero server round-trips and full user data sovereignty.</p>
      <div class="meta"><span>4 min read</span><span>Source: OpenSystems Journal</span></div>
    </article>

    <article class="story-card" data-topic="privacy" data-product="story-2">
      <div class="meta"><span>#2 • Web Privacy</span><span class="score">841 pts</span></div>
      <h2><a href="https://en.wikipedia.org/wiki/HTTPS">2. Universal Ephemeral Storage &amp; Strict HTTPS Upgrades Cut Fingerprinting by 79%</a></h2>
      <p>Modern privacy browsers combining partitioned DOM storage, CNAME-cloaking defense, and local static filter lists save mobile users an average of 38% cellular bandwidth on media-heavy portals.</p>
      <div class="meta"><span>6 min read</span><span>Source: Web Standards Lab</span></div>
    </article>

    <article class="story-card" data-topic="agents" data-product="story-3">
      <div class="meta"><span>#3 • Structured Retrieval</span><span class="score">762 pts</span></div>
      <h2><a href="https://en.wikipedia.org/wiki/Web_scraping">3. Local-First Web Scrapers Turn Multi-Tab Research Into Instant Structured Tables</a></h2>
      <p>Parallel DOM selector pipelines allow analysts to capture pricing, citations, and specifications across five open tabs simultaneously without uploading browsing history to third-party APIs.</p>
      <div class="meta"><span>5 min read</span><span>Source: Distributed Computing Weekly</span></div>
    </article>

    <article class="story-card" data-topic="privacy" data-product="story-4">
      <div class="meta"><span>#4 • UI Engineering</span><span class="score">615 pts</span></div>
      <h2><a href="https://en.wikipedia.org/wiki/Material_Design">4. Material 3 Expressive Spring Physics Measurably Improve One-Handed Mobile Ergonomics</a></h2>
      <p>Spatial spring curves paired with bottom-anchored browser navigation reduce thumb reach strain and accelerate tab switching comprehension by 27% in mobile usability studies.</p>
      <div class="meta"><span>3 min read</span><span>Source: UX Motion Digest</span></div>
    </article>
  </main>

  <script>
    function filterTopic(topic, btn) {
      document.querySelectorAll('.topic-btn').forEach(b => b.classList.remove('active'));
      if (btn) btn.classList.add('active');
      document.querySelectorAll('.story-card').forEach(card => {
        card.style.display = (topic === 'all' || card.dataset.topic === topic) ? 'block' : 'none';
      });
    }
  </script>
</body>
</html>
    """

    private const val GEAR_STORE_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>Apex Hardware Lab — Privacy &amp; Compute Gear</title>
<style>
  body {
    font-family: system-ui, -apple-system, sans-serif;
    margin: 0; padding: 20px 16px 96px;
    background: #0b1520; color: #f1f5f9;
  }
  .banner {
    padding: 18px; border-radius: 22px;
    background: linear-gradient(135deg, #102a43, #1a365d);
    margin-bottom: 18px; border: 1px solid rgba(94, 234, 212, 0.25);
  }
  h1 { margin: 4px 0 6px; font-size: 23px; }
  .grid { display: grid; grid-template-columns: 1fr; gap: 14px; }
  .product-card {
    background: #162231; border-radius: 20px; padding: 16px;
    border: 1px solid rgba(255,255,255,0.09);
  }
  .product-title { margin: 0 0 6px; font-size: 17px; color: #7dd3fc; }
  .price { font-size: 20px; font-weight: 800; color: #34d399; margin: 6px 0; display: inline-block; }
  .spec { font-size: 13px; color: #cbd5e1; margin: 4px 0 10px; }
  .tag { font-size: 11px; padding: 3px 9px; border-radius: 999px; background: #1e3a5f; color: #bae6fd; }
  a.buy-link { font-size: 13px; font-weight: 600; color: #60a5fa; text-decoration: none; }
</style>
</head>
<body>
  <div class="banner">
    <span class="tag">Retriever Benchmark Catalog</span>
    <h1>Privacy &amp; Edge AI Hardware Catalog</h1>
    <div style="font-size:13px;color:#94a3b8;">Open Retriever Extension &rarr; Tap Capture Page to extract products &amp; prices</div>
  </div>

  <section class="grid">
    <div class="product-card" data-product="hw-1">
      <h2 class="product-title">Apex Titan Security Key NFC-C</h2>
      <div class="price">$59.00</div>
      <p class="spec">FIDO2 / WebAuthn hardware passkey with tamper-resistant secure element.</p>
      <a class="buy-link" href="https://store.apex.browser/titan-key">View Specification &rarr;</a>
    </div>

    <div class="product-card" data-product="hw-2">
      <h2 class="product-title">Neural NPU Pocket Accelerator M4</h2>
      <div class="price">$249.00</div>
      <p class="spec">38 TOPS USB4 local inference module for quantized multi-agent workflows.</p>
      <a class="buy-link" href="https://store.apex.browser/npu-m4">View Specification &rarr;</a>
    </div>

    <div class="product-card" data-product="hw-3">
      <h2 class="product-title">Faraday Travel Sleeve Pro 16</h2>
      <div class="price">$89.00</div>
      <p class="spec">Dual-layer RF/cellular/BLE/GPS zero-signal shielding ballistic nylon pouch.</p>
      <a class="buy-link" href="https://store.apex.browser/faraday-pro">View Specification &rarr;</a>
    </div>

    <div class="product-card" data-product="hw-4">
      <h2 class="product-title">CoreVault Encrypted NVMe 2TB</h2>
      <div class="price">$194.00</div>
      <p class="spec">Hardware AES-256-XTS keypad SSD with physical write-protect switch.</p>
      <a class="buy-link" href="https://store.apex.browser/corevault-2tb">View Specification &rarr;</a>
    </div>

    <div class="product-card" data-product="hw-5">
      <h2 class="product-title">OpenRouter Travel WireGuard Mini</h2>
      <div class="price">$119.00</div>
      <p class="spec">Wi-Fi 7 pocket privacy router with DNS-over-HTTPS and ad-block gateway.</p>
      <a class="buy-link" href="https://store.apex.browser/wireguard-mini">View Specification &rarr;</a>
    </div>
  </section>
</body>
</html>
    """
}
