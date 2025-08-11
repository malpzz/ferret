document.addEventListener("DOMContentLoaded", function () {
  initializeDashboard();
  loadStats();
  loadRecentActivity();
});
function initializeDashboard() {
  const sidebarToggle = document.querySelector(".sidebar-toggle");
  const sidebar = document.querySelector(".sidebar");

  if (sidebarToggle) {
    sidebarToggle.addEventListener("click", () => {
      sidebar.classList.toggle("active");
    });
  }

  const logoutBtn = document.querySelector(".btn-logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      if (confirm("¿Estás seguro de que deseas cerrar sesión?")) {
        // Crear formulario de logout para Spring Security
        const logoutForm = document.createElement('form');
        logoutForm.method = 'POST';
        logoutForm.action = buildApiUrl('/logout');
        
        // Añadir token CSRF
        const csrfHeaders = getCsrfHeader();
        if (csrfHeaders._csrf) {
          const csrfInput = document.createElement('input');
          csrfInput.type = 'hidden';
          csrfInput.name = csrfHeaders._csrf_parameterName || '_csrf';
          csrfInput.value = csrfHeaders._csrf;
          logoutForm.appendChild(csrfInput);
        }
        
        document.body.appendChild(logoutForm);
        logoutForm.submit();
      }
    });
  }
}

function loadStats() {
  const stats = getStatsFromStorage();

  document.getElementById("totalClientes").textContent = stats.clientes;
  document.getElementById("totalProductos").textContent = stats.productos;
  document.getElementById("totalFacturas").textContent = stats.facturas;
  document.getElementById("ventasTotal").textContent = formatCurrency(
    stats.ventas
  );

  animateCounters();
}

function getStatsFromStorage() {
  const clientes = JSON.parse(localStorage.getItem("clientes") || "[]");
  const productos = JSON.parse(localStorage.getItem("productos") || "[]");
  const facturas = JSON.parse(localStorage.getItem("facturas") || "[]");

  const currentMonth = new Date().getMonth();
  const currentYear = new Date().getFullYear();
  const monthlyFacturas = facturas.filter((f) => {
    const facturaDate = new Date(f.fecha);
    return (
      facturaDate.getMonth() === currentMonth &&
      facturaDate.getFullYear() === currentYear
    );
  });

  const totalVentas = monthlyFacturas.reduce((sum, f) => sum + f.total, 0);

  return {
    clientes: clientes.length,
    productos: productos.length,
    facturas: monthlyFacturas.length,
    ventas: totalVentas,
  };
}

function formatCurrency(amount) {
  return new Intl.NumberFormat("es-AR", {
    style: "currency",
    currency: "ARS",
  }).format(amount);
}

function animateCounters() {
  const counters = document.querySelectorAll(".stat-content h3");

  counters.forEach((counter) => {
    const target = parseInt(counter.textContent.replace(/[^0-9]/g, ""));
    const increment = target / 50;
    let current = 0;

    const timer = setInterval(() => {
      current += increment;
      if (current >= target) {
        current = target;
        clearInterval(timer);
      }

      if (counter.id === "ventasTotal") {
        counter.textContent = formatCurrency(current);
      } else {
        counter.textContent = Math.floor(current);
      }
    }, 20);
  });
}

function loadRecentActivity() {
  const activityList = document.getElementById("activityList");
  const activities = getRecentActivities();

  if (activities.length === 0) {
    activityList.innerHTML = `
            <div class="activity-item">
                <div class="activity-content">
                    <h4>No hay actividad reciente</h4>
                    <p>Comienza usando el sistema para ver la actividad aquí</p>
                </div>
            </div>
        `;
    return;
  }

  activityList.innerHTML = activities
    .map(
      (activity) => `
        <div class="activity-item">
            <div class="activity-icon" style="background: ${activity.color}">
                <i class="${activity.icon}"></i>
            </div>
            <div class="activity-content">
                <h4>${activity.title}</h4>
                <p>${activity.description}</p>
            </div>
            <div class="activity-time">
                ${formatTimeAgo(activity.timestamp)}
            </div>
        </div>
    `
    )
    .join("");
}

function getRecentActivities() {
  const activities = JSON.parse(
    localStorage.getItem("recentActivities") || "[]"
  );
  return activities.slice(0, 5);
}

function addActivity(type, title, description) {
  const activities = JSON.parse(
    localStorage.getItem("recentActivities") || "[]"
  );

  const iconMap = {
    cliente: { icon: "fas fa-user-plus", color: "#3498db" },
    producto: { icon: "fas fa-box", color: "#2ecc71" },
    factura: { icon: "fas fa-file-invoice", color: "#f39c12" },
    pedido: { icon: "fas fa-shopping-cart", color: "#9b59b6" },
    stock: { icon: "fas fa-warehouse", color: "#e74c3c" },
  };

  const activity = {
    id: Date.now(),
    type,
    title,
    description,
    timestamp: new Date().toISOString(),
    ...iconMap[type],
  };

  activities.unshift(activity);

  if (activities.length > 50) {
    activities.splice(50);
  }

  localStorage.setItem("recentActivities", JSON.stringify(activities));
}

