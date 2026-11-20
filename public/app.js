const storageKey = 'nexomarket-demo-v1';
const categories = ['Todos', 'Computadoras', 'Celulares', 'Audio', 'Accesorios'];

const app = document.querySelector('#app');
let store = readStore();
let user = null;
let authMode = 'login';
let authNotice = '';
let productsStatus = 'idle';
let productsError = '';
let customerOrders = [];
let ordersStatus = 'idle';
let ordersError = '';
let selectedOrderStatus = 'TODOS';
let activeView = 'catalog';
let activeCategory = 'Todos';
let searchTerm = '';
let cart = [];
let cartOpen = false;
let checkoutOpen = false;
let toastTimer;
// Panel de pedidos del administrador (State, Command, Memento, Observer) y consulta con Proxy
let adminOrders = [];
let adminOrdersStatus = 'idle';
let orderHistory = null;
let proxyLookup = null;
const orderStatuses = ['PENDIENTE', 'PAGADO', 'PREPARANDO', 'ENVIADO', 'ENTREGADO', 'CANCELADO'];
const orderActions = [
  { action: 'pagar', label: 'Pagar' },
  { action: 'preparar', label: 'Preparar' },
  { action: 'enviar', label: 'Enviar' },
  { action: 'entregar', label: 'Entregar' },
  { action: 'cancelar', label: 'Cancelar' },
];

async function callApi(url, options = {}) {
  try {
    const response = await fetch(url, options);
    const data = await response.json().catch(() => ({}));
    return { ok: response.ok, status: response.status, data };
  } catch {
    return { ok: false, status: 0, data: { message: 'No se pudo conectar con el servidor.' } };
  }
}

async function loadAdminOrders() {
  adminOrdersStatus = 'loading';
  render();
  const result = await callApi('/api/orders');
  adminOrders = result.ok ? result.data : [];
  adminOrdersStatus = result.ok ? 'ready' : 'error';
  render();
}

async function loadOrderHistory(orderId) {
  const result = await callApi(`/api/orders/${orderId}/historial`);
  if (!result.ok) {
    notify(result.data.message || 'No se pudo cargar el historial.');
    return;
  }
  orderHistory = result.data;
  render();
}

async function runOrderAction(orderId, url, body) {
  const result = await callApi(url, body
    ? { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }
    : { method: 'POST' });
  if (!result.ok) {
    notify(result.data.message || 'La operación no se pudo completar.');
    return;
  }
  notify(`Pedido ${result.data.codigo}: ahora está ${result.data.estado}.`);
  await loadAdminOrders();
  await loadOrderHistory(orderId);
}

function readStore() {
  try {
    const saved = JSON.parse(localStorage.getItem(storageKey));
    if (saved && Array.isArray(saved.products) && Array.isArray(saved.orders)) {
      return { ...saved, products: [] };
    }
  } catch {}
  return { products: [], orders: [] };
}

function saveStore() {
  localStorage.setItem(storageKey, JSON.stringify(store));
}

function imageForCategory(category) {
  const images = {
    Computadoras: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=900&q=85',
    Celulares: 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?auto=format&fit=crop&w=900&q=85',
    Audio: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=85',
    Accesorios: 'https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=900&q=85',
  };
  return images[category] ?? images.Accesorios;
}

async function loadProducts() {
  productsStatus = 'loading';
  productsError = '';
  store.products = [];
  render();

  try {
    const response = await fetch('/api/products');
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const products = await response.json();
    store.products = products.map(product => ({ ...product, image: imageForCategory(product.category) }));
    productsStatus = 'ready';
  } catch {
    productsStatus = 'error';
    productsError = 'No se pudo cargar el catálogo desde PostgreSQL.';
  }
  render();
}

