let currentPedidoId = null;
let orderProducts = [];
let orderCounter = 1;

document.addEventListener("DOMContentLoaded", function () {
  loadPedidos();
  loadProveedores();
  loadProductos();
  initializePedidosPage();
  initializeOrderCounter();
});

function initializePedidosPage() {
  searchTable("searchPedidos", "#pedidosTable tbody");

  const statusFilter = document.getElementById("statusFilter");
  statusFilter.addEventListener("change", filterByStatus);

  const form = document.getElementById("pedidoForm");
  form.addEventListener("submit", handleFormSubmit);

  const logoutBtn = document.querySelector(".btn-logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      if (confirm("¿Estás seguro de que deseas cerrar sesión?")) {
        alert("Sesión cerrada correctamente");
      }
    });
  }

  const today = new Date().toISOString().split("T")[0];
  document.getElementById("fechaPedido").value = today;

  const nextWeek = new Date();
  nextWeek.setDate(nextWeek.getDate() + 7);
  document.getElementById("fechaEntregaEsperada").value = nextWeek
    .toISOString()
    .split("T")[0];
}

function initializeOrderCounter() {
  const pedidos = loadData("pedidos");
  if (pedidos.length > 0) {
    const maxNumber = Math.max(
      ...pedidos.map((p) => {
        const match = p.numeroPedido.match(/P(\d+)/);
        return match ? parseInt(match[1]) : 0;
      })
    );
    orderCounter = maxNumber + 1;
  }
}

function generateOrderNumber() {
  const number = `P${orderCounter.toString().padStart(6, "0")}`;
  orderCounter++;
  return number;
}

function loadPedidos() {
  const pedidos = loadData("pedidos");
  const proveedores = loadData("proveedores");

  // Enrich pedidos with supplier information
  const pedidosWithSuppliers = pedidos.map((pedido) => {
    const proveedor = proveedores.find((p) => p.id === pedido.idProveedor);
    return {
      ...pedido,
      nombreProveedor: proveedor
        ? proveedor.nombreProveedor
        : "Proveedor no encontrado",
    };
  });

  renderPedidosTable(pedidosWithSuppliers);
}

function loadProveedores() {
  const proveedores = loadData("proveedores");
  const select = document.getElementById("idProveedor");

  select.innerHTML = '<option value="">Seleccione un proveedor</option>';
  proveedores.forEach((proveedor) => {
    const option = document.createElement("option");
    option.value = proveedor.id;
    option.textContent = `${proveedor.nombreProveedor} - ${proveedor.email}`;
    select.appendChild(option);
  });
}

function loadProductos() {
  const productos = loadData("productos");
  const select = document.getElementById("selectProducto");

  select.innerHTML = '<option value="">Seleccione un producto</option>';
  productos.forEach((producto) => {
    const option = document.createElement("option");
    option.value = producto.id;
    option.textContent = `${producto.nombreProducto} - Stock: ${
      producto.stock || 0
    }`;
    option.dataset.precioCompra =
      producto.precioCompra || producto.precio * 0.7; // Estimate purchase price
    select.appendChild(option);
  });
}

function renderPedidosTable(pedidos) {
  const columns = [
    { key: "numeroPedido", label: "Número" },
    {
      key: "fechaPedido",
      label: "Fecha Pedido",
      format: (value) => new Date(value).toLocaleDateString(),
    },
    { key: "nombreProveedor", label: "Proveedor" },
    {
      key: "fechaEntregaEsperada",
      label: "Entrega Esperada",
      format: (value) => new Date(value).toLocaleDateString(),
    },
    {
      key: "total",
      label: "Total",
      format: (value) => `$${parseFloat(value || 0).toFixed(2)}`,
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
      onclick: "viewPedido",
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning",
      onclick: "editPedido",
    },
    {
      label: '<i class="fas fa-print"></i>',
      class: "btn-success",
      onclick: "printPedido",
    },
    {
      label: '<i class="fas fa-times"></i>',
      class: "btn-danger",
      onclick: "cancelPedido",
    },
  ];

  renderTable("pedidosTable", pedidos, columns, actions);
}

function filterByStatus() {
  const status = document.getElementById("statusFilter").value;
  const pedidos = loadData("pedidos");
  const proveedores = loadData("proveedores");

  let filteredPedidos = pedidos;
  if (status) {
    filteredPedidos = pedidos.filter((pedido) => pedido.estado === status);
  }

  const pedidosWithSuppliers = filteredPedidos.map((pedido) => {
    const proveedor = proveedores.find((p) => p.id === pedido.idProveedor);
    return {
      ...pedido,
      nombreProveedor: proveedor
        ? proveedor.nombreProveedor
        : "Proveedor no encontrado",
    };
  });

  renderPedidosTable(pedidosWithSuppliers);
}