function formatTimeAgo(timestamp) {
  const now = new Date();
  const past = new Date(timestamp);
  const diffInMinutes = Math.floor((now - past) / 60000);

  if (diffInMinutes < 1) return "Ahora mismo";
  if (diffInMinutes < 60) return `Hace ${diffInMinutes} min`;

  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) return `Hace ${diffInHours}h`;

  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 7) return `Hace ${diffInDays}d`;

  return past.toLocaleDateString("es-AR");
}

function initializeSampleData() {
  const existingEmployees = JSON.parse(
    localStorage.getItem("empleados") || "[]"
  );
  const needsUpdate =
    existingEmployees.length > 0 && existingEmployees[0].nombre !== undefined;

  if (!localStorage.getItem("dataInitialized") || needsUpdate) {
    const sampleClientes = [
      {
        id: 1,
        nombreCliente: "Juan",
        apellidos: "Pérez",
        direccion: "Av. Libertador 1234",
        telefono: "11-1234-5678",
        email: "juan.perez@email.com",
      },
      {
        id: 2,
        nombreCliente: "María",
        apellidos: "González",
        direccion: "Belgrano 567",
        telefono: "11-8765-4321",
        email: "maria.gonzalez@email.com",
      },
    ];

    const sampleProductos = [
      {
        id: 1,
        nombreProducto: "Martillo 500g",
        descripcion: "Martillo de acero forjado 500 gramos",
        precio: 2500,
        IdProveedor: 1,
      },
      {
        id: 2,
        nombreProducto: "Destornillador Phillips",
        descripcion: "Destornillador Phillips #2 mango ergonómico",
        precio: 800,
        IdProveedor: 1,
      },
      {
        id: 3,
        nombreProducto: "Taladro Eléctrico",
        descripcion: "Taladro eléctrico 600W con percutor",
        precio: 15000,
        IdProveedor: 1,
      },
    ];

    const sampleStock = [
      { id: 1, idProducto: 1, cantidad: 25 },
      { id: 2, idProducto: 2, cantidad: 50 },
      { id: 3, idProducto: 3, cantidad: 8 },
    ];

    const sampleProveedores = [
      {
        id: 1,
        nombreProveedor: "Herramientas SA",
        direccion: "Industrial 123",
        telefono: "11-5555-0001",
        email: "ventas@herramientas.com",
      },
    ];

    const sampleEmpleados = [
      {
        id: 1,
        nombreEmpleado: "Carlos",
        apellidos: "Rodríguez",
        direccion: "San Martín 456",
        telefono: "11-9999-0001",
        puesto: "Vendedor",
      },
    ];

    const sampleUsuarios = [
      {
        id: 1,
        nombreUsuario: "admin",
        contraseña: "admin123",
        idRol: 1,
      },
    ];

    const sampleRoles = [
      { id: 1, nombre: "Administrador" },
      { id: 2, nombre: "Vendedor" },
      { id: 3, nombre: "Gerente" },
    ];

    localStorage.setItem("clientes", JSON.stringify(sampleClientes));
    localStorage.setItem("productos", JSON.stringify(sampleProductos));
    localStorage.setItem("stock", JSON.stringify(sampleStock));
    localStorage.setItem("proveedores", JSON.stringify(sampleProveedores));
    localStorage.setItem("empleados", JSON.stringify(sampleEmpleados));
    localStorage.setItem("usuarios", JSON.stringify(sampleUsuarios));
    localStorage.setItem("roles", JSON.stringify(sampleRoles));
    localStorage.setItem("facturas", JSON.stringify([]));
    localStorage.setItem("pedidos", JSON.stringify([]));
    localStorage.setItem("horarios", JSON.stringify([]));
    localStorage.setItem("dataInitialized", "v2.0");

    addActivity(
      "producto",
      "Productos inicializados",
      "Se cargaron productos de ejemplo en el sistema"
    );
    addActivity(
      "cliente",
      "Clientes inicializados",
      "Se cargaron clientes de ejemplo en el sistema"
    );
  }
}

// Comentado para usar solo datos de API - limpiar localStorage existente
// initializeSampleData();

// Limpiar datos de ejemplo si existen para usar solo API
if (localStorage.getItem("dataInitialized")) {
  console.log('[Dashboard] Limpiando datos de ejemplo para usar API');
  localStorage.removeItem("productos");
  localStorage.removeItem("clientes");
  localStorage.removeItem("proveedores");
  localStorage.removeItem("empleados");
  localStorage.removeItem("dataInitialized");
}

window.addActivity = addActivity;
window.formatCurrency = formatCurrency;


