let currentFacturaId = null;
let invoiceProducts = [];
let invoiceCounter = 1;

document.addEventListener("DOMContentLoaded", async function () {
  await loadFacturas();
  await loadClientes();
  await loadProductos();
  initializeFacturasPage();
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
    logoutBtn.addEventListener("click", (e) => {
      e.preventDefault();
      if (confirm("¿Estás seguro de que deseas cerrar sesión?")) {
        // Crear un formulario para logout POST a Spring Security
        const logoutForm = document.createElement('form');
        logoutForm.method = 'POST';
        logoutForm.action = '/logout';
        
        // Agregar token CSRF si existe
        const csrfHeader = getCsrfHeader();
        if (csrfHeader['X-CSRF-TOKEN']) {
          const csrfInput = document.createElement('input');
          csrfInput.type = 'hidden';
          csrfInput.name = '_csrf';
          csrfInput.value = csrfHeader['X-CSRF-TOKEN'];
          logoutForm.appendChild(csrfInput);
        }
        
        document.body.appendChild(logoutForm);
        logoutForm.submit();
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

async function loadFacturas() {
  try {
    const facturas = await apiGet('/api/facturas');
    const facturasMapped = facturas.map(f => ({
      id: f.idFactura || f.id,
      numeroFactura: f.numero || f.numeroFactura || '-',
      fechaFactura: f.fecha,
      idCliente: (f.cliente && (f.cliente.idCliente || f.cliente.id)) || f.idCliente,
      nombreCliente: (f.cliente && f.cliente.nombreCliente) || '-',
      total: f.total || 0,
      estado: f.estado || 'pendiente',
      observaciones: f.observaciones || ''
    }));
    renderFacturasTable(facturasMapped);
  } catch (e) {
    showAlert(`Error cargando facturas: ${e.message}`, 'danger');
  }
}

async function loadClientes() {
  try {
    const clientes = await apiGet('/api/clientes');
    const select = document.getElementById("idCliente");
    select.innerHTML = '<option value="">Seleccione un cliente</option>';
    clientes.forEach((cliente) => {
      const option = document.createElement("option");
      option.value = cliente.idCliente || cliente.id;
      option.textContent = `${cliente.nombreCliente} - ${cliente.email || ''}`;
      select.appendChild(option);
    });
  } catch {}
}

async function loadProductos() {
  try {
    const productos = await apiGet('/api/productos');
    const select = document.getElementById("selectProducto");
    select.innerHTML = '<option value="">Seleccione un producto</option>';
    productos.forEach((producto) => {
      const option = document.createElement("option");
      option.value = producto.idProducto || producto.id;
      const precio = producto.precio || 0;
      option.textContent = `${producto.nombreProducto} - $${precio}`;
      option.dataset.precio = precio;
      option.dataset.stock = (producto.stock && producto.stock.cantidad) || producto.cantidadStock || 0;
      select.appendChild(option);
    });
  } catch {}
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

async function filterByStatus() {
  const status = document.getElementById("statusFilter").value;
  
  try {
    const facturas = await apiGet('/api/facturas');
    
    let filteredFacturas = facturas;
    if (status) {
      filteredFacturas = facturas.filter((factura) => 
        factura.estado && factura.estado.toLowerCase() === status.toLowerCase());
    }

    const facturasMapped = filteredFacturas.map(f => ({
      id: f.idFactura || f.id,
      numeroFactura: f.numero || f.numeroFactura || '-',
      fechaFactura: f.fecha,
      idCliente: (f.cliente && (f.cliente.idCliente || f.cliente.id)) || f.idCliente,
      nombreCliente: (f.cliente && f.cliente.nombreCliente) || 'Cliente no encontrado',
      total: f.total || 0,
      estado: f.estado || 'pendiente',
      observaciones: f.observaciones || ''
    }));

    renderFacturasTable(facturasMapped);
  } catch (e) {
    showAlert(`Error filtrando facturas: ${e.message}`, 'danger');
  }
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

async function editFactura(id) {
  try {
    const factura = await apiGet(`/api/facturas/${id}`);
    
    if (factura.estado === "ANULADA" || factura.estado === "anulada") {
      showAlert("No se puede editar una factura anulada", "danger");
      return;
    }

    document.getElementById("modalTitle").textContent = "Editar Factura";
    currentFacturaId = id;

    const form = document.getElementById("facturaForm");
    form.querySelector('[name="numeroFactura"]').value = factura.numero || factura.numeroFactura || '';
    form.querySelector('[name="fechaFactura"]').value = factura.fecha || factura.fechaFactura || '';
    form.querySelector('[name="idCliente"]').value = (factura.cliente && (factura.cliente.idCliente || factura.cliente.id)) || factura.idCliente || '';
    form.querySelector('[name="estado"]').value = factura.estado || '';
    form.querySelector('[name="observaciones"]').value = factura.observaciones || "";

    // Para facturas existentes, los productos podrían venir del endpoint de detalles
    invoiceProducts = factura.detalles || factura.productos || [];
    updateInvoiceProductsTable();
    calculateInvoiceTotals();

    openModal("facturaModal");
  } catch (e) {
    if (e.status === 404) {
      showAlert("Factura no encontrada", "danger");
    } else {
      showAlert(`Error cargando factura: ${e.message}`, "danger");
    }
  }
}

async function viewFactura(id) {
  try {
    const factura = await apiGet(`/api/facturas/${id}`);
    currentFacturaId = id;
    
    // Obtener cliente desde la factura o desde API
    let cliente = null;
    if (factura.cliente) {
      cliente = factura.cliente;
    } else if (factura.idCliente) {
      try {
        cliente = await apiGet(`/api/clientes/${factura.idCliente}`);
      } catch (e) {
        cliente = { nombreCliente: "Cliente no encontrado", email: "", telefono: "" };
      }
    }

    const detailsHtml = generateInvoicePreview(factura, cliente);
    document.getElementById("facturaDetails").innerHTML = detailsHtml;
    openModal("viewFacturaModal");
  } catch (e) {
    if (e.status === 404) {
      showAlert("Factura no encontrada", "danger");
    } else {
      showAlert(`Error cargando factura: ${e.message}`, "danger");
    }
  }
}

function generateInvoicePreview(factura, cliente) {
  let productosHtml = "";
  let subtotal = 0;
  let totalDescuento = 0;

  // Usar los productos/detalles de la factura directamente
  const items = factura.detalles || factura.productos || [];
  
  if (items.length > 0) {
    items.forEach((item) => {
      const itemSubtotal = item.cantidad * item.precio;
      const itemDescuento = itemSubtotal * ((item.descuento || 0) / 100);
      const itemTotal = itemSubtotal - itemDescuento;

      subtotal += itemSubtotal;
      totalDescuento += itemDescuento;

      productosHtml += `
                <tr>
                    <td>${item.nombreProducto || item.producto?.nombreProducto || "Producto"}</td>
                    <td>${item.cantidad}</td>
                    <td>$${parseFloat(item.precio).toFixed(2)}</td>
                    <td>${item.descuento || 0}%</td>
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

async function printFactura(id) {
  try {
    const factura = await apiGet(`/api/facturas/${id}`);
    currentFacturaId = id;
    
    // Obtener cliente
    let cliente = null;
    if (factura.cliente) {
      cliente = factura.cliente;
    } else if (factura.idCliente) {
      try {
        cliente = await apiGet(`/api/clientes/${factura.idCliente}`);
      } catch (e) {
        cliente = { nombreCliente: "Cliente no encontrado", email: "", telefono: "" };
      }
    }
    
    printInvoice(factura, cliente);
  } catch (e) {
    if (e.status === 404) {
      showAlert("Factura no encontrada", "danger");
    } else {
      showAlert(`Error cargando factura: ${e.message}`, "danger");
    }
  }
}

function printInvoice(factura = null, cliente = null) {
  // Si no se proporcionan, intentar obtener de localStorage (fallback)
  if (!factura) {
    factura = getRecord("facturas", currentFacturaId);
    const clientes = loadData("clientes");
    cliente = clientes.find((c) => c.id === factura.idCliente);
  }

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

async function anularFactura(id) {
  if (!confirm("¿Estás seguro de anular esta factura?")) return;
  try {
    await apiPost(`/api/facturas/${id}/anular`, {});
    await loadFacturas();
    showAlert("Factura anulada correctamente", 'success');
  } catch (e) {
    showAlert(e.data?.mensaje || `Error al anular: ${e.message}`, 'danger');
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

async function handleFormSubmit(e) {
  e.preventDefault();

  console.log("=== DEBUG FACTURA SUBMIT ===");
  
  const formData = new FormData(e.target);
  
  // Nota: Por ahora el backend solo crea facturas básicas sin productos
  // TODO: Implementar endpoint para agregar productos a facturas
  console.log("Productos en factura (no se enviarán):", invoiceProducts);

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

  const idClienteValue = formData.get("idCliente");
  const fechaString = formData.get("fechaFactura");
  
  const facturaData = {
    numero: formData.get("numeroFactura"),
    fecha: fechaString, // El formato YYYY-MM-DD debería ser parseado por Spring
    idCliente: idClienteValue ? parseInt(idClienteValue) : null,
    metodoPago: 'EFECTIVO',
    estado: formData.get("estado") || 'PENDIENTE',
    observaciones: (formData.get("observaciones") || '').trim()
  };

  console.log("Datos del formulario:");
  console.log("- Número:", facturaData.numero);
  console.log("- Fecha:", facturaData.fecha);
  console.log("- ID Cliente:", facturaData.idCliente);
  console.log("- Estado:", facturaData.estado);
  console.log("- Total:", facturaData.total);
  
  // Validación mejorada
  if (!facturaData.numero || !facturaData.fecha || !facturaData.idCliente || isNaN(facturaData.idCliente)) {
    console.log("Validación falló:");
    console.log("- Número válido:", !!facturaData.numero);
    console.log("- Fecha válida:", !!facturaData.fecha);
    console.log("- Cliente válido:", !!facturaData.idCliente && !isNaN(facturaData.idCliente));
    showAlert("Complete todos los campos obligatorios (Número, Fecha y Cliente)", "danger");
    return;
  }

  console.log("Enviando datos:", facturaData);
  console.log("Es edición?", currentFacturaId ? "SÍ (ID: " + currentFacturaId + ")" : "NO");

  try {
    let result;
    let actionType;
    
    if (currentFacturaId) {
      // Editar factura existente
      result = await apiPut(`/api/facturas/${currentFacturaId}`, facturaData);
      actionType = "actualizada";
    } else {
      // Crear nueva factura
      result = await apiPost('/api/facturas', facturaData);
      actionType = "creada";
    }
    
    console.log("Respuesta del servidor:", result);
    
    if (invoiceProducts.length > 0) {
      showAlert(`Factura ${actionType} correctamente. Nota: Los productos no se guardaron (funcionalidad pendiente)`, 'warning');
    } else {
      showAlert(`Factura ${actionType} correctamente`, 'success');
    }
    
    closeModal('facturaModal');
    await loadFacturas();
  } catch (e) {
    console.error("Error completo:", e);
    console.error("Error status:", e.status);
    console.error("Error data:", e.data);
    
    // Mostrar el error específico del servidor si está disponible
    let errorMsg = "Error al crear la factura";
    if (e.data && e.data.mensaje) {
      errorMsg = e.data.mensaje;
    } else if (e.data && typeof e.data === 'string') {
      errorMsg = e.data;
    } else if (e.message) {
      errorMsg = e.message;
    }
    
    showAlert(`Error ${e.status || 'desconocido'}: ${errorMsg}`, 'danger');
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


