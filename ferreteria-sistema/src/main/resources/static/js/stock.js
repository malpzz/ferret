// Stock Management JavaScript

document.addEventListener("DOMContentLoaded", function () {
  initializeStockPage();
  loadStock();
  loadProductsSelect();
});

function initializeStockPage() {
  searchTable("searchStock", "#stockTable tbody");

  const filterSelect = document.getElementById("filterStock");
  if (filterSelect) {
    filterSelect.addEventListener("change", filterStock);
  }

  const form = document.getElementById("movementForm");
  if (form) {
    form.addEventListener("submit", handleMovementSubmit);
  }

  // Initialize logout functionality
  initializeLogout();

  // Check for producto parameter in URL
  const urlParams = new URLSearchParams(window.location.search);
  const productoId = urlParams.get("producto");
  if (productoId) {
    setTimeout(() => {
      openMovementModal();
      const productoSelect = document.getElementById("producto");
      if (productoSelect) {
        productoSelect.value = productoId;
      }
    }, 500);
  }
}

async function loadStock() {
  try {
    console.log('DEBUG - Cargando stock desde API...');
    const stocks = await apiGet('/api/stock');
    console.log('DEBUG - Stocks recibidos:', stocks);
    
    window.stockData = stocks; // Guardar para uso posterior
    renderStockTable(stocks);
  } catch (e) {
    console.error('Error cargando stock:', e);
    showAlert(`Error cargando stock: ${e.message}`, 'danger');
  }
}

function renderStockTable(stockData) {
  const columns = [
    { 
      key: "nombreProducto", 
      label: "Producto",
      format: (value, item) => {
        const producto = item.productoInfo || {};
        return producto.nombreProducto || 'Sin nombre';
      }
    },
    { 
      key: "codigoProducto", 
      label: "Código",
      format: (value, item) => {
        const producto = item.productoInfo || {};
        return producto.codigoProducto || '-';
      }
    },
    { 
      key: "categoria", 
      label: "Categoría",
      format: (value, item) => {
        const producto = item.productoInfo || {};
        return producto.categoria || '-';
      }
    },
    {
      key: "cantidad",
      label: "Stock Actual",
      format: (value) => `${value || 0} uds`
    },
    {
      key: "stockMinimo",
      label: "Stock Mínimo",
      format: (value, item) => {
        const producto = item.productoInfo || {};
        return `${producto.stockMinimo || 0} uds`;
      }
    },
    {
      key: "estado",
      label: "Estado",
      format: (value, item) => {
        const producto = item.productoInfo || {};
        const stockMinimo = producto.stockMinimo || 0;
        const cantidad = item.cantidad || 0;
        
        if (cantidad === 0) {
          return '<span class="badge bg-danger">Sin Stock</span>';
        } else if (cantidad <= stockMinimo) {
          return '<span class="badge bg-warning">Bajo Mínimo</span>';
        } else {
          return '<span class="badge bg-success">Disponible</span>';
        }
      }
    },
    {
      key: "ubicacion",
      label: "Ubicación",
      format: (value) => value || 'ALMACEN PRINCIPAL'
    }
  ];

  const actions = [
    {
      label: "Movimiento",
      class: "btn-primary btn-sm",
      onclick: "openMovementModal",
      idField: "idStock"
    },
    {
      label: "Editar",
      class: "btn-secondary btn-sm",
      onclick: "editStock",
      idField: "idStock"
    }
  ];

  console.log('DEBUG - Renderizando tabla con stocks:', stockData.map(s => ({
    id: s.idStock,
    producto: s.productoInfo?.nombreProducto,
    cantidad: s.cantidad
  })));

  renderTable("stockTable", stockData, columns, actions);
}

async function loadProductsSelect() {
  try {
    const productos = await apiGet('/api/productos');
    
    const select = document.getElementById("producto");
    if (select) {
      select.innerHTML = '<option value="">Seleccione un producto</option>';
      
      productos.forEach((producto) => {
        const option = document.createElement("option");
        option.value = producto.idProducto || producto.id;
        option.textContent = `${producto.nombreProducto} - ${producto.categoria || ''}`;
        select.appendChild(option);
      });
    }
    
    const editSelect = document.getElementById("editProducto");
    if (editSelect) {
      editSelect.innerHTML = '<option value="">Seleccione un producto</option>';
      
      productos.forEach((producto) => {
        const option = document.createElement("option");
        option.value = producto.idProducto || producto.id;
        option.textContent = `${producto.nombreProducto} - ${producto.categoria || ''}`;
        editSelect.appendChild(option);
      });
    }
  } catch (e) {
    console.error('Error cargando productos:', e);
  }
}