function openAddModal() {
  document.getElementById("modalTitle").textContent = "Nuevo Pedido";
  clearForm("pedidoForm");
  currentPedidoId = null;
  orderProducts = [];

  // Generate new order number
  document.getElementById("numeroPedido").value = generateOrderNumber();
  const today = new Date().toISOString().split("T")[0];
  document.getElementById("fechaPedido").value = today;

  // Set expected delivery date (7 days from today)
  const nextWeek = new Date();
  nextWeek.setDate(nextWeek.getDate() + 7);
  document.getElementById("fechaEntregaEsperada").value = nextWeek
    .toISOString()
    .split("T")[0];

  document.getElementById("estado").value = "pendiente";

  updateOrderProductsTable();
  calculateOrderTotals();
  openModal("pedidoModal");
}

function editPedido(id) {
  const pedido = getRecord("pedidos", id);
  if (!pedido) {
    showAlert("Pedido no encontrado", "danger");
    return;
  }

  if (pedido.estado === "entregado" || pedido.estado === "cancelado") {
    showAlert("No se puede editar un pedido entregado o cancelado", "danger");
    return;
  }

  document.getElementById("modalTitle").textContent = "Editar Pedido";
  currentPedidoId = id;

  // Populate form
  const form = document.getElementById("pedidoForm");
  form.querySelector('[name="id"]').value = pedido.id;
  form.querySelector('[name="numeroPedido"]').value = pedido.numeroPedido;
  form.querySelector('[name="fechaPedido"]').value = pedido.fechaPedido;
  form.querySelector('[name="fechaEntregaEsperada"]').value =
    pedido.fechaEntregaEsperada;
  form.querySelector('[name="idProveedor"]').value = pedido.idProveedor;
  form.querySelector('[name="estado"]').value = pedido.estado;
  form.querySelector('[name="direccionEntrega"]').value =
    pedido.direccionEntrega || "";
  form.querySelector('[name="observaciones"]').value =
    pedido.observaciones || "";

  // Load order products
  orderProducts = pedido.productos || [];
  updateOrderProductsTable();
  calculateOrderTotals();

  openModal("pedidoModal");
}

function viewPedido(id) {
  const pedido = getRecord("pedidos", id);
  if (!pedido) {
    showAlert("Pedido no encontrado", "danger");
    return;
  }

  currentPedidoId = id;
  const proveedores = loadData("proveedores");
  const proveedor = proveedores.find((p) => p.id === pedido.idProveedor);

  const detailsHtml = generateOrderPreview(pedido, proveedor);
  document.getElementById("pedidoDetails").innerHTML = detailsHtml;

  // Show/hide receive order button
  const receiveBtn = document.getElementById("receiveOrderBtn");
  if (pedido.estado === "enviado") {
    receiveBtn.style.display = "inline-block";
  } else {
    receiveBtn.style.display = "none";
  }

  openModal("viewPedidoModal");
}

