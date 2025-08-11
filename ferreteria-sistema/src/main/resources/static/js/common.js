function generateId(items) {
  return items.length > 0 ? Math.max(...items.map((item) => item.id)) + 1 : 1;
}

function validateEmail(email) {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
}

function validatePhone(phone) {
  const re = /^[\d\-\+\(\)\s]+$/;
  return re.test(phone) && phone.length >= 8;
}

// --- API helpers (fetch to Spring controllers) ---
function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
  return null;
}

function getCsrfHeader() {
  const token = getCookie('XSRF-TOKEN');
  return token ? { 'X-XSRF-TOKEN': token } : {};
}

function buildApiUrl(url) {
  if (/^https?:\/\//i.test(url)) return url;
  // Prepend context path if app is deployed under one (e.g., /ferreteria)
  const segments = window.location.pathname.split('/').filter(Boolean);
  const context = segments.length > 0 ? `/${segments[0]}` : '';
  return `${context}${url}`;
}

const DEBUG_API = true;

async function apiFetch(url, options = {}) {
  const defaultHeaders = { 
    'Accept': 'application/json',
    'Cache-Control': 'no-cache, no-store, must-revalidate',
    'Pragma': 'no-cache',
    'Expires': '0'
  };
  const method = (options.method || 'GET').toUpperCase();
  const csrfHeaders = ['POST', 'PUT', 'PATCH', 'DELETE'].includes(method) ? getCsrfHeader() : {};
  const headers = { ...defaultHeaders, ...(options.headers || {}), ...csrfHeaders };

  const finalOptions = {
    credentials: 'same-origin',
    ...options,
    headers,
  };

  const builtUrl = buildApiUrl(url);
  if (DEBUG_API) console.log('[API] request', { method, url: builtUrl, options: finalOptions });
  const response = await fetch(builtUrl, finalOptions);
  let data;
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    data = await response.json().catch(() => null);
  } else {
    data = await response.text().catch(() => null);
  }

  if (!response.ok) {
    const err = new Error((data && data.mensaje) || response.statusText || 'Request error');
    err.status = response.status;
    err.data = data;
    if (DEBUG_API) console.error('[API] error', { method, url: builtUrl, status: response.status, data });
    throw err;
  }
  // Normalizar respuestas sin cuerpo como éxito
  if (data === null || data === '') {
    const normalized = { ok: true, status: response.status };
    if (DEBUG_API) console.log('[API] response (empty body normalized)', normalized);
    return normalized;
  }
  if (DEBUG_API) console.log('[API] response', { status: response.status, data });
  return data;
}

function apiGet(url) { return apiFetch(url, { method: 'GET' }); }
function apiPost(url, body) {
  return apiFetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
}
function apiPut(url, body) {
  return apiFetch(url, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
}
function apiDelete(url) { return apiFetch(url, { method: 'DELETE' }); }

function showAlert(message, type = "info") {
  const alertDiv = document.createElement("div");
  alertDiv.className = `alert alert-${type}`;
  alertDiv.textContent = message;

  const container = document.querySelector(".page-content");
  container.insertBefore(alertDiv, container.firstChild);

  setTimeout(() => {
    alertDiv.remove();
  }, 5000);
}

function confirmDelete(
  message = "¿Estás seguro de que deseas eliminar este elemento?"
) {
  return confirm(message);
}

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add("active");
    document.body.style.overflow = "hidden";
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove("active");
    document.body.style.overflow = "auto";
  }
}

function clearForm(formId) {
  const form = document.getElementById(formId);
  if (form) {
    form.reset();
    const idField = form.querySelector('[name="id"]');
    if (idField) {
      idField.value = "";
    }
  }
}

function saveData(key, data) {
  localStorage.setItem(key, JSON.stringify(data));
}

function loadData(key) {
  return JSON.parse(localStorage.getItem(key) || "[]");
}

function addRecord(key, record) {
  const data = loadData(key);
  data.push(record);
  saveData(key, data);
  return record;
}

function updateRecord(key, updatedRecord) {
  const data = loadData(key);
  const index = data.findIndex((item) => item.id === updatedRecord.id);
  if (index !== -1) {
    data[index] = updatedRecord;
    saveData(key, data);
    return updatedRecord;
  }
  return null;
}

function deleteRecord(key, id) {
  const data = loadData(key);
  const filteredData = data.filter((item) => item.id !== parseInt(id));
  saveData(key, filteredData);
  return filteredData;
}

function getRecord(key, id) {
  const data = loadData(key);
  return data.find((item) => item.id === parseInt(id));
}

