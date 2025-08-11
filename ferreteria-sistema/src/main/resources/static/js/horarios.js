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

document.addEventListener("DOMContentLoaded", async function () {
  await loadHorarios();
  await loadEmpleados();
  initializeHorariosPage();
  initializeDayCheckboxes();
  
  // Verificar si hay un empleadoId en la URL para preseleccionar
  const urlParams = new URLSearchParams(window.location.search);
  const empleadoId = urlParams.get('empleadoId');
  if (empleadoId) {
    console.log('DEBUG - Preseleccionando empleado:', empleadoId);
    const employeeFilter = document.getElementById("employeeFilter");
    if (employeeFilter) {
      employeeFilter.value = empleadoId;
      // Disparar el filtro para mostrar solo horarios de ese empleado
      filterByEmployee();
    }
  }
});

function initializeHorariosPage() {
  searchTable("searchHorarios", "#horariosTable tbody");

  const statusFilter = document.getElementById("statusFilter");
  statusFilter.addEventListener("change", filterByStatus);

  const employeeFilter = document.getElementById("employeeFilter");
  employeeFilter.addEventListener("change", filterByEmployee);

  const form = document.getElementById("horarioForm");
  form.addEventListener("submit", handleFormSubmit);

  // Initialize logout functionality
  initializeLogout();

  document.getElementById("fechaInicio").value = new Date()
    .toISOString()
    .split("T")[0];

  loadScheduleCalendar();
}

// Función simplificada - ya no hay checkboxes por día
function initializeDayCheckboxes() {
  // Esta función ya no es necesaria con el formulario simplificado
}

async function loadHorarios() {
  try {
    const horarios = await apiGet('/api/horarios');
    console.log('DEBUG - Horarios recibidos del backend:', horarios);
    
    // Inspeccionar estructura de cada horario
    if (horarios.length > 0) {
      console.log('DEBUG - Primer horario completo:', JSON.stringify(horarios[0], null, 2));
      console.log('DEBUG - ID del primer horario:', horarios[0].idHorario);
      console.log('DEBUG - Empleado del primer horario:', horarios[0].empleado);
    }
    
    // Los datos ya vienen con información del empleado desde el backend
    window.horariosData = horarios; // Guardar para uso posterior
    renderHorariosTable(horarios);
  } catch (e) {
    console.error('Error cargando horarios:', e);
    showAlert(`Error cargando horarios: ${e.message}`, 'danger');
  }
}

async function loadEmpleados() {
  const empleados = await apiGet('/api/empleados');

  const select = document.getElementById("idEmpleado");
  select.innerHTML = '<option value="">Seleccione un empleado</option>';

  const filterSelect = document.getElementById("employeeFilter");
  filterSelect.innerHTML = '<option value="">Todos los empleados</option>';

  empleados.forEach((empleado) => {
    const option = document.createElement("option");
    option.value = empleado.idEmpleado || empleado.id;
    option.textContent = `${empleado.nombreEmpleado} - ${empleado.puesto || ''}`;
    select.appendChild(option);

    const filterOption = option.cloneNode(true);
    filterSelect.appendChild(filterOption);
  });
}

function renderHorariosTable(horarios) {
  const columns = [
    { 
      key: "empleado", 
      label: "Empleado",
      format: (value, item) => getEmpleadoName(item)
    },
    {
      key: "fecha",
      label: "Fecha",
      format: (value) => value ? new Date(value).toLocaleDateString() : "-",
    },
    {
      key: "horaEntrada",
      label: "Hora Entrada",
      format: (value) => value ? formatHour(value) : "-",
    },
    {
      key: "horaSalida", 
      label: "Hora Salida",
      format: (value) => value ? formatHour(value) : "-",
    },
    {
      key: "horasTrabajadas",
      label: "Horas Trabajadas",
      format: (value) => value ? `${value}h` : "-",
    },
  ];

  const actions = [
    {
      label: '<i class="fas fa-eye"></i>',
      class: "btn-primary",
      onclick: "viewHorario",
      idField: "idHorario"  // Especificar el campo del ID
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning", 
      onclick: "editHorario",
      idField: "idHorario"  // Especificar el campo del ID
    },
    {
      label: '<i class="fas fa-copy"></i>',
      class: "btn-info",
      onclick: "duplicateHorario",
      idField: "idHorario"  // Especificar el campo del ID
    },
    {
      label: '<i class="fas fa-trash"></i>',
      class: "btn-danger",
      onclick: "deleteHorario",
      idField: "idHorario"  // Especificar el campo del ID
    },
  ];

  console.log('DEBUG - Renderizando tabla con horarios:', horarios.map(h => ({
    id: h.idHorario,
    empleado: h.empleadoInfo || h.empleado,
    fecha: h.fecha
  })));

  renderTable("horariosTable", horarios, columns, actions);
}

