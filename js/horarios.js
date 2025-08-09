let currentHorarioId = null;
const daysOfWeek = [
  "lunes",
  "martes",
  "miercoles",
  "jueves",
  "viernes",
  "sabado",
  "domingo",
];
const dayNames = {
  lunes: "Lunes",
  martes: "Martes",
  miercoles: "Miércoles",
  jueves: "Jueves",
  viernes: "Viernes",
  sabado: "Sábado",
  domingo: "Domingo",
};

document.addEventListener("DOMContentLoaded", function () {
  loadHorarios();
  loadEmpleados();
  initializeHorariosPage();
  initializeDayCheckboxes();
});

function initializeHorariosPage() {
  searchTable("searchHorarios", "#horariosTable tbody");

  const statusFilter = document.getElementById("statusFilter");
  statusFilter.addEventListener("change", filterByStatus);

  const employeeFilter = document.getElementById("employeeFilter");
  employeeFilter.addEventListener("change", filterByEmployee);

  const form = document.getElementById("horarioForm");
  form.addEventListener("submit", handleFormSubmit);

  const logoutBtn = document.querySelector(".btn-logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      if (confirm("¿Estás seguro de que deseas cerrar sesión?")) {
        alert("Sesión cerrada correctamente");
      }
    });
  }

  document.getElementById("fechaInicio").value = new Date()
    .toISOString()
    .split("T")[0];

  loadScheduleCalendar();
}

function initializeDayCheckboxes() {
  daysOfWeek.forEach((day) => {
    const checkbox = document.querySelector(`.day-checkbox[data-day="${day}"]`);
    const dayScheduleDiv = document.querySelector(
      `.day-schedule[data-day="${day}"]`
    );

    checkbox.addEventListener("change", function () {
      const timeInputs = dayScheduleDiv.querySelectorAll('input[type="time"]');

      if (this.checked) {
        dayScheduleDiv.classList.add("active");
        timeInputs.forEach((input) => (input.disabled = false));
      } else {
        dayScheduleDiv.classList.remove("active");
        timeInputs.forEach((input) => {
          input.disabled = true;
          input.value = "";
        });
      }
    });

    const timeInputs = dayScheduleDiv.querySelectorAll('input[type="time"]');
    timeInputs.forEach((input) => (input.disabled = true));
  });
}

function loadHorarios() {
  const horarios = loadData("horarios");
  const empleados = loadData("empleados");

  const horariosWithEmployees = horarios.map((horario) => {
    const empleado = empleados.find((e) => e.id === horario.idEmpleado);
    return {
      ...horario,
      nombreEmpleado: empleado
        ? empleado.nombreEmpleado
        : "Empleado no encontrado",
    };
  });

  renderHorariosTable(horariosWithEmployees);
}

function loadEmpleados() {
  const empleados = loadData("empleados");

  const select = document.getElementById("idEmpleado");
  select.innerHTML = '<option value="">Seleccione un empleado</option>';

  const filterSelect = document.getElementById("employeeFilter");
  filterSelect.innerHTML = '<option value="">Todos los empleados</option>';

  empleados.forEach((empleado) => {
    const option = document.createElement("option");
    option.value = empleado.id;
    option.textContent = `${empleado.nombreEmpleado} - ${empleado.puesto}`;
    select.appendChild(option);

    const filterOption = option.cloneNode(true);
    filterSelect.appendChild(filterOption);
  });
}

function renderHorariosTable(horarios) {
  const columns = [
    { key: "nombreEmpleado", label: "Empleado" },
    {
      key: "tipoTurno",
      label: "Tipo de Turno",
      format: (value) => value.charAt(0).toUpperCase() + value.slice(1),
    },
    {
      key: "fechaInicio",
      label: "Fecha Inicio",
      format: (value) => new Date(value).toLocaleDateString(),
    },
    {
      key: "fechaFin",
      label: "Fecha Fin",
      format: (value) =>
        value ? new Date(value).toLocaleDateString() : "Indefinido",
    },
    {
      key: "horasSemanales",
      label: "Horas/Semana",
      format: (value) => (value ? `${value}h` : "N/A"),
    },
    {
      key: "estado",
      label: "Estado",
      format: (value) =>
        `<span class="status-badge status-${value}">${
          value.charAt(0).toUpperCase() + value.slice(1)
        }</span>`,
    },
  ];

  const actions = [
    {
      label: '<i class="fas fa-eye"></i>',
      class: "btn-primary",
      onclick: "viewHorario",
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning",
      onclick: "editHorario",
    },
    {
      label: '<i class="fas fa-copy"></i>',
      class: "btn-info",
      onclick: "duplicateHorario",
    },
    {
      label: '<i class="fas fa-trash"></i>',
      class: "btn-danger",
      onclick: "deleteHorario",
    },
  ];

  renderTable("horariosTable", horarios, columns, actions);
}

