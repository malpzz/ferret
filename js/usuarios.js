// Usuarios CRUD JavaScript
let currentUsuarioId = null;

document.addEventListener("DOMContentLoaded", function () {
  loadUsuarios();
  loadRoles();
  initializeUsuariosPage();
});

function initializeUsuariosPage() {
  // Initialize search functionality
  searchTable("searchUsuarios", "#usuariosTable tbody");

  // Form submission
  const form = document.getElementById("usuarioForm");
  form.addEventListener("submit", handleFormSubmit);

  // Initialize logout functionality
  const logoutBtn = document.querySelector(".btn-logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      if (confirm("¿Estás seguro de que deseas cerrar sesión?")) {
        alert("Sesión cerrada correctamente");
      }
    });
  }

  // Password confirmation validation
  const confirmPassword = document.getElementById("confirmarContraseña");
  confirmPassword.addEventListener("input", validatePasswordMatch);
}

function loadUsuarios() {
  const usuarios = loadData("usuarios");
  const roles = loadData("roles");

  // Enrich usuarios with role information
  const usuariosWithRoles = usuarios.map((usuario) => {
    const rol = roles.find((r) => r.id === usuario.idRol);
    return {
      ...usuario,
      nombreRol: rol ? rol.nombre : "Sin rol",
    };
  });

  renderUsuariosTable(usuariosWithRoles);
}

function loadRoles() {
  const roles = loadData("roles");
  const select = document.getElementById("idRol");

  select.innerHTML = '<option value="">Seleccione un rol</option>';
  roles.forEach((rol) => {
    const option = document.createElement("option");
    option.value = rol.id;
    option.textContent = rol.nombre;
    select.appendChild(option);
  });
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
  const usuario = getRecord("usuarios", id);
  if (!usuario) {
    showAlert("Usuario no encontrado", "danger");
    return;
  }

  document.getElementById("modalTitle").textContent = "Editar Usuario";
  currentUsuarioId = id;

  // Populate form
  const form = document.getElementById("usuarioForm");
  form.querySelector('[name="id"]').value = usuario.id;
  form.querySelector('[name="nombreUsuario"]').value = usuario.nombreUsuario;
  form.querySelector('[name="idRol"]').value = usuario.idRol;

  // Clear password fields and make them optional for editing
  form.querySelector('[name="contraseña"]').value = "";
  form.querySelector('[name="confirmarContraseña"]').value = "";
  document.getElementById("contraseña").required = false;
  document.getElementById("confirmarContraseña").required = false;

  openModal("usuarioModal");
}

function viewUsuario(id) {
  const usuario = getRecord("usuarios", id);
  if (!usuario) {
    showAlert("Usuario no encontrado", "danger");
    return;
  }

  currentUsuarioId = id;

  const roles = loadData("roles");
  const rol = roles.find((r) => r.id === usuario.idRol);

  const detailsHtml = `
        <div class="usuario-details">
            <div class="detail-row">
                <strong>Nombre de Usuario:</strong> ${usuario.nombreUsuario}
            </div>
            <div class="detail-row">
                <strong>Rol:</strong> ${rol ? rol.nombre : "Sin rol asignado"}
            </div>
            <div class="detail-row">
                <strong>Última modificación:</strong> ${
                  usuario.fechaModificacion || "No disponible"
                }
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

function deleteUsuario(id) {
  const usuario = getRecord("usuarios", id);
  if (!usuario) {
    showAlert("Usuario no encontrado", "danger");
    return;
  }

  // Prevent deleting the admin user
  if (usuario.nombreUsuario === "admin") {
    showAlert("No se puede eliminar el usuario administrador", "danger");
    return;
  }

  if (
    confirmDelete(
      `¿Estás seguro de que deseas eliminar al usuario ${usuario.nombreUsuario}?`
    )
  ) {
    deleteRecord("usuarios", id);
    loadUsuarios();
    showAlert("Usuario eliminado correctamente", "success");

    // Add activity
    if (typeof addActivity === "function") {
      addActivity(
        "usuario",
        "Usuario eliminado",
        `Se eliminó el usuario ${usuario.nombreUsuario}`
      );
    }
  }
}

function validatePasswordMatch() {
  const password = document.getElementById("contraseña").value;
  const confirmPassword = document.getElementById("confirmarContraseña").value;

  if (confirmPassword && password !== confirmPassword) {
    document
      .getElementById("confirmarContraseña")
      .setCustomValidity("Las contraseñas no coinciden");
  } else {
    document.getElementById("confirmarContraseña").setCustomValidity("");
  }
}

function handleFormSubmit(e) {
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

  const usuarios = loadData("usuarios");
  const userExists = usuarios.some(
    (u) =>
      u.nombreUsuario.toLowerCase() ===
        usuarioData.nombreUsuario.toLowerCase() &&
      (!currentUsuarioId || u.id !== currentUsuarioId)
  );

  if (userExists) {
    showAlert("Ya existe un usuario con este nombre", "danger");
    return;
  }

  let result;
  let actionType;

  if (currentUsuarioId) {
    usuarioData.id = currentUsuarioId;
    usuarioData.fechaModificacion = new Date().toISOString();

    if (!password) {
      const existingUser = getRecord("usuarios", currentUsuarioId);
      usuarioData.contraseña = existingUser.contraseña;
    }

    result = updateRecord("usuarios", usuarioData);
    actionType = "actualizado";
  } else {
    usuarioData.id = generateId(usuarios);
    usuarioData.fechaCreacion = new Date().toISOString();
    result = addRecord("usuarios", usuarioData);
    actionType = "agregado";
  }

  if (result) {
    loadUsuarios();
    closeModal("usuarioModal");
    clearForm("usuarioForm");

    showAlert(`Usuario ${actionType} correctamente`, "success");

    if (typeof addActivity === "function") {
      const activityTitle = currentUsuarioId
        ? "Usuario actualizado"
        : "Nuevo usuario agregado";
      const activityDesc = `${usuarioData.nombreUsuario} fue ${actionType}`;
      addActivity("usuario", activityTitle, activityDesc);
    }

    currentUsuarioId = null;
  } else {
    showAlert("Error al guardar el usuario", "danger");
  }
}

window.openAddModal = openAddModal;
window.editUsuario = editUsuario;
window.viewUsuario = viewUsuario;
window.deleteUsuario = deleteUsuario;
window.editFromView = editFromView;
window.resetPassword = resetPassword;
