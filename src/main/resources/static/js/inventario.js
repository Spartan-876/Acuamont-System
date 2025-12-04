$(document).ready(function () {
    let dataTable;

    const API_BASE = '/inventario/api';
    const ENDPOINTS = {
        list: `${API_BASE}/listar`,
        movimiento: (productoid) => `${API_BASE}/movimientos/${productoid}`,
        tipoMovimiento: `${API_BASE}/tipoMovimientos`,
        listar_ajustes: (productoId) => `${API_BASE}/ajustes/${productoId}`,
        crear_ajustes: `${API_BASE}/guardarAjuste`
    };

    initializeDataTable();
    loadTipoMovimientos();
    setupEventListeners();

    function initializeDataTable() {
        dataTable = $('#tablaInventario').DataTable({
            responsive: true,
            processing: true,
            ajax: {
                url: ENDPOINTS.list,
                dataSrc: 'data'
            },
            columns: [
                { data: 'nombre' },
                {
                    data: 'stock',
                    render: function (data, type, row) {
                        const badgeClass = data <= row.stockSeguridad
                            ? ' badge bg-danger text-white fw-semibold'
                            : ' badge bg-success text-white fw-semibold';
                        return `<span class="${badgeClass}">${data}</span>`;
                    }
                },
                { data: 'stockSeguridad' },
                {
                    data: null,
                    orderable: false,
                    searchable: false,
                    render: (data, type, row) => createActionButtons(row)
                }
            ],
            columnDefs: [
                { responsivePriority: 1, targets: 1 },
                { responsivePriority: 2, targets: 2 },
                { responsivePriority: 3, targets: 3 },
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
                    title: 'Listado de Inventarios',
                    className: 'btn btn-success',
                    exportOptions: {
                        columns: [0, 1, 2],
                        modifier: {
                            page: 'all'
                        }
                    }
                },
                {
                    extend: 'pdfHtml5',
                    text: '<i class="bi bi-file-earmark-pdf"></i> Exportar a PDF',
                    title: 'Listado de Inventarios',
                    className: 'btn btn-danger',
                    pageSize: 'A4',
                    exportOptions: {
                        columns: [0, 1, 2],
                        modifier: {
                            page: 'all'
                        }
                    }
                },
                {
                    extend: 'print',
                    text: '<i class="bi bi-printer"></i> Imprimir',
                    className: 'btn btn-info',
                    pageSize: 'A4',
                    exportOptions: {
                        columns: [0, 1, 2],
                        modifier: {
                            page: 'all'
                        }
                    },
                    customize: function (win) {
                        $(win.document.body)
                            .css('font-size', '10pt')
                            .prepend('<h3 style="text-align:center;">Listado de Inventarios</h3>');
                    }
                }
            ]
        });
    }

    function setupEventListeners() {
        $('#tablaInventario').on('click', '.action-ver-movimientos', handleVerMovimientos);
        $('#tablaInventario').on('click', '.action-ajuste-inventario', showAjusteModal);
        $('#btnGuardarAjuste').on('click', handleAjusteInventario);
        $('#tablaInventario').on('click', '.action-historial-ajustes', handleHistorialAjustes);
    }

    function createActionButtons(row) {
        let buttons = `
            <div class="d-flex gap-1">
                <button class="m-auto btn btn-sm btn-warning action-ajuste-inventario" data-id="${row.id}" data-stock="${row.stock}">
                    <i class="bi bi-nut"></i> Ajuste Inventario
                </button>
                <button class="m-auto btn btn-sm btn-info action-ver-movimientos" data-id="${row.id}" data-nombre="${row.nombre}">
                    <i class="bi bi-graph-up-arrow"></i> Historial Ventas
                </button>
                <button class="m-auto btn btn-sm btn-primary action-historial-ajustes" data-id="${row.id}" data-nombre="${row.nombre}">
                    <i class="bi bi-clock-history"></i> Historial Ajustes
                </button>
            </div>
        `;
        return buttons;
    }

    function loadTipoMovimientos() {
        fetch(ENDPOINTS.tipoMovimiento)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const select = $('#tipo_movimiento');
                    select.empty().append('<option value="">Seleccione un tipo de movimiento...</option>');
                    data.data.forEach(tipo => {
                        select.append(`<option value="${tipo.id}">${tipo.nombre}</option>`);
                    });
                } else {
                    showNotification(data.message, 'error');
                }
            })
            .catch(error => {
                showNotification('Error cargando tipos de movimiento:' + error, 'error');
            });
    }

    function handleVerMovimientos() {
        const productoId = $(this).data('id');
        const productoNombre = $(this).data('nombre') || 'Producto';
        const tbody = $('#tablaMovimientos tbody');
        const modal = $('#modal-movimientos');
        const tabla = $('#tablaMovimientos');

        modal.modal('show');
        tbody.html(`
            <tr>
                <td colspan="5" class="text-center text-muted py-3">
                    <i class="bi bi-arrow-repeat me-2"></i> Cargando movimientos...
                </td>
            </tr>
        `);

        $('#total-cantidad').html('0');
        $('#total-subtotal').html('<strong>0.00</strong>');

        fetch(ENDPOINTS.movimiento(productoId))
            .then(res => res.ok ? res.json() : Promise.reject("Error al obtener movimientos"))
            .then(data => {
                if (!data.success) {
                    showNotification(data.message, 'error');
                    return;
                }
                const movimientos = data.data
                    .filter(v => v.estado !== 2)
                    .flatMap(v => (v.detalleVentas || [])
                        .filter(d => d.producto?.id === productoId)
                        .map(d => ({
                            fecha: v.fecha?.split('T')[0] || '',
                            doc: `${v.serieComprobante?.serie || ''}-${v.correlativo || ''}`,
                            precio: d.precioUnitario || '0.00',
                            cantidad: d.cantidad || 0,
                            subtotal: d.subtotal || '0.00'
                        }))
                    );

                if (movimientos.length === 0) {
                    if ($.fn.DataTable.isDataTable(tabla)) {
                        tabla.DataTable().clear().draw();
                    }
                    tbody.html(`
                        <tr>
                            <td colspan="5" class="text-center text-muted py-3">
                                <i class="bi bi-info-circle me-2"></i>
                                No se encontraron movimientos para <strong>${productoNombre}</strong>.
                            </td>
                        </tr>
                    `);
                    return;
                }

                let totalCantidad = 0;
                let totalSubtotal = 0;
                const tableData = movimientos.map(m => {
                    totalCantidad += m.cantidad || 0;
                    totalSubtotal += parseFloat(m.subtotal) || 0;
                    return [m.fecha, m.doc, m.precio, m.cantidad, `<strong>${m.subtotal}</strong>`];
                });

                $('#total-cantidad').html(totalCantidad.toFixed(0));
                $('#total-subtotal').html(`<strong>${totalSubtotal.toFixed(2)}</strong>`);

                if ($.fn.DataTable.isDataTable(tabla)) {
                    tabla.DataTable().clear().rows.add(tableData).draw();
                } else {
                    tbody.empty();
                    tabla.DataTable({
                        data: tableData,
                        paging: true,
                        searching: false,
                        info: false,
                        lengthChange: false,
                        pageLength: 5,
                        order: [[0, 'desc']],
                        language: {
                            emptyTable: 'Sin movimientos registrados',
                            paginate: { next: 'Siguiente', previous: 'Anterior' }
                        },
                        createdRow: row => $(row).addClass('align-middle'),
                        dom: 'Bfrtip',
                        buttons: [
                            {
                                extend: 'excelHtml5',
                                text: '<i class="bi bi-file-earmark-excel"></i> Exportar a Excel',
                                title: `Listado de Movimientos de ${productoNombre}`,
                                className: 'btn btn-success',
                                exportOptions: {
                                    columns: [0, 1, 2, 3, 4],
                                    modifier: {
                                        page: 'all'
                                    }
                                }
                            },
                            {
                                extend: 'pdfHtml5',
                                text: '<i class="bi bi-file-earmark-pdf"></i> Exportar a PDF',
                                title: `Listado de Movimientos de ${productoNombre}`,
                                className: 'btn btn-danger',
                                pageSize: 'A4',
                                exportOptions: {
                                    columns: [0, 1, 2, 3, 4],
                                    modifier: {
                                        page: 'all'
                                    }
                                }
                            },
                            {
                                extend: 'print',
                                text: '<i class="bi bi-printer"></i> Imprimir',
                                title: `Listado de Movimientos de ${productoNombre}`,
                                className: 'btn btn-info',
                                pageSize: 'A4',
                                exportOptions: {
                                    columns: [0, 1, 2, 3, 4],
                                    modifier: {
                                        page: 'all'
                                    }
                                },
                                customize: function (win) {
                                    $(win.document.body)
                                        .css('font-size', '10pt')
                                }
                            }
                        ]
                    });
                }
            })
            .catch(err => {
                console.error(err);
                tbody.html(`
                    <tr>
                        <td colspan="5" class="text-center text-danger py-3">
                            <i class="bi bi-exclamation-triangle me-2"></i>
                            Error al cargar los movimientos.
                        </td>
                    </tr>
                `);
            });
    }

    function showAjusteModal() {
        const productoId = $(this).data('id');
        const stock = $(this).data('stock');
        $('#btnGuardarAjuste').data('stock', stock);
        $('#btnGuardarAjuste').data('id', productoId);
        $('#registrarAjusteModal').modal('show');
        clearForm();

    }

    function handleAjusteInventario() {
        const formData = {
            productoId: $(this).data('id'),
            cantidad: $('#cantidad').val(),
            tipoMovimientoId: $('#tipo_movimiento').val(),
            comentario: $('#comentario').val()
        }

        const stock = $(this).data('stock');

        if (formData.cantidad <= 0) {
            showFieldError('cantidad', 'La cantidad debe ser mayor a 0');
            return;
        }

        if (!formData.tipoMovimientoId) {
            showFieldError('tipo_movimiento', 'Debe seleccionar un tipo de movimiento');
            return;
        }

        if (formData.comentario.length > 250) {
            showFieldError('comentario', 'El comentario no puede superar los 250 caracteres');
            return;
        }

        if (formData.tipoMovimientoId === '2') {
            if (formData.cantidad > stock) {
                showFieldError('cantidad', 'La cantidad no puede superar el stock actual');
                return;
            }
        }

        showLoading(true);

        fetch(ENDPOINTS.crear_ajustes, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al registrar ajuste');
                }
                return response.json();
            })
            .then(data => {
                if (!data.success) {
                    throw new Error(data.message);
                }
                showNotification('Registro de ajuste exitoso', 'success');
                dataTable.ajax.reload();
            })
            .catch(error => {
                console.error('Error al registrar ajuste:', error);
                showNotification('Error al registrar ajuste: ' + error.message, 'error');
            })
            .finally(() => {
                showLoading(false);
                $('#registrarAjusteModal').modal('hide');
            });

    }

    function handleHistorialAjustes() {
        const productoId = $(this).data('id');
        const productoNombre = $(this).data('nombre') || 'Producto';
        const tbody = $('#tablaajustes tbody');
        const modal = $('#modal-ajustes');
        const tabla = $('#tablaajustes');

        tabla.data('current-product-name', productoNombre);

        modal.modal('show');
        tbody.html(`
            <tr>
                <td colspan="5" class="text-center text-muted py-3">
                    <i class="bi bi-arrow-repeat me-2"></i> Cargando movimientos...
                </td>
            </tr>
        `);

        fetch(ENDPOINTS.listar_ajustes(productoId))
            .then(res => res.ok ? res.json() : Promise.reject("Error al obtener movimientos"))
            .then(data => {
                if (!data.success) {
                    showNotification(data.message, 'error');
                    return;
                }
                const ajustes = data.data
                    .map(m => ({
                        fecha: m.fecha?.split('T')[0] || '',
                        tipoMovimiento: m.tipoMovimiento?.nombre || '',
                        cantidad: m.cantidad || 0,
                        comentario: m.comentario || ''
                    }));

                if (ajustes.length === 0) {
                    if ($.fn.DataTable.isDataTable(tabla)) {
                        tabla.DataTable().clear().draw();
                    }
                    tbody.html(`
                    <tr>
                        <td colspan="5" class="text-center text-muted py-3">
                            <i class="bi bi-info-circle me-2"></i>
                            No se encontraron ajustes para <strong>${productoNombre}</strong>.
                        </td>
                    </tr>
                `);
                    return;
                }

                if ($.fn.DataTable.isDataTable(tabla)) {
                    tabla.DataTable().clear().rows.add(ajustes).draw();
                } else {
                    tbody.empty();
                    tabla.DataTable({
                        data: ajustes,
                        columns: [
                            { data: 'fecha' },
                            {
                                data: 'tipoMovimiento',
                                render: function (data, type, row) {
                                    const badgeClass = data === 'Entrada'
                                        ? 'badge bg-success text-white fw-semibold'
                                        : 'badge bg-danger text-white fw-semibold';
                                    return `<span class="${badgeClass}">${data}</span>`;
                                }
                            },
                            {
                                data: 'cantidad',
                                render: function (data, type, row) {
                                    if (row.tipoMovimiento === 'Entrada') {
                                        return `<strong class="text-success">+${data}</strong>`;
                                    } else if (row.tipoMovimiento === 'Salida') {
                                        return `<strong class="text-danger">-${data}</strong>`;
                                    } else {
                                        return `<strong class="text-warning"}${data}</strong>`;
                                    }
                                }
                            },
                            { data: 'comentario' }
                        ],

                        paging: true,
                        searching: false,
                        info: false,
                        lengthChange: false,
                        pageLength: 5,
                        order: [[0, 'desc']],
                        columnDefs: [
                            { responsivePriority: 1, targets: 1 },
                            { responsivePriority: 2, targets: 2 },
                            { responsivePriority: 3, targets: 3 },
                        ],
                        language: {
                            url: 'https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json'
                        },
                        dom: 'Bfrtip',
                        buttons: [
                            {
                                extend: 'excelHtml5',
                                text: '<i class="bi bi-file-earmark-excel"></i> Exportar a Excel',
                                title: function () {
                                    return 'Listado de Movimientos de ' + $('#tablaajustes').data('current-product-name');
                                },
                                className: 'btn btn-success',
                                exportOptions: {
                                    columns: [0, 1, 2, 3],
                                    modifier: {
                                        page: 'all'
                                    }
                                }
                            },
                            {
                                extend: 'pdfHtml5',
                                text: '<i class="bi bi-file-earmark-pdf"></i> Exportar a PDF',
                                title: function () {
                                    return 'Listado de Movimientos de ' + $('#tablaajustes').data('current-product-name');
                                },
                                className: 'btn btn-danger',
                                pageSize: 'A4',
                                exportOptions: {
                                    columns: [0, 1, 2, 3],
                                    modifier: {
                                        page: 'all'
                                    }
                                }
                            },
                            {
                                extend: 'print',
                                text: '<i class="bi bi-printer"></i> Imprimir',
                                title: function () {
                                    return 'Listado de Movimientos de ' + $('#tablaajustes').data('current-product-name');
                                },
                                className: 'btn btn-info',
                                pageSize: 'A4',
                                exportOptions: {
                                    columns: [0, 1, 2, 3],
                                    modifier: {
                                        page: 'all'
                                    }
                                },
                                customize: function (win) {
                                    $(win.document.body)
                                        .css('font-size', '10pt')
                                }
                            }
                        ]
                    });
                }
            })
            .catch(err => {
                console.error(err);
                tbody.html(`
                    <tr>
                        <td colspan="5" class="text-center text-danger py-3">
                            <i class="bi bi-exclamation-triangle me-2"></i>
                            Error al cargar los movimientos.
                        </td>
                    </tr>
                `);
            });
    }

    function clearForm() {
        $('#formRegistrarAjuste')[0].reset();
        clearFieldErrors();

    }

    function showFieldError(fieldName, message) {
        const field = $(`#${fieldName}`);
        const errorDiv = $(`#${fieldName}-error`);

        field.addClass('is-invalid');
        errorDiv.text(message).show();
    }

    function showNotification(message, type = 'success') {
        const toastClass = type === 'success' ? 'text-bg-success' : 'text-bg-danger';
        const notification = $(`
            <div class="toast align-items-center ${toastClass} border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">${message}</div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `);

        $('#notification-container').append(notification);
        const toast = new bootstrap.Toast(notification, { delay: 3000 });
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

    function clearFieldErrors() {
        $('.form-control').removeClass('is-invalid');
        $('.invalid-feedback').text('');
        $('.invalid-feedback').removeClass('d-block');
        $('.invalid-feedback').css('display', '');
    }

});
