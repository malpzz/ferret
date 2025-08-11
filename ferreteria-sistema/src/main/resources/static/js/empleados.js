let currentEmpleadoId = null;

document.addEventListener("DOMContentLoaded", async function () {
  await loadEmpleados();
  initializeEmpleadosPage();
});

function initializeEmpleadosPage() {
  searchTable("searchEmpleados", "#empleadosTable tbody");

  const form = document.getElementById("empleadoForm");
  form.addEventListener("submit", handleFormSubmit);

  // Initialize logout functionality
  initializeLogout();
}

async function loadEmpleados() {
  try {
    const empleados = await apiGet('/api/empleados');
    renderEmpleadosTable(empleados.map(e => ({
      id: e.idEmpleado || e.id,
      nombreEmpleado: e.nombreEmpleado,
      apellidos: e.apellidos,
      puesto: e.puesto || '-',
      telefono: e.telefono,
    })));
  } catch (e) {
    showAlert(`Error cargando empleados: ${e.message}`, 'danger');
  }
}

function renderEmpleadosTable(empleados) {
  const columns = [
    { key: "nombreEmpleado", label: "Nombre" },
    { key: "apellidos", label: "Apellidos" },
    { key: "puesto", label: "Puesto" },
    { key: "telefono", label: "Teléfono" },
  ];

  const actions = [
    {
      label: '<i class="fas fa-eye"></i>',
      class: "btn-primary",
      onclick: "viewEmpleado",
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning",
      onclick: "editEmpleado",
    },
    {
      label: '<i class="fas fa-trash"></i>',
      class: "btn-danger",
      onclick: "deleteEmpleado",
    },
  ];

  renderTable("empleadosTable", empleados, columns, actions);
}

function openAddModal() {
  document.getElementById("modalTitle").textContent = "Nuevo Empleado";
  clearForm("empleadoForm");
  currentEmpleadoId = null;
  openModal("empleadoModal");
}

async function editEmpleado(id) {
  try {
    const empleado = await apiGet(`/api/empleados/${id}`);
    
    document.getElementById("modalTitle").textContent = "Editar Empleado";
    currentEmpleadoId = id;

    const form = document.getElementById("empleadoForm");
    form.querySelector('[name="nombreEmpleado"]').value = empleado.nombreEmpleado || '';
    form.querySelector('[name="apellidos"]').value = empleado.apellidos || '';
    form.querySelector('[name="direccion"]').value = empleado.direccion || '';
    form.querySelector('[name="telefono"]').value = empleado.telefono || '';
    form.querySelector('[name="email"]').value = empleado.email || '';
    form.querySelector('[name="cedula"]').value = empleado.cedula || '';
    form.querySelector('[name="puesto"]').value = empleado.puesto || '';
    form.querySelector('[name="salario"]').value = empleado.salario || '';

    openModal("empleadoModal");
  } catch (e) {
    showAlert(`Error cargando empleado: ${e.message}`, "danger");
  }
}

async function viewEmpleado(id) {
  try {
    const empleado = await apiGet(`/api/empleados/${id}`);
    currentEmpleadoId = id;

    // Cargar horarios del empleado desde la API
    let empleadoHorarios = [];
    try {
      empleadoHorarios = await apiGet(`/api/horarios/empleado/${id}`);
      console.log('DEBUG - Horarios del empleado:', empleadoHorarios);
    } catch (e) {
      console.log('DEBUG - Error cargando horarios del empleado:', e);
      empleadoHorarios = [];
    }

    const detailsHtml = `
        <div class="empleado-details">
            <div class="detail-row">
                <strong>Nombre Completo:</strong> ${empleado.nombreEmpleado} ${empleado.apellidos}
            </div>
            <div class="detail-row">
                <strong>Puesto:</strong> ${empleado.puesto || 'No especificado'}
            </div>
            <div class="detail-row">
                <strong>Dirección:</strong> ${empleado.direccion || 'No especificada'}
            </div>
            <div class="detail-row">
                <strong>Teléfono:</strong> ${empleado.telefono || 'No especificado'}
            </div>
            <div class="detail-row">
                <strong>Email:</strong> ${empleado.email || 'No especificado'}
            </div>
            <div class="detail-row">
                <strong>Cédula:</strong> ${empleado.cedula || 'No especificada'}
            </div>
            <div class="detail-row">
                <strong>Salario:</strong> ${empleado.salario ? '$' + empleado.salario : 'No especificado'}
            </div>
            <div class="detail-row">
                <strong>Horarios Asignados:</strong>
                ${
                  empleadoHorarios.length > 0
                    ? `<ul>${empleadoHorarios
                        .map(
                          (h) => {
                            const fecha = h.fecha ? new Date(h.fecha).toLocaleDateString() : 'Sin fecha';
                            const entrada = h.horaEntrada ? formatHour(h.horaEntrada) : '-';
                            const salida = h.horaSalida ? formatHour(h.horaSalida) : '-';
                            return `<li><strong>${fecha}:</strong> ${entrada} - ${salida}</li>`;
                          }
                        )
                        .join("")}</ul>`
                    : '<span class="text-muted">Sin horarios asignados</span>'
                }
            </div>
        </div>
        <style>
            .empleado-details .detail-row {
                padding: 10px 0;
                border-bottom: 1px solid #e1e8ed;
            }
            .empleado-details .detail-row:last-child {
                border-bottom: none;
            }
            .empleado-details ul {
                margin: 5px 0 0 20px;
            }
            .text-muted { color: #6c757d; }
        </style>
    `;

    document.getElementById("empleadoDetails").innerHTML = detailsHtml;
    openModal("viewEmpleadoModal");
  } catch (e) {
    showAlert(`Error cargando empleado: ${e.message}`, "danger");
  }
}