function generateOrderPreview(pedido, proveedor) {
  const productos = loadData("productos");

  let productosHtml = "";
  let subtotal = 0;

  if (pedido.productos && pedido.productos.length > 0) {
    pedido.productos.forEach((item) => {
      const producto = productos.find((p) => p.id === item.idProducto);
      const itemTotal = item.cantidad * item.precioCompra;
      subtotal += itemTotal;

      productosHtml += `
                <tr>
                    <td>${
                      producto
                        ? producto.nombreProducto
                        : "Producto no encontrado"
                    }</td>
                    <td>${item.cantidad}</td>
                    <td>$${parseFloat(item.precioCompra).toFixed(2)}</td>
                    <td>$${itemTotal.toFixed(2)}</td>
                </tr>
            `;
    });
  }

  const iva = subtotal * 0.21;
  const total = subtotal + iva;

  const statusClass = pedido.estado;
  const statusText =
    pedido.estado.charAt(0).toUpperCase() + pedido.estado.slice(1);

  return `
        <div class="order-preview">
            <div class="order-header">
                <div>
                    <h2>Pedido de Compra</h2>
                    <p><strong>Ferretería El Martillo</strong><br>
                    Av. Principal 123<br>Ciudad, País</p>
                </div>
                <div class="order-number">
                    ${pedido.numeroPedido}
                </div>
            </div>

            <div class="order-details" style="display: flex; justify-content: space-between; margin-bottom: 20px;">
                <div>
                    <h4>Proveedor:</h4>
                    <p><strong>${
                      proveedor
                        ? proveedor.nombreProveedor
                        : "Proveedor no encontrado"
                    }</strong><br>
                    ${proveedor ? proveedor.email : ""}<br>
                    ${proveedor ? proveedor.telefono : ""}</p>
                </div>
                <div>
                    <h4>Detalles del Pedido:</h4>
                    <p><strong>Fecha:</strong> ${new Date(
                      pedido.fechaPedido
                    ).toLocaleDateString()}<br>
                    <strong>Entrega Esperada:</strong> ${new Date(
                      pedido.fechaEntregaEsperada
                    ).toLocaleDateString()}<br>
                    <strong>Estado:</strong> <span class="status-badge status-${statusClass}">${statusText}</span></p>
                </div>
            </div>

            ${
              pedido.direccionEntrega
                ? `
                <div class="delivery-info">
                    <h4><i class="fas fa-map-marker-alt"></i> Dirección de Entrega:</h4>
                    <p>${pedido.direccionEntrega}</p>
                </div>
            `
                : ""
            }

            <table class="table" style="margin-bottom: 20px;">
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Precio Unitario</th>
                        <th>Total</th>
                    </tr>
                </thead>
                <tbody>
                    ${productosHtml}
                </tbody>
            </table>

            <div style="display: flex; justify-content: flex-end;">
                <div style="width: 300px;">
                    <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                        <span>Subtotal:</span>
                        <span>$${subtotal.toFixed(2)}</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 10px;">
                        <span>IVA (21%):</span>
                        <span>$${iva.toFixed(2)}</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; font-weight: bold; font-size: 1.2em; border-top: 2px solid #007bff; padding-top: 10px;">
                        <span>TOTAL:</span>
                        <span>$${total.toFixed(2)}</span>
                    </div>
                </div>
            </div>

            ${
              pedido.observaciones
                ? `
                <div style="margin-top: 20px;">
                    <h4>Observaciones:</h4>
                    <p>${pedido.observaciones}</p>
                </div>
            `
                : ""
            }

            ${
              pedido.fechaEntrega
                ? `
                <div style="margin-top: 20px; padding: 15px; background: #d4edda; border-radius: 8px;">
                    <h4 style="color: #155724;">Pedido Entregado</h4>
                    <p><strong>Fecha de entrega:</strong> ${new Date(
                      pedido.fechaEntrega
                    ).toLocaleDateString()}</p>
                </div>
            `
                : ""
            }
        </div>
    `;
}

function editFromView() {
  closeModal("viewPedidoModal");
  editPedido(currentPedidoId);
}

function printPedido(id) {
  const pedido = getRecord("pedidos", id);
  if (!pedido) {
    showAlert("Pedido no encontrado", "danger");
    return;
  }

  currentPedidoId = id;
  printOrder();
}

function printOrder() {
  const pedido = getRecord("pedidos", currentPedidoId);
  const proveedores = loadData("proveedores");
  const proveedor = proveedores.find((p) => p.id === pedido.idProveedor);

  const printContent = generateOrderPreview(pedido, proveedor);

  const printWindow = window.open("", "_blank");
  printWindow.document.write(`
        <html>
            <head>
                <title>Pedido ${pedido.numeroPedido}</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .order-preview { background: white; }
                    .table { width: 100%; border-collapse: collapse; }
                    .table th, .table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    .table th { background-color: #f5f5f5; }
                    .status-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.75rem; }
                    .status-pendiente { background: #fff3cd; color: #856404; }
                    .status-enviado { background: #cce5ff; color: #004085; }
                    .status-entregado { background: #d4edda; color: #155724; }
                    .status-cancelado { background: #f8d7da; color: #721c24; }
                    .delivery-info { background: #e3f2fd; padding: 15px; border-radius: 8px; margin: 15px 0; }
                </style>
            </head>
            <body>
                ${printContent}
            </body>
        </html>
    `);

  printWindow.document.close();
  printWindow.print();
}