function formatHour(decimalHour) {
  if (!decimalHour) return "-";
  const hours = Math.floor(decimalHour);
  const minutes = Math.round((decimalHour - hours) * 60);
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
}

// Función helper para obtener consistentemente el nombre del empleado
function getEmpleadoName(horario) {
  // Obtener el empleadoInfo del horario (puede venir como empleadoInfo o empleado)
  const empleadoInfo = horario.empleadoInfo || horario.empleado;
  
  if (!empleadoInfo) {
    return `Empleado #${horario.idEmpleado || 'N/A'}`;
  }
  
  const nombre = empleadoInfo.nombreEmpleado || '';
  const apellidos = empleadoInfo.apellidos || '';
  
  if (nombre && apellidos) {
    return `${nombre} ${apellidos}`.trim();
  } else if (nombre) {
    return nombre.trim();
  } else if (apellidos) {
    return apellidos.trim();
  } else {
    // Si no hay nombre, usar el puesto como fallback
    return empleadoInfo.puesto || `Empleado #${empleadoInfo.idEmpleado || horario.idEmpleado}`;
  }
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
  
  if (!window.horariosData) {
    console.log('DEBUG - No hay datos de horarios cargados');
    return;
  }

  let filteredHorarios = [...window.horariosData];

  console.log('DEBUG - Aplicando filtros. Total horarios:', filteredHorarios.length);
  console.log('DEBUG - Filtro empleado ID:', employeeId);
  console.log('DEBUG - Filtro estado:', status);

  // Filtrar por empleado
  if (employeeId) {
    filteredHorarios = filteredHorarios.filter(
      (horario) => {
        const idEmpleadoHorario = horario.idEmpleado || 
                                 (horario.empleadoInfo ? horario.empleadoInfo.idEmpleado : null) ||
                                 (horario.empleado ? horario.empleado.idEmpleado : null);
        return idEmpleadoHorario == employeeId;
      }
    );
    console.log('DEBUG - Después de filtrar por empleado:', filteredHorarios.length);
  }

  // Filtrar por estado (si implementas estados más adelante)
  if (status) {
    filteredHorarios = filteredHorarios.filter(
      (horario) => horario.estado === status
    );
    console.log('DEBUG - Después de filtrar por estado:', filteredHorarios.length);
  }

  renderHorariosTable(filteredHorarios);
}

function loadScheduleCalendar() {
  // Simplificado por ahora - el calendario semanal es complejo para la estructura actual
  // Podemos implementarlo más adelante si es necesario
  const calendarContainer = document.getElementById("scheduleCalendar");
  if (calendarContainer) {
    calendarContainer.innerHTML = '<p class="text-muted">Calendario de horarios en desarrollo</p>';
  }
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

  document.getElementById("fechaInicio").value = new Date()
    .toISOString()
    .split("T")[0];

  openModal("horarioModal");
}

async function editHorario(id) {
  // Buscar el horario en los datos cargados o en el backend
  let horario = window.horariosData?.find(h => h.idHorario == id);
  
  if (!horario) {
    try {
      horario = await apiGet(`/api/horarios/${id}`);
    } catch (e) {
      console.error('Error obteniendo horario:', e);
      showAlert("Horario no encontrado", "danger");
      return;
    }
  }

  document.getElementById("modalTitle").textContent = "Editar Horario";
  currentHorarioId = id;

  const form = document.getElementById("horarioForm");
  form.querySelector('[name="id"]').value = horario.idHorario;
  
  // Obtener ID del empleado desde diferentes fuentes posibles
  const empleadoId = horario.idEmpleado || 
                    (horario.empleadoInfo ? horario.empleadoInfo.idEmpleado : '') ||
                    (horario.empleado ? horario.empleado.idEmpleado : '');
  form.querySelector('[name="idEmpleado"]').value = empleadoId;
  
  form.querySelector('[name="fechaInicio"]').value = horario.fecha;
  form.querySelector('[name="observaciones"]').value = horario.observaciones || "";
  
  // Para horarios simples - solo convertir las horas decimales a formato HH:MM
  if (horario.horaEntrada) {
    form.querySelector('[name="horaEntrada"]').value = decimalToTimeString(horario.horaEntrada);
  }
  if (horario.horaSalida) {
    form.querySelector('[name="horaSalida"]').value = decimalToTimeString(horario.horaSalida);
  }

  openModal("horarioModal");
}

