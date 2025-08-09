let currentEmpleadoId = null;

document.addEventListener("DOMContentLoaded", function () {
  loadEmpleados();
  initializeEmpleadosPage();
});

function initializeEmpleadosPage() {
  searchTable("searchEmpleados", "#empleadosTable tbody");

  const form = document.getElementById("empleadoForm");
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

function loadEmpleados() {
  const empleados = loadData("empleados");
  renderEmpleadosTable(empleados);
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

function editEmpleado(id) {
  const empleado = getRecord("empleados", id);
  if (!empleado) {
    showAlert("Empleado no encontrado", "danger");
    return;
  }

  document.getElementById("modalTitle").textContent = "Editar Empleado";
  currentEmpleadoId = id;

  const form = document.getElementById("empleadoForm");
  form.querySelector('[name="id"]').value = empleado.id;
  form.querySelector('[name="nombreEmpleado"]').value = empleado.nombreEmpleado;
  form.querySelector('[name="apellidos"]').value = empleado.apellidos;
  form.querySelector('[name="direccion"]').value = empleado.direccion;
  form.querySelector('[name="telefono"]').value = empleado.telefono;
  form.querySelector('[name="puesto"]').value = empleado.puesto;

  openModal("empleadoModal");
}

function viewEmpleado(id) {
  const empleado = getRecord("empleados", id);
  if (!empleado) {
    showAlert("Empleado no encontrado", "danger");
    return;
  }

  currentEmpleadoId = id;

  const horarios = loadData("horarios");
  const empleadoHorarios = horarios.filter((h) => h.idEmpleado === id);

  const detailsHtml = `
        <div class="empleado-details">
            <div class="detail-row">
                <strong>Nombre Completo:</strong> ${empleado.nombreEmpleado} ${
    empleado.apellidos
  }
            </div>
            <div class="detail-row">
                <strong>Puesto:</strong> ${empleado.puesto}
            </div>
            <div class="detail-row">
                <strong>Dirección:</strong> ${empleado.direccion}
            </div>
            <div class="detail-row">
                <strong>Teléfono:</strong> ${empleado.telefono}
            </div>
            <div class="detail-row">
                <strong>Horarios Asignados:</strong>
                ${
                  empleadoHorarios.length > 0
                    ? `<ul>${empleadoHorarios
                        .map(
                          (h) =>
                            `<li>${h.dia}: ${h.horaEntrada} - ${h.horaSalida}</li>`
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
}

function editFromView() {
  closeModal("viewEmpleadoModal");
  editEmpleado(currentEmpleadoId);
}

function manageSchedule() {
  if (currentEmpleadoId) {
    closeModal("viewEmpleadoModal");
    window.location.href = `horarios.html?empleado=${currentEmpleadoId}`;
  }
}

function deleteEmpleado(id) {
  const empleado = getRecord("empleados", id);
  if (!empleado) {
    showAlert("Empleado no encontrado", "danger");
    return;
  }

  if (
    confirmDelete(
      `¿Estás seguro de que deseas eliminar al empleado ${empleado.nombreEmpleado} ${empleado.apellidos}?`
    )
  ) {
    const horarios = loadData("horarios");
    const hasSchedules = horarios.some((h) => h.idEmpleado === id);

    if (hasSchedules) {
      const confirmDelete2 = confirm(
        "Este empleado tiene horarios asignados. ¿Desea eliminarlos también?"
      );
      if (confirmDelete2) {
        const updatedHorarios = horarios.filter((h) => h.idEmpleado !== id);
        saveData("horarios", updatedHorarios);
      } else {
        return;
      }
    }

    deleteRecord("empleados", id);
    loadEmpleados();
    showAlert("Empleado eliminado correctamente", "success");

    if (typeof addActivity === "function") {
      addActivity(
        "empleado",
        "Empleado eliminado",
        `Se eliminó al empleado ${empleado.nombreEmpleado} ${empleado.apellidos}`
      );
    }
  }
}

function handleFormSubmit(e) {
  e.preventDefault();

  const validationRules = [
    { field: "nombreEmpleado", label: "Nombre", required: true, minLength: 2 },
    { field: "apellidos", label: "Apellidos", required: true, minLength: 2 },
    { field: "direccion", label: "Dirección", required: true, minLength: 5 },
    { field: "telefono", label: "Teléfono", required: true, type: "phone" },
    { field: "puesto", label: "Puesto", required: true },
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
    puesto: formData.get("puesto"),
  };

  let result;
  let actionType;

  if (currentEmpleadoId) {
    empleadoData.id = currentEmpleadoId;
    result = updateRecord("empleados", empleadoData);
    actionType = "actualizado";
  } else {
    const empleados = loadData("empleados");
    empleadoData.id = generateId(empleados);
    result = addRecord("empleados", empleadoData);
    actionType = "agregado";
  }

  if (result) {
    loadEmpleados();
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
