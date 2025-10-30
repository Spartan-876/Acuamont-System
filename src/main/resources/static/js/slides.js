$(document).ready(function () {

    const tablaRedes = $('#tabla-redes-sociales');
    const modal = $('#modalEditarURL');
    const API_BASE = '/redes/api';
    let dataTable;

    const ENDPOINTS = {
        listar: `${API_BASE}/listar`,
        actualizar: (id) => `${API_BASE}/actualizar/${id}`,
        cambiarEstado: (id) => `${API_BASE}/cambiar-estado/${id}`
    }

    initializeDataTable();
    setupEventListeners();


    function initializeDataTable() {
        dataTable = tablaRedes.DataTable({
            responsive: false,
            processing: true,
            ajax: {
                url: ENDPOINTS.listar,
                dataSrc: 'data'
            },
            columns: [

                {
                    data: 'icono',
                    orderable: false,
                    render: function (data, type, row) {
                        return `<i class="${data} fs-4"></i>`;
                    }
                },
                { data: 'nombre' },
                {
                    data: 'url',
                    render: function (data, type, row) {
                        return data ? `<a href="${data}" target="_blank">${data}</a>` : '<span class="text-muted">—</span>';
                    }
                },
                {
                    data: 'estado',
                    render: function (data, type, row) {
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
                { responsivePriority: 2, targets: 3 },
            ],
            language: {
                url: 'https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json'
            },
            lengthChange: false,
            pageLength: 5,
            searching: false,
            info: false,
            paging: false,
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
            </div>
        `;
    }

    function setupEventListeners() {
        tablaRedes.on('click', '.action-edit', openModalForEdit);
        $('#btnGuardarURL').on('click', editar);
        tablaRedes.on('click', '.action-status', cambiarEstado);
    }

    function cambiarEstado(e) {
        e.preventDefault();
        const id = $(this).data('id');
        showLoading(true);
        fetch(ENDPOINTS.cambiarEstado(id), {
            method: 'POST'
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showNotification(data.message, 'success');
                    dataTable.ajax.reload();
                } else {
                    showNotification(data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error de conexion al cambiar estado', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    function editar(e) {
        e.preventDefault();

        const id = $('#redSocialId').val();
        const url = $('#redSocialURL').val();

        if (!id) {
            showNotification('Error: No se encontró el ID de la red social.', 'error');
            return;
        }

        showLoading(true);

        const dto = {
            url: url
        };

        fetch(ENDPOINTS.actualizar(id), {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dto)
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    $('#btnGuardarURL').blur();
                    modal.modal('hide');
                    dataTable.ajax.reload();
                    showNotification(data.message || 'URL actualizada correctamente', 'success');
                } else {
                    showNotification(data.message || 'Error al actualizar', 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error de conexión al guardar.', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    function openModalForEdit(redSocial) {
        try {
            const rowData = dataTable.row($(this).parents('tr')).data();

            if (!rowData) {
                showNotification('No se pudieron cargar los datos de la fila.', 'error');
                return;
            }

            $('#redSocialId').val(rowData.id);
            $('#redSocialNombre').val(rowData.nombre);
            $('#redSocialUrl').val(rowData.url_enlace);

            modal.modal('show');

        } catch (e) {
            console.error("Error al abrir el modal:", e);
            showNotification('Error al leer datos de la tabla.', 'error');
        }
    }


    function showLoading(show) {
        const $overlay = $('#loading-overlay');
        if (show) {
            $overlay.css('display', 'flex');
        } else {
            $overlay.css('display', 'none');
        }
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

});