async function loadCustomerOrders() {
  ordersStatus = 'loading';
  ordersError = '';
  customerOrders = [];
  render();

  const query = selectedOrderStatus === 'TODOS'
    ? ''
    : `?estado=${encodeURIComponent(selectedOrderStatus)}`;

  try {
    const response = await fetch(`/api/orders${query}`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    customerOrders = await response.json();
    ordersStatus = 'ready';
  } catch {
    ordersStatus = 'error';
    ordersError = 'No se pudieron cargar los pedidos desde PostgreSQL.';
  }
  render();
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[character]);
}

function money(value) {
  return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(value);
}

function brand() {
  return '<a class="brand" href="#inicio" aria-label="NexoMarket inicio"><span class="brand-mark">N</span><span>Nexo<span>Market</span></span></a>';
}

function renderLogin() {
  const registering = authMode === 'register';
  app.innerHTML = `
    <main class="login-shell">
      <section class="login-story">
        ${brand()}
        <p class="eyebrow">NEXOMARKET · RETAIL DIGITAL</p>
        <h1>Lo que buscas, más cerca.</h1>
        <p>Explora tecnología, arma tu pedido y sigue cada etapa de la compra en un solo lugar.</p>
        <div class="login-art">
          <img src="https://images.unsplash.com/photo-1498049794561-7780e7231661?auto=format&fit=crop&w=1400&q=85" alt="Selección de productos tecnológicos">
          <span class="login-art-label">Tecnología para todos los días</span>
        </div>
      </section>
      <section class="login-panel" aria-labelledby="login-title">
        <p class="eyebrow">BIENVENIDO</p>
        <h2 id="login-title">${registering ? 'Crear cuenta' : 'Iniciar sesión'}</h2>
        <p>${registering ? 'Regístrate para comprar en NexoMarket.' : 'Ingresa con tu cuenta para continuar.'}</p>
        <form data-form="${registering ? 'register' : 'login'}">
          ${registering ? '<label class="field"><span>Nombre completo</span><input name="name" autocomplete="name" maxlength="80" placeholder="Tu nombre" required></label>' : ''}
          <label class="field"><span>Correo electrónico</span><input name="email" type="email" autocomplete="username" placeholder="nombre@correo.com" required></label>
          <label class="field"><span>Contraseña</span><input name="password" type="password" autocomplete="${registering ? 'new-password' : 'current-password'}" minlength="8" placeholder="${registering ? 'Mínimo 8 caracteres' : 'Ingresa tu contraseña'}" required></label>
          <p class="login-error ${authNotice.startsWith('Cuenta creada') ? 'success' : ''}" role="alert">${escapeHtml(authNotice)}</p>
          <button class="button login-submit" type="submit">${registering ? 'Registrarme' : 'Iniciar sesión'}</button>
        </form>
        <div class="auth-footer">
          ${registering
            ? '¿Ya tienes cuenta? <button class="text-button" type="button" data-auth-mode="login">Inicia sesión</button>'
            : '¿No tienes cuenta? <button class="text-button" type="button" data-auth-mode="register">Regístrate</button>'}
        </div>
        <div class="demo-note">${registering
          ? 'Las cuentas nuevas se registran como Cliente. Los permisos de Administrador se asignan desde la base de datos.'
          : 'El acceso y el perfil se validan en la base de datos.'}</div>
      </section>
    </main>`;
}

function header() {
  const links = user.role === 'Administrador'
    ? [['admin', 'Panel'], ['inventory', 'Inventario'], ['orders', 'Pedidos']]
    : [['catalog', 'Tienda'], ['my-orders', 'Mis pedidos']];
  return `
    <header class="app-header">
      ${brand()}
      <nav class="app-nav" aria-label="Navegación principal">
        ${links.map(([view, label]) => `<button type="button" data-view="${view}" aria-current="${activeView === view ? 'page' : 'false'}">${label}</button>`).join('')}
      </nav>
      <div class="header-actions">
        ${user.role === 'Cliente' ? `<button class="button secondary small" type="button" data-action="cart-open">Mi bolsa (${cart.reduce((total, line) => total + line.quantity, 0)})</button>` : ''}
        <div class="user-chip"><strong>${escapeHtml(user.name)}</strong><small>${escapeHtml(user.role)}</small></div>
        <button class="button dark small" type="button" data-action="logout">Salir</button>
      </div>
    </header>`;
}

