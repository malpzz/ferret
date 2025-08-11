let currentRolId = null;

document.addEventListener("DOMContentLoaded", async function () {
  await loadRoles();
  initializeRolesPage();
});

function initializeRolesPage() {
  searchTable("searchRoles", "#rolesTable tbody");

  const form = document.getElementById("rolForm");
  form.addEventListener("submit", handleFormSubmit);

  // Initialize logout functionality
  initializeLogout();
}

async function loadRoles() {
  try {
    const roles = await apiGet('/api/roles');
    console.log('DEBUG - Roles recibidos del backend:', roles);
    
    // Los datos ya vienen con la estructura correcta del backend
    window.rolesData = roles; // Guardar para uso posterior
    renderRolesTable(roles);
  } catch (e) {
    console.error('Error cargando roles:', e);
    showAlert(`Error cargando roles: ${e.message}`, 'danger');
  }
}

function renderRolesTable(roles) {
  const columns = [
    { key: "nombre", label: "Nombre del Rol" },
    { key: "descripcion", label: "Descripción" },
    {
      key: "usuariosCount",
      label: "Usuarios",
      format: (value) => `<span class="users-count">${value}</span>`,
    },
  ];

  const actions = [
    {
      label: '<i class="fas fa-eye"></i>',
      class: "btn-primary",
      onclick: "viewRol",
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning",
      onclick: "editRol",
    },
    {
      label: '<i class="fas fa-trash"></i>',
      class: "btn-danger",
      onclick: "deleteRol",
    },
  ];

  renderTable("rolesTable", roles, columns, actions);
}

function openAddModal() {
  document.getElementById("modalTitle").textContent = "Nuevo Rol";
  clearForm("rolForm");
  currentRolId = null;
  openModal("rolModal");
}

async function editRol(id) {
  let rol;
  try {
    rol = await apiGet(`/api/roles/${id}`);
    console.log('DEBUG - Rol para editar:', rol);
  } catch (e) {
    console.error('Error obteniendo rol:', e);
    showAlert("Rol no encontrado", "danger");
    return;
  }

  document.getElementById("modalTitle").textContent = "Editar Rol";
  currentRolId = id;

  const form = document.getElementById("rolForm");
  form.querySelector('[name="id"]').value = rol.idRol;
  form.querySelector('[name="nombre"]').value = rol.nombre;
  form.querySelector('[name="descripcion"]').value = rol.descripcion || "";

  openModal("rolModal");
}

async function viewRol(id) {
  // Buscar el rol en los datos cargados
  const rol = window.rolesData?.find(r => r.idRol == id);
  if (!rol) {
    showAlert("Rol no encontrado", "danger");
    return;
  }

  currentRolId = id;
  
  // Cargar usuarios específicos de este rol
  let usuariosDelRol = [];
  try {
    usuariosDelRol = await apiGet(`/api/roles/${id}/usuarios`);
    console.log('DEBUG - Usuarios del rol:', usuariosDelRol);
  } catch (e) {
    console.error('Error cargando usuarios del rol:', e);
  }

  // Generar HTML de usuarios
  const usuariosHtml = usuariosDelRol.length > 0
    ? usuariosDelRol
        .map(usuario => 
          `<div class="user-item">
             <i class="fas fa-user"></i>
             <span class="user-name">${usuario.nombreCompleto || usuario.nombreUsuario}</span>
             <span class="user-email">(${usuario.email})</span>
           </div>`
        )
        .join("")
    : '<span class="text-muted">No hay usuarios asignados a este rol</span>';

  const detailsHtml = `
        <div class="rol-details">
            <div class="detail-row">
                <strong>Nombre:</strong> ${rol.nombre}
            </div>
            <div class="detail-row">
                <strong>Descripción:</strong> ${
                  rol.descripcion || "Sin descripción"
                }
            </div>
            <div class="detail-row">
                <strong>Usuarios asignados:</strong> <span class="users-count">${rol.usuariosCount}</span>
            </div>
            <div class="detail-row">
                <strong>Lista de usuarios:</strong>
                <div class="users-display mt-2">
                    ${usuariosHtml}
                </div>
            </div>
        </div>
        <style>
            .rol-details .detail-row {
                padding: 10px 0;
                border-bottom: 1px solid #e1e8ed;
            }
            .rol-details .detail-row:last-child {
                border-bottom: none;
            }
            .users-display {
                max-height: 200px;
                overflow-y: auto;
            }
            .user-item {
                display: flex;
                align-items: center;
                padding: 5px 0;
                border-bottom: 1px solid #f0f0f0;
            }
            .user-item:last-child {
                border-bottom: none;
            }
            .user-item i {
                margin-right: 8px;
                color: #6c757d;
            }
            .user-name {
                font-weight: 500;
                margin-right: 8px;
            }
            .user-email {
                color: #6c757d;
                font-size: 0.85rem;
            }
        </style>
    `;

  document.getElementById("rolDetails").innerHTML = detailsHtml;
  openModal("viewRolModal");
}

function editFromView() {
  closeModal("viewRolModal");
  editRol(currentRolId);
}

async function deleteRol(id) {
  if (!confirm("¿Estás seguro de eliminar este rol?")) return;
  try {
    await apiDelete(`/api/roles/${id}`);
    await loadRoles();
    showAlert("Rol eliminado correctamente", "success");
  } catch (e) {
    showAlert(e.data?.mensaje || `Error al eliminar rol: ${e.message}`, 'danger');
  }
}

async function handleFormSubmit(e) {
  e.preventDefault();

  const formData = new FormData(e.target);
  const rolData = {
    nombre: formData.get("nombre").trim(),
    descripcion: formData.get("descripcion").trim(),
  };

  console.log('DEBUG - Datos del rol a enviar:', rolData);

  if (!rolData.nombre) {
    showAlert("El nombre del rol es obligatorio", "danger");
    return;
  }

  try {
    if (currentRolId) {
      const resultado = await apiPut(`/api/roles/${currentRolId}`, rolData);
      console.log('DEBUG - Rol actualizado:', resultado);
    } else {
      const resultado = await apiPost('/api/roles', rolData);
      console.log('DEBUG - Rol creado:', resultado);
    }
  } catch (e) {
    console.error('Error guardando rol:', e);
    showAlert(e.data?.mensaje || `Error al guardar rol: ${e.message}`, 'danger');
    return;
  }
  await loadRoles();
  closeModal("rolModal");
  showAlert("Rol guardado correctamente", "success");
}

// Funcionalidad de roles completamente integrada con el backend