function filterByStatus() {
  applyFilters();
}

function filterByEmployee() {
  applyFilters();
}

function applyFilters() {
  const status = document.getElementById("statusFilter").value;
  const employeeId = document.getElementById("employeeFilter").value;
  const horarios = loadData("horarios");
  const empleados = loadData("empleados");

  let filteredHorarios = horarios;

  if (status) {
    filteredHorarios = filteredHorarios.filter(
      (horario) => horario.estado === status
    );
  }

  if (employeeId) {
    filteredHorarios = filteredHorarios.filter(
      (horario) => horario.idEmpleado === employeeId
    );
  }

  const horariosWithEmployees = filteredHorarios.map((horario) => {
    const empleado = empleados.find((e) => e.id === horario.idEmpleado);
    return {
      ...horario,
      nombreEmpleado: empleado
        ? empleado.nombreEmpleado
        : "Empleado no encontrado",
    };
  });

  renderHorariosTable(horariosWithEmployees);
  loadScheduleCalendar();
}

function loadScheduleCalendar() {
  const employeeId = document.getElementById("employeeFilter").value;
  const horarios = loadData("horarios");
  const empleados = loadData("empleados");

  let activeSchedules = horarios.filter((h) => h.estado === "activo");

  if (employeeId) {
    activeSchedules = activeSchedules.filter(
      (h) => h.idEmpleado === employeeId
    );
  }

  const calendarHtml = generateWeeklyCalendar(activeSchedules, empleados);
  document.getElementById("scheduleCalendar").innerHTML = calendarHtml;
}

function generateWeeklyCalendar(schedules, employees) {
  const daysHeader = [
    "Empleado",
    "Lunes",
    "Martes",
    "Miércoles",
    "Jueves",
    "Viernes",
    "Sábado",
    "Domingo",
  ];

  let calendarHtml = `
        <table class="calendar-table">
            <thead>
                <tr>
                    ${daysHeader.map((day) => `<th>${day}</th>`).join("")}
                </tr>
            </thead>
            <tbody>
    `;

  const employeesWithSchedules = employees.filter((emp) =>
    schedules.some((schedule) => schedule.idEmpleado === emp.id)
  );

  employeesWithSchedules.forEach((employee) => {
    const employeeSchedules = schedules.filter(
      (s) => s.idEmpleado === employee.id
    );

    calendarHtml += `<tr>`;
    calendarHtml += `<td><strong>${employee.nombre}</strong><br><small>${employee.puesto}</small></td>`;

    daysOfWeek.forEach((day) => {
      calendarHtml += `<td>`;

      employeeSchedules.forEach((schedule) => {
        if (schedule.horarios && schedule.horarios[day]) {
          const daySchedule = schedule.horarios[day];
          if (daySchedule.entrada || daySchedule.salida) {
            calendarHtml += `<div class="employee-schedule">`;
            if (daySchedule.entrada && daySchedule.salida) {
              calendarHtml += `${daySchedule.entrada} - ${daySchedule.salida}`;
            }
            if (daySchedule.entrada_tarde && daySchedule.salida_tarde) {
              calendarHtml += `<br>${daySchedule.entrada_tarde} - ${daySchedule.salida_tarde}`;
            }
            calendarHtml += `</div>`;
          }
        }
      });

      calendarHtml += `</td>`;
    });

    calendarHtml += `</tr>`;
  });

  calendarHtml += `
            </tbody>
        </table>
    `;

  return calendarHtml;
}