function footer() {
  return '<footer class="footer-note">Catálogo conectado a PostgreSQL · Checkout de demostración</footer>';
}

function renderProducts() {
  const matchingProducts = store.products.filter(product => {
    const matchesCategory = activeCategory === 'Todos' || product.category === activeCategory;
    const matchesSearch = `${product.name} ${product.category}`.toLocaleLowerCase('es').includes(searchTerm.toLocaleLowerCase('es'));
    return matchesCategory && matchesSearch && product.stock > 0;
  });

  return `
    <main class="page">
      <div class="page-heading">
        <div><p class="eyebrow">NEXOMARKET · CATÁLOGO</p><h1>Encuentra tu próximo favorito.</h1><p>Productos disponibles para tu siguiente pedido.</p></div>
        <form class="toolbar" data-form="search"><label class="search-box"><span aria-hidden="true">⌕</span><input name="query" value="${escapeHtml(searchTerm)}" placeholder="Buscar en la tienda" aria-label="Buscar en la tienda"></label><button class="button secondary" type="submit">Buscar</button></form>
      </div>
      <div class="category-tabs" role="group" aria-label="Filtrar por categoría">
        ${categories.map(category => `<button type="button" data-category="${category}" aria-pressed="${activeCategory === category}">${category}</button>`).join('')}
      </div>
      <section class="product-grid" aria-label="Productos">
        ${productsStatus === 'loading' ? '<p class="empty-state">Cargando productos desde PostgreSQL…</p>' : ''}
        ${productsStatus === 'error' ? `<p class="empty-state">${escapeHtml(productsError)}</p>` : ''}
        ${productsStatus === 'ready' && matchingProducts.length ? matchingProducts.map(product => `
          <article class="product-card">
            <div class="product-image"><img src="${escapeHtml(product.image)}" alt="${escapeHtml(product.name)}" loading="lazy"><span class="stock-tag ${product.stock < 5 ? 'low' : ''}">${product.stock < 5 ? `Últimas ${product.stock}` : 'Disponible'}</span></div>
            <div class="product-info"><span class="product-category">${escapeHtml(product.category)}</span><h2 class="product-name">${escapeHtml(product.name)}</h2><div class="product-bottom"><strong class="product-price">${money(product.price)}</strong><button class="button small" type="button" data-add-cart="${product.id}">Agregar</button></div></div>
          </article>`).join('') : ''}
        ${productsStatus === 'ready' && !matchingProducts.length ? `<p class="empty-state">${store.products.length ? 'No encontramos productos con esos filtros.' : 'La base de datos aún no tiene productos.'}</p>` : ''}
      </section>
    </main>`;
}

