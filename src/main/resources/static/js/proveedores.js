$(document).ready(function () {
    let dataTable;
    let isEditing = false;
    let proveedorModal;

    const API_CLIENTES = '/clientes/api';
    const API_BASE_URL = '/proveedores/api';
    const API_MIAPI = 'http://localhost:3001';
    const ENDPOINTS = {
        list: `${API_BASE_URL}/listar`,
        save: `${API_BASE_URL}/guardar`,
        get: (id) => `${API_BASE_URL}/${id}`,
        delete: (id) => `${API_BASE_URL}/eliminar/${id}`,
        toggleStatus: (id) => `${API_BASE_URL}/cambiar-estado/${id}`,
        buscarDocumento: (documento) => `${API_MIAPI}${API_CLIENTES}/buscar-documento/${documento}`
    };

    initializeDataTable();

    proveedorModal = new bootstrap.Modal(document.getElementById('proveedorModal'));

    $('#btnBuscarDocumento').on('click', function () {
        const doc = $('#documento').val().trim();
        buscarDocumento(doc);
    });

    setupEventListeners();


    function initializeDataTable() {
        dataTable = $('#tablaProveedores').DataTable({
            responsive: true,
            processing: true,
            deferRender: true,
            ajax: {
                url: ENDPOINTS.list,
                dataSrc: 'data'
            },
            columns: [
                { data: 'id' },
                { data: 'nombre' },
                { data: 'documento' },
                { data: 'telefono' },
                { data: 'correo' },
                {
                    data: 'estado', render: function (data, type, row) {
                        return data === 1
                            ? '<span class="badge bg-success">Activo</span>'
                            : '<span class="badge bg-danger">Inactivo</span>';
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
                    title: 'Listado de Proveedores',
                    className: 'btn btn-success',
                    exportOptions: {
                        columns: [0, 1, 2, 3, 4, 5],
                        modifier: {
                            page: 'all'
                        }
                    }
                },
                {
                    extend: 'pdfHtml5',
                    text: '<i class="bi bi-file-earmark-pdf"></i> Exportar a PDF',
                    title: 'Listado de Proveedores',
                    className: 'btn btn-danger',
                    orientation: 'landscape',
                    pageSize: 'A4',
                    exportOptions: {
                        columns: [0, 1, 2, 3, 4, 5],
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
                        columns: [0, 1, 2, 3, 4, 5],
                        modifier: {
                            page: 'all'
                        }
                    },
                    customize: function (win) {
                        $(win.document.body)
                            .css('font-size', '10pt')
                            .prepend('<h3 style="text-align:center;">Listado de Proveedores</h3>');
                    }
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
        $('#btnNuevoRegistro').on('click', openModalForNew);

        $('#formProveedor').on('submit', function (e) {
            e.preventDefault();
            saveProveedor();
        });

        $('#tablaProveedores tbody').on('click', '.action-edit', handleEdit);
        $('#tablaProveedores tbody').on('click', '.action-status', handleToggleStatus);
        $('#tablaProveedores tbody').on('click', '.action-delete', handleDelete);
    }

    function loadProveedores() {
        dataTable.ajax.reload();
    }

    function saveProveedor() {
        clearFieldErrors();

        const formData = {
            id: $('#id').val(),
            nombre: $('#nombre').val(),
            documento: $('#documento').val(),
            telefono: $('#telefono').val(),
            correo: $('#correo').val()
        };

        if (!validateForm(formData)) {
            return;
        }

        showLoading(true);

        fetch(ENDPOINTS.save, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    hiddenModal();
                    showNotification(data.message, 'success');
                    loadProveedores();
                } else {
                    if (data.errors) {
                        Object.keys(data.errors).forEach(field => {
                            showFieldError(field, data.errors[field]);
                        });
                    } else {
                        showNotification(data.message, 'error');
                    }
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Ocurrió un error al guardar el proveedor.', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    function handleEdit(e) {
        e.preventDefault();
        const id = $(this).data('id');

        showLoading(true);

        fetch(ENDPOINTS.get(id))
            .then(response => {
                if (!response.ok) {
                    throw new Error('Proveedor no encontrado');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    openModalForEdit(data.data);
                } else {
                    showNotification('Error al cargar el proveedor:' + data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Ocurrió un error al cargar el proveedor.', 'error');
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
                    loadProveedores();
                } else {
                    showNotification(data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Ocurrió un error al cambiar el estado del proveedor.', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    function handleDelete(e) {
        e.preventDefault();
        const id = $(this).data('id');
        Swal.fire({
            title: '¿Estas seguro?',
            text: "¡No podras revertir esta accion!",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Si, ¡eliminar!',
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
                            loadProveedores();
                        } else {
                            showNotification(data.message, 'error');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        showNotification('Ocurrió un error al eliminar el proveedor.', 'error');
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
        $('#modalProveedorTitle').text('Nuevo Proveedor');
        $('#nombre, #documento').prop('readonly', false);
        showModal();
    }

    function openModalForEdit(proveedor) {
        isEditing = true;
        clearForm();
        $('#modalProveedorTitle').text('Editar Proveedor');
        $('#id').val(proveedor.id);
        $('#nombre').val(proveedor.nombre).prop('readonly', true);
        $('#documento').val(proveedor.documento).prop('readonly', true);
        $('#telefono').val(proveedor.telefono);
        $('#correo').val(proveedor.correo);
        showModal();
    }


    function showModal() {
        proveedorModal.show();
    }

    function hiddenModal() {
        proveedorModal.hide();
        clearForm();
    }

    function clearForm() {
        $('#formProveedor')[0].reset();
        $('#formProveedor .form-control').removeClass('is-invalid');
        $(' .invalid-feedback').text('');
        isEditing = false;
    }

    function validateForm(formData) {
        let hasErrors = false;

        clearFieldErrors();

        if (!formData.nombre || formData.nombre.trim() === '') {
            showFieldError('nombre', 'El nombre es obligatorio.');
            hasErrors = true;
        }
        if (!formData.documento || formData.documento.trim() === '') {
            showFieldError('documento', 'El documento es obligatorio.');
            hasErrors = true;
        }

        if (!formData.telefono && !formData.telefono.trim() === '') {
            if (formData.telefono && !/^\d{9}$/.test(formData.telefono.trim() || formData.telefono.contains(' '))) {
                showFieldError('telefono', 'El teléfono debe tener 9 dígitos, sin espacios.');
                hasErrors = true;
            }
        }

        return !hasErrors;
    }

    function clearFieldErrors() {
        $('.invalid-feedback').text('');
        $('#formProveedor .form-control').removeClass('is-invalid');
    }

    function showFieldError(fieldName, message) {
        const field = $(`#${fieldName}`);
        const errorDiv = $(`#${fieldName}-error`);

        field.addClass('is-invalid');
        errorDiv.text(message);
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
        const $overlay = $('#loading-overlay');
        if (show) {
            $overlay.css('display', 'flex');
        } else {
            $overlay.css('display', 'none');
        }
    }

    function buscarDocumento(doc) {
        if (!doc || doc.length !== 11 || isNaN(doc)) {
            showNotification('Ingrese un RUC de 11 dígitos', 'error');
            return;
        }

        showLoading(true);

        fetch(ENDPOINTS.buscarDocumento(doc))
            .then(response => response.json())
            .then(data => {
                console.log("Respuesta del servidor:", data);

                if (data.success && data.datos) {
                    const persona = data.datos;

                    if (doc.length === 11) {
                        $('#nombre').val(persona.razon_social || '');
                        $('#documento').val(persona.ruc || doc);
                    }

                    showNotification('Datos obtenidos correctamente', 'success');
                } else {
                    showNotification(data.message || 'No se encontró información para el documento ingresado', 'error');
                    $('#nombre').val('');
                    $('#documento').val('');
                }
            })
            .catch(error => {
                console.error('Error al consultar el documento:', error);
                showNotification('Ocurrió un error al buscar el documento', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }


});