function openAddModal() {
  document.getElementById("modalTitle").textContent = "Nuevo Horario";
  clearForm("horarioForm");
  currentHorarioId = null;

  daysOfWeek.forEach((day) => {
    const checkbox = document.querySelector(`.day-checkbox[data-day="${day}"]`);
    const dayScheduleDiv = document.querySelector(
      `.day-schedule[data-day="${day}"]`
    );
    const timeInputs = dayScheduleDiv.querySelectorAll('input[type="time"]');

    checkbox.checked = false;
    dayScheduleDiv.classList.remove("active");
    timeInputs.forEach((input) => {
      input.disabled = true;
      input.value = "";
    });
  });

  document.getElementById("fechaInicio").value = new Date()
    .toISOString()
    .split("T")[0];
  document.getElementById("estado").value = "activo";
  document.getElementById("tipoTurno").value = "completo";

  openModal("horarioModal");
}

function editHorario(id) {
  const horario = getRecord("horarios", id);
  if (!horario) {
    showAlert("Horario no encontrado", "danger");
    return;
  }

  document.getElementById("modalTitle").textContent = "Editar Horario";
  currentHorarioId = id;

  const form = document.getElementById("horarioForm");
  form.querySelector('[name="id"]').value = horario.id;
  form.querySelector('[name="idEmpleado"]').value = horario.idEmpleado;
  form.querySelector('[name="fechaInicio"]').value = horario.fechaInicio;
  form.querySelector('[name="fechaFin"]').value = horario.fechaFin || "";
  form.querySelector('[name="tipoTurno"]').value = horario.tipoTurno;
  form.querySelector('[name="horasSemanales"]').value =
    horario.horasSemanales || "";
  form.querySelector('[name="estado"]').value = horario.estado;
  form.querySelector('[name="observaciones"]').value =
    horario.observaciones || "";

  daysOfWeek.forEach((day) => {
    const checkbox = document.querySelector(`.day-checkbox[data-day="${day}"]`);
    const dayScheduleDiv = document.querySelector(
      `.day-schedule[data-day="${day}"]`
    );
    const timeInputs = dayScheduleDiv.querySelectorAll('input[type="time"]');

    if (horario.horarios && horario.horarios[day]) {
      const dayData = horario.horarios[day];
      checkbox.checked = true;
      dayScheduleDiv.classList.add("active");

      timeInputs.forEach((input) => (input.disabled = false));

      if (dayData.entrada)
        form.querySelector(`[name="${day}_entrada"]`).value = dayData.entrada;
      if (dayData.salida)
        form.querySelector(`[name="${day}_salida"]`).value = dayData.salida;
      if (dayData.entrada_tarde)
        form.querySelector(`[name="${day}_entrada_tarde"]`).value =
          dayData.entrada_tarde;
      if (dayData.salida_tarde)
        form.querySelector(`[name="${day}_salida_tarde"]`).value =
          dayData.salida_tarde;
    } else {
      checkbox.checked = false;
      dayScheduleDiv.classList.remove("active");
      timeInputs.forEach((input) => {
        input.disabled = true;
        input.value = "";
      });
    }
  });

  openModal("horarioModal");
}

function viewHorario(id) {
  const horario = getRecord("horarios", id);
  if (!horario) {
    showAlert("Horario no encontrado", "danger");
    return;
  }

  currentHorarioId = id;
  const empleados = loadData("empleados");
  const empleado = empleados.find((e) => e.id === horario.idEmpleado);

  const detailsHtml = generateSchedulePreview(horario, empleado);
  document.getElementById("horarioDetails").innerHTML = detailsHtml;
  openModal("viewHorarioModal");
}