function editFromView() {
  closeModal("viewEmpleadoModal");
  editEmpleado(currentEmpleadoId);
}

function manageSchedule() {
  if (currentEmpleadoId) {
    closeModal("viewEmpleadoModal");
    // Redirigir al módulo de horarios con el empleado preseleccionado
    window.location.href = buildApiUrl('/horarios').replace('/api', '') + `?empleadoId=${currentEmpleadoId}`;
  }
}

// Función para formatear horas decimales a HH:MM
function formatHour(decimalHour) {
  if (!decimalHour) return "-";
  const hours = Math.floor(decimalHour);
  const minutes = Math.round((decimalHour - hours) * 60);
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
}

async function deleteEmpleado(id) {
  try {
    const empleado = await apiGet(`/api/empleados/${id}`);
    
    if (confirmDelete(
      `¿Estás seguro de que deseas eliminar al empleado ${empleado.nombreEmpleado} ${empleado.apellidos}?`
    )) {
      await apiDelete(`/api/empleados/${id}`);
      await loadEmpleados();
      showAlert("Empleado eliminado correctamente", "success");

      if (typeof addActivity === "function") {
        addActivity(
          "empleado",
          "Empleado eliminado",
          `Se eliminó al empleado ${empleado.nombreEmpleado} ${empleado.apellidos}`
        );
      }
    }
  } catch (e) {
    if (e.status === 404) {
      showAlert("Empleado no encontrado", "danger");
    } else if (e.status === 409) {
      // Usar el mensaje del servidor si está disponible
      const mensaje = e.data?.mensaje || "No se puede eliminar el empleado porque tiene horarios asociados";
      showAlert(mensaje, "warning");
    } else {
      // Usar el mensaje del servidor si está disponible
      const mensaje = e.data?.mensaje || e.message || "Error desconocido";
      showAlert(`Error eliminando empleado: ${mensaje}`, "danger");
    }
  }
}

async function handleFormSubmit(e) {
  e.preventDefault();

  const validationRules = [
    { field: "nombreEmpleado", label: "Nombre", required: true, minLength: 2 },
    { field: "apellidos", label: "Apellidos", required: true, minLength: 2 },
    { field: "direccion", label: "Dirección", required: true, minLength: 5 },
    { field: "telefono", label: "Teléfono", required: true, type: "phone" },
    { field: "email", label: "Email", required: false, type: "email" },
    { field: "cedula", label: "Cédula", required: true, minLength: 5 },
    { field: "puesto", label: "Puesto", required: true },
    { field: "salario", label: "Salario", required: false, type: "number", min: 0 },
  ];

  if (!validateForm("empleadoForm", validationRules)) {
    return;
  }

  const formData = new FormData(e.target);
  const empleadoData = {
    nombreEmpleado: formData.get("nombreEmpleado").trim(),
    apellidos: formData.get("apellidos").trim(),
    direccion: formData.get("direccion").trim(),
    telefono: formData.get("telefono").trim(),
    email: formData.get("email") ? formData.get("email").trim() : null,
    cedula: formData.get("cedula").trim(),
    puesto: formData.get("puesto"),
    salario: formData.get("salario") ? parseFloat(formData.get("salario")) : null,
  };

  let result;
  let actionType;

  try {
    if (currentEmpleadoId) {
      result = await apiPut(`/api/empleados/${currentEmpleadoId}`, empleadoData);
      actionType = "actualizado";
    } else {
      result = await apiPost('/api/empleados', empleadoData);
      actionType = "agregado";
    }
  } catch (e) {
    const mensaje = e.data?.mensaje || e.message || "Error desconocido";
    showAlert(`Error al guardar empleado: ${mensaje}`, 'danger');
    return;
  }

  if (result) {
    await loadEmpleados();
    closeModal("empleadoModal");
    clearForm("empleadoForm");
    showAlert(`Empleado ${actionType} correctamente`, "success");

    if (typeof addActivity === "function") {
      const activityTitle = currentEmpleadoId
        ? "Empleado actualizado"
        : "Nuevo empleado agregado";
      const activityDesc = `${empleadoData.nombreEmpleado} ${empleadoData.apellidos} (${empleadoData.puesto}) fue ${actionType}`;
      addActivity("empleado", activityTitle, activityDesc);
    }

    currentEmpleadoId = null;
  } else {
    showAlert("Error al guardar el empleado", "danger");
  }
}

window.openAddModal = openAddModal;
window.editEmpleado = editEmpleado;
window.viewEmpleado = viewEmpleado;
window.deleteEmpleado = deleteEmpleado;
window.editFromView = editFromView;
window.manageSchedule = manageSchedule;