function renderOrders() {
  return `
    <main class="page">
      <div class="page-heading">
        <div><p class="eyebrow">CUENTA · CLIENTE</p><h1>Mis pedidos</h1><p>Consulta el estado de tus compras.</p></div>
        <div class="toolbar">
          <form class="toolbar" data-form="order-filter">
            <label class="field"><span>Estado</span><select name="status" aria-label="Filtrar pedidos por estado">
              ${['TODOS', ...orderStatuses].map(status => `<option value="${status}" ${selectedOrderStatus === status ? 'selected' : ''}>${status === 'TODOS' ? 'Todos' : status}</option>`).join('')}
            </select></label>
            <button class="button secondary" type="submit">Filtrar</button>
          </form>
          <button class="button secondary" data-view="catalog" type="button">Volver a la tienda</button>
        </div>
      </div>
      <section class="order-list">
        ${ordersStatus === 'loading' ? '<p class="empty-state">Cargando pedidos…</p>' : ''}
        ${ordersStatus === 'error' ? `<p class="empty-state">${escapeHtml(ordersError)}</p>` : ''}
        ${ordersStatus === 'ready' && customerOrders.length ? customerOrders.map(order => `<article class="order-row"><div><h3>Pedido #${order.id}</h3><p>${new Date(order.createdAt).toLocaleString('es-PE')} · ${order.items.map(item => `${escapeHtml(item.name)} × ${item.quantity}`).join(', ')}</p></div><div class="order-meta"><strong>${money(order.total)}</strong><span class="status ${statusClass(order.status)}">${escapeHtml(order.status)}</span></div></article>`).join('') : ''}
        ${ordersStatus === 'ready' && !customerOrders.length ? '<p class="empty-state">No hay pedidos para ese estado.</p>' : ''}
      </section>
      <section class="form-panel" style="margin-top:24px">
        <h2>Consultar un pedido por número</h2>
        <p class="muted">La consulta pasa por el Proxy: solo puedes ver tus propios pedidos.</p>
        <form class="toolbar" data-form="proxy-lookup">
          <label class="field"><span>N.º de pedido</span><input name="orderId" type="number" min="1" required></label>
          <button class="button secondary" type="submit">Consultar</button>
        </form>
        ${proxyLookup ? (proxyLookup.ok
          ? `<p><strong>${escapeHtml(proxyLookup.data.codigo)}</strong> · ${money(proxyLookup.data.montoTotal)} · <span class="status ${statusClass(proxyLookup.data.estado)}">${escapeHtml(proxyLookup.data.estado)}</span></p>`
          : `<p class="stock-low">Acceso rechazado (${proxyLookup.status}): ${escapeHtml(proxyLookup.data.message || 'sin detalle')}</p>`) : ''}
      </section>
    </main>`;
}

function renderOrderHistory() {
  if (!orderHistory) return '';
  const { pedido, eventos, comandos, puedeDeshacer } = orderHistory;
  return `
    <section class="orders-panel" style="margin-top:24px">
      <div class="section-heading"><h2>Historial de ${escapeHtml(pedido.codigo)}</h2><span class="muted">Estado actual: ${escapeHtml(pedido.estado)}</span></div>
      <div class="admin-layout">
        <div><h3>Auditoría (Observer)</h3>
          ${eventos.length ? `<ul>${eventos.map(evento => `<li>${new Date(evento.fecha).toLocaleTimeString('es-PE')} · ${escapeHtml(evento.estadoAnterior)} → <strong>${escapeHtml(evento.estadoNuevo)}</strong> · ${escapeHtml(evento.detalle)}</li>`).join('')}</ul>` : '<p class="muted">Sin cambios registrados.</p>'}
        </div>
        <div><h3>Comandos (Command)</h3>
          ${comandos.length ? `<ul>${comandos.map(comando => `<li>${new Date(comando.fecha).toLocaleTimeString('es-PE')} · ${escapeHtml(comando.descripcion)} · ${escapeHtml(comando.tipo)}</li>`).join('')}</ul>` : '<p class="muted">Sin comandos ejecutados.</p>'}
          <p class="muted">${puedeDeshacer ? 'Hay estados guardados (Memento) para deshacer.' : 'No hay estados guardados para deshacer.'}</p>
        </div>
      </div>
    </section>`;
}

function renderAdmin() {
  const revenue = adminOrders.filter(order => order.status !== 'CANCELADO').reduce((total, order) => total + Number(order.total), 0);
  const lowStockCount = store.products.filter(product => product.stock < 5).length;
  const pendingCount = adminOrders.filter(order => ['PAGADO', 'CONFIRMADO', 'PREPARANDO'].includes(order.status)).length;
  const inventory = `
    <section class="table-panel">
      <div class="section-heading" style="padding:18px 18px 0"><h2>Productos y existencias</h2><span class="muted">${store.products.length} productos</span></div>
      <div class="table-scroll"><table><thead><tr><th>Producto</th><th>Categoría</th><th>Precio</th><th>Stock</th><th>Acción</th></tr></thead>
        <tbody>${store.products.map(product => `<tr><td>${escapeHtml(product.name)}</td><td>${escapeHtml(product.category)}</td><td>${money(product.price)}</td><td class="${product.stock < 5 ? 'stock-low' : ''}">${product.stock}</td><td><form data-form="stock" data-product-id="${product.id}" style="display:flex;gap:6px"><input class="table-input" name="stock" type="number" min="0" value="${product.stock}" aria-label="Stock de ${escapeHtml(product.name)}"><button class="button secondary small" type="submit">Guardar</button></form></td></tr>`).join('')}</tbody>
      </table></div>
    </section>`;
  const orders = `
    <section class="orders-panel">
      <div class="section-heading"><h2>Pedidos (PostgreSQL)</h2><span class="muted">${adminOrders.length} en total · las acciones usan State, Command y Memento</span></div>
      ${adminOrdersStatus === 'loading' ? '<p class="empty-state">Cargando pedidos…</p>' : ''}
      ${adminOrdersStatus === 'error' ? '<p class="empty-state">No se pudieron cargar los pedidos desde PostgreSQL.</p>' : ''}
      <div class="table-scroll"><table><thead><tr><th>Pedido</th><th>Cliente</th><th>Fecha</th><th>Total</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>${adminOrders.map(order => `<tr><td>#${order.id}</td><td class="orders-customer">${escapeHtml(order.customerName)}<br><span class="muted">${escapeHtml(order.customerEmail)}</span></td><td>${new Date(order.createdAt).toLocaleDateString('es-PE')}</td><td>${money(order.total)}</td><td><span class="status ${statusClass(order.status)}">${escapeHtml(order.status)}</span></td><td><div style="display:flex;flex-wrap:wrap;gap:4px">${orderActions.map(item => `<button class="button secondary small" type="button" data-order-action="${item.action}" data-order-id="${order.id}">${item.label}</button>`).join('')}<button class="button small" type="button" data-order-undo="${order.id}">Deshacer</button><button class="button secondary small" type="button" data-order-history="${order.id}">Historial</button></div></td></tr>`).join('') || '<tr><td colspan="6" class="muted">Aún no hay pedidos registrados.</td></tr>'}</tbody>
      </table></div>
    </section>
    ${renderOrderHistory()}
    <section class="form-panel" style="margin-top:24px">
      <h2>Reportes (Bridge + Composite)</h2>
      <p class="muted">El mismo reporte se exporta en distintos formatos.</p>
      <div style="display:flex;flex-wrap:wrap;gap:8px">
        <a class="button secondary small" href="/api/reports/catalog?formato=html" target="_blank" rel="noopener">Catálogo HTML</a>
        <a class="button secondary small" href="/api/reports/catalog?formato=csv">Catálogo CSV (Excel)</a>
        <a class="button secondary small" href="/api/reports/orders?formato=html" target="_blank" rel="noopener">Pedidos HTML</a>
        <a class="button secondary small" href="/api/reports/orders?formato=csv">Pedidos CSV (Excel)</a>
      </div>
    </section>`;

  return `
    <main class="page">
      <div class="page-heading"><div><p class="eyebrow">NEXOMARKET · ADMINISTRACIÓN</p><h1>Resumen de la tienda</h1><p>Controla productos, existencias y pedidos desde un solo lugar.</p></div></div>
      <section class="metric-grid" aria-label="Indicadores de la tienda">
        <div class="metric"><span>Productos</span><strong>${store.products.length}</strong></div>
        <div class="metric"><span>Stock bajo</span><strong>${lowStockCount}<small>menos de 5</small></strong></div>
        <div class="metric"><span>Pedidos por preparar</span><strong>${pendingCount}</strong></div>
        <div class="metric"><span>Ventas registradas</span><strong>${money(revenue)}</strong></div>
      </section>
      <div class="admin-layout">
        <section class="form-panel"><h2>Agregar producto</h2>
          <form data-form="product">
            <label class="field"><span>Nombre</span><input name="name" required maxlength="80" placeholder="Nombre del producto"></label>
            <label class="field"><span>Categoría</span><select name="category">${categories.slice(1).map(category => `<option>${category}</option>`).join('')}</select></label>
            <label class="field"><span>Precio (S/)</span><input name="price" type="number" min="0.01" step="0.01" required></label>
            <label class="field"><span>Stock inicial</span><input name="stock" type="number" min="0" step="1" required></label>
            <button class="button" type="submit">Guardar producto</button>
          </form>
        </section>
        ${inventory}
      </div>
      ${orders}
    </main>`;
}

function statusClass(status) {
  if (status === 'ENVIADO' || status === 'ENTREGADO') return 'sent';
  if (['PENDIENTE', 'PAGADO', 'CONFIRMADO', 'PREPARANDO'].includes(status)) return 'pending';
  return '';
}

function renderCart() {
  if (!cartOpen) return '';
  const lines = cart.map(line => ({ ...line, product: store.products.find(product => product.id === line.productId) })).filter(line => line.product);
  const total = lines.reduce((sum, line) => sum + line.product.price * line.quantity, 0);
  return `<div class="cart-overlay" data-action="overlay-close"><aside class="cart-drawer" role="dialog" aria-modal="true" aria-labelledby="cart-title"><div class="drawer-heading"><h2 id="cart-title">Tu bolsa (${lines.reduce((sum, line) => sum + line.quantity, 0)})</h2><button class="icon-button" type="button" data-action="cart-close" aria-label="Cerrar bolsa">×</button></div>
    <div class="cart-lines">${lines.length ? lines.map(line => `<article class="cart-line"><img src="${escapeHtml(line.product.image)}" alt=""><div><h3>${escapeHtml(line.product.name)}</h3><p>${money(line.product.price)} c/u</p><div class="quantity"><button type="button" data-quantity="-1" data-product-id="${line.productId}" aria-label="Reducir cantidad">−</button><span>${line.quantity}</span><button type="button" data-quantity="1" data-product-id="${line.productId}" aria-label="Aumentar cantidad">+</button></div></div><button class="icon-button" type="button" data-remove-cart="${line.productId}" aria-label="Quitar ${escapeHtml(line.product.name)}">×</button></article>`).join('') : '<p class="empty-state">Tu bolsa está vacía.</p>'}</div>
    ${lines.length ? `<div class="cart-total"><span>Total</span><span>${money(total)}</span></div><button class="button login-submit" type="button" data-action="checkout-open">Continuar compra</button>` : ''}
    ${checkoutOpen && lines.length ? `<form class="checkout-form" data-form="checkout"><h3>Datos de entrega</h3><label class="field"><span>Nombre del destinatario</span><input name="customerName" required value="${escapeHtml(user.name)}"></label><label class="field"><span>Dirección</span><input name="address" required placeholder="Av. / calle y número"></label><label class="field"><span>Método de pago</span><select name="payment"><option>YAPE</option><option>TARJETA</option></select></label><p class="muted" style="font-size:.78rem">Pago y envío en modo de demostración.</p><button class="button login-submit" type="submit">Confirmar pedido · ${money(total)}</button></form>` : ''}
  </aside></div>`;
}

function render() {
  if (!user) {
    renderLogin();
    return;
  }
  if (user.role === 'Cliente' && !['catalog', 'my-orders'].includes(activeView)) activeView = 'catalog';
  if (user.role === 'Administrador' && !['admin', 'inventory', 'orders'].includes(activeView)) activeView = 'admin';
  const page = user.role === 'Administrador'
    ? renderAdmin()
    : activeView === 'my-orders' ? renderOrders() : renderProducts();
  app.innerHTML = `${header()}${page}${footer()}${renderCart()}`;
}

function notify(message) {
  document.querySelector('.toast')?.remove();
  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.setAttribute('role', 'status');
  toast.textContent = message;
  document.body.append(toast);
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.remove(), 3000);
}

async function setSession(account) {
  user = account;
  activeView = account.role === 'Administrador' ? 'admin' : 'catalog';
  productsStatus = 'loading';
  orderHistory = null;
  proxyLookup = null;
  render();
  await loadProducts();
  if (account.role === 'Administrador') await loadAdminOrders();
}

async function restoreSession() {
  try {
    const response = await fetch('/api/auth/session');
    if (response.status === 204) {
      renderLogin();
      return;
    }
    if (response.ok) {
      await setSession(await response.json());
      return;
    }
  } catch {}
  renderLogin();
}

function submitCheckout(form) {
  const formData = new FormData(form);
  const orderItems = cart.map(line => {
    const product = store.products.find(item => item.id === line.productId);
    return { productId: product.id, name: product.name, quantity: line.quantity, price: product.price };
  });
  const orderTotal = orderItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const order = {
    id: store.orders.reduce((highest, item) => Math.max(highest, item.id), 10000) + 1,
    customerName: String(formData.get('customerName')).trim(),
    customerEmail: user.email,
    address: String(formData.get('address')).trim(),
    payment: String(formData.get('payment')),
    items: orderItems,
    total: orderTotal,
    status: 'PREPARANDO',
    createdAt: new Date().toISOString(),
  };
  store.orders.push(order);
  saveStore();
  cart = [];
  cartOpen = false;
  checkoutOpen = false;
  render();
  notify(`Pedido #${order.id} registrado en modo demostración (aún no se guarda en PostgreSQL).`);
}

