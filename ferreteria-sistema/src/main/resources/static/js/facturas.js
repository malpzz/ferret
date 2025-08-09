let currentFacturaId = null;
let invoiceProducts = [];
let invoiceCounter = 1;

document.addEventListener("DOMContentLoaded", function () {
  loadFacturas();
  loadClientes();
  loadProductos();
  initializeFacturasPage();
  initializeInvoiceCounter();
});

function initializeFacturasPage() {
  searchTable("searchFacturas", "#facturasTable tbody");

  const statusFilter = document.getElementById("statusFilter");
  statusFilter.addEventListener("change", filterByStatus);

  const form = document.getElementById("facturaForm");
  form.addEventListener("submit", handleFormSubmit);

  const selectProducto = document.getElementById("selectProducto");
  selectProducto.addEventListener("change", onProductSelect);

  const logoutBtn = document.querySelector(".btn-logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      if (confirm("¿Estás seguro de que deseas cerrar sesión?")) {
        alert("Sesión cerrada correctamente");
      }
    });
  }

  document.getElementById("fechaFactura").value = new Date()
    .toISOString()
    .split("T")[0];
}

function initializeInvoiceCounter() {
  const facturas = loadData("facturas");
  if (facturas.length > 0) {
    const maxNumber = Math.max(
      ...facturas.map((f) => {
        const match = f.numeroFactura.match(/F(\d+)/);
        return match ? parseInt(match[1]) : 0;
      })
    );
    invoiceCounter = maxNumber + 1;
  }
}

function generateInvoiceNumber() {
  const number = `F${invoiceCounter.toString().padStart(6, "0")}`;
  invoiceCounter++;
  return number;
}

function loadFacturas() {
  const facturas = loadData("facturas");
  const clientes = loadData("clientes");

  const facturasWithClients = facturas.map((factura) => {
    const cliente = clientes.find((c) => c.id === factura.idCliente);
    return {
      ...factura,
      nombreCliente: cliente ? cliente.nombre : "Cliente no encontrado",
    };
  });

  renderFacturasTable(facturasWithClients);
}

function loadClientes() {
  const clientes = loadData("clientes");
  const select = document.getElementById("idCliente");

  select.innerHTML = '<option value="">Seleccione un cliente</option>';
  clientes.forEach((cliente) => {
    const option = document.createElement("option");
    option.value = cliente.id;
    option.textContent = `${cliente.nombreCliente} - ${cliente.email}`;
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
    option.textContent = `${producto.nombreProducto} - $${producto.precio}`;
    option.dataset.precio = producto.precio;
    option.dataset.stock = producto.stock || 0;
    select.appendChild(option);
  });
}

function renderFacturasTable(facturas) {
  const columns = [
    { key: "numeroFactura", label: "Número" },
    {
      key: "fechaFactura",
      label: "Fecha",
      format: (value) => new Date(value).toLocaleDateString(),
    },
    { key: "nombreCliente", label: "Cliente" },
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
      onclick: "viewFactura",
    },
    {
      label: '<i class="fas fa-edit"></i>',
      class: "btn-warning",
      onclick: "editFactura",
    },
    {
      label: '<i class="fas fa-print"></i>',
      class: "btn-success",
      onclick: "printFactura",
    },
    {
      label: '<i class="fas fa-times"></i>',
      class: "btn-danger",
      onclick: "anularFactura",
    },
  ];

  renderTable("facturasTable", facturas, columns, actions);
}

function filterByStatus() {
  const status = document.getElementById("statusFilter").value;
  const facturas = loadData("facturas");
  const clientes = loadData("clientes");

  let filteredFacturas = facturas;
  if (status) {
    filteredFacturas = facturas.filter((factura) => factura.estado === status);
  }

  const facturasWithClients = filteredFacturas.map((factura) => {
    const cliente = clientes.find((c) => c.id === factura.idCliente);
    return {
      ...factura,
      nombreCliente: cliente ? cliente.nombreCliente : "Cliente no encontrado",
    };
  });

  renderFacturasTable(facturasWithClients);
}

function openAddModal() {
  document.getElementById("modalTitle").textContent = "Nueva Factura";
  clearForm("facturaForm");
  currentFacturaId = null;
  invoiceProducts = [];

  document.getElementById("numeroFactura").value = generateInvoiceNumber();
  document.getElementById("fechaFactura").value = new Date()
    .toISOString()
    .split("T")[0];
  document.getElementById("estado").value = "pendiente";

  updateInvoiceProductsTable();
  calculateInvoiceTotals();
  openModal("facturaModal");
}