function generateSchedulePreview(horario, empleado) {
  let totalHours = 0;
  let workingDays = 0;

  const schedulePreviewHtml = daysOfWeek
    .map((day) => {
      const dayData = horario.horarios && horario.horarios[day];
      let dayClass = "off";
      let dayContent = "<strong>Descanso</strong>";
      let dayHours = 0;

      if (dayData && (dayData.entrada || dayData.salida)) {
        dayClass = "working";
        workingDays++;
        dayContent = `<strong>${dayNames[day]}</strong><br>`;

        if (dayData.entrada && dayData.salida) {
          dayContent += `${dayData.entrada} - ${dayData.salida}<br>`;
          dayHours += calculateHoursDifference(dayData.entrada, dayData.salida);
        }

        if (dayData.entrada_tarde && dayData.salida_tarde) {
          dayContent += `${dayData.entrada_tarde} - ${dayData.salida_tarde}`;
          dayHours += calculateHoursDifference(
            dayData.entrada_tarde,
            dayData.salida_tarde
          );
        }

        totalHours += dayHours;
      } else {
        dayContent = `<strong>${dayNames[day]}</strong><br>Descanso`;
      }

      return `<div class="schedule-day ${dayClass}">${dayContent}</div>`;
    })
    .join("");

  return `
        <div class="schedule-preview-container">
            <div class="hours-summary">
                <h4><i class="fas fa-clock"></i> Resumen del Horario</h4>
                <div style="display: flex; justify-content: space-between; margin-bottom: 15px;">
                    <div>
                        <strong>Empleado:</strong> ${
                          empleado ? empleado.nombreEmpleado : "No encontrado"
                        }<br>
                        <strong>Puesto:</strong> ${
                          empleado ? empleado.puesto : "N/A"
                        }<br>
                        <strong>Tipo de Turno:</strong> ${
                          horario.tipoTurno.charAt(0).toUpperCase() +
                          horario.tipoTurno.slice(1)
                        }
                    </div>
                    <div>
                        <strong>Fecha Inicio:</strong> ${new Date(
                          horario.fechaInicio
                        ).toLocaleDateString()}<br>
                        <strong>Fecha Fin:</strong> ${
                          horario.fechaFin
                            ? new Date(horario.fechaFin).toLocaleDateString()
                            : "Indefinido"
                        }<br>
                        <strong>Estado:</strong> <span class="status-badge status-${
                          horario.estado
                        }">${
    horario.estado.charAt(0).toUpperCase() + horario.estado.slice(1)
  }</span>
                    </div>
                </div>
                <div style="display: flex; justify-content: space-around; background: #e3f2fd; padding: 10px; border-radius: 8px;">
                    <div><strong>Días Laborales:</strong> ${workingDays}</div>
                    <div><strong>Horas Calculadas:</strong> ${totalHours.toFixed(
                      1
                    )}h</div>
                    <div><strong>Horas Definidas:</strong> ${
                      horario.horasSemanales || "N/A"
                    }h</div>
                </div>
            </div>

            <h4>Horario Semanal</h4>
            <div class="schedule-preview">
                ${schedulePreviewHtml}
            </div>

            ${
              horario.observaciones
                ? `
                <div style="margin-top: 20px;">
                    <h4>Observaciones:</h4>
                    <p>${horario.observaciones}</p>
                </div>
            `
                : ""
            }
        </div>
    `;
}

function calculateHoursDifference(startTime, endTime) {
  if (!startTime || !endTime) return 0;

  const start = new Date(`2024-01-01 ${startTime}`);
  const end = new Date(`2024-01-01 ${endTime}`);

  if (end < start) {
    end.setDate(end.getDate() + 1);
  }

  return (end - start) / (1000 * 60 * 60);
}

function editFromView() {
  closeModal("viewHorarioModal");
  editHorario(currentHorarioId);
}

function printSchedule() {
  const horario = getRecord("horarios", currentHorarioId);
  const empleados = loadData("empleados");
  const empleado = empleados.find((e) => e.id === horario.idEmpleado);

  const printContent = generateSchedulePreview(horario, empleado);

  const printWindow = window.open("", "_blank");
  printWindow.document.write(`
        <html>
            <head>
                <title>Horario - ${
                  empleado ? empleado.nombreEmpleado : "Empleado"
                }</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .hours-summary { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 8px; padding: 15px; margin: 15px 0; }
                    .schedule-preview { display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px; margin: 20px 0; }
                    .schedule-day { border: 1px solid #dee2e6; border-radius: 8px; padding: 10px; text-align: center; min-height: 80px; }
                    .schedule-day.working { background: #e8f5e8; border-color: #28a745; }
                    .schedule-day.off { background: #f8f9fa; color: #6c757d; }
                    .status-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.75rem; }
                    .status-activo { background: #d4edda; color: #155724; }
                    .status-inactivo { background: #f8d7da; color: #721c24; }
                </style>
            </head>
            <body>
                <h1>Horario de Trabajo</h1>
                ${printContent}
            </body>
        </html>
    `);

  printWindow.document.close();
  printWindow.print();
}

