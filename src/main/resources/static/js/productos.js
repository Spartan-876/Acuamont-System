
$(document).ready(function () {
    // Variables globales
    let dataTable;
    let isEditing = false;
    let productoModal;

    // Configuración inicial
    const API_BASE = '/productos/api';
    const ENDPOINTS = {
        list: `${API_BASE}/listar`,
        save: `${API_BASE}/guardar`,
        get: (id) => `${API_BASE}/${id}`,
        delete: (id) => `${API_BASE}/eliminar/${id}`,
        categorias: `${API_BASE}/categorias`,
        toggleStatus: (id) => `${API_BASE}/cambiar-estado/${id}`,
    };

    // Inicializar Componentes
    initializeDataTable();
    productoModal = new bootstrap.Modal(document.getElementById('productoModal'));

    loadCategorias();

    // Event Listeners
    setupEventListeners();

    function initializeDataTable() {
        dataTable = $('#tablaProductos').DataTable({
            responsive: true,
            processing: true,
            deferRender: true,
            ajax: {
                url: ENDPOINTS.list,
                dataSrc: 'data'
            },
            columns: [
                { data: 'id' },
                {
                    data: 'imagen', render: function (data, type, row) {
                        return `<img src="/Fotos-Productos/${data}" class=" w-100 h-100 img-fluid">`;
                    }
                },
                { data: 'nombre' },
                { data: 'descripcion' },
                {
                    data: 'categoria',
                    render: function (data, type, row) {
                        return data ? data.nombre : '';
                    }
                },
                {
                    data: 'precioCompra',
                    render: function (data, type, row) {
                        return new Intl.NumberFormat('es-PE',{
                            style: 'currency', currency: 'PEN'
                        }).format(data);
                    }
                },
                {
                    data: 'precioVenta',
                    render: function (data, type, row) {
                        return new Intl.NumberFormat('es-PE',{
                            style: 'currency', currency: 'PEN'
                        }).format(data);
                    }
                },
                { data: 'stock' },
                { data: 'stockSeguridad' },
                {
                    data: 'estado', render: function (data, type, row) {
                        return data === 1
                            ? '<span class="badge text-bg-success">Activo</span>'
                            : '<span class="badge text-bg-danger">Inactivo</span>';
                    }
                },
                {
                    data: null,
                    orderable: false,
                    searchable: false,
                    render: function (data, type, row) {
                        return createActionButtons(row);
                    }
                }
            ],
            columnDefs: [
                { responsivePriority: 1, targets: 1 },
                { responsivePriority: 2, targets: 2 },
                { responsivePriority: 3, targets: 3 },
                { responsivePriority: 4, targets: 4 },
                { responsivePriority: 5, targets: 5 },
                { responsivePriority: 6, targets: 6 },
                { responsivePriority: 7, targets: 7 },
                { responsivePriority: 8, targets: 8 },
                { responsivePriority: 9, targets: 9 },
                { responsivePriority: 10, targets: 10 },
            ],
            language: {
                url: '//cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json'
            },
            pageLength: 10
        });
    }

    function createActionButtons(row) {
        const statusIcon = row.estado === 1
            ? '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-eye-slash-fill" viewBox="0 0 16 16"><path d="m10.79 12.912-1.614-1.615a3.5 3.5 0 0 1-4.474-4.474l-2.06-2.06C.938 6.278 0 8 0 8s3 5.5 8 5.5a7.029 7.029 0 0 0 2.79-.588M5.21 3.088A7.028 7.028 0 0 1 8 2.5c5 0 8 5.5 8 5.5s-.939 1.721-2.641 3.238l-2.062-2.062a3.5 3.5 0 0 0-4.474-4.474L5.21 3.089z"/><path d="M5.525 7.646a2.5 2.5 0 0 0 2.829 2.829l-2.83-2.829zm4.95.708-2.829-2.83a2.5 2.5 0 0 1 2.829 2.829zm3.171 6-12-12 .708-.708 12 12z"/></svg>'
            : '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-eye-fill" viewBox="0 0 16 16"><path d="M10.5 8a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0"/><path d="M0 8s3-5.5 8-5.5S16 8 16 8s-3 5.5-8 5.5S0 8 0 8m8 3.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7"/></svg>';

        const statusClass = row.estado === 1 ? 'action-btn-status-deactivate' : 'action-btn-status-activate';
        const statusTitle = row.estado === 1 ? 'Desactivar' : 'Activar';

        return `
            <div class="d-flex gap-1">
                <button data-id="${row.id}" class="btn btn-sm btn-primary action-edit" title="Editar">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil-square" viewBox="0 0 16 16"><path d="M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z"/><path fill-rule="evenodd" d="M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5z"/></svg>
                </button>
                <button data-id="${row.id}" class="btn btn-sm ${row.estado ? 'btn-warning' : 'btn-success'} action-status" title="${statusTitle}">
                    ${statusIcon}
                </button>
                <button data-id="${row.id}" class="btn btn-sm btn-danger action-delete" title="Eliminar">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash3-fill" viewBox="0 0 16 16"><path d="M11 1.5v1h3.5a.5.5 0 0 1 0 1h-.538l-.853 10.66A2 2 0 0 1 11.115 16h-6.23a2 2 0 0 1-1.994-1.84L2.038 3.5H1.5a.5.5 0 0 1 0-1H5v-1A1.5 1.5 0 0 1 6.5 0h3A1.5 1.5 0 0 1 11 1.5m-5 0v1h4v-1a.5.5 0 0 0-.5-.5h-3a.5.5 0 0 0-.5.5M4.5 5.029l.5 8.5a.5.5 0 1 0 .998-.06l-.5-8.5a.5.5 0 1 0-.998.06m6.53-.528a.5.5 0 0 0-.528.47l-.5 8.5a.5.5 0 0 0 .998.058l.5-8.5a.5.5 0 0 0-.47-.528M8 4.5a.5.5 0 0 0-.5.5v8.5a.5.5 0 0 0 1 0V5a.5.5 0 0 0-.5-.5"/></svg>
                </button>
            </div>
        `;
    }

    function setupEventListeners() {
        // Botón nuevo registro
        $('#btnNuevoRegistro').on('click', openModalForNew);

        // No es necesario un listener para cerrar el modal, Bootstrap lo maneja con data-bs-dismiss

        // Submit form
        $('#formProducto').on('submit', function (e) {
            e.preventDefault();
            saveProducto();
        });

        // Eventos de la tabla (delegados)
        $('#tablaProductos tbody').on('click', '.action-edit', handleEdit);
        $('#tablaProductos tbody').on('click', '.action-status', handleToggleStatus);
        $('#tablaProductos tbody').on('click', '.action-delete', handleDelete);
    }

    function loadProductos() {
        dataTable.ajax.reload();
    }

    function loadCategorias() {
        fetch(ENDPOINTS.categorias)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const select = $('#id_categoria');
                    select.empty().append('<option value="">Seleccione una categoría...</option>');
                    data.data.forEach(categoria => {
                        select.append(`<option value="${categoria.id}">${categoria.nombre}</option>`);
                    });
                } else {
                    showNotification('Error al cargar categorias', 'error');
                }

            }).catch(error => {
                console.error('Error cargando categorias:', error);
            });
    }

    function saveProducto() {
        const form = document.getElementById("formProducto");
        const formData = new FormData(form);

        if (!validateForm(formData)) return;
        for (let pair of formData.entries()) {
            console.log(pair[0], pair[1]);
        }

        fetch(ENDPOINTS.save, {
            method: "POST",
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showNotification(data.message, "success");

                // Ocultar el modal
                const modal = bootstrap.Modal.getInstance(document.getElementById("productoModal"));
                modal.hide();

                // Limpiar formulario e imagen preview
                form.reset();
                document.getElementById("imagenPreview").src = "https://via.placeholder.com/150";

                // Recargar DataTable
                if (dataTable) {
                    dataTable.ajax.reload(null, false); // false para no reiniciar la paginación
                }
            } else {
                showNotification(data.message, "error");
            }
        })
        .catch(error => {
            console.error("Error:", error);
            showNotification("Error al guardar el producto", "error");
        });
    }

    // Previsualización de la imagen antes de subir
    document.getElementById("imagen").addEventListener("change", function(event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                document.getElementById("imagenPreview").src = e.target.result;
            };
            reader.readAsDataURL(file);
        } else {
            document.getElementById("imagenPreview").src = "https://via.placeholder.com/150";
        }
    });


    function handleEdit(e) {
        e.preventDefault();
        const id = $(this).data('id');

        showLoading(true);

        fetch(ENDPOINTS.get(id))
            .then(response => {
                if (!response.ok) {
                    throw new Error('Producto no encontrado');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    openModalForEdit(data.data);
                } else {
                    showNotification('Error al cargar producto: ' + data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error al cargar los datos del producto', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }


    function handleToggleStatus(e) {
        e.preventDefault();
        const id = $(this).data('id');

        showLoading(true);

        fetch(ENDPOINTS.toggleStatus(id), {
            method: 'POST'
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showNotification(data.message, 'success');
                    loadProductos(); // Recargar la tabla
                } else {
                    showNotification('Error: ' + data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error de conexión al cambiar estado', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }


    function handleDelete(e) {
        e.preventDefault();

        const id = $(this).data('id');

        Swal.fire({
            title: '¿Estás seguro?',
            text: "¡No podrás revertir esta acción!",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Sí, ¡eliminar!',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                showLoading(true);

                fetch(ENDPOINTS.delete(id), {
                    method: 'DELETE'
                })
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            showNotification(data.message, 'success');
                            loadProductos(); // Recargar la tabla
                        } else {
                            showNotification('Error: ' + data.message, 'error');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        showNotification('Error de conexión al eliminar el producto', 'error');
                    })
                    .finally(() => {
                        showLoading(false);
                    });
            }
        });
    }

    function openModalForNew() {
        isEditing = false;
        clearForm();
        $('#modalTitle').text('Agregar Producto');
        showModal();
    }

    function openModalForEdit(producto) {
        isEditing = true;
        clearForm();
        $('#modalTitle').text('Editar Producto');

        $('#id').val(producto.id);
        $('#nombre').val(producto.nombre);
        $('#descripcion').val(producto.descripcion);
        $('#precio_compra').val(producto.precioCompra);
        $('#precio_venta').val(producto.precioVenta);
        $('#stock').val(producto.stock);
        $('#stock_seguridad').val(producto.stockSeguridad);

        // Previsualizar imagen existente
        $('#imagenPreview').attr('src', producto.imagen ? producto.imagen : "https://via.placeholder.com/150");

        showModal();
    }


    function showModal() {
        productoModal.show();
    }

    function hiddenModal() {
        productoModal.hide();
        clearForm();
    }

    function clearForm() {
        $('#formProducto')[0].reset();
        $('#formProducto .form-control').removeClass('is-invalid');
        $('.invalid-feedback').text('');
        isEditing = false;
    }

    function validateForm(formData) {
        let hasErrors = false;

        clearFieldErrors();

        const nombre = formData.get("nombre");
        const descripcion = formData.get("descripcion");
        const precioCompra = Number(formData.get("precioCompra"));
        const precioVenta = Number(formData.get("precioVenta"));
        const stock = Number(formData.get("stock"));
        const stockSeguridad = Number(formData.get("stockSeguridad"));
        const imagen = formData.get("imagen");
        const categoriaId = formData.get("id_categoria");

        if (!nombre || nombre.length < 2) {
            showFieldError('nombre', 'El nombre es obligatorio y debe tener al menos 2 caracteres');
            hasErrors = true;
        }

        if (!descripcion || descripcion.length < 2) {
            showFieldError('descripcion', 'La descripción es obligatoria y debe tener al menos 2 caracteres');
            hasErrors = true;
        }

        if (isNaN(precioCompra) || precioCompra < 0) {
            showFieldError('precio_compra', 'El precio de compra es obligatorio y debe ser >= 0');
            hasErrors = true;
        }

        if (isNaN(precioVenta) || precioVenta < 0) {
            showFieldError('precio_venta', 'El precio de venta es obligatorio y debe ser >= 0');
            hasErrors = true;
        }

        if (isNaN(stock) || stock < 0) {
            showFieldError('stock', 'El stock es obligatorio y debe ser >= 0');
            hasErrors = true;
        }

        if (isNaN(stockSeguridad) || stockSeguridad < 0) {
            showFieldError('stock_seguridad', 'El stock de seguridad es obligatorio y debe ser >= 0');
            hasErrors = true;
        }

        if (!categoriaId) {
            showFieldError('id_categoria', 'Debe seleccionar una categoría');
            hasErrors = true;
        }

        return !hasErrors;
    }


    function clearFieldErrors() {
        $('.invalid-feedback').text('');
        $('.form-control').removeClass('is-invalid');
    }

    function showNotification(message, type = 'success') {
        const toastClass = type === 'success' ? 'text-bg-success' : 'text-bg-danger';

        const notification = $(`
            <div class="toast align-items-center ${toastClass} border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">
                        ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        `);

        $('#notification-container').append(notification);

        const toast = new bootstrap.Toast(notification, {
            delay: 5000
        });
        toast.show();
    }

    function showLoading(show) {
        const overlayId = 'loading-overlay';
        const $overlay = $(`#${overlayId}`);

        if (show) {
            if ($overlay.length === 0) {
                const spinner = $('<div>', { class: 'spinner-border text-primary', role: 'status' })
                    .append($('<span>', { class: 'visually-hidden' }).text('Loading...'));
                const newOverlay = $('<div>', { id: overlayId, class: 'loading-overlay' })
                    .append(spinner);
                $('body').append(newOverlay);
            }
        } else {
            $overlay.remove();
        }
    }

    function showFieldError(fieldName, message) {
            const field = $(`#${fieldName}`);
            const errorDiv = $(`#${fieldName}-error`);

            field.addClass('is-invalid');
            errorDiv.text(message);
    }

});