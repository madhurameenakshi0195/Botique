const API = window.SITE.api.replace(/\/$/, '');
const CATS = { 'Sarees': [], 'Jewellery': ['Necklace', 'Earrings', 'Bangles'], 'Embroidery designs': [], 'Cloth pieces': [], "Women's essentials": [] };
const KIND = { 'Sarees': 'saree', 'Jewellery': 'jewel', 'Embroidery designs': 'emb', 'Cloth pieces': 'cloth', "Women's essentials": 'ess' };
const $ = s => document.querySelector(s);
const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
let token = localStorage.getItem('madhura_admin_token') || '';
let items = [], editingId = null;

function say(el, text, kind) { el.textContent = text || ''; el.className = 'msg' + (kind ? ' ' + kind : ''); }

async function api(path, opts = {}) {
  const res = await fetch(API + path, {
    ...opts,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) }
  });
  if (res.status === 401 && token && path !== '/api/admin/login') { logout(); throw new Error('Your session ended. Please log in again.'); }
  if (res.status === 204) return null;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || 'Something went wrong.');
  return data;
}

function show() {
  const on = !!token;
  $('#login').hidden = on; $('#app').hidden = !on; $('#logout').hidden = !on;
  if (on) refresh();
}
function logout() { token = ''; localStorage.removeItem('madhura_admin_token'); show(); }

$('#loginForm').addEventListener('submit', async e => {
  e.preventDefault();
  say($('#loginMsg'), 'Logging in…');
  try {
    const d = await api('/api/admin/login', { method: 'POST', body: JSON.stringify({ email: $('#email').value, password: $('#password').value }) });
    token = d.token; localStorage.setItem('madhura_admin_token', token);
    $('#password').value = ''; say($('#loginMsg'), ''); show();
  } catch (err) { say($('#loginMsg'), err.message, 'err'); }
});
$('#logout').addEventListener('click', e => { e.preventDefault(); logout(); });

$('#f-cat').innerHTML = Object.keys(CATS).map(c => `<option>${esc(c)}</option>`).join('');
$('#f-sub').innerHTML = CATS.Jewellery.map(c => `<option>${c}</option>`).join('');
function syncSub() { $('#subWrap').hidden = $('#f-cat').value !== 'Jewellery'; }
$('#f-cat').addEventListener('change', syncSub);

function openForm(p) {
  editingId = p ? p.id : null;
  $('#formTitle').textContent = p ? 'Edit product' : 'Add product';
  $('#f-name').value = p ? p.name : '';
  $('#f-cat').value = p ? p.category : 'Sarees';
  syncSub();
  $('#f-sub').value = p && p.subcategory ? p.subcategory : 'Necklace';
  $('#f-price').value = p ? p.price : '';
  $('#f-img').value = p ? p.image_url : '';
  $('#f-desc').value = p ? p.description : '';
  $('#f-stock').checked = p ? p.in_stock : true;
  say($('#formMsg'), '');
  $('#formPanel').hidden = false;
  $('#formPanel').scrollIntoView({ behavior: 'smooth', block: 'start' });
}
function closeForm() { $('#formPanel').hidden = true; editingId = null; }
$('#newBtn').addEventListener('click', () => openForm(null));
$('#cancelBtn').addEventListener('click', closeForm);

$('#pform').addEventListener('submit', async e => {
  e.preventDefault();
  const body = {
    name: $('#f-name').value, category: $('#f-cat').value, subcategory: $('#f-sub').value,
    price: $('#f-price').value, image_url: $('#f-img').value, description: $('#f-desc').value, in_stock: $('#f-stock').checked
  };
  say($('#formMsg'), 'Saving…');
  try {
    await api(editingId ? `/api/products/${editingId}` : '/api/products', { method: editingId ? 'PUT' : 'POST', body: JSON.stringify(body) });
    closeForm(); say($('#listMsg'), 'Product saved.', 'ok'); refresh();
  } catch (err) { say($('#formMsg'), err.message, 'err'); }
});

function row(p) {
  const pic = p.image_url ? `<img src="${esc(p.image_url)}" alt="" style="width:100%;height:100%;object-fit:cover">` : '';
  return `<div class="item"><div class="thumb"><div class="pic ${p.image_url ? '' : KIND[p.category] || ''}">${pic}</div></div>
  <div class="meta"><strong>${esc(p.name)}</strong><span>${esc(p.subcategory || p.category)} · ₹${Number(p.price).toLocaleString('en-IN')}${p.in_stock ? '' : ' · Sold out'}</span></div>
  <div class="acts"><button class="btn sm line" data-edit="${p.id}">Edit</button><button class="btn sm danger" data-del="${p.id}">Delete</button></div></div>`;
}
async function refresh() {
  try {
    items = await api('/api/products');
    $('#list').innerHTML = items.length ? items.map(row).join('') : '<p class="note">No products yet. Tap "Add product" to start.</p>';
  } catch (err) { say($('#listMsg'), err.message, 'err'); }
}
$('#list').addEventListener('click', async e => {
  const ed = e.target.closest('[data-edit]'), del = e.target.closest('[data-del]');
  if (ed) openForm(items.find(p => p.id === +ed.dataset.edit));
  if (del && confirm('Delete this product? This cannot be undone.')) {
    try { await api(`/api/products/${del.dataset.del}`, { method: 'DELETE' }); say($('#listMsg'), 'Product deleted.', 'ok'); refresh(); }
    catch (err) { say($('#listMsg'), err.message, 'err'); }
  }
});
show();
