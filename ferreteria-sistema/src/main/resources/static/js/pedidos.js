let currentPedidoId = null;
let orderProducts = [];
let orderCounter = 1;

document.addEventListener("DOMContentLoaded", async function () {
  await loadPedidos();
  await loadProveedores();
  await loadProductos();
  initializePedidosPage();
});

function initializePedidosPage() {
  searchTable("searchPedidos", "#pedidosTable tbody");

  const statusFilter = document.getElementById("statusFilter");
  if (statusFilter) {
    statusFilter.addEventListener("change", filterByStatus);
  }

  const form = document.getElementById("pedidoForm");
  if (form) {
    form.addEventListener("submit", handleFormSubmit);
  }

  const logoutBtn = document.querySelector(".btn-logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      if (confirm("¿Estás seguro de que deseas cerrar sesión?")) {
        alert("Sesión cerrada correctamente");
      }
    });
  }

  const today = new Date().toISOString().split("T")[0];
  document.getElementById("fechaPedido").value = today;

  const nextWeek = new Date();
  nextWeek.setDate(nextWeek.getDate() + 7);
  document.getElementById("fechaEntregaEsperada").value = nextWeek
    .toISOString()
    .split("T")[0];
}

function generateOrderNumber() { return `P${Date.now()}`; }

function generateOrderNumber() {
  const number = `P${orderCounter.toString().padStart(6, "0")}`;
  orderCounter++;
  return number;
}

async function loadPedidos() {
  try {
    const pedidos = await apiGet('/api/pedidos');
    const proveedores = await apiGet('/api/proveedores');
    const pedidosWithSuppliers = pedidos.map(p => {
      const estadoNorm = ((p.estado ?? '').toString().trim() || 'PENDIENTE').toUpperCase();
      return ({
        id: p.idPedido || p.id,
        numeroPedido: p.numero || p.numeroPedido || '-',
        fechaPedido: p.fecha,
        idProveedor: p.proveedor?.idProveedor || p.idProveedor,
        nombreProveedor: p.proveedor?.nombreProveedor || (proveedores.find(x=> (x.idProveedor||x.id) === (p.idProveedor))?.nombreProveedor) || '-',
        fechaEntregaEsperada: p.fechaEntrega || p.fechaEntregaEsperada,
        total: p.total || 0,
        estado: estadoNorm,
      });
    });
    renderPedidosTable(pedidosWithSuppliers);
  } catch (e) {
    showAlert(`Error cargando pedidos: ${e.message}`, 'danger');
  }
}

async function loadProveedores() {
  try {
    const proveedores = await apiGet('/api/proveedores');
    const select = document.getElementById("idProveedor");
    select.innerHTML = '<option value="">Seleccione un proveedor</option>';
    proveedores.forEach((proveedor) => {
      const option = document.createElement("option");
      option.value = proveedor.idProveedor || proveedor.id;
      option.textContent = `${proveedor.nombreProveedor} - ${proveedor.email}`;
      select.appendChild(option);
    });
  } catch {}
}

async function loadProductos() {
  try {
    const productos = await apiGet('/api/productos');
    const select = document.getElementById("selectProducto");
    select.innerHTML = '<option value="">Seleccione un producto</option>';
    productos.forEach((producto) => {
      const option = document.createElement("option");
      option.value = producto.idProducto || producto.id;
      const stock = (producto.stock && producto.stock.cantidad) || producto.cantidadStock || 0;
      option.textContent = `${producto.nombreProducto} - Stock: ${stock}`;
      option.dataset.precioCompra = producto.precioCompra || producto.precio;
      select.appendChild(option);
    });
  } catch {}
}

function renderPedidosTable(pedidos) {
  const columns = [
    { key: "numeroPedido", label: "Número" },
    {
      key: "fechaPedido",
      label: "Fecha Pedido",
      format: (value) => new Date(value).toLocaleDateString(),
    },
    { key: "nombreProveedor", label: "Proveedor" },
    {
      key: "fechaEntregaEsperada",
      label: "Entrega Esperada",
      format: (value) => new Date(value).toLocaleDateString(),
    },
    {
      key: "total",
      label: "Total",
      format: (value) => `$${parseFloat(value || 0).toFixed(2)}`,
    },
    {
      key: "estado",
      label: "Estado",
      format: (value) => {
        const v = (value||'PENDIENTE').toString().trim().toUpperCase();
        const pretty = v.charAt(0) + v.slice(1).toLowerCase();
        return `<span class="status-badge status-${v.toLowerCase()}">${pretty}</span>`;
      },
    },
  ];

  const actions = [
    {
      label: '<i class="fas fa-eye"></i>',
      class: "btn-primary",
      onclick: "viewPedido",
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning",
      onclick: "editPedido",
    },
    {
      label: '<i class="fas fa-print"></i>',
      class: "btn-success",
      onclick: "printPedido",
    },
    {
      label: '<i class="fas fa-times"></i>',
      class: "btn-danger",
      onclick: "cancelPedido",
    },
  ];

  renderTable("pedidosTable", pedidos, columns, actions);
}

