let currentProductoId = null;
let currentProductoData = null;
const DEBUG_PRODUCTOS = true;

document.addEventListener("DOMContentLoaded", async function () {
  await loadProductos();
  await loadProveedores();
  initializeProductosPage();
});

function initializeProductosPage() {
  searchTable("searchProductos", "#productosTable tbody");
  loadProveedores();

  const form = document.getElementById("productoForm");
  form.addEventListener("submit", handleFormSubmit);

  // Initialize logout functionality
  initializeLogout();
}

async function loadProductos() {
  try {
    // Agregar timestamp para evitar cache
    const cacheBuster = `?_t=${Date.now()}`;
    const productos = await apiGet(`/api/productos${cacheBuster}`);
    
    if (DEBUG_PRODUCTOS) console.log('[Productos] productos desde API', productos);
    renderProductosTable(productos);
  } catch (e) {
    if (DEBUG_PRODUCTOS) console.error('[Productos] error loadProductos', e);
    showAlert(`Error cargando productos: ${e.message}`, 'danger');
  }
}

async function loadProveedores() {
  try {
    const proveedores = await apiGet('/api/proveedores');
    const select = document.getElementById("IdProveedor");
    if (!select) return;
    const currentVal = select.value;
    select.innerHTML = '<option value="">Seleccione un proveedor</option>';
    proveedores.forEach((proveedor) => {
      const option = document.createElement("option");
      option.value = (proveedor.idProveedor || proveedor.id) + '';
      option.textContent = proveedor.nombreProveedor;
      select.appendChild(option);
    });
    if (currentVal) select.value = currentVal; // mantener selección si se recarga
  } catch (e) {
    // opcional: showAlert
  }
}

