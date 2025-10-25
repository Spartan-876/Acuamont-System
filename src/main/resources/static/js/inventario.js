$(document).ready(function () {
    let dataTable;

    const API_BASE = '/inventario/api';
    const ENDPOINTS = {
        list: `${API_BASE}/listar`,
        movimiento: (productoid) => `${API_BASE}/movimientos/${productoid}`
    };

    initializeDataTable();
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
                    render: function (data, type, row) {
                        return `
                            <button 
                                class="m-auto btn btn-sm btn-info action-ver-movimientos" 
                                data-id="${row.id}"
                                data-nombre="${row.nombre}"
                                >
                                <i class="bi bi-graph-up-arrow"></i> Movimientos
                            </button>
                        `;
                    }
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
                            .prepend('<h3 style="text-align:center;">Listado de Productos</h3>');
                    }
                }
            ]
        });
    }

    function setupEventListeners() {
        $('#tablaInventario tbody').on('click', '.action-ver-movimientos', handleVerMovimientos);
    }

    function handleVerMovimientos(e) {
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

        fetch(ENDPOINTS.movimiento(productoId))
            .then(res => res.ok ? res.json() : Promise.reject("Error al obtener movimientos"))
            .then(data => {
                const movimientos = data
                    .filter(v => v.estado !== 2)
                    .flatMap(v => (v.detalleVentas || [])
                        .filter(d => d.producto?.id === productoId)
                        .map(d => ({
                            fecha: new Date(v.fecha).toLocaleDateString('es-PE'),
                            doc: `${v.serieComprobante?.serie || ''}-${v.correlativo || ''}`,
                            precio: d.precioUnitario?.toFixed(2) || '0.00',
                            cantidad: d.cantidad || 0,
                            subtotal: d.subtotal?.toFixed(2) || '0.00'
                        }))
                    );

                if (movimientos.length === 0) {
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

                if ($.fn.DataTable.isDataTable(tabla)) {
                    const table = tabla.DataTable();
                    table.clear();
                    movimientos.forEach(m => table.row.add([
                        m.fecha, m.doc, m.precio, m.cantidad, `<strong>${m.subtotal}</strong>`
                    ]));
                    table.draw();
                } else {
                    tabla.DataTable({
                        data: movimientos.map(m => [
                            m.fecha, m.doc, m.precio, m.cantidad, `<strong>${m.subtotal}</strong>`
                        ]),
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
                                        .prepend('<h3 style="text-align:center;">Listado de Productos</h3>');
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
});