function decimalToTimeString(decimalHour) {
  const hours = Math.floor(decimalHour);
  const minutes = Math.round((decimalHour - hours) * 60);
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
}

async function viewHorario(id) {
  // Buscar el horario en los datos cargados
  let horario = window.horariosData?.find(h => h.idHorario == id);
  
  if (!horario) {
    try {
      horario = await apiGet(`/api/horarios/${id}`);
    } catch (e) {
      console.error('Error obteniendo horario:', e);
      showAlert("Horario no encontrado", "danger");
      return;
    }
  }

  currentHorarioId = id;
  // Usar empleadoInfo si está disponible, sino usar empleado
  const empleado = horario.empleadoInfo || horario.empleado;

  const detailsHtml = generateSimpleSchedulePreview(horario, empleado);
  document.getElementById("horarioDetails").innerHTML = detailsHtml;
  openModal("viewHorarioModal");
}

function generateSimpleSchedulePreview(horario, empleado) {
  const empleadoName = getEmpleadoName(horario);
  const empleadoInfo = horario.empleadoInfo || horario.empleado || empleado;
  
  return `
    <div class="schedule-preview-container">
      <div class="hours-summary">
        <h4><i class="fas fa-clock"></i> Detalles del Horario</h4>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 15px;">
          <div>
            <strong>Empleado:</strong> ${empleadoName}<br>
            <strong>Puesto:</strong> ${empleadoInfo ? empleadoInfo.puesto || 'N/A' : 'N/A'}<br>
            <strong>Fecha:</strong> ${new Date(horario.fecha).toLocaleDateString()}
          </div>
          <div>
            <strong>Hora de Entrada:</strong> ${formatHour(horario.horaEntrada)}<br>
            <strong>Hora de Salida:</strong> ${formatHour(horario.horaSalida)}<br>
            <strong>Horas Trabajadas:</strong> ${horario.horasTrabajadas ? `${horario.horasTrabajadas}h` : 'N/A'}
          </div>
        </div>
        ${horario.observaciones ? `
          <div style="margin-top: 15px;">
            <strong>Observaciones:</strong>
            <p style="margin: 5px 0; padding: 10px; background: #f8f9fa; border-radius: 4px;">${horario.observaciones}</p>
          </div>
        ` : ''}
      </div>
    </div>
  `;
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

async function duplicateHorario(id) {
  const horario = window.horariosData?.find(h => h.idHorario == id);
  if (!horario) {
    showAlert("Horario no encontrado", "danger");
    return;
  }

  // Obtener el nombre del empleado usando la función helper
  const empleadoName = getEmpleadoName(horario);

  if (confirm(`¿Duplicar el horario de ${empleadoName}?`)) {
    try {
      // Encontrar una fecha libre para el duplicado
      const fechaDuplicado = await findNextAvailableDate(horario.idEmpleado);
      
      if (!fechaDuplicado) {
        showAlert("No se puede duplicar: el empleado ya tiene horarios en los próximos 30 días", "warning");
        return;
      }

      const newHorario = {
        idEmpleado: horario.idEmpleado,
        fecha: fechaDuplicado,
        horaEntrada: horario.horaEntrada,
        horaSalida: horario.horaSalida,
        observaciones: `Duplicado de horario del ${new Date(horario.fecha).toLocaleDateString()}`
      };

      console.log('DEBUG - Datos del horario original:', horario);
      console.log('DEBUG - Datos del nuevo horario a enviar:', newHorario);
      console.log('DEBUG - Fecha libre encontrada:', fechaDuplicado);

      await apiPost('/api/horarios', newHorario);
      showAlert(`Horario duplicado correctamente para el ${new Date(fechaDuplicado).toLocaleDateString()}`, "success");
      await loadHorarios();
    } catch (e) {
      console.error('Error duplicando horario:', e);
      
      // Mejorar mensaje de error para restricción de BD
      if (e.message && e.message.includes('unique constraint')) {
        showAlert("No se puede duplicar: ya existe un horario para este empleado en esa fecha", "warning");
      } else {
        showAlert(e.data?.mensaje || `Error al duplicar horario: ${e.message}`, 'danger');
      }
    }
  }
}

// Función para encontrar la próxima fecha disponible para un empleado
async function findNextAvailableDate(idEmpleado) {
  try {
    // Obtener todos los horarios del empleado
    const horariosEmpleado = await apiGet(`/api/horarios/empleado/${idEmpleado}`);
    console.log('DEBUG - Horarios existentes del empleado:', horariosEmpleado);
    
    // Crear set de fechas ocupadas para búsqueda rápida
    const fechasOcupadas = new Set(horariosEmpleado.map(h => h.fecha));
    console.log('DEBUG - Fechas ocupadas:', Array.from(fechasOcupadas));
    
    // Empezar desde mañana
    const hoy = new Date();
    let fechaProbar = new Date(hoy);
    fechaProbar.setDate(fechaProbar.getDate() + 1); // Mañana
    
    // Buscar en los próximos 30 días
    for (let i = 0; i < 30; i++) {
      const fechaString = fechaProbar.toISOString().split("T")[0];
      console.log(`DEBUG - Probando fecha: ${fechaString}`);
      
      if (!fechasOcupadas.has(fechaString)) {
        console.log(`DEBUG - Fecha libre encontrada: ${fechaString}`);
        return fechaString;
      }
      
      // Probar el siguiente día
      fechaProbar.setDate(fechaProbar.getDate() + 1);
    }
    
    console.log('DEBUG - No se encontró fecha libre en 30 días');
    return null; // No se encontró fecha libre en 30 días
    
  } catch (e) {
    console.error('Error buscando fecha disponible:', e);
    // Si hay error obteniendo horarios, usar mañana como fallback
    const mañana = new Date();
    mañana.setDate(mañana.getDate() + 1);
    return mañana.toISOString().split("T")[0];
  }
}

async function deleteHorario(id) {
  const horario = window.horariosData?.find(h => h.idHorario == id);
  if (!horario) {
    showAlert("Horario no encontrado", "danger");
    return;
  }

  const empleadoName = getEmpleadoName(horario);

  if (confirm(`¿Estás seguro de eliminar el horario de ${empleadoName}?`)) {
    try {
      await apiDelete(`/api/horarios/${id}`);
      showAlert("Horario eliminado correctamente", "success");
      await loadHorarios();
    } catch (e) {
      console.error('Error eliminando horario:', e);
      showAlert(e.data?.mensaje || `Error al eliminar horario: ${e.message}`, 'danger');
    }
  }
}

async function handleFormSubmit(e) {
  e.preventDefault();

  const formData = new FormData(e.target);

  // Convertir horas de formato HH:MM a decimal
  function timeStringToDecimal(timeString) {
    if (!timeString) return null;
    const [hours, minutes] = timeString.split(':').map(Number);
    return hours + (minutes / 60);
  }

  const horarioData = {
    idEmpleado: parseInt(formData.get("idEmpleado")),
    fecha: formData.get("fechaInicio"),
    horaEntrada: timeStringToDecimal(formData.get("horaEntrada")),
    horaSalida: timeStringToDecimal(formData.get("horaSalida")),
    observaciones: formData.get("observaciones")?.trim() || null
  };

  console.log('DEBUG - Datos del horario a enviar:', horarioData);
  console.log('DEBUG - Tipos de datos en formulario:', {
    idEmpleado: typeof horarioData.idEmpleado,
    fecha: typeof horarioData.fecha,
    horaEntrada: typeof horarioData.horaEntrada,
    horaSalida: typeof horarioData.horaSalida,
    observaciones: typeof horarioData.observaciones
  });

  // Validación básica
  if (!horarioData.idEmpleado || !horarioData.fecha || !horarioData.horaEntrada || !horarioData.horaSalida) {
    showAlert("Complete todos los campos obligatorios", "danger");
    return;
  }

  if (horarioData.horaSalida <= horarioData.horaEntrada) {
    showAlert("La hora de salida debe ser posterior a la hora de entrada", "danger");
    return;
  }

  try {
    if (currentHorarioId) {
      await apiPut(`/api/horarios/${currentHorarioId}`, horarioData);
      showAlert("Horario actualizado correctamente", "success");
    } else {
      await apiPost('/api/horarios', horarioData);
      showAlert("Horario creado correctamente", "success");
    }
    closeModal("horarioModal");
    await loadHorarios();
  } catch (e) {
    console.error('Error guardando horario:', e);
    showAlert(e.data?.mensaje || `Error al guardar horario: ${e.message}`, 'danger');
  }
}

// Funcionalidad de horarios completamente integrada con el backend
// Ya no se necesitan datos de muestra en localStorage

window.openAddModal = openAddModal;
window.editHorario = editHorario;
window.viewHorario = viewHorario;
window.deleteHorario = deleteHorario;
window.editFromView = editFromView;
window.printSchedule = printSchedule;
window.duplicateHorario = duplicateHorario;