function editFactura(id) {
  const factura = getRecord("facturas", id);
  if (!factura) {
    showAlert("Factura no encontrada", "danger");
    return;
  }

  if (factura.estado === "anulada") {
    showAlert("No se puede editar una factura anulada", "danger");
    return;
  }

  document.getElementById("modalTitle").textContent = "Editar Factura";
  currentFacturaId = id;

  const form = document.getElementById("facturaForm");
  form.querySelector('[name="id"]').value = factura.id;
  form.querySelector('[name="numeroFactura"]').value = factura.numeroFactura;
  form.querySelector('[name="fechaFactura"]').value = factura.fechaFactura;
  form.querySelector('[name="idCliente"]').value = factura.idCliente;
  form.querySelector('[name="estado"]').value = factura.estado;
  form.querySelector('[name="observaciones"]').value =
    factura.observaciones || "";

  invoiceProducts = factura.productos || [];
  updateInvoiceProductsTable();
  calculateInvoiceTotals();

  openModal("facturaModal");
}

function viewFactura(id) {
  const factura = getRecord("facturas", id);
  if (!factura) {
    showAlert("Factura no encontrada", "danger");
    return;
  }

  currentFacturaId = id;
  const clientes = loadData("clientes");
  const cliente = clientes.find((c) => c.id === factura.idCliente);

  const detailsHtml = generateInvoicePreview(factura, cliente);
  document.getElementById("facturaDetails").innerHTML = detailsHtml;
  openModal("viewFacturaModal");
}

function generateInvoicePreview(factura, cliente) {
  const productos = loadData("productos");

  let productosHtml = "";
  let subtotal = 0;
  let totalDescuento = 0;

  if (factura.productos && factura.productos.length > 0) {
    factura.productos.forEach((item) => {
      const producto = productos.find((p) => p.id === item.idProducto);
      const itemSubtotal = item.cantidad * item.precio;
      const itemDescuento = itemSubtotal * (item.descuento / 100);
      const itemTotal = itemSubtotal - itemDescuento;

      subtotal += itemSubtotal;
      totalDescuento += itemDescuento;

      productosHtml += `
                <tr>
                    <td>${
                      producto
                        ? producto.nombreProducto
                        : "Producto no encontrado"
                    }</td>
                    <td>${item.cantidad}</td>
                    <td>$${parseFloat(item.precio).toFixed(2)}</td>
                    <td>${item.descuento}%</td>
                    <td>$${itemTotal.toFixed(2)}</td>
                </tr>
            `;
    });
  }

  const baseImponible = subtotal - totalDescuento;
  const iva = baseImponible * 0.21;
  const total = baseImponible + iva;

  return `
        <div class="invoice-preview">
            <div class="invoice-header">
                <div>
                    <h2>Ferretería El Martillo</h2>
                    <p>Av. Principal 123<br>Ciudad, País<br>Tel: (123) 456-7890</p>
                </div>
                <div class="invoice-number">
                    ${factura.numeroFactura}
                </div>
            </div>

            <div class="invoice-details" style="display: flex; justify-content: space-between; margin-bottom: 20px;">
                <div>
                    <h4>Cliente:</h4>
                    <p><strong>${
                      cliente ? cliente.nombreCliente : "Cliente no encontrado"
                    }</strong><br>
                    ${cliente ? cliente.email : ""}<br>
                    ${cliente ? cliente.telefono : ""}</p>
                </div>
                <div>
                    <h4>Detalles de la Factura:</h4>
                    <p><strong>Fecha:</strong> ${new Date(
                      factura.fechaFactura
                    ).toLocaleDateString()}<br>
                    <strong>Estado:</strong> <span class="status-badge status-${
                      factura.estado
                    }">${
    factura.estado.charAt(0).toUpperCase() + factura.estado.slice(1)
  }</span></p>
                </div>
            </div>

            <table class="table" style="margin-bottom: 20px;">
                <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Precio Unit.</th>
                        <th>Descuento</th>
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
                    <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                        <span>Descuento:</span>
                        <span>-$${totalDescuento.toFixed(2)}</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
                        <span>Base Imponible:</span>
                        <span>$${baseImponible.toFixed(2)}</span>
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
              factura.observaciones
                ? `
                <div style="margin-top: 20px;">
                    <h4>Observaciones:</h4>
                    <p>${factura.observaciones}</p>
                </div>
            `
                : ""
            }
        </div>
    `;
}

function editFromView() {
  closeModal("viewFacturaModal");
  editFactura(currentFacturaId);
}

function printFactura(id) {
  const factura = getRecord("facturas", id);
  if (!factura) {
    showAlert("Factura no encontrada", "danger");
    return;
  }

  currentFacturaId = id;
  printInvoice();
}

