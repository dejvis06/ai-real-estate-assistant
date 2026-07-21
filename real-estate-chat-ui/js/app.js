const API_URL = 'http://localhost:8080/api/chat';

// Agent config injected per page
const AGENT_TYPE     = window.AGENT_CONFIG?.agentType    || 'PROPERTY';
const RESERVATION_ID = window.AGENT_CONFIG?.reservationId || null;

// Unique conversation ID per browser session
const conversationId = 'session-' + Date.now();

// DOM refs
const form       = document.getElementById('chatForm');
const input      = document.getElementById('messageInput');
const sendBtn    = document.getElementById('sendBtn');
const messagesEl = document.getElementById('chatMessages');
const newChatBtn = document.getElementById('newChatBtn');

// ── Welcome state content per agent ──────────────────────

const WELCOME = {
  PROPERTY: {
    icon: '🏡',
    heading: 'How can I help you find your perfect home?',
    body: 'Ask me about available properties, prices, locations, or schedule a viewing.',
  },
  RESERVATION: {
    icon: '📅',
    heading: 'How can I help you with your reservation?',
    body: 'Ask me about your viewing schedule, cancellation policies, or reservation status.',
  },
  FAQ: {
    icon: '💬',
    heading: 'Have a question? I\'m here to help.',
    body: 'Ask me anything about buying, selling, financing, or our services.',
  },
};

function welcomeContent() {
  return WELCOME[AGENT_TYPE] || WELCOME.FAQ;
}

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

function buildWelcomeEl() {
  const w = welcomeContent();
  const el = document.createElement('div');
  el.id = 'welcomeState';
  el.className = 'welcome-state';
  el.innerHTML = `
    <div class="welcome-icon">${w.icon}</div>
    <h2>${w.heading}</h2>
    <p>${w.body}</p>
  `;
  return el;
}

// ── Typing indicator ──────────────────────────────────────

function showTyping() {
  removeWelcome();
  const wrapper = document.createElement('div');
  wrapper.className = 'message bot';
  wrapper.id = 'typingIndicator';
  wrapper.innerHTML = `
    <div class="avatar">${welcomeContent().icon}</div>
    <div class="bubble">
      <div class="typing-indicator"><span></span><span></span><span></span></div>
    </div>`;
  messagesEl.appendChild(wrapper);
  scrollToBottom();
}

function hideTyping() {
  document.getElementById('typingIndicator')?.remove();
}

// ── Markdown renderer ─────────────────────────────────────
// Handles the subset AI models realistically produce in chat responses.
// Raw HTML is escaped first to prevent injection, then Markdown tokens
// are replaced with their HTML equivalents.

function renderMarkdown(text) {
  const escaped = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');

  return escaped
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')  // **bold**
    .replace(/\*(.+?)\*/g,     '<em>$1</em>')           // *italic*
    .replace(/\n/g,            '<br>');                 // line breaks
}

// ── Render: plain text bubble ─────────────────────────────

function appendTextMessage(role, text, isError = false) {
  removeWelcome();
  const wrapper = document.createElement('div');
  wrapper.className = `message ${role}`;

  const avatar = document.createElement('div');
  avatar.className = 'avatar';
  avatar.textContent = role === 'user' ? 'You' : welcomeContent().icon;

  const col = document.createElement('div');

  const bubble = document.createElement('div');
  bubble.className = 'bubble' + (isError ? ' error' : '');

  if (role === 'bot' && !isError) {
    // Bot responses may contain Markdown bold/italic from the AI model
    bubble.innerHTML = renderMarkdown(text);
  } else {
    // User messages and error messages are always plain text
    bubble.textContent = text;
  }

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

  const bedsHtml  = p.bedrooms  != null ? `<span class="prop-stat">🛏 ${p.bedrooms} Bed${p.bedrooms !== 1 ? 's' : ''}</span>` : '';
  const bathsHtml = p.bathrooms != null ? `<span class="prop-stat">🚿 ${p.bathrooms} Bath${p.bathrooms !== 1 ? 's' : ''}</span>` : '';
  const areaHtml  = p.area      != null ? `<span class="prop-stat">📐 ${p.area} m²</span>` : '';
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
  avatar.textContent = welcomeContent().icon;

  const col = document.createElement('div');
  col.style.minWidth = '0';
  col.style.flex = '1';

  if (data.message) {
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    bubble.textContent = data.message;
    col.appendChild(bubble);
  }

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
    const body = { message: userText, agentType: AGENT_TYPE };

    if (RESERVATION_ID !== null) {
      body.reservationId = RESERVATION_ID;
    }

    const res = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Conversation-Id': conversationId,
      },
      body: JSON.stringify(body),
    });

    if (!res.ok) throw new Error(`Server error: ${res.status}`);

    hideTyping();

    // All agents return JSON. The shape differs per agent:
    //   PROPERTY    → PropertyResponse { type, message, properties[] }
    //   FAQ         → TextResponse     { message }
    //   RESERVATION → TextResponse     { message }
    const data = await res.json();

    if (AGENT_TYPE === 'PROPERTY') {
      if (data.type === 'property_list' && data.properties && data.properties.length > 0) {
        appendPropertyResponse(data);
      } else {
        appendTextMessage('bot', data.message || 'No response received.');
      }
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
  messagesEl.appendChild(buildWelcomeEl());
  input.focus();
});