// Filtros y acciones mínimas
async function filterByStatus() {
  const status = document.getElementById("statusFilter")?.value || "";
  try {
    const pedidos = await apiGet('/api/pedidos');
    const proveedores = await apiGet('/api/proveedores');
    let list = pedidos.map(p => ({
      id: p.idPedido || p.id,
      numeroPedido: p.numero || p.numeroPedido || '-',
      fechaPedido: p.fecha,
      idProveedor: p.proveedor?.idProveedor || p.idProveedor,
      nombreProveedor: p.proveedor?.nombreProveedor || (proveedores.find(x=> (x.idProveedor||x.id) === (p.idProveedor))?.nombreProveedor) || '-',
      fechaEntregaEsperada: p.fechaEntrega || p.fechaEntregaEsperada,
      total: p.total || 0,
      estado: p.estado || 'pendiente',
    }));
    if (status) list = list.filter(p => (p.estado||'').toLowerCase() === status.toLowerCase());
    renderPedidosTable(list);
  } catch (e) { showAlert(`Error filtrando pedidos: ${e.message}`, 'danger'); }
}

function openAddModal() {
  if (typeof clearForm === 'function') clearForm('pedidoForm');
  const numero = generateOrderNumber();
  const numInput = document.getElementById('numeroPedido');
  if (numInput) numInput.value = numero;
  if (typeof openModal === 'function') openModal('pedidoModal');
}

// Stubs seguros para acciones (evitan ReferenceError si se invocan desde botones)
async function viewPedido(id) {
  try {
    const pedido = await apiGet(`/api/pedidos/${id}`);
    const cont = document.getElementById('pedidoViewContent');
    if (cont) {
      cont.innerHTML = `
        <div class="detail-grid">
          <div><strong>Número:</strong> ${pedido.numero || pedido.numeroPedido || '-'}</div>
          <div><strong>Fecha:</strong> ${pedido.fecha ? new Date(pedido.fecha).toLocaleDateString() : '-'}</div>
          <div><strong>Proveedor:</strong> ${pedido.proveedor?.nombreProveedor || '-'}</div>
          <div><strong>Entrega:</strong> ${pedido.fechaEntrega || pedido.fechaEntregaEsperada ? new Date(pedido.fechaEntrega || pedido.fechaEntregaEsperada).toLocaleDateString() : '-'}</div>
          <div><strong>Total:</strong> ${typeof pedido.total !== 'undefined' ? `$${parseFloat(pedido.total||0).toFixed(2)}` : '-'}</div>
          <div><strong>Estado:</strong> ${(pedido.estado||'pendiente')}</div>
        </div>`;
      if (typeof openModal === 'function') openModal('pedidoViewModal');
    } else {
      alert(`Pedido ${pedido.numero || id} - Estado: ${pedido.estado || 'pendiente'}`);
    }
  } catch (e) {
    showAlert(`No se pudo cargar el pedido: ${e.message}`, 'danger');
  }
}

async function editPedido(id) {
  try {
    const pedido = await apiGet(`/api/pedidos/${id}`);
    currentPedidoId = id;
    const numInput = document.getElementById('numeroPedido');
    const provSel = document.getElementById('idProveedor');
    const fechaEnt = document.getElementById('fechaEntregaEsperada');
    const estadoSel = document.getElementById('estadoPedido') || document.querySelector('#pedidoForm select[name="estado"]');
    if (numInput) numInput.value = pedido.numero || pedido.numeroPedido || '';
    if (provSel) provSel.value = (pedido.proveedor?.idProveedor || pedido.idProveedor || '')+'';
    if (fechaEnt) fechaEnt.value = (pedido.fechaEntrega || pedido.fechaEntregaEsperada || '').toString().substring(0,10);
    if (estadoSel) {
      const normalized = (pedido.estado || 'PENDIENTE').toString().trim().toUpperCase();
      // Intenta seleccionar por coincidencia case-insensitive
      let matched = false;
      Array.from(estadoSel.options || []).forEach(opt => {
        if (opt && opt.value && opt.value.toString().trim().toUpperCase() === normalized) {
          estadoSel.value = opt.value;
          matched = true;
        }
      });
      if (!matched) estadoSel.value = normalized; // fallback
    }
    if (typeof openModal === 'function') openModal('pedidoModal');
  } catch (e) {
    showAlert(`No se pudo cargar el pedido para editar: ${e.message}`, 'danger');
  }
}

