let currentProductoId = null;

document.addEventListener("DOMContentLoaded", function () {
  loadProductos();
  initializeProductosPage();
});

function initializeProductosPage() {
  searchTable("searchProductos", "#productosTable tbody");
  loadProveedores();

  const form = document.getElementById("productoForm");
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

function loadProductos() {
  const productos = loadData("productos");
  const stock = loadData("stock");

  const productosWithStock = productos.map((producto) => {
    const stockInfo = stock.find((s) => s.idProducto === producto.id);
    return {
      ...producto,
      cantidad: stockInfo ? stockInfo.cantidad : 0,
    };
  });

  renderProductosTable(productosWithStock);
}

function loadProveedores() {
  const proveedores = loadData("proveedores");
  const select = document.getElementById("IdProveedor");

  select.innerHTML = '<option value="">Seleccione un proveedor</option>';
  proveedores.forEach((proveedor) => {
    const option = document.createElement("option");
    option.value = proveedor.id;
    option.textContent = proveedor.nombreProveedor;
    select.appendChild(option);
  });
}

function renderProductosTable(productos) {
  const columns = [
    { key: "nombreProducto", label: "Nombre" },
    {
      key: "descripcion",
      label: "Descripción",
      format: (value) =>
        value.length > 50 ? value.substring(0, 50) + "..." : value,
    },
    {
      key: "precio",
      label: "Precio",
      format: (value) => formatCurrency(value),
    },
    {
      key: "cantidad",
      label: "Stock",
      format: (value) => {
        const className =
          value === 0
            ? "text-danger"
            : value < 10
            ? "text-warning"
            : "text-success";
        return `<span class="${className}">${value} unidades</span>`;
      },
    },
  ];

  const actions = [
    {
      label: '<i class="fas fa-eye"></i>',
      class: "btn-primary",
      onclick: "viewProducto",
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning",
      onclick: "editProducto",
    },
    {
      label: '<i class="fas fa-trash"></i>',
      class: "btn-danger",
      onclick: "deleteProducto",
    },
  ];

  renderTable("productosTable", productos, columns, actions);
}

function openAddModal() {
  document.getElementById("modalTitle").textContent = "Nuevo Producto";
  clearForm("productoForm");
  currentProductoId = null;
  openModal("productoModal");
}

function editProducto(id) {
  const producto = getRecord("productos", id);
  if (!producto) {
    showAlert("Producto no encontrado", "danger");
    return;
  }

  document.getElementById("modalTitle").textContent = "Editar Producto";
  currentProductoId = id;

  const form = document.getElementById("productoForm");
  form.querySelector('[name="id"]').value = producto.id;
  form.querySelector('[name="nombreProducto"]').value = producto.nombreProducto;
  form.querySelector('[name="descripcion"]').value = producto.descripcion;
  form.querySelector('[name="precio"]').value = producto.precio;
  form.querySelector('[name="IdProveedor"]').value = producto.IdProveedor;

  openModal("productoModal");
}

function viewProducto(id) {
  const producto = getRecord("productos", id);
  if (!producto) {
    showAlert("Producto no encontrado", "danger");
    return;
  }

  currentProductoId = id;

  const stock = loadData("stock");
  const stockInfo = stock.find((s) => s.idProducto === id);
  const cantidadStock = stockInfo ? stockInfo.cantidad : 0;

  const detailsHtml = `
        <div class="producto-details">
            <div class="detail-row">
                <strong>Nombre:</strong> ${producto.nombreProducto}
            </div>
            <div class="detail-row">
                <strong>Descripción:</strong> ${producto.descripcion}
            </div>
            <div class="detail-row">
                <strong>Precio:</strong> ${formatCurrency(producto.precio)}
            </div>
            <div class="detail-row">
                <strong>Stock Actual:</strong>
                <span class="${
                  cantidadStock === 0
                    ? "text-danger"
                    : cantidadStock < 10
                    ? "text-warning"
                    : "text-success"
                }">
                    ${cantidadStock} unidades
                </span>
            </div>
        </div>
        <style>
            .producto-details .detail-row {
                padding: 10px 0;
                border-bottom: 1px solid #e1e8ed;
            }
            .producto-details .detail-row:last-child {
                border-bottom: none;
            }
            .text-danger { color: #e74c3c; }
            .text-warning { color: #f39c12; }
            .text-success { color: #2ecc71; }
        </style>
    `;

  document.getElementById("productoDetails").innerHTML = detailsHtml;
  openModal("viewProductoModal");
}

function editFromView() {
  closeModal("viewProductoModal");
  editProducto(currentProductoId);
}

function manageStock() {
  if (currentProductoId) {
    closeModal("viewProductoModal");
    window.location.href = `stock.html?producto=${currentProductoId}`;
  }
}

function deleteProducto(id) {
  const producto = getRecord("productos", id);
  if (!producto) {
    showAlert("Producto no encontrado", "danger");
    return;
  }

  if (
    confirmDelete(
      `¿Estás seguro de que deseas eliminar el producto "${producto.nombreProducto}"?`
    )
  ) {
    const facturas = loadData("facturas");
    const detalleFacturas = loadData("detalleFacturas") || [];
    const pedidos = loadData("pedidos");
    const detallePedidos = loadData("detallePedidos") || [];

    const isUsedInFacturas = detalleFacturas.some((df) => df.idProducto === id);
    const isUsedInPedidos = detallePedidos.some((dp) => dp.idProducto === id);

    if (isUsedInFacturas || isUsedInPedidos) {
      showAlert(
        "No se puede eliminar el producto porque está asociado a facturas o pedidos",
        "danger"
      );
      return;
    }

    deleteRecord("productos", id);

    const stock = loadData("stock");
    const updatedStock = stock.filter((s) => s.idProducto !== id);
    saveData("stock", updatedStock);

    loadProductos();
    showAlert("Producto eliminado correctamente", "success");

    if (typeof addActivity === "function") {
      addActivity(
        "producto",
        "Producto eliminado",
        `Se eliminó el producto "${producto.nombreProducto}"`
      );
    }
  }
}

function handleFormSubmit(e) {
  e.preventDefault();

  const validationRules = [
    { field: "nombreProducto", label: "Nombre", required: true, minLength: 3 },
    {
      field: "descripcion",
      label: "Descripción",
      required: true,
      minLength: 10,
    },
    { field: "precio", label: "Precio", required: true, type: "number" },
    { field: "IdProveedor", label: "Proveedor", required: true },
  ];

  if (!validateForm("productoForm", validationRules)) {
    return;
  }

  const formData = new FormData(e.target);
  const productoData = {
    nombreProducto: formData.get("nombreProducto").trim(),
    descripcion: formData.get("descripcion").trim(),
    precio: parseFloat(formData.get("precio")),
    IdProveedor: parseInt(formData.get("IdProveedor")),
  };

  const productos = loadData("productos");
  const nameExists = productos.some(
    (p) =>
      p.nombreProducto.toLowerCase() ===
        productoData.nombreProducto.toLowerCase() &&
      (!currentProductoId || p.id !== currentProductoId)
  );

  if (nameExists) {
    showAlert("Ya existe un producto con este nombre", "danger");
    return;
  }

  let result;
  let actionType;

  if (currentProductoId) {
    productoData.id = currentProductoId;
    result = updateRecord("productos", productoData);
    actionType = "actualizado";
  } else {
    productoData.id = generateId(productos);
    result = addRecord("productos", productoData);
    actionType = "agregado";

    const stock = loadData("stock");
    const stockData = {
      id: generateId(stock),
      idProducto: productoData.id,
      cantidad: 0,
    };
    addRecord("stock", stockData);
  }

  if (result) {
    loadProductos();
    closeModal("productoModal");
    clearForm("productoForm");
    showAlert(`Producto ${actionType} correctamente`, "success");

    if (typeof addActivity === "function") {
      const activityTitle = currentProductoId
        ? "Producto actualizado"
        : "Nuevo producto agregado";
      const activityDesc = `${productoData.nombreProducto} fue ${actionType}`;
      addActivity("producto", activityTitle, activityDesc);
    }

    currentProductoId = null;
  } else {
    showAlert("Error al guardar el producto", "danger");
  }
}

window.openAddModal = openAddModal;
window.editProducto = editProducto;
window.viewProducto = viewProducto;
window.deleteProducto = deleteProducto;
window.editFromView = editFromView;
window.manageStock = manageStock;