function renderProductosTable(productos) {
  const columns = [
    { key: "nombreProducto", label: "Nombre" },
    {
      key: "descripcion",
      label: "Descripción",
      format: (value) =>
        (value && value.length > 50) ? value.substring(0, 50) + "..." : (value || ""),
    },
    {
      key: "proveedor", 
      label: "Proveedor",
      format: (value, item) => {
        // Mostrar nombre del proveedor si está disponible
        if (item.proveedor && item.proveedor.nombreProveedor) {
          return item.proveedor.nombreProveedor;
        }
        // Si solo tiene ID, mostrar "Proveedor #ID"
        if (item.idProveedor) {
          return `Proveedor #${item.idProveedor}`;
        }
        return "Sin proveedor";
      },
    },
    {
      key: "precio",
      label: "Precio",
      format: (value) => formatCurrency(value || 0),
    },
    {
      key: "cantidadStock",
      label: "Stock",
      format: (value, item) => {
        const cantidad = value || (item.stock && item.stock.cantidad) || 0;
        const className =
          cantidad === 0
            ? "text-danger"
            : cantidad < 10
            ? "text-warning"
            : "text-success";
        return `<span class="${className}">${cantidad} unidades</span>`;
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

  // Mapear productos para que tengan el campo 'id' que espera renderTable
  const productosConId = productos.map(p => ({ ...p, id: p.idProducto }));
  renderTable("productosTable", productosConId, columns, actions);
}

function openAddModal() {
  document.getElementById("modalTitle").textContent = "Nuevo Producto";
  clearForm("productoForm");
  currentProductoId = null;
  openModal("productoModal");
}

async function editProducto(id) {
  // Cargar desde API para edición
  try {
    const producto = await apiGet(`/api/productos/${id}`);
    currentProductoData = producto;
    if (DEBUG_PRODUCTOS) console.log('[Productos] editProducto carga', { id, producto });

    document.getElementById("modalTitle").textContent = "Editar Producto";
    currentProductoId = id;

    const form = document.getElementById("productoForm");
    if (form.querySelector('[name="id"]')) form.querySelector('[name="id"]').value = producto.idProducto || producto.id;
    const nombreInput = form.querySelector('[name="nombreProducto"]');
    const descInput = form.querySelector('[name="descripcion"]');
    const codigoInput = form.querySelector('[name="codigo"]');
    const precioInput = form.querySelector('[name="precio"]');
    const categoriaSel = form.querySelector('[name="categoria"]');
    if (nombreInput) nombreInput.value = producto.nombreProducto || '';
    if (descInput) descInput.value = producto.descripcion || '';
    if (codigoInput) codigoInput.value = producto.codigoProducto || '';
    if (precioInput) precioInput.value = (producto.precio != null ? producto.precio : 0);
    if (categoriaSel) categoriaSel.value = (producto.categoria || '').toUpperCase();
    
    // Cargar proveedores y setear el valor del proveedor
    const provSel = form.querySelector('[name="IdProveedor"]');
    if (provSel) {
      await loadProveedores();
      
      // Obtener el ID del proveedor (puede venir en diferentes campos)
      const provId = producto.proveedor?.idProveedor || producto.idProveedor || producto.IdProveedor;
      
      if (DEBUG_PRODUCTOS) {
        console.log('[Productos] configurando proveedor', {
          'producto completo': producto,
          'producto.proveedor': producto.proveedor,
          'producto.idProveedor': producto.idProveedor,
          'producto.IdProveedor': producto.IdProveedor,
          'todas las propiedades': Object.keys(producto),
          provId: provId,
          'opciones disponibles': Array.from(provSel.options).map(opt => ({ value: opt.value, text: opt.textContent }))
        });
      }
      
      // Setear el valor si existe
      if (provId) {
        provSel.value = provId.toString();
        if (DEBUG_PRODUCTOS) console.log('[Productos] proveedor seleccionado:', provId);
      } else {
        provSel.value = '';
        if (DEBUG_PRODUCTOS) console.log('[Productos] sin proveedor asignado');
      }
    }

    closeModal("viewProductoModal");
    openModal("productoModal");
  } catch (e) {
    showAlert("No se pudo cargar el producto", "danger");
  }
}

async function viewProducto(id) {
  try {
    const producto = await apiGet(`/api/productos/${id}`);
    
    if (!producto) {
      showAlert("Producto no encontrado", "danger");
      return;
    }

    currentProductoId = id;

    const cantidadStock = producto.cantidadStock || (producto.stock && producto.stock.cantidad) || 0;

    const detailsHtml = `
        <div class="producto-details">
            <div class="detail-row">
                <strong>ID:</strong> ${producto.idProducto}
            </div>
            <div class="detail-row">
                <strong>Código:</strong> ${producto.codigoProducto || 'N/A'}
            </div>
            <div class="detail-row">
                <strong>Nombre:</strong> ${producto.nombreProducto}
            </div>
            <div class="detail-row">
                <strong>Descripción:</strong> ${producto.descripcion || 'N/A'}
            </div>
            <div class="detail-row">
                <strong>Categoría:</strong> ${producto.categoria || 'N/A'}
            </div>
            <div class="detail-row">
                <strong>Marca:</strong> ${producto.marca || 'N/A'}
            </div>
            <div class="detail-row">
                <strong>Precio:</strong> ${formatCurrency(producto.precio || 0)}
            </div>
            <div class="detail-row">
                <strong>Precio Compra:</strong> ${formatCurrency(producto.precioCompra || 0)}
            </div>
            <div class="detail-row">
                <strong>Stock:</strong> 
                <span class="${
                  cantidadStock === 0
                    ? "text-danger"
                    : cantidadStock < 10
                    ? "text-warning"
                    : "text-success"
                }">
                    ${cantidadStock} ${producto.unidadMedida || 'unidades'}
                </span>
            </div>
            <div class="detail-row">
                <strong>Stock Mínimo:</strong> ${producto.stockMinimo || 0}
            </div>
            <div class="detail-row">
                <strong>Proveedor:</strong> ${
                  producto.proveedor?.nombreProveedor || 
                  (producto.idProveedor ? `Proveedor #${producto.idProveedor}` : 'Sin proveedor')
                }
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
  } catch (e) {
    if (DEBUG_PRODUCTOS) console.error('[Productos] error viewProducto', e);
    showAlert("No se pudo cargar el producto", "danger");
  }
}

function editFromView() {
  closeModal("viewProductoModal");
  editProducto(currentProductoId);
}

function manageStock() {
  if (currentProductoId) {
    closeModal("viewProductoModal");
    window.location.href = `/stock?producto=${currentProductoId}`;
  }
}

async function deleteProducto(id) {
  try {
    // Obtener datos del producto desde la API
    const producto = await apiGet(`/api/productos/${id}`);
    if (!producto) {
      showAlert("Producto no encontrado", "danger");
      return;
    }

    if (
      confirmDelete(
        `¿Estás seguro de que deseas eliminar el producto "${producto.nombreProducto}"?`
      )
    ) {
      if (DEBUG_PRODUCTOS) console.log('[Productos] eliminando producto', id);
      
      // Llamar a la API para eliminar
      await apiDelete(`/api/productos/${id}`);
      
      // Recargar la tabla
      await loadProductos();
      showAlert("Producto eliminado correctamente", "success");

      if (typeof addActivity === "function") {
        addActivity(
          "producto",
          "Producto eliminado",
          `${producto.nombreProducto} fue eliminado del catálogo`
        );
      }
    }
  } catch (e) {
    if (DEBUG_PRODUCTOS) console.error('[Productos] error al eliminar', e);
    if (e.status === 404) {
      showAlert("Producto no encontrado", "danger");
    } else if (e.status === 409) {
      showAlert("No se puede eliminar el producto porque está siendo usado en facturas o pedidos", "danger");
    } else {
      showAlert(e.data?.mensaje || `Error al eliminar: ${e.message}`, "danger");
    }
  }
}

async function handleFormSubmit(e) {
  e.preventDefault();

  const validationRules = [
    { field: "nombreProducto", label: "Nombre", required: true, minLength: 3 },
    { field: "codigo", label: "Código", required: true, minLength: 3 },
    {
      field: "descripcion",
      label: "Descripción",
      required: true,
      minLength: 10,
    },
    { field: "precio", label: "Precio", required: true, type: "number" },
    { field: "IdProveedor", label: "Proveedor", required: true },
    // Si el formulario tiene campo categoría, validar también
    // { field: "categoria", label: "Categoría", required: true },
  ];

  if (!validateForm("productoForm", validationRules)) {
    return;
  }

  const formData = new FormData(e.target);
  const provIdStr = formData.get("IdProveedor");
  const precioVal = parseFloat(formData.get("precio") || (currentProductoData?.precio || 0));
  const precioCompraRaw = formData.get("precioCompra");
  const precioCompraParsed = precioCompraRaw ? parseFloat(precioCompraRaw) : (currentProductoData?.precioCompra ?? null);
  const precioCompraFinal = (Number.isFinite(precioCompraParsed) && precioCompraParsed > 0)
    ? precioCompraParsed
    : (Number.isFinite(precioVal) && precioVal > 0 ? precioVal : 0.01);
  const productoData = {
    nombreProducto: (formData.get("nombreProducto") || currentProductoData?.nombreProducto || '').toString().trim(),
    descripcion: (formData.get("descripcion") || currentProductoData?.descripcion || '').toString().trim(),
    codigoProducto: (formData.get("codigo") || currentProductoData?.codigoProducto || '').toString().trim(),
    categoria: ((formData.get("categoria") || currentProductoData?.categoria || 'OTROS') + '').toUpperCase(),
    marca: (formData.get("marca") || currentProductoData?.marca || null),
    precio: precioVal,
    precioCompra: precioCompraFinal,
    unidadMedida: (formData.get("unidadMedida") || currentProductoData?.unidadMedida || 'UNIDAD'),
    stockMinimo: parseInt(formData.get("stockMinimo") || (currentProductoData?.stockMinimo || 0), 10),
    proveedor: provIdStr ? { idProveedor: parseInt(provIdStr, 10) } : (currentProductoData?.proveedor ? { idProveedor: currentProductoData.proveedor.idProveedor } : null),
  };
  if (DEBUG_PRODUCTOS) console.log('[Productos] submit payload', { currentProductoId, productoData });

  // Validación adicional: en creación, el código es obligatorio y único
  if (!currentProductoId && (!productoData.codigoProducto || productoData.codigoProducto.length < 3)) {
    showAlert("El código de producto es requerido (mín. 3 caracteres)", 'danger');
    return;
  }
  if (!Number.isFinite(productoData.precio) || productoData.precio <= 0) {
    showAlert("El precio debe ser mayor que 0", 'danger');
    return;
  }

  const productos = [];
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

  try {
    if (currentProductoId) {
      result = await apiPut(`/api/productos/${currentProductoId}`, productoData);
      actionType = "actualizado";
    } else {
      result = await apiPost('/api/productos', productoData);
      actionType = "agregado";
    }
  } catch (e) {
    if (DEBUG_PRODUCTOS) console.error('[Productos] error al guardar', e);
    showAlert(e.data?.mensaje || `Error al guardar: ${e.message}`, 'danger');
    return;
  }

  if (result) {
    if (DEBUG_PRODUCTOS) console.log('[Productos] guardado OK', { actionType });
    await loadProductos();
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
    if (DEBUG_PRODUCTOS) console.warn('[Productos] guardado sin resultado');
    showAlert("Error al guardar el producto", "danger");
  }
}

window.openAddModal = openAddModal;
window.editProducto = editProducto;
window.viewProducto = viewProducto;
window.deleteProducto = deleteProducto;
window.editFromView = editFromView;
window.manageStock = manageStock;