function printInvoice() {
  const factura = getRecord("facturas", currentFacturaId);
  const clientes = loadData("clientes");
  const cliente = clientes.find((c) => c.id === factura.idCliente);

  const printContent = generateInvoicePreview(factura, cliente);

  const printWindow = window.open("", "_blank");
  printWindow.document.write(`
        <html>
            <head>
                <title>Factura ${factura.numeroFactura}</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .invoice-preview { background: white; }
                    .table { width: 100%; border-collapse: collapse; }
                    .table th, .table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    .table th { background-color: #f5f5f5; }
                    .status-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.75rem; }
                    .status-pagada { background: #d4edda; color: #155724; }
                    .status-pendiente { background: #fff3cd; color: #856404; }
                    .status-anulada { background: #f8d7da; color: #721c24; }
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

function anularFactura(id) {
  const factura = getRecord("facturas", id);
  if (!factura) {
    showAlert("Factura no encontrada", "danger");
    return;
  }

  if (factura.estado === "anulada") {
    showAlert("La factura ya está anulada", "warning");
    return;
  }

  if (confirm(`¿Estás seguro de anular la factura ${factura.numeroFactura}?`)) {
    factura.estado = "anulada";
    factura.fechaAnulacion = new Date().toISOString();

    if (updateRecord("facturas", id, factura)) {
      showAlert("Factura anulada correctamente", "success");
      loadFacturas();
      logActivity(`Factura anulada: ${factura.numeroFactura}`);
    } else {
      showAlert("Error al anular la factura", "danger");
    }
  }
}

function onProductSelect() {
  const select = document.getElementById("selectProducto");
  const selectedOption = select.options[select.selectedIndex];

  if (selectedOption.value) {
    const precio = selectedOption.dataset.precio;
    document.getElementById("precio").value = precio;
    document.getElementById("cantidad").value = 1;
    document.getElementById("descuento").value = 0;
  } else {
    document.getElementById("precio").value = "";
    document.getElementById("cantidad").value = "";
    document.getElementById("descuento").value = 0;
  }
}

function addProductToInvoice() {
  const selectProducto = document.getElementById("selectProducto");
  const cantidad = parseFloat(document.getElementById("cantidad").value);
  const precio = parseFloat(document.getElementById("precio").value);
  const descuento = parseFloat(document.getElementById("descuento").value) || 0;

  if (!selectProducto.value || !cantidad || !precio) {
    showAlert("Complete todos los campos del producto", "warning");
    return;
  }

  if (cantidad <= 0 || precio <= 0) {
    showAlert("La cantidad y el precio deben ser mayores a 0", "warning");
    return;
  }

  const existingIndex = invoiceProducts.findIndex(
    (item) => item.idProducto === selectProducto.value
  );

  if (existingIndex !== -1) {
    invoiceProducts[existingIndex].cantidad += cantidad;
    invoiceProducts[existingIndex].precio = precio;
    invoiceProducts[existingIndex].descuento = descuento;
  } else {
    invoiceProducts.push({
      idProducto: selectProducto.value,
      nombreProducto:
        selectProducto.options[selectProducto.selectedIndex].text.split(
          " - "
        )[0],
      cantidad: cantidad,
      precio: precio,
      descuento: descuento,
    });
  }

  document.getElementById("selectProducto").value = "";
  document.getElementById("cantidad").value = "";
  document.getElementById("precio").value = "";
  document.getElementById("descuento").value = 0;

  updateInvoiceProductsTable();
  calculateInvoiceTotals();
}

function removeProductFromInvoice(index) {
  invoiceProducts.splice(index, 1);
  updateInvoiceProductsTable();
  calculateInvoiceTotals();
}

function updateInvoiceProductsTable() {
  const tbody = document.getElementById("invoiceProductsTable");
  tbody.innerHTML = "";

  invoiceProducts.forEach((item, index) => {
    const subtotal = item.cantidad * item.precio;
    const descuentoAmount = subtotal * (item.descuento / 100);
    const total = subtotal - descuentoAmount;

    const row = document.createElement("tr");
    row.innerHTML = `
            <td>${item.nombreProducto}</td>
            <td>${item.cantidad}</td>
            <td>$${item.precio.toFixed(2)}</td>
            <td>${item.descuento}%</td>
            <td>$${total.toFixed(2)}</td>
            <td>
                <button type="button" class="btn btn-danger btn-sm" onclick="removeProductFromInvoice(${index})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
    tbody.appendChild(row);
  });
}

