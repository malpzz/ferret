function initializeStockPage() {
  document.addEventListener("DOMContentLoaded", function () {
    loadStock();
    loadAlerts();
    initializeStockPage();
    loadProductsSelect();
  });
}

function initializeStockPage() {
  searchTable("searchStock", "#stockTable tbody");

  const filterSelect = document.getElementById("filterStock");
  filterSelect.addEventListener("change", filterStock);

  const form = document.getElementById("movementForm");
  form.addEventListener("submit", handleMovementSubmit);

  const logoutBtn = document.querySelector(".btn-logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      if (confirm("¿Estás seguro de que deseas cerrar sesión?")) {
        alert("Sesión cerrada correctamente");
      }
    });
  }

  const urlParams = new URLSearchParams(window.location.search);
  const productoId = urlParams.get("producto");
  if (productoId) {
    setTimeout(() => {
      openMovementModal();
      document.getElementById("producto").value = productoId;
    }, 500);
  }
}

function loadStock() {
  const productos = loadData("productos");
  const stock = loadData("stock");

  const stockData = productos.map((producto) => {
    const stockInfo = stock.find((s) => s.idProducto === producto.id);
    return {
      id: producto.id,
      nombre: producto.nombreProducto,
      descripcion: producto.descripcion,
      precio: producto.precio,
      cantidad: stockInfo ? stockInfo.cantidad : 0,
    };
  });

  renderStockTable(stockData);
}

function renderStockTable(stockData) {
  const columns = [
    { key: "nombreProducto", label: "Producto" },
    {
      key: "cantidad",
      label: "Stock Actual",
      format: (value, item) => {
        let className = "stock-good";
        let icon = "fas fa-check-circle";
        let color = "#2ecc71";

        if (value === 0) {
          className = "stock-out";
          icon = "fas fa-times-circle";
          color = "#e74c3c";
        } else if (value < 10) {
          className = "stock-low";
          icon = "fas fa-exclamation-triangle";
          color = "#f39c12";
        }

        return `<span class="${className}"><i class="${icon}" style="color: ${color}; margin-right: 5px;"></i>${value} unidades</span>`;
      },
    },
    {
      key: "precio",
      label: "Precio Unit.",
      format: (value) => formatCurrency(value),
    },
  ];

  const actions = [
    {
      label: '<i class="fas fa-history"></i>',
      class: "btn-info",
      onclick: "viewHistory",
    },
    {
      label: '<i class="fas fa-plus"></i>',
      class: "btn-success",
      onclick: "quickEntry",
    },
    {
      label: '<i class="fas fa-minus"></i>',
      class: "btn-warning",
      onclick: "quickExit",
    },
  ];

  renderTable("stockTable", stockData, columns, actions);

  setTimeout(() => {
    const rows = document.querySelectorAll("#stockTable tbody tr");
    rows.forEach((row, index) => {
      const stock = stockData[index];
      if (stock) {
        if (stock.cantidad === 0) {
          row.classList.add("stock-out");
        } else if (stock.cantidad < 10) {
          row.classList.add("stock-low");
        } else {
          row.classList.add("stock-good");
        }
      }
    });
  }, 100);
}

function filterStock() {
  const filter = document.getElementById("filterStock").value;
  const productos = loadData("productos");
  const stock = loadData("stock");

  let filteredData = productos.map((producto) => {
    const stockInfo = stock.find((s) => s.idProducto === producto.id);
    return {
      id: producto.id,
      nombre: producto.nombreProducto,
      descripcion: producto.descripcion,
      precio: producto.precio,
      cantidad: stockInfo ? stockInfo.cantidad : 0,
    };
  });

  switch (filter) {
    case "bajo":
      filteredData = filteredData.filter(
        (item) => item.cantidad > 0 && item.cantidad < 10
      );
      break;
    case "agotado":
      filteredData = filteredData.filter((item) => item.cantidad === 0);
      break;
    case "disponible":
      filteredData = filteredData.filter((item) => item.cantidad > 0);
      break;
  }

  renderStockTable(filteredData);
}

function loadProductsSelect() {
  const productos = loadData("productos");
  const select = document.getElementById("producto");

  select.innerHTML = '<option value="">Seleccione un producto</option>';
  productos.forEach((producto) => {
    const option = document.createElement("option");
    option.value = producto.id;
    option.textContent = `${producto.nombreProducto} - ${formatCurrency(
      producto.precio
    )}`;
    select.appendChild(option);
  });
}

function loadAlerts() {
  const productos = loadData("productos");
  const stock = loadData("stock");
  const alertsList = document.getElementById("alertsList");

  const alerts = [];

  productos.forEach((producto) => {
    const stockInfo = stock.find((s) => s.idProducto === producto.id);
    const cantidad = stockInfo ? stockInfo.cantidad : 0;

    if (cantidad === 0) {
      alerts.push({
        type: "danger",
        icon: "fas fa-times-circle",
        message: `${producto.nombreProducto} está agotado`,
        action: `Reponer stock del producto ${producto.nombreProducto}`,
      });
    } else if (cantidad < 10) {
      alerts.push({
        type: "warning",
        icon: "fas fa-exclamation-triangle",
        message: `${producto.nombreProducto} tiene stock bajo (${cantidad} unidades)`,
        action: `Considerar reponer stock de ${producto.nombreProducto}`,
      });
    }
  });

  if (alerts.length === 0) {
    alertsList.innerHTML = `
            <div class="alert alert-success">
                <i class="fas fa-check-circle"></i> No hay alertas de stock en este momento
            </div>
        `;
  } else {
    alertsList.innerHTML = alerts
      .map(
        (alert) => `
            <div class="alert alert-${alert.type}">
                <i class="${alert.icon}"></i>
                <strong>${alert.message}</strong><br>
                <small>${alert.action}</small>
            </div>
        `
      )
      .join("");
  }
}

