// Usuarios CRUD JavaScript
let currentUsuarioId = null;

document.addEventListener("DOMContentLoaded", async function () {
  await loadUsuarios();
  await loadRoles();
  initializeUsuariosPage();
});

function initializeUsuariosPage() {
  // Initialize search functionality
  searchTable("searchUsuarios", "#usuariosTable tbody");

  // Form submission
  const form = document.getElementById("usuarioForm");
  form.addEventListener("submit", handleFormSubmit);

  // Initialize logout functionality
  initializeLogout();

  // Password confirmation validation
  const confirmPassword = document.getElementById("confirmarContraseña");
  if (confirmPassword) {
    confirmPassword.addEventListener("input", validatePasswordMatch);
  }
}

async function loadUsuarios() {
  try {
    const usuarios = await apiGet('/api/usuarios');
    console.log('DEBUG - Usuarios recibidos:', usuarios); // Debug
    
    const usuariosMapped = usuarios.map(u => {
      console.log('DEBUG - Usuario:', u); // Debug individual
      return {
        id: u.idUsuario || u.id,
        nombreUsuario: u.nombreUsuario,
        nombreRol: (u.rolInfo && u.rolInfo.nombre) || u.nombreRol || '-',
        idRol: (u.rolInfo && u.rolInfo.idRol) || u.idRol || null,
        nombre: u.nombre,
        apellidos: u.apellidos,
        email: u.email,
        telefono: u.telefono,
        activo: u.activo
      };
    });
    
    // Guardar en memoria para editUsuario
    window.usuariosData = usuariosMapped;
    renderUsuariosTable(usuariosMapped);
  } catch (e) {
    showAlert(`Error cargando usuarios: ${e.message}`, 'danger');
  }
}

async function loadRoles() {
  const select = document.getElementById("idRol");
  select.innerHTML = '<option value="">Seleccione un rol</option>';
  try {
    const roles = await apiGet('/api/roles');
    roles.forEach((rol) => {
      const option = document.createElement("option");
      option.value = rol.idRol || rol.id;
      option.textContent = rol.nombre;
      select.appendChild(option);
    });
  } catch {}
}

function renderUsuariosTable(usuarios) {
  const columns = [
    { key: "nombreUsuario", label: "Nombre de Usuario" },
    { key: "nombreRol", label: "Rol" },
  ];

  const actions = [
    {
      label: '<i class="fas fa-eye"></i>',
      class: "btn-primary",
      onclick: "viewUsuario",
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning",
      onclick: "editUsuario",
    },
    {
      label: '<i class="fas fa-trash"></i>',
      class: "btn-danger",
      onclick: "deleteUsuario",
    },
  ];

  renderTable("usuariosTable", usuarios, columns, actions);
}

function openAddModal() {
  document.getElementById("modalTitle").textContent = "Nuevo Usuario";
  clearForm("usuarioForm");
  currentUsuarioId = null;

  // Reset password fields to required for new user
  document.getElementById("contraseña").required = true;
  document.getElementById("confirmarContraseña").required = true;

  openModal("usuarioModal");
}

function editUsuario(id) {
  // Buscar usuario en los datos cargados
  const usuario = window.usuariosData ? window.usuariosData.find(u => u.id == id) : null;
  if (!usuario) {
    showAlert("Usuario no encontrado", "danger");
    console.error("Usuario no encontrado, ID:", id);
    console.error("Datos disponibles:", window.usuariosData);
    return;
  }

  console.log("DEBUG - Editando usuario:", usuario);

  document.getElementById("modalTitle").textContent = "Editar Usuario";
  currentUsuarioId = id;

  // Populate form
  const form = document.getElementById("usuarioForm");
  form.querySelector('[name="id"]').value = usuario.id;
  form.querySelector('[name="nombreUsuario"]').value = usuario.nombreUsuario || '';
  form.querySelector('[name="idRol"]').value = usuario.idRol || '';

  // Clear password fields and make them optional for editing
  document.getElementById("contraseña").value = "";
  document.getElementById("confirmarContraseña").value = "";
  document.getElementById("contraseña").required = false;
  document.getElementById("confirmarContraseña").required = false;

  openModal("usuarioModal");
}