function calculateInvoiceTotals() {
  let subtotal = 0;
  let totalDescuento = 0;

  invoiceProducts.forEach((item) => {
    const itemSubtotal = item.cantidad * item.precio;
    const itemDescuento = itemSubtotal * (item.descuento / 100);
    subtotal += itemSubtotal;
    totalDescuento += itemDescuento;
  });

  const baseImponible = subtotal - totalDescuento;
  const iva = baseImponible * 0.21;
  const total = baseImponible + iva;

  document.getElementById("invoiceSubtotal").textContent = `$${subtotal.toFixed(
    2
  )}`;
  document.getElementById(
    "invoiceDiscount"
  ).textContent = `$${totalDescuento.toFixed(2)}`;
  document.getElementById("invoiceIva").textContent = `$${iva.toFixed(2)}`;
  document.getElementById("invoiceTotal").textContent = `$${total.toFixed(2)}`;
}

function handleFormSubmit(e) {
  e.preventDefault();

  const formData = new FormData(e.target);

  if (invoiceProducts.length === 0) {
    showAlert("Debe agregar al menos un producto a la factura", "danger");
    return;
  }

  let subtotal = 0;
  let totalDescuento = 0;

  invoiceProducts.forEach((item) => {
    const itemSubtotal = item.cantidad * item.precio;
    const itemDescuento = itemSubtotal * (item.descuento / 100);
    subtotal += itemSubtotal;
    totalDescuento += itemDescuento;
  });

  const baseImponible = subtotal - totalDescuento;
  const iva = baseImponible * 0.21;
  const total = baseImponible + iva;

  const facturaData = {
    numeroFactura: formData.get("numeroFactura"),
    fechaFactura: formData.get("fechaFactura"),
    idCliente: formData.get("idCliente"),
    estado: formData.get("estado"),
    observaciones: formData.get("observaciones").trim(),
    productos: [...invoiceProducts],
    subtotal: subtotal,
    descuento: totalDescuento,
    iva: iva,
    total: total,
  };

  if (
    !facturaData.numeroFactura ||
    !facturaData.fechaFactura ||
    !facturaData.idCliente
  ) {
    showAlert("Complete todos los campos obligatorios", "danger");
    return;
  }

  if (currentFacturaId) {
    facturaData.id = currentFacturaId;
    facturaData.fechaModificacion = new Date().toISOString();

    if (updateRecord("facturas", currentFacturaId, facturaData)) {
      showAlert("Factura actualizada correctamente", "success");
      closeModal("facturaModal");
      loadFacturas();
      logActivity(`Factura modificada: ${facturaData.numeroFactura}`);
    } else {
      showAlert("Error al actualizar la factura", "danger");
    }
  } else {
    facturaData.id = generateId();
    facturaData.fechaCreacion = new Date().toISOString();

    if (saveRecord("facturas", facturaData)) {
      showAlert("Factura creada correctamente", "success");
      closeModal("facturaModal");
      loadFacturas();
      logActivity(`Nueva factura creada: ${facturaData.numeroFactura}`);
    } else {
      showAlert("Error al crear la factura", "danger");
    }
  }
}

function initializeSampleFacturas() {
  const facturas = loadData("facturas");
  if (facturas.length === 0) {
    const clientes = loadData("clientes");
    const productos = loadData("productos");

    if (clientes.length > 0 && productos.length > 0) {
      const sampleFacturas = [
        {
          id: "1",
          numeroFactura: "F000001",
          fechaFactura: "2024-01-15",
          idCliente: clientes[0].id,
          estado: "pagada",
          productos: [
            {
              idProducto: productos[0].id,
              nombreProducto: productos[0].nombre,
              cantidad: 2,
              precio: productos[0].precio,
              descuento: 0,
            },
          ],
          subtotal: productos[0].precio * 2,
          descuento: 0,
          iva: productos[0].precio * 2 * 0.21,
          total: productos[0].precio * 2 * 1.21,
          observaciones: "Factura de ejemplo",
          fechaCreacion: new Date().toISOString(),
        },
      ];

      saveData("facturas", sampleFacturas);
      invoiceCounter = 2;
    }
  }
}

document.addEventListener("DOMContentLoaded", function () {
  setTimeout(initializeSampleFacturas, 500);
});

window.openAddModal = openAddModal;
window.editFactura = editFactura;
window.viewFactura = viewFactura;
window.printFactura = printFactura;
window.anularFactura = anularFactura;
window.editFromView = editFromView;
window.printInvoice = printInvoice;
window.addProductToInvoice = addProductToInvoice;
window.removeProductFromInvoice = removeProductFromInvoice;


