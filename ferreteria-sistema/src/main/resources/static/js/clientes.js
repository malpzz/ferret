let currentClienteId = null;

document.addEventListener("DOMContentLoaded", function () {
  loadClientes();
  initializeClientesPage();
});

function initializeClientesPage() {
  searchTable("searchClientes", "#clientesTable tbody");

  const form = document.getElementById("clienteForm");
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

function loadClientes() {
  const clientes = loadData("clientes");
  renderClientesTable(clientes);
}

function renderClientesTable(clientes) {
  const columns = [
    { key: "nombreCliente", label: "Nombre" },
    { key: "apellidos", label: "Apellidos" },
    { key: "telefono", label: "Teléfono" },
    { key: "email", label: "Email" },
  ];

  const actions = [
    {
      label: '<i class="fas fa-eye"></i>',
      class: "btn-primary",
      onclick: "viewCliente",
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning",
      onclick: "editCliente",
    },
    {
      label: '<i class="fas fa-trash"></i>',
      class: "btn-danger",
      onclick: "deleteCliente",
    },
  ];

  renderTable("clientesTable", clientes, columns, actions);
}

function openAddModal() {
  document.getElementById("modalTitle").textContent = "Nuevo Cliente";
  clearForm("clienteForm");
  currentClienteId = null;
  openModal("clienteModal");
}

function editCliente(id) {
  const cliente = getRecord("clientes", id);
  if (!cliente) {
    showAlert("Cliente no encontrado", "danger");
    return;
  }

  document.getElementById("modalTitle").textContent = "Editar Cliente";
  currentClienteId = id;

  const form = document.getElementById("clienteForm");
  form.querySelector('[name="id"]').value = cliente.id;
  form.querySelector('[name="nombreCliente"]').value = cliente.nombreCliente;
  form.querySelector('[name="apellidos"]').value = cliente.apellidos;
  form.querySelector('[name="direccion"]').value = cliente.direccion;
  form.querySelector('[name="telefono"]').value = cliente.telefono;
  form.querySelector('[name="email"]').value = cliente.email;

  openModal("clienteModal");
}

function viewCliente(id) {
  const cliente = getRecord("clientes", id);
  if (!cliente) {
    showAlert("Cliente no encontrado", "danger");
    return;
  }

  currentClienteId = id;

  const detailsHtml = `
        <div class="cliente-details">
            <div class="detail-row">
                <strong>Nombre Completo:</strong> ${cliente.nombreCliente} ${cliente.apellidos}
            </div>
            <div class="detail-row">
                <strong>Dirección:</strong> ${cliente.direccion}
            </div>
            <div class="detail-row">
                <strong>Teléfono:</strong> ${cliente.telefono}
            </div>
            <div class="detail-row">
                <strong>Email:</strong> ${cliente.email}
            </div>
        </div>
        <style>
            .cliente-details .detail-row {
                padding: 10px 0;
                border-bottom: 1px solid #e1e8ed;
            }
            .cliente-details .detail-row:last-child {
                border-bottom: none;
            }
        </style>
    `;

  document.getElementById("clienteDetails").innerHTML = detailsHtml;
  openModal("viewClienteModal");
}

function editFromView() {
  closeModal("viewClienteModal");
  editCliente(currentClienteId);
}

function deleteCliente(id) {
  const cliente = getRecord("clientes", id);
  if (!cliente) {
    showAlert("Cliente no encontrado", "danger");
    return;
  }

  if (
    confirmDelete(
      `¿Estás seguro de que deseas eliminar al cliente ${cliente.nombreCliente} ${cliente.apellidos}?`
    )
  ) {
    const facturas = loadData("facturas");
    const isUsed = facturas.some((f) => f.idCliente === id);

    if (isUsed) {
      showAlert(
        "No se puede eliminar el cliente porque tiene facturas asociadas",
        "danger"
      );
      return;
    }

    deleteRecord("clientes", id);
    loadClientes();
    showAlert("Cliente eliminado correctamente", "success");

    if (typeof addActivity === "function") {
      addActivity(
        "cliente",
        "Cliente eliminado",
        `Se eliminó el cliente ${cliente.nombreCliente} ${cliente.apellidos}`
      );
    }
  }
}

function handleFormSubmit(e) {
  e.preventDefault();

  const validationRules = [
    { field: "nombreCliente", label: "Nombre", required: true, minLength: 2 },
    { field: "apellidos", label: "Apellidos", required: true, minLength: 2 },
    { field: "direccion", label: "Dirección", required: true, minLength: 5 },
    { field: "telefono", label: "Teléfono", required: true, type: "phone" },
    { field: "email", label: "Email", required: true, type: "email" },
  ];

  if (!validateForm("clienteForm", validationRules)) {
    return;
  }

  const formData = new FormData(e.target);
  const clienteData = {
    nombreCliente: formData.get("nombreCliente").trim(),
    apellidos: formData.get("apellidos").trim(),
    direccion: formData.get("direccion").trim(),
    telefono: formData.get("telefono").trim(),
    email: formData.get("email").trim().toLowerCase(),
  };

  const clientes = loadData("clientes");
  const emailExists = clientes.some(
    (c) =>
      c.email === clienteData.email &&
      (!currentClienteId || c.id !== currentClienteId)
  );

  if (emailExists) {
    showAlert("Ya existe un cliente con este email", "danger");
    return;
  }

  let result;
  let actionType;

  if (currentClienteId) {
    clienteData.id = currentClienteId;
    result = updateRecord("clientes", clienteData);
    actionType = "actualizado";
  } else {
    clienteData.id = generateId(clientes);
    result = addRecord("clientes", clienteData);
    actionType = "agregado";
  }

  if (result) {
    loadClientes();
    closeModal("clienteModal");
    clearForm("clienteForm");
    showAlert(`Cliente ${actionType} correctamente`, "success");

    if (typeof addActivity === "function") {
      const activityTitle = currentClienteId
        ? "Cliente actualizado"
        : "Nuevo cliente agregado";
      const activityDesc = `${clienteData.nombreCliente} ${clienteData.apellidos} fue ${actionType}`;
      addActivity("cliente", activityTitle, activityDesc);
    }

    currentClienteId = null;
  } else {
    showAlert("Error al guardar el cliente", "danger");
  }
}

window.openAddModal = openAddModal;
window.editCliente = editCliente;
window.viewCliente = viewCliente;
window.deleteCliente = deleteCliente;
window.editFromView = editFromView;