app.addEventListener('click', async event => {
  const authModeButton = event.target.closest('[data-auth-mode]');
  if (authModeButton) {
    authMode = authModeButton.dataset.authMode;
    authNotice = '';
    renderLogin();
    return;
  }

  const categoryButton = event.target.closest('[data-category]');
  if (categoryButton) {
    activeCategory = categoryButton.dataset.category;
    render();
    return;
  }

  const viewButton = event.target.closest('[data-view]');
  if (viewButton) {
    activeView = viewButton.dataset.view;
    if (activeView === 'my-orders') loadCustomerOrders();
    else render();
    return;
  }

  const addButton = event.target.closest('[data-add-cart]');
  if (addButton) {
    const product = store.products.find(item => item.id === Number(addButton.dataset.addCart));
    const line = cart.find(item => item.productId === product.id);
    if (line) line.quantity = Math.min(line.quantity + 1, product.stock);
    else cart.push({ productId: product.id, quantity: 1 });
    render();
    notify(`${product.name} agregado a tu bolsa.`);
    return;
  }

  const quantityButton = event.target.closest('[data-quantity]');
  if (quantityButton) {
    const productId = Number(quantityButton.dataset.productId);
    const product = store.products.find(item => item.id === productId);
    const line = cart.find(item => item.productId === productId);
    line.quantity = Math.min(product.stock, line.quantity + Number(quantityButton.dataset.quantity));
    if (line.quantity < 1) cart = cart.filter(item => item.productId !== productId);
    render();
    return;
  }

  const removeButton = event.target.closest('[data-remove-cart]');
  if (removeButton) {
    cart = cart.filter(item => item.productId !== Number(removeButton.dataset.removeCart));
    render();
    return;
  }

  const orderActionButton = event.target.closest('[data-order-action]');
  if (orderActionButton) {
    const orderId = orderActionButton.dataset.orderId;
    await runOrderAction(orderId, `/api/orders/${orderId}/acciones`, { accion: orderActionButton.dataset.orderAction });
    return;
  }

  const undoButton = event.target.closest('[data-order-undo]');
  if (undoButton) {
    const orderId = undoButton.dataset.orderUndo;
    await runOrderAction(orderId, `/api/orders/${orderId}/deshacer`);
    return;
  }

  const historyButton = event.target.closest('[data-order-history]');
  if (historyButton) {
    await loadOrderHistory(historyButton.dataset.orderHistory);
    return;
  }

  const actionButton = event.target.closest('[data-action]');
  if (!actionButton) return;
  if (actionButton.dataset.action === 'logout') {
    try { await fetch('/api/auth/logout', { method: 'POST' }); } catch {}
    user = null;
    cart = [];
    cartOpen = false;
    render();
  } else if (actionButton.dataset.action === 'cart-open') {
    cartOpen = true;
    render();
  } else if (actionButton.dataset.action === 'cart-close' || actionButton.dataset.action === 'overlay-close' && event.target === actionButton) {
    cartOpen = false;
    checkoutOpen = false;
    render();
  } else if (actionButton.dataset.action === 'checkout-open') {
    checkoutOpen = true;
    render();
  }
});

