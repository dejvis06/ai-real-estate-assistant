const API_URL = 'http://localhost:8080/api/chat';

// Unique conversation ID per browser session
const conversationId = 'session-' + Date.now();

// DOM refs
const form         = document.getElementById('chatForm');
const input        = document.getElementById('messageInput');
const sendBtn      = document.getElementById('sendBtn');
const messagesEl   = document.getElementById('chatMessages');
const newChatBtn   = document.getElementById('newChatBtn');

// ── Helpers ───────────────────────────────────────────────

function now() {
  return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function removeWelcome() {
  const w = document.getElementById('welcomeState');
  if (w) w.remove();
}

function scrollToBottom() {
  messagesEl.scrollTop = messagesEl.scrollHeight;
}

function setLoading(loading) {
  sendBtn.disabled = loading;
  input.disabled   = loading;
}

// ── Typing indicator ──────────────────────────────────────

function showTyping() {
  removeWelcome();
  const wrapper = document.createElement('div');
  wrapper.className = 'message bot';
  wrapper.id = 'typingIndicator';
  wrapper.innerHTML = `
    <div class="avatar">🏠</div>
    <div class="bubble">
      <div class="typing-indicator"><span></span><span></span><span></span></div>
    </div>`;
  messagesEl.appendChild(wrapper);
  scrollToBottom();
}

function hideTyping() {
  document.getElementById('typingIndicator')?.remove();
}

// ── Render: plain text bubble ─────────────────────────────

function appendTextMessage(role, text, isError = false) {
  removeWelcome();
  const wrapper = document.createElement('div');
  wrapper.className = `message ${role}`;

  const avatar = document.createElement('div');
  avatar.className = 'avatar';
  avatar.textContent = role === 'user' ? 'You' : '🏠';

  const col = document.createElement('div');

  const bubble = document.createElement('div');
  bubble.className = 'bubble' + (isError ? ' error' : '');
  bubble.textContent = text;

  const time = document.createElement('div');
  time.className = 'msg-time';
  time.textContent = now();

  col.appendChild(bubble);
  col.appendChild(time);
  wrapper.appendChild(avatar);
  wrapper.appendChild(col);

  messagesEl.appendChild(wrapper);
  scrollToBottom();
}

// ── Render: property cards ────────────────────────────────

function formatPrice(price, currency) {
  if (!price) return '';
  const symbol = currency === 'EUR' ? '€' : currency === 'USD' ? '$' : currency === 'GBP' ? '£' : (currency + ' ');
  return symbol + price.toLocaleString();
}

function buildPropertyCard(p) {
  const image = (p.imageUrls && p.imageUrls.length > 0)
    ? p.imageUrls[0]
    : 'https://placehold.co/320x200/e2e8f0/94a3b8?text=No+Image';

  const bedsHtml   = p.bedrooms  != null ? `<span class="prop-stat">🛏 ${p.bedrooms} Bed${p.bedrooms !== 1 ? 's' : ''}</span>` : '';
  const bathsHtml  = p.bathrooms != null ? `<span class="prop-stat">🚿 ${p.bathrooms} Bath${p.bathrooms !== 1 ? 's' : ''}</span>` : '';
  const areaHtml   = p.area      != null ? `<span class="prop-stat">📐 ${p.area} m²</span>` : '';
  const statusHtml = p.status    ? `<span class="prop-badge prop-badge--${p.status.toLowerCase()}">${p.status}</span>` : '';
  const typeHtml   = p.listingType ? `<span class="prop-badge prop-badge--type">${p.listingType}</span>` : '';

  const card = document.createElement('div');
  card.className = 'prop-card';
  card.innerHTML = `
    <div class="prop-img-wrap">
      <img class="prop-img" src="${image}" alt="${p.title || 'Property'}" onerror="this.src='https://placehold.co/320x200/e2e8f0/94a3b8?text=No+Image'"/>
      <div class="prop-badges">${statusHtml}${typeHtml}</div>
    </div>
    <div class="prop-body">
      <div class="prop-price">${formatPrice(p.price, p.currency)}</div>
      <div class="prop-title">${p.title || p.referenceCode}</div>
      ${p.city ? `<div class="prop-location">📍 ${p.city}${p.country ? ', ' + p.country : ''}</div>` : ''}
      <div class="prop-stats">${bedsHtml}${bathsHtml}${areaHtml}</div>
      ${p.referenceCode ? `<div class="prop-ref">${p.referenceCode}</div>` : ''}
    </div>`;
  return card;
}

function appendPropertyResponse(data) {
  removeWelcome();

  const wrapper = document.createElement('div');
  wrapper.className = 'message bot';

  const avatar = document.createElement('div');
  avatar.className = 'avatar';
  avatar.textContent = '🏠';

  const col = document.createElement('div');
  col.style.minWidth = '0';
  col.style.flex = '1';

  // Intro message
  if (data.message) {
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    bubble.textContent = data.message;
    col.appendChild(bubble);
  }

  // Property grid
  if (data.properties && data.properties.length > 0) {
    const grid = document.createElement('div');
    grid.className = 'prop-grid';
    data.properties.forEach(p => grid.appendChild(buildPropertyCard(p)));
    col.appendChild(grid);
  }

  const time = document.createElement('div');
  time.className = 'msg-time';
  time.textContent = now();
  col.appendChild(time);

  wrapper.appendChild(avatar);
  wrapper.appendChild(col);
  messagesEl.appendChild(wrapper);
  scrollToBottom();
}

// ── API call ──────────────────────────────────────────────

async function sendMessage(userText) {
  setLoading(true);
  appendTextMessage('user', userText);
  showTyping();

  try {
    const res = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Conversation-Id': conversationId,
      },
      body: JSON.stringify({ message: userText }),
    });

    if (!res.ok) throw new Error(`Server error: ${res.status}`);

    const data = await res.json();
    hideTyping();

    if (data.type === 'property_list' && data.properties && data.properties.length > 0) {
      appendPropertyResponse(data);
    } else {
      appendTextMessage('bot', data.message || 'No response received.');
    }
  } catch (err) {
    hideTyping();
    const msg = err.message.includes('Failed to fetch')
      ? 'Cannot reach the server. Make sure the Spring Boot app is running on port 8080.'
      : err.message;
    appendTextMessage('bot', msg, true);
  } finally {
    setLoading(false);
  }
}

// ── Event listeners ───────────────────────────────────────

form.addEventListener('submit', (e) => {
  e.preventDefault();
  const text = input.value.trim();
  if (!text) return;
  input.value = '';
  sendMessage(text);
});

document.querySelectorAll('.suggestion-item').forEach((item) => {
  item.addEventListener('click', () => {
    const query = item.dataset.query;
    if (!query) return;
    input.value = query;
    form.dispatchEvent(new Event('submit'));
  });
});

newChatBtn.addEventListener('click', () => {
  messagesEl.innerHTML = '';
  const welcome = document.createElement('div');
  welcome.id = 'welcomeState';
  welcome.className = 'welcome-state';
  welcome.innerHTML = `
    <div class="welcome-icon">🏠</div>
    <h2>How can I help you find your perfect home?</h2>
    <p>Ask me about available properties, prices, locations, or schedule a viewing.</p>
  `;
  messagesEl.appendChild(welcome);
  input.focus();
});