function filterStock() {
  const filter = document.getElementById("filterStock").value;
  let filteredData = [...window.stockData];

  if (filter === "bajo-minimo") {
    filteredData = filteredData.filter(stock => {
      const producto = stock.productoInfo || {};
      return stock.cantidad <= (producto.stockMinimo || 0);
    });
  } else if (filter === "sin-stock") {
    filteredData = filteredData.filter(stock => stock.cantidad === 0);
  }

  renderStockTable(filteredData);
}

function openMovementModal(stockId = null) {
  if (stockId && window.stockData) {
    const stock = window.stockData.find(s => s.idStock == stockId);
    if (stock && stock.productoInfo) {
      const productoSelect = document.getElementById("producto");
      if (productoSelect) {
        productoSelect.value = stock.productoInfo.idProducto;
      }
    }
  }
  
  // Limpiar el formulario
  const form = document.getElementById("movementForm");
  if (form) {
    form.reset();
    if (stockId && window.stockData) {
      const stock = window.stockData.find(s => s.idStock == stockId);
      if (stock) {
        document.getElementById("producto").value = stock.productoInfo?.idProducto || '';
      }
    }
  }
  
  openModal("movementModal");
}

function editStock(stockId) {
  const stock = window.stockData?.find(s => s.idStock == stockId);
  if (!stock) {
    showAlert("Stock no encontrado", "danger");
    return;
  }

  // Poblar el formulario de edición
  document.getElementById("editStockId").value = stock.idStock;
  document.getElementById("editCantidad").value = stock.cantidad;
  document.getElementById("editUbicacion").value = stock.ubicacion || '';
  
  const productoInfo = stock.productoInfo || {};
  document.getElementById("editProductoInfo").textContent = 
    `${productoInfo.nombreProducto} (${productoInfo.codigoProducto})`;

  openModal("editStockModal");
}

async function handleEditSubmit(e) {
  e.preventDefault();

  const formData = new FormData(e.target);
  const stockId = formData.get("editStockId");
  const cantidad = parseInt(formData.get("editCantidad"));
  const ubicacion = formData.get("editUbicacion");

  if (!stockId || cantidad < 0) {
    showAlert("Datos inválidos", "danger");
    return;
  }

  try {
    const updateData = {
      cantidad: cantidad,
      ubicacion: ubicacion || "ALMACEN PRINCIPAL"
    };

    await apiPut(`/api/stock/${stockId}`, updateData);
    showAlert("Stock actualizado correctamente", "success");
    
    closeModal("editStockModal");
    await loadStock();
  } catch (e) {
    console.error('Error actualizando stock:', e);
    showAlert(e.data?.mensaje || `Error actualizando stock: ${e.message}`, 'danger');
  }
}

async function handleMovementSubmit(e) {
  e.preventDefault();

  const formData = new FormData(e.target);
  const idProducto = parseInt(formData.get("producto"));
  const tipo = formData.get("tipoMovimiento");
  const cantidad = parseInt(formData.get("cantidad"));
  const motivo = formData.get("motivo").trim();

  if (!idProducto || !tipo || !cantidad || cantidad <= 0) {
    showAlert("Todos los campos obligatorios son requeridos", "danger");
    return;
  }

  try {
    const movementData = {
      idProducto: idProducto,
      tipo: tipo,
      cantidad: cantidad,
      motivo: motivo || `${tipo.toUpperCase()} de stock`
    };

    console.log('DEBUG - Enviando movimiento:', movementData);

    const response = await apiPost('/api/stock/movimiento', movementData);
    showAlert(response.mensaje || "Movimiento registrado correctamente", "success");
    
    closeModal("movementModal");
    document.getElementById("movementForm").reset();
    
    await loadStock();
  } catch (e) {
    console.error('Error en movimiento de stock:', e);
    showAlert(e.data?.mensaje || `Error en movimiento: ${e.message}`, 'danger');
  }
}

function quickEntry(productId, cantidad = 1) {
  openMovementModal();
  document.getElementById("producto").value = productId;
  document.getElementById("tipoMovimiento").value = "entrada";
  document.getElementById("cantidad").value = cantidad;
}

// Inicializar event listeners para formularios
document.addEventListener("DOMContentLoaded", function() {
  const editForm = document.getElementById("editStockForm");
  if (editForm) {
    editForm.addEventListener("submit", handleEditSubmit);
  }
});

// Funciones globales para uso en HTML
window.openMovementModal = openMovementModal;
window.quickEntry = quickEntry;
window.editStock = editStock;
window.filterStock = filterStock;