app.addEventListener('submit', async event => {
  const form = event.target.closest('form[data-form]');
  if (!form) return;
  event.preventDefault();
  const formData = new FormData(form);

  if (form.dataset.form === 'login') {
    const email = String(formData.get('email')).trim().toLowerCase();
    const password = String(formData.get('password'));
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });
    if (!response.ok) {
      authNotice = (await response.json().catch(() => ({}))).message || 'No se pudo iniciar sesión.';
      renderLogin();
      return;
    }
    await setSession(await response.json());
  }

  if (form.dataset.form === 'register') {
    const name = String(formData.get('name')).trim();
    const email = String(formData.get('email')).trim().toLowerCase();
    const password = String(formData.get('password'));
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password }),
    });
    if (!response.ok) {
      authNotice = (await response.json().catch(() => ({}))).message || 'No se pudo crear la cuenta.';
      renderLogin();
      return;
    }
    authMode = 'login';
    authNotice = (await response.json()).message;
    renderLogin();
  }

  if (form.dataset.form === 'search') {
    searchTerm = String(formData.get('query')).trim();
    render();
  }

  if (form.dataset.form === 'order-filter') {
    selectedOrderStatus = String(formData.get('status'));
    loadCustomerOrders();
  }

  if (form.dataset.form === 'checkout') submitCheckout(form);

  if (form.dataset.form === 'product') {
    const product = {
      name: String(formData.get('name')).trim(),
      category: String(formData.get('category')),
      price: Number(formData.get('price')),
      stock: Number(formData.get('stock')),
    };
    if (!product.name || product.price <= 0 || product.stock < 0) return;
    try {
      const response = await fetch('/api/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product),
      });
      if (!response.ok) throw new Error();
      await loadProducts();
      notify('Producto guardado en PostgreSQL.');
    } catch {
      notify('No se pudo guardar el producto en PostgreSQL.');
    }
  }

  if (form.dataset.form === 'stock') {
    const product = store.products.find(item => item.id === Number(form.dataset.productId));
    const newStock = Number(formData.get('stock'));
    if (!product || !Number.isInteger(newStock) || newStock < 0) return;
    try {
      const response = await fetch(`/api/products/${product.id}/stock`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ stock: newStock }),
      });
      if (!response.ok) throw new Error();
      await loadProducts();
      notify(`Stock de ${product.name} actualizado en PostgreSQL.`);
    } catch {
      notify('No se pudo actualizar el stock en PostgreSQL.');
    }
  }

  if (form.dataset.form === 'proxy-lookup') {
    const orderId = Number(formData.get('orderId'));
    if (!Number.isInteger(orderId) || orderId < 1) return;
    proxyLookup = await callApi(`/api/orders/${orderId}`);
    render();
  }
});

render();
restoreSession();