function duplicateHorario(id) {
  const horario = getRecord("horarios", id);
  if (!horario) {
    showAlert("Horario no encontrado", "danger");
    return;
  }

  const empleados = loadData("empleados");
  const empleado = empleados.find((e) => e.id === horario.idEmpleado);
  const empleadoName = empleado ? empleado.nombreEmpleado : "empleado";

  if (confirm(`¿Duplicar el horario de ${empleadoName}?`)) {
    const newHorario = {
      ...horario,
      id: generateId(),
      fechaInicio: new Date().toISOString().split("T")[0],
      fechaFin: null,
      fechaCreacion: new Date().toISOString(),
    };

    if (saveRecord("horarios", newHorario)) {
      showAlert("Horario duplicado correctamente", "success");
      loadHorarios();
      loadScheduleCalendar();
      logActivity(`Horario duplicado para: ${empleadoName}`);
    } else {
      showAlert("Error al duplicar el horario", "danger");
    }
  }
}

function deleteHorario(id) {
  const horario = getRecord("horarios", id);
  if (!horario) {
    showAlert("Horario no encontrado", "danger");
    return;
  }

  const empleados = loadData("empleados");
  const empleado = empleados.find((e) => e.id === horario.idEmpleado);
  const empleadoName = empleado ? empleado.nombreEmpleado : "empleado";

  if (confirm(`¿Estás seguro de eliminar el horario de ${empleadoName}?`)) {
    if (deleteRecord("horarios", id)) {
      showAlert("Horario eliminado correctamente", "success");
      loadHorarios();
      loadScheduleCalendar();
      logActivity(`Horario eliminado para: ${empleadoName}`);
    } else {
      showAlert("Error al eliminar el horario", "danger");
    }
  }
}

function handleFormSubmit(e) {
  e.preventDefault();

  const formData = new FormData(e.target);

  const horarios = {};
  let hasAnySchedule = false;

  daysOfWeek.forEach((day) => {
    const checkbox = document.querySelector(`.day-checkbox[data-day="${day}"]`);

    if (checkbox.checked) {
      const entrada = formData.get(`${day}_entrada`);
      const salida = formData.get(`${day}_salida`);
      const entrada_tarde = formData.get(`${day}_entrada_tarde`);
      const salida_tarde = formData.get(`${day}_salida_tarde`);

      if (entrada || salida || entrada_tarde || salida_tarde) {
        horarios[day] = {
          entrada: entrada || null,
          salida: salida || null,
          entrada_tarde: entrada_tarde || null,
          salida_tarde: salida_tarde || null,
        };
        hasAnySchedule = true;
      }
    }
  });

  if (!hasAnySchedule) {
    showAlert(
      "Debe definir al menos un horario para un día de la semana",
      "danger"
    );
    return;
  }

  // Calculate total weekly hours if not provided
  let calculatedHours = 0;
  Object.values(horarios).forEach((daySchedule) => {
    if (daySchedule.entrada && daySchedule.salida) {
      calculatedHours += calculateHoursDifference(
        daySchedule.entrada,
        daySchedule.salida
      );
    }
    if (daySchedule.entrada_tarde && daySchedule.salida_tarde) {
      calculatedHours += calculateHoursDifference(
        daySchedule.entrada_tarde,
        daySchedule.salida_tarde
      );
    }
  });

  const horarioData = {
    idEmpleado: formData.get("idEmpleado"),
    fechaInicio: formData.get("fechaInicio"),
    fechaFin: formData.get("fechaFin") || null,
    tipoTurno: formData.get("tipoTurno"),
    horasSemanales:
      parseFloat(formData.get("horasSemanales")) || calculatedHours,
    estado: formData.get("estado"),
    observaciones: formData.get("observaciones").trim(),
    horarios: horarios,
  };

  // Validation
  if (
    !horarioData.idEmpleado ||
    !horarioData.fechaInicio ||
    !horarioData.tipoTurno
  ) {
    showAlert("Complete todos los campos obligatorios", "danger");
    return;
  }

  // Validate date range
  if (horarioData.fechaFin) {
    const startDate = new Date(horarioData.fechaInicio);
    const endDate = new Date(horarioData.fechaFin);

    if (endDate <= startDate) {
      showAlert(
        "La fecha de fin debe ser posterior a la fecha de inicio",
        "danger"
      );
      return;
    }
  }

  // Check for overlapping schedules for the same employee
  const existingSchedules = loadData("horarios");
  const employeeSchedules = existingSchedules.filter(
    (h) =>
      h.idEmpleado === horarioData.idEmpleado &&
      h.id !== currentHorarioId &&
      h.estado === "activo"
  );

  const hasOverlap = employeeSchedules.some((existing) => {
    const existingStart = new Date(existing.fechaInicio);
    const existingEnd = existing.fechaFin
      ? new Date(existing.fechaFin)
      : new Date("2099-12-31");
    const newStart = new Date(horarioData.fechaInicio);
    const newEnd = horarioData.fechaFin
      ? new Date(horarioData.fechaFin)
      : new Date("2099-12-31");

    return newStart <= existingEnd && newEnd >= existingStart;
  });

  if (hasOverlap) {
    showAlert(
      "Ya existe un horario activo para este empleado que se superpone con las fechas indicadas",
      "danger"
    );
    return;
  }

  if (currentHorarioId) {
    // Edit existing horario
    horarioData.id = currentHorarioId;
    horarioData.fechaModificacion = new Date().toISOString();

    if (updateRecord("horarios", currentHorarioId, horarioData)) {
      showAlert("Horario actualizado correctamente", "success");
      closeModal("horarioModal");
      loadHorarios();
      loadScheduleCalendar();

      const empleados = loadData("empleados");
      const empleado = empleados.find((e) => e.id === horarioData.idEmpleado);
      logActivity(
        `Horario modificado para: ${
          empleado ? empleado.nombreEmpleado : "empleado"
        }`
      );
    } else {
      showAlert("Error al actualizar el horario", "danger");
    }
  } else {
    // Create new horario
    horarioData.id = generateId();
    horarioData.fechaCreacion = new Date().toISOString();

    if (saveRecord("horarios", horarioData)) {
      showAlert("Horario creado correctamente", "success");
      closeModal("horarioModal");
      loadHorarios();
      loadScheduleCalendar();

      const empleados = loadData("empleados");
      const empleado = empleados.find((e) => e.id === horarioData.idEmpleado);
      logActivity(
        `Nuevo horario creado para: ${
          empleado ? empleado.nombreEmpleado : "empleado"
        }`
      );
    } else {
      showAlert("Error al crear el horario", "danger");
    }
  }
}

