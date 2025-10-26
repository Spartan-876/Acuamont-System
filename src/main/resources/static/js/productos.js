
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
                        return new Intl.NumberFormat('es-PE', {
                            style: 'currency', currency: 'PEN'
                        }).format(data);
                    }
                },
                {
                    data: 'precioVenta',
                    render: function (data, type, row) {
                        return new Intl.NumberFormat('es-PE', {
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
                url: 'https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json'
            },
            pageLength: 10,
            lengthMenu: [10, 25, 50],
            dom: 'lBfrtip',
            buttons: [
                {
                    extend: 'excelHtml5',
                    text: '<i class="bi bi-file-earmark-excel"></i> Exportar a Excel',
                    title: 'Listado de Productos',
                    className: 'btn btn-success',
                    exportOptions: {
                        columns: [0, 2, 3, 4, 5, 6, 7, 8, 9],
                        modifier: {
                            page: 'all'
                        }
                    }
                },
                {
                    extend: 'pdfHtml5',
                    text: '<i class="bi bi-file-earmark-pdf"></i> Exportar a PDF',
                    title: 'Listado de Productos',
                    className: 'btn btn-danger',
                    orientation: 'landscape',
                    pageSize: 'A4',
                    exportOptions: {
                        columns: [0, 2, 3, 4, 5, 6, 7, 8, 9],
                        modifier: {
                            page: 'all'
                        }
                    }
                },
                {
                    extend: 'print',
                    text: '<i class="bi bi-printer"></i> Imprimir',
                    className: 'btn btn-info',
                    orientation: 'landscape',
                    pageSize: 'A4',
                    exportOptions: {
                        columns: [0, 2, 3, 4, 5, 6, 7, 8, 9],
                        modifier: {
                            page: 'all'
                        }
                    },
                    customize: function (win) {
                        $(win.document.body)
                            .css('font-size', '10pt')
                            .prepend('<h3 style="text-align:center;">Listado de Productos</h3>');
                    }
                },
                {
                    extend: 'colvis',
                    text: '<i class="bi bi-eye"></i> Mostrar/Ocultar',
                    className: 'btn btn-secondary'
                }
            ]
        });
    }

    function createActionButtons(row) {
        const statusIcon = row.estado === 1
            ? '<i class="bi bi-eye-slash-fill"></i>'
            : '<i class="bi bi-eye-fill"></i>';

        const statusClass = row.estado === 1 ? 'action-btn-status-deactivate' : 'action-btn-status-activate';
        const statusTitle = row.estado === 1 ? 'Desactivar' : 'Activar';

        return `
            <div class="d-flex gap-1">
                <button data-id="${row.id}" class="btn btn-sm btn-primary action-edit" title="Editar">
                    <i class="bi bi-pencil-square"></i>
                </button>
                <button data-id="${row.id}" class="btn btn-sm ${row.estado ? 'btn-warning' : 'btn-success'} action-status" title="${statusTitle}">
                    ${statusIcon}
                </button>
                <button data-id="${row.id}" class="btn btn-sm btn-danger action-delete" title="Eliminar">
                    <i class="bi bi-trash3-fill"></i>
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
    document.getElementById("imagen").addEventListener("change", function (event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
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