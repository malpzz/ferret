let currentProveedorId = null;

document.addEventListener("DOMContentLoaded", async function () {
  await loadProveedores();
  initializeProveedoresPage();
});

function initializeProveedoresPage() {
  searchTable("searchProveedores", "#proveedoresTable tbody");

  const form = document.getElementById("proveedorForm");
  form.addEventListener("submit", handleFormSubmit);

  // Initialize logout functionality
  initializeLogout();
}

async function loadProveedores() {
  try {
    const proveedores = await apiGet('/api/proveedores');
    renderProveedoresTable(proveedores.map(p => ({
      id: p.idProveedor || p.id,
      nombreProveedor: p.nombreProveedor,
      direccion: p.direccion,
      telefono: p.telefono,
      email: p.email,
    })));
  } catch (e) {
    showAlert(`Error cargando proveedores: ${e.message}`, 'danger');
  }
}

function renderProveedoresTable(proveedores) {
  const columns = [
    { key: "nombreProveedor", label: "Empresa" },
    { key: "direccion", label: "Dirección" },
    { key: "telefono", label: "Teléfono" },
    { key: "email", label: "Email" },
  ];

  const actions = [
    {
      label: '<i class="fas fa-eye"></i>',
      class: "btn-primary",
      onclick: "viewProveedor",
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning",
      onclick: "editProveedor",
    },
    {
      label: '<i class="fas fa-trash"></i>',
      class: "btn-danger",
      onclick: "deleteProveedor",
    },
  ];

  renderTable("proveedoresTable", proveedores, columns, actions);
}

function openAddModal() {
  document.getElementById("modalTitle").textContent = "Nuevo Proveedor";
  clearForm("proveedorForm");
  currentProveedorId = null;
  openModal("proveedorModal");
}

function editProveedor(id) {
  const proveedor = getRecord("proveedores", id);
  if (!proveedor) {
    showAlert("Proveedor no encontrado", "danger");
    return;
  }

  document.getElementById("modalTitle").textContent = "Editar Proveedor";
  currentProveedorId = id;

  const form = document.getElementById("proveedorForm");
  form.querySelector('[name="id"]').value = proveedor.id;
  form.querySelector('[name="nombreProveedor"]').value =
    proveedor.nombreProveedor;
  form.querySelector('[name="direccion"]').value = proveedor.direccion;
  form.querySelector('[name="telefono"]').value = proveedor.telefono;
  form.querySelector('[name="email"]').value = proveedor.email;

  openModal("proveedorModal");
}

function viewProveedor(id) {
  const proveedor = getRecord("proveedores", id);
  if (!proveedor) {
    showAlert("Proveedor no encontrado", "danger");
    return;
  }

  currentProveedorId = id;

  const pedidos = loadData("pedidos");
  const proveedorPedidos = pedidos.filter((p) => p.idProveedor === id);

  const detailsHtml = `
        <div class="proveedor-details">
            <div class="detail-row">
                <strong>Empresa:</strong> ${proveedor.nombreProveedor}
            </div>
            <div class="detail-row">
                <strong>Dirección:</strong> ${proveedor.direccion}
            </div>
            <div class="detail-row">
                <strong>Teléfono:</strong> ${proveedor.telefono}
            </div>
            <div class="detail-row">
                <strong>Email:</strong> ${proveedor.email}
            </div>
            <div class="detail-row">
                <strong>Pedidos Realizados:</strong>
                <span class="badge-info">${proveedorPedidos.length} pedidos</span>
            </div>
        </div>
        <style>
            .proveedor-details .detail-row {
                padding: 10px 0;
                border-bottom: 1px solid #e1e8ed;
            }
            .proveedor-details .detail-row:last-child {
                border-bottom: none;
            }
            .badge-info {
                background: #17a2b8;
                color: white;
                padding: 4px 8px;
                border-radius: 4px;
                font-size: 0.8rem;
            }
        </style>
    `;

  document.getElementById("proveedorDetails").innerHTML = detailsHtml;
  openModal("viewProveedorModal");
}

function editFromView() {
  closeModal("viewProveedorModal");
  editProveedor(currentProveedorId);
}

function viewOrders() {
  if (currentProveedorId) {
    closeModal("viewProveedorModal");
    window.location.href = `/pedidos?proveedor=${currentProveedorId}`;
  }
}

async function deleteProveedor(id) {
  if (!confirmDelete("¿Estás seguro de eliminar este proveedor?")) return;
  try {
    await apiDelete(`/api/proveedores/${id}`);
    await loadProveedores();
    showAlert("Proveedor eliminado correctamente", "success");
  } catch (e) {
    showAlert(e.data?.mensaje || `Error al eliminar: ${e.message}`, 'danger');
  }
}

async function handleFormSubmit(e) {
  e.preventDefault();
  const validationRules = [
    { field: "nombreProveedor", label: "Nombre", required: true, minLength: 3 },
    { field: "direccion", label: "Dirección", required: true, minLength: 5 },
    { field: "telefono", label: "Teléfono", required: true, type: "phone" },
    { field: "email", label: "Email", required: true, type: "email" },
  ];
  if (!validateForm("proveedorForm", validationRules)) return;

  const formData = new FormData(e.target);
  const proveedorData = {
    nombreProveedor: formData.get("nombreProveedor").trim(),
    direccion: formData.get("direccion").trim(),
    telefono: formData.get("telefono").trim(),
    email: formData.get("email").trim().toLowerCase(),
  };
  try {
    if (currentProveedorId) {
      await apiPut(`/api/proveedores/${currentProveedorId}`, proveedorData);
    } else {
      await apiPost('/api/proveedores', proveedorData);
    }
  } catch (e) {
    showAlert(e.data?.mensaje || `Error al guardar: ${e.message}`, 'danger');
    return;
  }
  await loadProveedores();
  closeModal("proveedorModal");
  clearForm("proveedorForm");
  showAlert("Proveedor guardado", "success");
  currentProveedorId = null;
}

window.openAddModal = openAddModal;
window.editProveedor = editProveedor;
window.viewProveedor = viewProveedor;
window.deleteProveedor = deleteProveedor;
window.editFromView = editFromView;
window.viewOrders = viewOrders;


