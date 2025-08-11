let currentRolId = null;

document.addEventListener("DOMContentLoaded", async function () {
  await loadRoles();
  initializeRolesPage();
});

function initializeRolesPage() {
  searchTable("searchRoles", "#rolesTable tbody");

  const form = document.getElementById("rolForm");
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

async function loadRoles() {
  try {
    const roles = await apiGet('/api/roles');
    renderRolesTable(roles.map(r => ({
      id: r.idRol || r.id,
      nombre: r.nombre,
      descripcion: r.descripcion || '',
      permisos: r.permisos || [],
      usuariosCount: r.usuariosCount || 0,
    })));
  } catch (e) {
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
    {
      key: "permisos",
      label: "Permisos",
      format: (value) => (value ? `${value.length} permisos` : "0 permisos"),
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
  clearPermissions();
  openModal("rolModal");
}

async function editRol(id) {
  let rol;
  try {
    rol = await apiGet(`/api/roles/${id}`);
  } catch {
    showAlert("Rol no encontrado", "danger");
    return;
  }

  document.getElementById("modalTitle").textContent = "Editar Rol";
  currentRolId = id;

  const form = document.getElementById("rolForm");
  form.querySelector('[name="id"]').value = rol.id;
  form.querySelector('[name="nombre"]').value = rol.nombre;
  form.querySelector('[name="descripcion"]').value = rol.descripcion || "";

  clearPermissions();
  if (rol.permisos && Array.isArray(rol.permisos)) {
    rol.permisos.forEach((permiso) => {
      const checkbox = form.querySelector(
        `[name="permissions"][value="${permiso}"]`
      );
      if (checkbox) {
        checkbox.checked = true;
      }
    });
  }

  openModal("rolModal");
}

function viewRol(id) {
  const rol = getRecord("roles", id);
  if (!rol) {
    showAlert("Rol no encontrado", "danger");
    return;
  }

  currentRolId = id;
  const usuarios = loadData("usuarios");
  const userCount = usuarios.filter((user) => user.idRol === rol.id).length;

  const permissionsHtml =
    rol.permisos && rol.permisos.length > 0
      ? rol.permisos
          .map((permiso) => {
            const permissionLabels = {
              clientes_ver: "Ver clientes",
              clientes_crear: "Crear clientes",
              clientes_editar: "Editar clientes",
              clientes_eliminar: "Eliminar clientes",
              productos_ver: "Ver productos",
              productos_crear: "Crear productos",
              productos_editar: "Editar productos",
              productos_eliminar: "Eliminar productos",
              stock_ver: "Ver stock",
              stock_modificar: "Modificar stock",
              ventas_ver: "Ver facturas",
              ventas_crear: "Crear facturas",
              ventas_anular: "Anular facturas",
              admin_usuarios: "Gestionar usuarios",
              admin_roles: "Gestionar roles",
              admin_reportes: "Ver reportes",
            };
            return `<span class="permission-tag">${
              permissionLabels[permiso] || permiso
            }</span>`;
          })
          .join("")
      : '<span class="text-muted">Sin permisos asignados</span>';

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
                <strong>Usuarios asignados:</strong> <span class="users-count">${userCount}</span>
            </div>
            <div class="detail-row">
                <strong>Permisos:</strong>
                <div class="permissions-display mt-2">
                    ${permissionsHtml}
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
            .permission-tag {
                display: inline-block;
                background: #e3f2fd;
                color: #1976d2;
                padding: 2px 8px;
                border-radius: 12px;
                font-size: 0.75rem;
                margin: 2px;
            }
            .permissions-display {
                max-height: 200px;
                overflow-y: auto;
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

function clearPermissions() {
  const checkboxes = document.querySelectorAll('[name="permissions"]');
  checkboxes.forEach((checkbox) => {
    checkbox.checked = false;
  });
}

async function handleFormSubmit(e) {
  e.preventDefault();

  const formData = new FormData(e.target);
  const rolData = {
    nombre: formData.get("nombre").trim(),
    descripcion: formData.get("descripcion").trim(),
    permisos: formData.getAll("permissions"),
  };

  if (!rolData.nombre) {
    showAlert("El nombre del rol es obligatorio", "danger");
    return;
  }

  try {
    if (currentRolId) {
      await apiPut(`/api/roles/${currentRolId}`, rolData);
    } else {
      await apiPost('/api/roles', rolData);
    }
  } catch (e) {
    showAlert(e.data?.mensaje || `Error al guardar rol: ${e.message}`, 'danger');
    return;
  }
  await loadRoles();
  closeModal("rolModal");
  showAlert("Rol guardado", "success");
}

function initializeSampleRoles() {
  const roles = loadData("roles");
  if (roles.length === 0) {
    const sampleRoles = [
      {
        id: "1",
        nombre: "Administrador",
        descripcion: "Acceso completo al sistema",
        permisos: [
          "clientes_ver",
          "clientes_crear",
          "clientes_editar",
          "clientes_eliminar",
          "productos_ver",
          "productos_crear",
          "productos_editar",
          "productos_eliminar",
          "stock_ver",
          "stock_modificar",
          "ventas_ver",
          "ventas_crear",
          "ventas_anular",
          "admin_usuarios",
          "admin_roles",
          "admin_reportes",
        ],
        fechaCreacion: new Date().toISOString(),
      },
      {
        id: "2",
        nombre: "Vendedor",
        descripcion: "Personal de ventas con acceso limitado",
        permisos: [
          "clientes_ver",
          "clientes_crear",
          "clientes_editar",
          "productos_ver",
          "stock_ver",
          "ventas_ver",
          "ventas_crear",
        ],
        fechaCreacion: new Date().toISOString(),
      },
      {
        id: "3",
        nombre: "Almacenista",
        descripcion: "Gestión de inventario y productos",
        permisos: [
          "productos_ver",
          "productos_crear",
          "productos_editar",
          "stock_ver",
          "stock_modificar",
        ],
        fechaCreacion: new Date().toISOString(),
      },
    ];

    saveData("roles", sampleRoles);
  }
}

document.addEventListener("DOMContentLoaded", function () {
  initializeSampleRoles();
});