function openMovementModal() {
  clearForm("movementForm");
  loadProductsSelect();
  openModal("movementModal");
}

function quickEntry(productId) {
  openMovementModal();
  document.getElementById("producto").value = productId;
  document.getElementById("tipoMovimiento").value = "entrada";
}

function quickExit(productId) {
  const stock = loadData("stock");
  const stockInfo = stock.find((s) => s.idProducto === productId);
  const currentStock = stockInfo ? stockInfo.cantidad : 0;

  if (currentStock === 0) {
    showAlert("No hay stock disponible para dar salida", "warning");
    return;
  }

  openMovementModal();
  document.getElementById("producto").value = productId;
  document.getElementById("tipoMovimiento").value = "salida";

  const cantidadInput = document.getElementById("cantidad");
  cantidadInput.setAttribute("max", currentStock);
}

function viewHistory(productId) {
  const producto = getRecord("productos", productId);
  const movements = JSON.parse(localStorage.getItem("stockMovements") || "[]");
  const productMovements = movements.filter((m) => m.idProducto === productId);

  const historyContent = document.getElementById("historyContent");

  if (productMovements.length === 0) {
    historyContent.innerHTML = `
            <div class="alert alert-info">
                No hay movimientos registrados para ${producto.nombreProducto}
            </div>
        `;
  } else {
    historyContent.innerHTML = `
            <h4>Historial de Movimientos - ${producto.nombreProducto}</h4>
            <div class="table-container">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Fecha</th>
                            <th>Tipo</th>
                            <th>Cantidad</th>
                            <th>Stock Resultante</th>
                            <th>Motivo</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${productMovements
                          .map(
                            (movement) => `
                            <tr>
                                <td>${new Date(
                                  movement.fecha
                                ).toLocaleDateString("es-AR")}</td>
                                <td>
                                    <span class="badge ${
                                      movement.tipo === "entrada"
                                        ? "badge-success"
                                        : "badge-warning"
                                    }">
                                        ${movement.tipo.toUpperCase()}
                                    </span>
                                </td>
                                <td>${movement.cantidad}</td>
                                <td>${movement.stockResultante}</td>
                                <td>${movement.motivo || "-"}</td>
                            </tr>
                        `
                          )
                          .join("")}
                    </tbody>
                </table>
            </div>
            <style>
                .badge { padding: 4px 8px; border-radius: 4px; font-size: 0.75rem; font-weight: 500; }
                .badge-success { background: #2ecc71; color: white; }
                .badge-warning { background: #f39c12; color: white; }
            </style>
        `;
  }

  openModal("historyModal");
}

function handleMovementSubmit(e) {
  e.preventDefault();

  const formData = new FormData(e.target);
  const productId = parseInt(formData.get("producto"));
  const tipo = formData.get("tipoMovimiento");
  const cantidad = parseInt(formData.get("cantidad"));
  const motivo = formData.get("motivo").trim();

  if (!productId || !tipo || !cantidad) {
    showAlert("Todos los campos obligatorios son requeridos", "danger");
    return;
  }

  const stock = loadData("stock");
  const stockInfo = stock.find((s) => s.idProducto === productId);
  const currentStock = stockInfo ? stockInfo.cantidad : 0;

  let newStock;
  if (tipo === "entrada") {
    newStock = currentStock + cantidad;
  } else {
    if (cantidad > currentStock) {
      showAlert("No hay suficiente stock para la salida solicitada", "danger");
      return;
    }
    newStock = currentStock - cantidad;
  }

  if (stockInfo) {
    stockInfo.cantidad = newStock;
  } else {
    stock.push({
      id: generateId(stock),
      idProducto: productId,
      cantidad: newStock,
    });
  }

  saveData("stock", stock);

  const movements = JSON.parse(localStorage.getItem("stockMovements") || "[]");
  const movement = {
    id: generateId(movements),
    idProducto: productId,
    tipo,
    cantidad,
    stockAnterior: currentStock,
    stockResultante: newStock,
    motivo,
    fecha: new Date().toISOString(),
    usuario: "Admin",
  };

  movements.unshift(movement);
  localStorage.setItem("stockMovements", JSON.stringify(movements));

  loadStock();
  loadAlerts();
  closeModal("movementModal");

  const producto = getRecord("productos", productId);
  showAlert(
    `Movimiento de stock registrado correctamente para ${producto.nombreProducto}`,
    "success"
  );

  if (typeof addActivity === "function") {
    const activityDesc = `${tipo.toUpperCase()}: ${cantidad} unidades de ${
      producto.nombreProducto
    }`;
    addActivity("stock", "Movimiento de stock", activityDesc);
  }
}

window.openMovementModal = openMovementModal;
window.quickEntry = quickEntry;
window.quickExit = quickExit;
window.viewHistory = viewHistory;