function initializeSampleHorarios() {
  const horarios = loadData("horarios");
  if (horarios.length === 0) {
    const empleados = loadData("empleados");

    if (empleados.length > 0) {
      const sampleHorarios = [
        {
          id: "1",
          idEmpleado: empleados[0].id,
          fechaInicio: "2024-01-01",
          fechaFin: null,
          tipoTurno: "completo",
          horasSemanales: 40,
          estado: "activo",
          observaciones: "Horario estándar de tiempo completo",
          horarios: {
            lunes: {
              entrada: "08:00",
              salida: "17:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
            martes: {
              entrada: "08:00",
              salida: "17:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
            miercoles: {
              entrada: "08:00",
              salida: "17:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
            jueves: {
              entrada: "08:00",
              salida: "17:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
            viernes: {
              entrada: "08:00",
              salida: "17:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
          },
          fechaCreacion: new Date().toISOString(),
        },
      ];

      if (empleados.length > 1) {
        sampleHorarios.push({
          id: "2",
          idEmpleado: empleados[1].id,
          fechaInicio: "2024-01-01",
          fechaFin: null,
          tipoTurno: "parcial",
          horasSemanales: 24,
          estado: "activo",
          observaciones: "Horario de medio tiempo - mañanas",
          horarios: {
            lunes: {
              entrada: "08:00",
              salida: "12:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
            martes: {
              entrada: "08:00",
              salida: "12:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
            miercoles: {
              entrada: "08:00",
              salida: "12:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
            jueves: {
              entrada: "08:00",
              salida: "12:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
            viernes: {
              entrada: "08:00",
              salida: "12:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
            sabado: {
              entrada: "08:00",
              salida: "12:00",
              entrada_tarde: null,
              salida_tarde: null,
            },
          },
          fechaCreacion: new Date().toISOString(),
        });
      }

      saveData("horarios", sampleHorarios);
    }
  }
}

document.addEventListener("DOMContentLoaded", function () {
  setTimeout(initializeSampleHorarios, 500);
});