async function printPedido(id) {
  try {
    const pedido = await apiGet(`/api/pedidos/${id}`);
    const win = window.open('', '_blank');
    if (!win) return;
    win.document.write(`<!doctype html><html><head><title>Pedido ${pedido.numero || id}</title></head><body>`);
    win.document.write(`<h2>Pedido ${pedido.numero || id}</h2>`);
    win.document.write(`<p>Fecha: ${pedido.fecha ? new Date(pedido.fecha).toLocaleDateString() : '-'}</p>`);
    win.document.write(`<p>Proveedor: ${pedido.proveedor?.nombreProveedor || '-'}</p>`);
    win.document.write(`<p>Entrega: ${pedido.fechaEntrega || pedido.fechaEntregaEsperada ? new Date(pedido.fechaEntrega || pedido.fechaEntregaEsperada).toLocaleDateString() : '-'}</p>`);
    win.document.write(`<p>Total: ${typeof pedido.total !== 'undefined' ? `$${parseFloat(pedido.total||0).toFixed(2)}` : '-'}</p>`);
    win.document.write(`</body></html>`);
    win.document.close();
    win.focus();
    win.print();
  } catch (e) {
    showAlert(`No se pudo imprimir: ${e.message}`, 'danger');
  }
}

async function cancelPedido(id) {
  try {
    await apiPost(`/api/pedidos/${id}/estado?estado=CANCELADO`, {});
    await loadPedidos();
    showAlert('Pedido cancelado', 'success');
  } catch (e) { showAlert(`No se pudo cancelar: ${e.message}`, 'danger'); }
}

// Stubs adicionales requeridos por window.* asignados
function editFromView(){ /* noop */ }
function printOrder(){ window.print(); }
function addProductToOrder(){ /* noop */ }
function removeProductFromOrder(){ /* noop */ }
async function receiveOrder(id){ try { await apiPost(`/api/pedidos/${id}/estado?estado=RECIBIDO`, {}); await loadPedidos(); } catch(e){} }

async function handleFormSubmit(e) {
  e.preventDefault();
  try {
    const form = e.target;
    const fd = new FormData(form);
    const numero = (document.getElementById('numeroPedido')?.value || fd.get('numero') || '').toString().trim() || generateOrderNumber();
    const idProveedorStr = (document.getElementById('idProveedor')?.value || fd.get('idProveedor') || '').toString();
    const idProveedor = idProveedorStr ? parseInt(idProveedorStr, 10) : null;
    let estadoVal = (document.getElementById('estadoPedido')?.value || fd.get('estado') || '').toString().trim();
    let estado = (estadoVal || 'PENDIENTE').toUpperCase();
    const fechaEntregaSel = (document.getElementById('fechaEntregaEsperada')?.value || fd.get('fechaEntregaEsperada') || '').toString().trim();
    const descripcion = (document.getElementById('descripcionPedido')?.value || fd.get('descripcion') || '').toString().trim() || null;
    const observaciones = (document.getElementById('observacionesPedido')?.value || fd.get('observaciones') || '').toString().trim() || null;

    if (!idProveedor) {
      showAlert('Seleccione un proveedor', 'warning');
      return;
    }

    if (currentPedidoId) {
      // Solo actualización de estado disponible por ahora
      await apiPost(`/api/pedidos/${currentPedidoId}/estado?estado=${encodeURIComponent(estado)}`, {});
      showAlert('Estado de pedido actualizado', 'success');
    } else {
      const payload = {
        numero,
        idProveedor,
        estado,
        // Formato aceptable para Date en backend (config Jackson): 'yyyy-MM-dd HH:mm:ss'
        fechaEntrega: fechaEntregaSel ? `${fechaEntregaSel} 00:00:00` : null,
        descripcion,
        observaciones
      };
      await apiPost('/api/pedidos', payload);
      showAlert('Pedido creado', 'success');
    }

    if (typeof closeModal === 'function') closeModal('pedidoModal');
    await loadPedidos();
  } catch (e) {
    showAlert(`No se pudo guardar el pedido: ${e.message}`, 'danger');
  }
}

window.openAddModal = openAddModal;
window.editPedido = editPedido;
window.viewPedido = viewPedido;
window.printPedido = printPedido;
window.cancelPedido = cancelPedido;
window.editFromView = editFromView;
window.printOrder = printOrder;
window.addProductToOrder = addProductToOrder;
window.removeProductFromOrder = removeProductFromOrder;
window.receiveOrder = receiveOrder;