function viewUsuario(id) {
  // Buscar usuario en los datos cargados
  const usuario = window.usuariosData ? window.usuariosData.find(u => u.id == id) : null;
  if (!usuario) {
    showAlert("Usuario no encontrado", "danger");
    return;
  }

  currentUsuarioId = id;

  const detailsHtml = `
        <div class="usuario-details">
            <div class="detail-row">
                <strong>Nombre de Usuario:</strong> ${usuario.nombreUsuario || 'N/A'}
            </div>
            <div class="detail-row">
                <strong>Nombre Completo:</strong> ${(usuario.nombre || '') + ' ' + (usuario.apellidos || '').trim() || 'N/A'}
            </div>
            <div class="detail-row">
                <strong>Email:</strong> ${usuario.email || 'N/A'}
            </div>
            <div class="detail-row">
                <strong>Teléfono:</strong> ${usuario.telefono || 'N/A'}
            </div>
            <div class="detail-row">
                <strong>Rol:</strong> ${usuario.nombreRol || "Sin rol asignado"}
            </div>
            <div class="detail-row">
                <strong>Estado:</strong> ${usuario.activo ? 'Activo' : 'Inactivo'}
            </div>
        </div>
        <style>
            .usuario-details .detail-row {
                padding: 10px 0;
                border-bottom: 1px solid #e1e8ed;
            }
            .usuario-details .detail-row:last-child {
                border-bottom: none;
            }
        </style>
    `;

  document.getElementById("usuarioDetails").innerHTML = detailsHtml;
  openModal("viewUsuarioModal");
}

function editFromView() {
  closeModal("viewUsuarioModal");
  editUsuario(currentUsuarioId);
}

function resetPassword() {
  if (!currentUsuarioId) return;

  const newPassword = prompt(
    "Ingrese la nueva contraseña (mínimo 6 caracteres):"
  );
  if (!newPassword) return;

  if (newPassword.length < 6) {
    showAlert("La contraseña debe tener al menos 6 caracteres", "danger");
    return;
  }

  const usuario = getRecord("usuarios", currentUsuarioId);
  if (usuario) {
    usuario.contraseña = newPassword; // In real app, this would be hashed
    usuario.fechaModificacion = new Date().toISOString();
    updateRecord("usuarios", usuario);
    showAlert("Contraseña actualizada correctamente", "success");

    // Add activity
    if (typeof addActivity === "function") {
      addActivity(
        "usuario",
        "Contraseña resetada",
        `Se resetó la contraseña del usuario ${usuario.nombreUsuario}`
      );
    }
  }
}

async function deleteUsuario(id) {
  if (!confirmDelete("¿Estás seguro de eliminar este usuario?")) return;
  try {
    await apiDelete(`/api/usuarios/${id}`);
    await loadUsuarios();
    showAlert("Usuario eliminado correctamente", "success");
  } catch (e) {
    showAlert(e.data?.mensaje || `Error al eliminar: ${e.message}`, 'danger');
  }
}

function validatePasswordMatch() {
  const password = document.getElementById("contraseña").value;
  const confirmPassword = document.getElementById("confirmarContraseña").value;

  if (confirmPassword && password !== confirmPassword) {
    document.getElementById("confirmarContraseña").setCustomValidity("Las contraseñas no coinciden");
  } else {
    document.getElementById("confirmarContraseña").setCustomValidity("");
  }
}

async function handleFormSubmit(e) {
  e.preventDefault();

  // Validation rules
  const validationRules = [
    {
      field: "nombreUsuario",
      label: "Nombre de Usuario",
      required: true,
      minLength: 3,
    },
    { field: "idRol", label: "Rol", required: true },
  ];

  // Add password validation for new users
  if (!currentUsuarioId) {
    validationRules.push(
      {
        field: "contraseña",
        label: "Contraseña",
        required: true,
        minLength: 6,
      },
      {
        field: "confirmarContraseña",
        label: "Confirmar Contraseña",
        required: true,
      }
    );
  }

  if (!validateForm("usuarioForm", validationRules)) {
    return;
  }

  const formData = new FormData(e.target);
  const usuarioData = {
    nombreUsuario: formData.get("nombreUsuario").trim(),
    idRol: parseInt(formData.get("idRol")),
  };

  // Handle password
  const password = formData.get("contraseña");
  const confirmPassword = formData.get("confirmarContraseña");

  if (password) {
    if (password !== confirmPassword) {
      showAlert("Las contraseñas no coinciden", "danger");
      return;
    }
    if (password.length < 6) {
      showAlert("La contraseña debe tener al menos 6 caracteres", "danger");
      return;
    }
    usuarioData.contraseña = password;
  }

  try {
    if (currentUsuarioId) {
      await apiPut(`/api/usuarios/${currentUsuarioId}`, usuarioData);
    } else {
      await apiPost('/api/usuarios', usuarioData);
    }
  } catch (e) {
    showAlert(e.data?.mensaje || `Error al guardar: ${e.message}`, 'danger');
    return;
  }
  await loadUsuarios();
  closeModal("usuarioModal");
  clearForm("usuarioForm");
  showAlert("Usuario guardado", "success");
  currentUsuarioId = null;
}

window.openAddModal = openAddModal;
window.editUsuario = editUsuario;
window.viewUsuario = viewUsuario;
window.deleteUsuario = deleteUsuario;
window.editFromView = editFromView;
window.resetPassword = resetPassword;


