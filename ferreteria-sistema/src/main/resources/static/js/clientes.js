let currentClienteId = null;

document.addEventListener("DOMContentLoaded", async function () {
  await loadClientes();
  initializeClientesPage();
});

function initializeClientesPage() {
  searchTable("searchClientes", "#clientesTable tbody");

  const form = document.getElementById("clienteForm");
  form.addEventListener("submit", handleFormSubmit);

  // Initialize logout functionality
  initializeLogout();
}

async function loadClientes() {
  try {
    const clientes = await apiGet('/api/clientes');
    renderClientesTable(clientes.map(c => ({
      id: c.idCliente,
      nombreCliente: c.nombreCliente,
      apellidos: c.apellidos,
      telefono: c.telefono,
      email: c.email,
      direccion: c.direccion,
    })));
  } catch (e) {
    showAlert(`Error cargando clientes: ${e.message}`, 'danger');
  }
}

function renderClientesTable(clientes) {
  const columns = [
    { key: "nombreCliente", label: "Nombre" },
    { key: "apellidos", label: "Apellidos" },
    { key: "direccion", label: "Dirección", format: (value) => value || "Sin dirección" },
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

async function editCliente(id) {
  try {
    // Obtener datos del cliente desde la API
    const cliente = await apiGet(`/api/clientes/${id}`);
    if (!cliente) {
      showAlert("Cliente no encontrado", "danger");
      return;
    }

    document.getElementById("modalTitle").textContent = "Editar Cliente";
    currentClienteId = id;

    const form = document.getElementById("clienteForm");
    form.querySelector('[name="id"]').value = cliente.idCliente || '';
    form.querySelector('[name="nombreCliente"]').value = cliente.nombreCliente || '';
    form.querySelector('[name="apellidos"]').value = cliente.apellidos || '';
    form.querySelector('[name="direccion"]').value = cliente.direccion || '';
    form.querySelector('[name="telefono"]').value = cliente.telefono || '';
    form.querySelector('[name="email"]').value = cliente.email || '';

    openModal("clienteModal");
  } catch (e) {
    console.error('[Clientes] error al cargar cliente para editar', e);
    if (e.status === 404) {
      showAlert("Cliente no encontrado", "danger");
    } else {
      showAlert(`Error al cargar cliente: ${e.message}`, "danger");
    }
  }
}

async function viewCliente(id) {
  try {
    // Obtener datos del cliente desde la API
    const cliente = await apiGet(`/api/clientes/${id}`);
    if (!cliente) {
      showAlert("Cliente no encontrado", "danger");
      return;
    }

    currentClienteId = id;

    const detailsHtml = `
        <div class="cliente-details">
            <div class="detail-row">
                <strong>ID:</strong> ${cliente.idCliente}
            </div>
            <div class="detail-row">
                <strong>Nombre Completo:</strong> ${cliente.nombreCliente} ${cliente.apellidos}
            </div>
            <div class="detail-row">
                <strong>Dirección:</strong> ${cliente.direccion || 'Sin dirección'}
            </div>
            <div class="detail-row">
                <strong>Teléfono:</strong> ${cliente.telefono || 'Sin teléfono'}
            </div>
            <div class="detail-row">
                <strong>Email:</strong> ${cliente.email || 'Sin email'}
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
  } catch (e) {
    console.error('[Clientes] error al cargar cliente', e);
    if (e.status === 404) {
      showAlert("Cliente no encontrado", "danger");
    } else {
      showAlert(`Error al cargar cliente: ${e.message}`, "danger");
    }
  }
}

function editFromView() {
  closeModal("viewClienteModal");
  editCliente(currentClienteId);
}

async function deleteCliente(id) {
  try {
    // Obtener datos del cliente desde la API para confirmar eliminación
    const cliente = await apiGet(`/api/clientes/${id}`);
    if (!cliente) {
      showAlert("Cliente no encontrado", "danger");
      return;
    }

    if (
      confirmDelete(
        `¿Estás seguro de que deseas eliminar al cliente ${cliente.nombreCliente} ${cliente.apellidos}?`
      )
    ) {
      console.log('[Clientes] eliminando cliente', id);
      
      // Llamar a la API para eliminar
      await apiDelete(`/api/clientes/${id}`);
      
      // Recargar la tabla
      await loadClientes();
      showAlert("Cliente eliminado correctamente", "success");

      if (typeof addActivity === "function") {
        addActivity(
          "cliente",
          "Cliente eliminado",
          `${cliente.nombreCliente} ${cliente.apellidos} fue eliminado`
        );
      }
    }
  } catch (e) {
    console.error('[Clientes] error al eliminar', e);
    if (e.status === 404) {
      showAlert("Cliente no encontrado", "danger");
    } else if (e.status === 409) {
      showAlert("No se puede eliminar el cliente porque tiene facturas asociadas", "danger");
    } else {
      showAlert(`Error al eliminar cliente: ${e.message}`, "danger");
    }
  }
}

async function handleFormSubmit(e) {
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

  // validación se realiza en servidor

  let result;
  let actionType;

  try {
    if (currentClienteId) {
      result = await apiPut(`/api/clientes/${currentClienteId}`, clienteData);
      actionType = "actualizado";
    } else {
      result = await apiPost('/api/clientes', clienteData);
      actionType = "agregado";
    }
  } catch (e) {
    showAlert(e.data?.mensaje || `Error al guardar: ${e.message}`, 'danger');
    return;
  }

  if (result) {
    await loadClientes();
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