function renderTable(containerId, data, columns, actions = []) {
  const container = document.getElementById(containerId);
  if (!container) return;

  let html = `
        <div class="table-container">
            <table class="table">
                <thead>
                    <tr>
                        ${columns
                          .map((col) => `<th>${col.label}</th>`)
                          .join("")}
                        ${actions.length > 0 ? "<th>Acciones</th>" : ""}
                    </tr>
                </thead>
                <tbody>
    `;

  if (data.length === 0) {
    html += `
            <tr>
                <td colspan="${
                  columns.length + (actions.length > 0 ? 1 : 0)
                }" class="text-center">
                    No hay datos disponibles
                </td>
            </tr>
        `;
  } else {
    data.forEach((item) => {
      html += "<tr>";
      columns.forEach((col) => {
        let value = item[col.key];
        if (col.format) {
          value = col.format(value, item);
        }
        html += `<td>${value || "-"}</td>`;
      });

      if (actions.length > 0) {
        html += "<td>";
        actions.forEach((action) => {
          html += `<button class="btn ${action.class}" onclick="${action.onclick}(${item.id})">${action.label}</button> `;
        });
        html += "</td>";
      }
      html += "</tr>";
    });
  }

  html += `
                </tbody>
            </table>
        </div>
    `;

  container.innerHTML = html;
}

function validateForm(formId, rules) {
  const form = document.getElementById(formId);
  if (!form) return false;

  let isValid = true;
  const errors = [];

  rules.forEach((rule) => {
    const field = form.querySelector(`[name="${rule.field}"]`);
    if (!field) return;

    const value = field.value.trim();

    if (rule.required && !value) {
      errors.push(`${rule.label} es requerido`);
      field.classList.add("error");
      isValid = false;
    } else {
      field.classList.remove("error");
    }

    if (value && rule.type) {
      switch (rule.type) {
        case "email":
          if (!validateEmail(value)) {
            errors.push(`${rule.label} debe ser un email válido`);
            field.classList.add("error");
            isValid = false;
          }
          break;
        case "phone":
          if (!validatePhone(value)) {
            errors.push(`${rule.label} debe ser un teléfono válido`);
            field.classList.add("error");
            isValid = false;
          }
          break;
        case "number":
          if (isNaN(value) || parseFloat(value) < 0) {
            errors.push(`${rule.label} debe ser un número válido`);
            field.classList.add("error");
            isValid = false;
          }
          break;
        case "min":
          if (value.length < rule.minLength) {
            errors.push(
              `${rule.label} debe tener al menos ${rule.minLength} caracteres`
            );
            field.classList.add("error");
            isValid = false;
          }
          break;
      }
    }
  });

  if (!isValid) {
    showAlert(errors.join(", "), "danger");
  }

  return isValid;
}

document.addEventListener("DOMContentLoaded", function () {
  document.addEventListener("click", function (e) {
    if (
      e.target.classList.contains("modal-close") ||
      e.target.classList.contains("modal")
    ) {
      const modal = e.target.closest(".modal");
      if (modal) {
        modal.classList.remove("active");
        document.body.style.overflow = "auto";
      }
    }
  });

  document.addEventListener("click", function (e) {
    if (e.target.classList.contains("modal-content")) {
      e.stopPropagation();
    }
  });

  const currentPath = window.location.pathname;
  const sidebarLinks = document.querySelectorAll(".sidebar-nav a");

  sidebarLinks.forEach((link) => {
    link.classList.remove("active");
    const href = link.getAttribute("href");
    if (!href) return;
    try {
      const url = new URL(href, window.location.origin);
      if (url.pathname === currentPath) {
        link.classList.add("active");
      }
    } catch (e) {
      // ignore invalid URLs
    }
  });
});

function formatCurrency(amount) {
  return new Intl.NumberFormat("es-AR", {
    style: "currency",
    currency: "ARS",
  }).format(amount);
}

function searchTable(searchInputId, tableBodySelector) {
  const searchInput = document.getElementById(searchInputId);
  const tableRows = document.querySelectorAll(`${tableBodySelector} tr`);

  if (!searchInput) return;

  searchInput.addEventListener("input", function () {
    const searchTerm = this.value.toLowerCase();

    tableRows.forEach((row) => {
      const text = row.textContent.toLowerCase();
      if (text.includes(searchTerm)) {
        row.style.display = "";
      } else {
        row.style.display = "none";
      }
    });
  });
}

function logActivity(message, type = "info") {
  const activities = loadData("activities") || [];
  const activity = {
    id: generateId(),
    message: message,
    type: type,
    timestamp: new Date().toISOString(),
    user: "Admin",
  };

  activities.unshift(activity);

  if (activities.length > 100) {
    activities.splice(100);
  }

  saveData("activities", activities);
}

function saveRecord(key, record) {
  const result = addRecord(key, record);
  if (result) {
    logActivity(
      `Nuevo registro creado en ${key}: ${
        record.nombre ||
        record.numeroFactura ||
        record.numeroPedido ||
        "ID " + record.id
      }`,
      "success"
    );
  }
  return result;
}

window.generateId = generateId;
window.validateEmail = validateEmail;
window.validatePhone = validatePhone;
window.showAlert = showAlert;
window.confirmDelete = confirmDelete;
window.openModal = openModal;
window.closeModal = closeModal;
window.clearForm = clearForm;
window.saveData = saveData;
window.loadData = loadData;
window.addRecord = addRecord;
window.updateRecord = updateRecord;
window.deleteRecord = deleteRecord;
window.getRecord = getRecord;
window.renderTable = renderTable;
window.validateForm = validateForm;
window.formatCurrency = formatCurrency;
window.searchTable = searchTable;
window.logActivity = logActivity;
window.saveRecord = saveRecord;