function cancelPedido(id) {
  const pedido = getRecord("pedidos", id);
  if (!pedido) {
    showAlert("Pedido no encontrado", "danger");
    return;
  }

  if (pedido.estado === "entregado") {
    showAlert("No se puede cancelar un pedido ya entregado", "warning");
    return;
  }

  if (pedido.estado === "cancelado") {
    showAlert("El pedido ya está cancelado", "warning");
    return;
  }

  if (confirm(`¿Estás seguro de cancelar el pedido ${pedido.numeroPedido}?`)) {
    pedido.estado = "cancelado";
    pedido.fechaCancelacion = new Date().toISOString();

    if (updateRecord("pedidos", id, pedido)) {
      showAlert("Pedido cancelado correctamente", "success");
      loadPedidos();
      logActivity(`Pedido cancelado: ${pedido.numeroPedido}`);
    } else {
      showAlert("Error al cancelar el pedido", "danger");
    }
  }
}

function receiveOrder() {
  const pedido = getRecord("pedidos", currentPedidoId);
  if (!pedido) {
    showAlert("Pedido no encontrado", "danger");
    return;
  }

  if (pedido.estado !== "enviado") {
    showAlert("Solo se pueden recibir pedidos en estado 'enviado'", "warning");
    return;
  }

  if (confirm(`¿Confirmar la recepción del pedido ${pedido.numeroPedido}?`)) {
    // Update order status
    pedido.estado = "entregado";
    pedido.fechaEntrega = new Date().toISOString();

    // Update stock for all products in the order
    const productos = loadData("productos");
    let stockUpdated = true;

    pedido.productos.forEach((item) => {
      const producto = productos.find((p) => p.id === item.idProducto);
      if (producto) {
        const currentStock = parseInt(producto.stock) || 0;
        producto.stock = currentStock + parseInt(item.cantidad);
        if (!updateRecord("productos", producto.id, producto)) {
          stockUpdated = false;
        }
      }
    });

    if (updateRecord("pedidos", currentPedidoId, pedido) && stockUpdated) {
      showAlert("Pedido recibido correctamente. Stock actualizado.", "success");
      closeModal("viewPedidoModal");
      loadPedidos();
      logActivity(
        `Pedido recibido: ${pedido.numeroPedido} - Stock actualizado`
      );
    } else {
      showAlert("Error al recibir el pedido", "danger");
    }
  }
}

function addProductToOrder() {
  const selectProducto = document.getElementById("selectProducto");
  const cantidad = parseFloat(document.getElementById("cantidad").value);
  const precioCompra = parseFloat(
    document.getElementById("precioCompra").value
  );

  if (!selectProducto.value || !cantidad || !precioCompra) {
    showAlert("Complete todos los campos del producto", "warning");
    return;
  }

  if (cantidad <= 0 || precioCompra <= 0) {
    showAlert("La cantidad y el precio deben ser mayores a 0", "warning");
    return;
  }

  // Check if product already exists in order
  const existingIndex = orderProducts.findIndex(
    (item) => item.idProducto === selectProducto.value
  );

  if (existingIndex !== -1) {
    // Update existing product
    orderProducts[existingIndex].cantidad += cantidad;
    orderProducts[existingIndex].precioCompra = precioCompra;
  } else {
    // Add new product
    orderProducts.push({
      idProducto: selectProducto.value,
      nombreProducto:
        selectProducto.options[selectProducto.selectedIndex].text.split(
          " - "
        )[0],
      cantidad: cantidad,
      precioCompra: precioCompra,
    });
  }

  // Clear form
  document.getElementById("selectProducto").value = "";
  document.getElementById("cantidad").value = "";
  document.getElementById("precioCompra").value = "";

  updateOrderProductsTable();
  calculateOrderTotals();
}

function removeProductFromOrder(index) {
  orderProducts.splice(index, 1);
  updateOrderProductsTable();
  calculateOrderTotals();
}

