let currentProveedorId = null;

document.addEventListener("DOMContentLoaded", function () {
  loadProveedores();
  initializeProveedoresPage();
});

function initializeProveedoresPage() {
  searchTable("searchProveedores", "#proveedoresTable tbody");

  const form = document.getElementById("proveedorForm");
  form.addEventListener("submit", handleFormSubmit);

  const logoutBtn = document.querySelector(".btn-logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      if (confirm("¿Estás seguro de que deseas cerrar sesión?")) {
        alert("Sesión cerrada correctamente");
      }
    });
  }
}

function loadProveedores() {
  const proveedores = loadData("proveedores");
  renderProveedoresTable(proveedores);
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
    window.location.href = `pedidos.html?proveedor=${currentProveedorId}`;
  }
}

function deleteProveedor(id) {
  const proveedor = getRecord("proveedores", id);
  if (!proveedor) {
    showAlert("Proveedor no encontrado", "danger");
    return;
  }

  if (
    confirmDelete(
      `¿Estás seguro de que deseas eliminar al proveedor ${proveedor.nombreProveedor}?`
    )
  ) {
    const pedidos = loadData("pedidos");
    const hasPedidos = pedidos.some((p) => p.idProveedor === id);

    if (hasPedidos) {
      showAlert(
        "No se puede eliminar el proveedor porque tiene pedidos asociados",
        "danger"
      );
      return;
    }

    deleteRecord("proveedores", id);
    loadProveedores();
    showAlert("Proveedor eliminado correctamente", "success");

    if (typeof addActivity === "function") {
      addActivity(
        "proveedor",
        "Proveedor eliminado",
        `Se eliminó al proveedor ${proveedor.nombreProveedor}`
      );
    }
  }
}

function handleFormSubmit(e) {
  e.preventDefault();

  const validationRules = [
    { field: "nombreProveedor", label: "Nombre", required: true, minLength: 3 },
    { field: "direccion", label: "Dirección", required: true, minLength: 5 },
    { field: "telefono", label: "Teléfono", required: true, type: "phone" },
    { field: "email", label: "Email", required: true, type: "email" },
  ];

  if (!validateForm("proveedorForm", validationRules)) {
    return;
  }

  const formData = new FormData(e.target);
  const proveedorData = {
    nombreProveedor: formData.get("nombreProveedor").trim(),
    direccion: formData.get("direccion").trim(),
    telefono: formData.get("telefono").trim(),
    email: formData.get("email").trim().toLowerCase(),
  };

  const proveedores = loadData("proveedores");
  const emailExists = proveedores.some(
    (p) =>
      p.email === proveedorData.email &&
      (!currentProveedorId || p.id !== currentProveedorId)
  );

  if (emailExists) {
    showAlert("Ya existe un proveedor con este email", "danger");
    return;
  }

  let result;
  let actionType;

  if (currentProveedorId) {
    proveedorData.id = currentProveedorId;
    result = updateRecord("proveedores", proveedorData);
    actionType = "actualizado";
  } else {
    proveedorData.id = generateId(proveedores);
    result = addRecord("proveedores", proveedorData);
    actionType = "agregado";
  }

  if (result) {
    loadProveedores();
    closeModal("proveedorModal");
    clearForm("proveedorForm");
    showAlert(`Proveedor ${actionType} correctamente`, "success");

    if (typeof addActivity === "function") {
      const activityTitle = currentProveedorId
        ? "Proveedor actualizado"
        : "Nuevo proveedor agregado";
      const activityDesc = `${proveedorData.nombreProveedor} fue ${actionType}`;
      addActivity("proveedor", activityTitle, activityDesc);
    }

    currentProveedorId = null;
  } else {
    showAlert("Error al guardar el proveedor", "danger");
  }
}

window.openAddModal = openAddModal;
window.editProveedor = editProveedor;
window.viewProveedor = viewProveedor;
window.deleteProveedor = deleteProveedor;
window.editFromView = editFromView;
window.viewOrders = viewOrders;
