$(document).ready(function () {
    let dataTable;
    let isEditing = false;
    let clienteModal;

    const API_BASE_URL = '/clientes/api';
    const ENDPOINTS = {
        list: `${API_BASE_URL}/listar`,
        save: `${API_BASE_URL}/guardar`,
        get: (id) => `${API_BASE_URL}/${id}`,
        delete: (id) => `${API_BASE_URL}/eliminar/${id}`,
        toggleStatus: (id) => `${API_BASE_URL}/cambiar-estado/${id}`,
        buscarDocumento: (dni) => `${API_BASE_URL}/buscar-documento/${dni}`
    };

    initializeDataTable();

    clienteModal = new bootstrap.Modal(document.getElementById('clienteModal'));

    $('#btnBuscarDocumento').on('click', function () {
        const doc = $('#documento').val().trim();
        buscarDocumento(doc);
    });

    setupEventListeners();


    function initializeDataTable() {
        dataTable = $('#tablaClientes').DataTable({
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
                {data: 'correo'},
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
                url: '//cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json'
            },
            pageLength: 10,
            lengthMenu: [5, 10, 25, 50],
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
        $('#btnNuevoRegistro').on('click', openModalForNew);

        $('#formCliente').on('submit', function (e) {
            e.preventDefault();
            saveCliente();
        });

        $('#tablaClientes tbody').on('click', '.action-edit', handleEdit);
        $('#tablaClientes tbody').on('click', '.action-status', handleToggleStatus);
        $('#tablaClientes tbody').on('click', '.action-delete', handleDelete);
    }

    function loadClientes() {
        dataTable.ajax.reload();
    }

    function saveCliente() {
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
                    loadClientes();
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
                showNotification('Ocurrió un error al guardar el cliente.', 'error');
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
                    throw new Error('Cliente no encontrado');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    openModalForEdit(data.data);
                } else {
                    showNotification('Error al cargar el cliente:' + data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Ocurrió un error al cargar el cliente.', 'error');
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
                    loadClientes();
                } else {
                    showNotification(data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Ocurrió un error al cambiar el estado del cliente.', 'error');
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
                            loadClientes();
                        } else {
                            showNotification(data.message, 'error');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        showNotification('Ocurrió un error al eliminar el cliente.', 'error');
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
        $('#modalClienteTitle').text('Nuevo Cliente');
        $('#nombre, #documento').prop('readonly', false);
        showModal();
    }

    function openModalForEdit(cliente) {
        isEditing = true;
        clearForm();
        $('#modalClienteTitle').text('Editar Cliente');
        $('#id').val(cliente.id);
        $('#nombre').val(cliente.nombre).prop('readonly', true);
        $('#documento').val(cliente.documento).prop('readonly', true);
        $('#telefono').val(cliente.telefono);
        $('#correo').val(cliente.correo);
        showModal();
    }


    function showModal() {
        clienteModal.show();
    }

    function hiddenModal() {
        clienteModal.hide();
        clearForm();
    }

    function clearForm() {
        $('#formCliente')[0].reset();
        $('#formCliente .form-control').removeClass('is-invalid');
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
        if (!formData.telefono || formData.telefono.trim() === '') {
            showFieldError('telefono', 'El teléfono es obligatorio.');
            hasErrors = true;
        }
        if (formData.telefono && !/^\d{9}$/.test(formData.telefono.trim() || formData.telefono.contains(' '))) {
            showFieldError('telefono', 'El teléfono debe tener 9 dígitos, sin espacios.');
            hasErrors = true;
        }
        if (!formData.correo || formData.correo.trim() === '') {
            showFieldError('correo', 'El correo es obligatorio.');
            hasErrors = true;
        }

        return !hasErrors;
    }

    function clearFieldErrors() {
        $('.invalid-feedback').text('');
        $('#formCliente .form-control').removeClass('is-invalid');
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
        if (!doc || (doc.length !== 8 && doc.length !== 11) || isNaN(doc)) {
            showNotification('Ingrese un DNI válido de 8 dígitos o RUC de 11 dígitos', 'error');
            return;
        }

        showLoading(true);

        fetch(ENDPOINTS.buscarDocumento(doc))
            .then(response => response.json())
            .then(data => {
                console.log("Respuesta del servidor:", data);

                if (data.success && data.datos) {
                    const persona = data.datos;

                    if (doc.length === 8) {
                        $('#nombre').val(`${persona.nombres || ''} ${persona.ape_paterno || ''} ${persona.ape_materno || ''}`.trim());
                        $('#documento').val(persona.dni || doc);
                    } else if (doc.length === 11) {
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