function updateOrderProductsTable() {
  const tbody = document.getElementById("orderProductsTable");
  tbody.innerHTML = "";

  orderProducts.forEach((item, index) => {
    const total = item.cantidad * item.precioCompra;

    const row = document.createElement("tr");
    row.innerHTML = `
            <td>${item.nombreProducto}</td>
            <td>${item.cantidad}</td>
            <td>$${item.precioCompra.toFixed(2)}</td>
            <td>$${total.toFixed(2)}</td>
            <td>
                <button type="button" class="btn btn-danger btn-sm" onclick="removeProductFromOrder(${index})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
    tbody.appendChild(row);
  });
}

function calculateOrderTotals() {
  let subtotal = 0;

  orderProducts.forEach((item) => {
    subtotal += item.cantidad * item.precioCompra;
  });

  const iva = subtotal * 0.21;
  const total = subtotal + iva;

  document.getElementById("orderSubtotal").textContent = `$${subtotal.toFixed(
    2
  )}`;
  document.getElementById("orderIva").textContent = `$${iva.toFixed(2)}`;
  document.getElementById("orderTotal").textContent = `$${total.toFixed(2)}`;
}

function handleFormSubmit(e) {
  e.preventDefault();

  const formData = new FormData(e.target);

  if (orderProducts.length === 0) {
    showAlert("Debe agregar al menos un producto al pedido", "danger");
    return;
  }

  // Calculate totals
  let subtotal = 0;
  orderProducts.forEach((item) => {
    subtotal += item.cantidad * item.precioCompra;
  });

  const iva = subtotal * 0.21;
  const total = subtotal + iva;

  const pedidoData = {
    numeroPedido: formData.get("numeroPedido"),
    fechaPedido: formData.get("fechaPedido"),
    fechaEntregaEsperada: formData.get("fechaEntregaEsperada"),
    idProveedor: formData.get("idProveedor"),
    estado: formData.get("estado"),
    direccionEntrega: formData.get("direccionEntrega").trim(),
    observaciones: formData.get("observaciones").trim(),
    productos: [...orderProducts],
    subtotal: subtotal,
    iva: iva,
    total: total,
  };

  // Validation
  if (
    !pedidoData.numeroPedido ||
    !pedidoData.fechaPedido ||
    !pedidoData.fechaEntregaEsperada ||
    !pedidoData.idProveedor
  ) {
    showAlert("Complete todos los campos obligatorios", "danger");
    return;
  }

  // Validate expected delivery date
  const pedidoDate = new Date(pedidoData.fechaPedido);
  const expectedDate = new Date(pedidoData.fechaEntregaEsperada);

  if (expectedDate <= pedidoDate) {
    showAlert(
      "La fecha de entrega esperada debe ser posterior a la fecha del pedido",
      "danger"
    );
    return;
  }

  if (currentPedidoId) {
    // Edit existing pedido
    pedidoData.id = currentPedidoId;
    pedidoData.fechaModificacion = new Date().toISOString();

    if (updateRecord("pedidos", currentPedidoId, pedidoData)) {
      showAlert("Pedido actualizado correctamente", "success");
      closeModal("pedidoModal");
      loadPedidos();
      logActivity(`Pedido modificado: ${pedidoData.numeroPedido}`);
    } else {
      showAlert("Error al actualizar el pedido", "danger");
    }
  } else {
    // Create new pedido
    pedidoData.id = generateId();
    pedidoData.fechaCreacion = new Date().toISOString();

    if (saveRecord("pedidos", pedidoData)) {
      showAlert("Pedido creado correctamente", "success");
      closeModal("pedidoModal");
      loadPedidos();
      logActivity(`Nuevo pedido creado: ${pedidoData.numeroPedido}`);
    } else {
      showAlert("Error al crear el pedido", "danger");
    }
  }
}

// Initialize sample data if none exists
function initializeSamplePedidos() {
  const pedidos = loadData("pedidos");
  if (pedidos.length === 0) {
    const proveedores = loadData("proveedores");
    const productos = loadData("productos");

    if (proveedores.length > 0 && productos.length > 0) {
      const samplePedidos = [
        {
          id: "1",
          numeroPedido: "P000001",
          fechaPedido: "2024-01-10",
          fechaEntregaEsperada: "2024-01-17",
          idProveedor: proveedores[0].id,
          estado: "enviado",
          productos: [
            {
              idProducto: productos[0].id,
              nombreProducto: productos[0].nombre,
              cantidad: 50,
              precioCompra: productos[0].precio * 0.7,
            },
          ],
          subtotal: productos[0].precio * 0.7 * 50,
          iva: productos[0].precio * 0.7 * 50 * 0.21,
          total: productos[0].precio * 0.7 * 50 * 1.21,
          direccionEntrega: "Ferretería El Martillo - Av. Principal 123",
          observaciones: "Pedido de ejemplo para reposición de stock",
          fechaCreacion: new Date().toISOString(),
        },
      ];

      saveData("pedidos", samplePedidos);
      orderCounter = 2;
    }
  }
}

// Initialize sample data when page loads
document.addEventListener("DOMContentLoaded", function () {
  setTimeout(initializeSamplePedidos, 500); // Delay to ensure other data is loaded
});
