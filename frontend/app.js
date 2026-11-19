const API = window.SITE.api.replace(/\/$/, '');
const CATS = ['All', 'Sarees', 'Jewellery', 'Embroidery designs', 'Cloth pieces', "Women's essentials"];
const SUBS = ['All jewellery', 'Necklace', 'Earrings', 'Bangles'];
const KIND = { 'Sarees': 'saree', 'Jewellery': 'jewel', 'Embroidery designs': 'emb', 'Cloth pieces': 'cloth', "Women's essentials": 'ess' };
let cat = 'All', sub = 'All jewellery';
const $ = s => document.querySelector(s);
const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const money = n => '₹' + Number(n).toLocaleString('en-IN');

function chips(el, list, cur, key) {
  el.innerHTML = list.map(c => `<button class="chip" aria-pressed="${c === cur}" data-${key}="${esc(c)}">${esc(c)}</button>`).join('');
}

function card(p) {
  const pic = p.image_url
    ? `<img src="${esc(p.image_url)}" alt="${esc(p.name)}" loading="lazy">`
    : '';
  return `<article class="card"><div class="pic ${p.image_url ? '' : KIND[p.category] || ''}">${pic}${p.in_stock ? '' : '<span class="tag">Sold out</span>'}</div>
  <div class="info"><h3>${esc(p.name)}</h3><small>${esc(p.subcategory || p.category)}</small>
  ${p.description ? `<p>${esc(p.description)}</p>` : ''}<span class="price">${money(p.price)}</span></div></article>`;
}

async function load() {
  chips($('#cats'), CATS, cat, 'cat');
  $('#subs').hidden = cat !== 'Jewellery';
  chips($('#subs'), SUBS, sub, 'sub');
  const grid = $('#grid');
  grid.innerHTML = '<p class="note">Loading…</p>';
  const q = new URLSearchParams();
  if (cat !== 'All') q.set('category', cat);
  if (cat === 'Jewellery' && sub !== 'All jewellery') q.set('sub', sub);
  try {
    const res = await fetch(`${API}/api/products?${q}`);
    if (!res.ok) throw new Error();
    const items = await res.json();
    grid.innerHTML = items.length ? items.map(card).join('') : '<p class="note">Nothing here yet. New pieces are added soon.</p>';
  } catch {
    grid.innerHTML = '<p class="note">Could not load products. Check your connection and refresh the page.</p>';
  }
}

document.addEventListener('click', e => {
  const c = e.target.closest('[data-cat]');
  if (c) { cat = c.dataset.cat; sub = 'All jewellery'; load(); return; }
  const s = e.target.closest('[data-sub]');
  if (s) { sub = s.dataset.sub; load(); }
});

const o = window.SITE.owner;
for (const [id, v] of [['o-name', o.name], ['o-phone', o.phone], ['o-address', o.address]]) {
  const el = $('#' + id);
  if (v) el.textContent = v; else el.className = 'blank';
}
load();
