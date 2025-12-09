
$(document).ready(function () {

    let dataTableUtilidadesVentas;
    let miGraficoUtilidadesVentas;

    let dataTableUtilidadesUsuarios;
    let miGraficoUtilidadesUsuarios;

    let dataTableUtilidadesProducto;
    let miGraficoUtilidadesProducto;

    const API_BASE = '/reportes/api';
    const ENDPOINTS = {
        utilidades_ventas: `${API_BASE}/utilidad-ventas`,
        utilidades_ventas_filtro: `${API_BASE}/utilidad-ventas-rango`,
        utilidades_usuario: `${API_BASE}/utilidad-usuarios`,
        utilidades_usuario_filtro: `${API_BASE}/utilidad-usuarios-rango`,
        utilidades_producto: `${API_BASE}/utilidad-producto`,
        utilidades_producto_filtro: `${API_BASE}/utilidad-producto-rango`
    };

    //Inicializaciones
    initializeDataTableUtilidadesVentas();
    initializeDataTableUtilidadesUsuarios();
    initializeDataTableUtilidadesProducto();
    graficoUtilidadesVentas();
    graficoUtilidadesUsuarios();
    graficoUtilidadesProducto();
    setupEventListeners();

    function setupEventListeners() {
        $('#btnFiltrarVentas').on('click', filtrarUtilidadesVentasFechas);
        $('#btnLimpiarFiltrosVentas').on('click', limpiarFiltroUtilidadesVentasFechas);

        $('#btnFiltrarVendedor').on('click', filtrarUtilidadesUsuarioFechas);
        $('#btnLimpiarFiltrosVendedor').on('click', limpiarFiltroUtilidadesUsuarioFechas);

        $('#btnFiltrarProducto').on('click', filtrarUtilidadesProductoFechas);
        $('#btnLimpiarFiltrosProducto').on('click', limpiarFiltroUtilidadesProductoFechas);

    }

    //Pagina de Utilidades por Venta

    function initializeDataTableUtilidadesVentas() {
        dataTableUtilidadesVentas = $('#tablaUtilidadesVentas').DataTable({
            responsive: true,
            processing: true,
            deferRender: true,
            ajax: {
                url: ENDPOINTS.utilidades_ventas,
                dataSrc: function (json) {
                    if (json.success) {
                        actualizarDatosGraficoUtilidadVentas(json.data);
                        return json.data;
                    } else {
                        return [];
                    }
                }
            },
            columns: [
                { data: 'documento' },
                { data: 'cliente' },
                {
                    data: 'fecha',
                    render: (data) => data ? new Date(data).toLocaleDateString('es-PE') : ''
                },
                {
                    data: 'totalVenta',
                    render: (data) => `S/ ${(data ?? 0).toFixed(2)}`
                },
                {
                    data: 'utilidad',
                    render: (data) => `S/ ${(data ?? 0).toFixed(2)}`
                }
            ],
            columnDefs: [
                { responsivePriority: 1, targets: 1 },
                { responsivePriority: 2, targets: 2 },
                { responsivePriority: 3, targets: 3 },
                { responsivePriority: 4, targets: 4 },
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
                    title: 'Reporte de utilidades por venta',
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
                    title: 'Reporte de utilidades por venta',
                    className: 'btn btn-danger',
                    orientation: 'portrait',
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
                    className: 'btn btn-info',
                    orientation: 'portrait',
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
                            .prepend('<h3 style="text-align:center;">Reporte de utilidades por Ventas</h3>');
                    }
                },
                {
                    extend: 'colvis',
                    text: '<i class="bi bi-eye"></i> Mostrar/Ocultar',
                    className: 'btn btn-secondary'
                }
            ]
        })
    }

    function filtrarUtilidadesVentasFechas() {
        clearFieldErrors();

        const inicio = $('#fechaInicioVentas').val();
        const fin = $('#fechaFinVentas').val();
        let hayErrores = false;

        if (!inicio) {
            showFieldError('fechaInicioVentas', 'Debes seleccionar una fecha de inicio.');
            hayErrores = true;
        }

        if (!fin) {
            showFieldError('fechaFinVentas', 'Debes seleccionar una fecha de fin.');
            hayErrores = true;
        }

        if (hayErrores) return;

        if (inicio > fin) {
            showNotification('La fecha fin debe ser despues de incio', 'error');
            return;
        }

        const nuevaUrl = `${ENDPOINTS.utilidades_ventas_filtro}?inicio=${inicio}&fin=${fin}`;
        dataTableUtilidadesVentas.ajax.url(nuevaUrl).load();
    }

    function limpiarFiltroUtilidadesVentasFechas() {
        $('#fechaInicio').val('');
        $('#fechaFin').val('');
        clearFieldErrors();

        const nuevaUrl = `${ENDPOINTS.utilidades_ventas}`
        dataTableUtilidadesVentas.ajax.url(nuevaUrl).load();
    }

    function graficoUtilidadesVentas() {
        const ctx = document.getElementById('graficoVentas').getContext('2d');
        miGraficoUtilidadesVentas = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [
                    {
                        label: 'Total Venta (S/)',
                        data: [],
                        borderColor: 'rgba(54, 162, 235, 1)',
                        backgroundColor: 'rgba(54, 162, 235, 0.1)',
                        borderWidth: 2,
                        tension: 0.4,
                        fill: true
                    },
                    {
                        label: 'Utilidad (S/)',
                        data: [],
                        borderColor: 'rgba(16, 185, 129, 1)',
                        backgroundColor: 'rgba(16, 185, 129, 0.1)',
                        borderWidth: 2,
                        tension: 0.4,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false,
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function (value) { return 'S/ ' + value; }
                        }
                    }
                }
            }
        });
    }

    function actualizarDatosGraficoUtilidadVentas(datos) {
        const datosOrdenados = [...datos].reverse();

        const etiquetas = datosOrdenados.map(d => {
            const fecha = new Date(d.fecha);
            return fecha.toLocaleDateString() + ' ' + fecha.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        });

        const valoresVenta = datosOrdenados.map(d => d.totalVenta);
        const valoresUtilidad = datosOrdenados.map(d => d.utilidad);

        miGraficoUtilidadesVentas.data.labels = etiquetas;
        miGraficoUtilidadesVentas.data.datasets[0].data = valoresVenta;
        miGraficoUtilidadesVentas.data.datasets[1].data = valoresUtilidad;

        miGraficoUtilidadesVentas.update();
    }

    //Pagina de utilidades por Usuario

    function initializeDataTableUtilidadesUsuarios() {
        dataTableUtilidadesUsuarios = $('#tablaUtilidadesVendedor').DataTable({
            responsive: true,
            processing: true,
            deferRender: true,
            ajax: {
                url: ENDPOINTS.utilidades_usuario,
                dataSrc: function (json) {
                    if (json.success) {
                        actualizarDatosGraficoUtilidadUsuarios(json.data);
                        return json.data;
                    } else {
                        return [];
                    }
                }
            },
            columns: [
                { data: 'usuario' },
                { data: 'cantidadVentas' },
                {
                    data: 'utilidad',
                    render: (data) => `S/ ${(data ?? 0).toFixed(2)}`
                }
            ],
            columnDefs: [
                { responsivePriority: 1, targets: 1 },
                { responsivePriority: 2, targets: 2 },
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
                    title: 'Reporte de utilidades por venta',
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
                    title: 'Reporte de utilidades por Vendedor',
                    className: 'btn btn-danger',
                    orientation: 'portrait',
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
                    orientation: 'portrait',
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
                            .prepend('<h3 style="text-align:center;">Reporte de utilidades por Vendedor</h3>');
                    }
                },
                {
                    extend: 'colvis',
                    text: '<i class="bi bi-eye"></i> Mostrar/Ocultar',
                    className: 'btn btn-secondary'
                }
            ]
        })
    }

    function filtrarUtilidadesUsuarioFechas() {
        clearFieldErrors();

        const inicio = $('#fechaInicioVendedor').val();
        const fin = $('#fechaFinVendedor').val();
        let hayErrores = false;

        if (!inicio) {
            showFieldError('fechaInicioVendedor', 'Debes seleccionar una fecha de inicio.');
            hayErrores = true;
        }

        if (!fin) {
            showFieldError('fechaFinVendedor', 'Debes seleccionar una fecha de fin.');
            hayErrores = true;
        }

        if (hayErrores) return;

        if (inicio > fin) {
            showNotification('La fecha fin debe ser despues de incio', 'error');
            return;
        }

        const nuevaUrl = `${ENDPOINTS.utilidades_usuario_filtro}?inicio=${inicio}&fin=${fin}`;
        dataTableUtilidadesUsuarios.ajax.url(nuevaUrl).load();
    }

    function limpiarFiltroUtilidadesUsuarioFechas() {
        $('#fechaInicioVendedor').val('');
        $('#fechaFinVendedor').val('');
        clearFieldErrors();

        const nuevaUrl = `${ENDPOINTS.utilidades_usuario}`
        dataTableUtilidadesUsuarios.ajax.url(nuevaUrl).load();
    }

    function graficoUtilidadesUsuarios() {
        const ctxUser = document.getElementById('graficoVendedores').getContext('2d');
        miGraficoUtilidadesUsuarios = new Chart(ctxUser, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Utilidad Generada (S/)',
                    data: [],
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.5)',
                        'rgba(54, 162, 235, 0.5)',
                        'rgba(255, 206, 86, 0.5)',
                        'rgba(75, 192, 192, 0.5)'
                    ],
                    borderColor: [
                        'rgba(255, 99, 132, 1)',
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 206, 86, 1)',
                        'rgba(75, 192, 192, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { beginAtZero: true } }
            }
        });
    }

    function actualizarDatosGraficoUtilidadUsuarios(datos) {
        if (!miGraficoUtilidadesUsuarios) return;

        const etiquetas = datos.map(d => d.usuario);
        const valores = datos.map(d => d.utilidad);

        miGraficoUtilidadesUsuarios.data.labels = etiquetas;
        miGraficoUtilidadesUsuarios.data.datasets[0].data = valores;
        miGraficoUtilidadesUsuarios.update();
    }

    //Pagina de utilidades por producto

    function initializeDataTableUtilidadesProducto() {
        dataTableUtilidadesProducto = $('#tablaUtilidadesProducto').DataTable({
            responsive: true,
            processing: true,
            deferRender: true,
            ajax: {
                url: ENDPOINTS.utilidades_producto,
                dataSrc: function (json) {
                    if (json.success) {
                        actualizarDatosGraficoUtilidadProducto(json.data);
                        return json.data;
                    } else {
                        return [];
                    }
                }
            },
            columns: [
                { data: 'producto' },
                { data: 'cantidadVendida' },
                {
                    data: 'totalVenta',
                    render: (data) => `S/ ${(data ?? 0).toFixed(2)}`
                },
                {
                    data: 'utilidad',
                    render: (data) => `S/ ${(data ?? 0).toFixed(2)}`
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
                    title: 'Reporte de utilidades por Producto',
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
                    title: 'Reporte de utilidades por Producto',
                    className: 'btn btn-danger',
                    orientation: 'portrait',
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
                    className: 'btn btn-info',
                    orientation: 'portrait',
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
                            .prepend('<h3 style="text-align:center;">Reporte de utilidades por Producto</h3>');
                    }
                },
                {
                    extend: 'colvis',
                    text: '<i class="bi bi-eye"></i> Mostrar/Ocultar',
                    className: 'btn btn-secondary'
                }
            ]
        })
    }

    function filtrarUtilidadesProductoFechas() {
        clearFieldErrors();

        const inicio = $('#fechaInicioProducto').val();
        const fin = $('#fechaFinProducto').val();
        let hayErrores = false;

        if (!inicio) {
            showFieldError('fechaInicioProducto', 'Debes seleccionar una fecha de inicio.');
            hayErrores = true;
        }

        if (!fin) {
            showFieldError('fechaFinProducto', 'Debes seleccionar una fecha de fin.');
            hayErrores = true;
        }

        if (hayErrores) return;

        if (inicio > fin) {
            showNotification('La fecha fin debe ser despues de incio', 'error');
            return;
        }

        const nuevaUrl = `${ENDPOINTS.utilidades_producto_filtro}?inicio=${inicio}&fin=${fin}`;
        dataTableUtilidadesProducto.ajax.url(nuevaUrl).load();
    }

    function limpiarFiltroUtilidadesProductoFechas() {
        $('#fechaInicioProducto').val('');
        $('#fechaFinProducto').val('');
        clearFieldErrors();

        const nuevaUrl = `${ENDPOINTS.utilidades_producto}`
        dataTableUtilidadesProducto.ajax.url(nuevaUrl).load();
    }

    function graficoUtilidadesProducto() {
        const ctxProd = document.getElementById('graficoProducto').getContext('2d');
        miGraficoUtilidadesProducto = new Chart(ctxProd, {
            type: 'bar',
            data: { labels: [], datasets: [] },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'top' },
                },
                scales: { x: { beginAtZero: true } }
            }
        });
    }

    function actualizarDatosGraficoUtilidadProducto(datos) {
        if (!miGraficoUtilidadesProducto) return;

        const top10 = datos.slice(0, 10);

        const etiquetas = top10.map(d => d.producto);
        const utilidad = top10.map(d => d.utilidad);
        const ventas = top10.map(d => d.totalVenta);

        miGraficoUtilidadesProducto.data = {
            labels: etiquetas,
            datasets: [
                {
                    label: 'Utilidad',
                    data: utilidad,
                    backgroundColor: 'rgba(75, 192, 192, 0.7)',
                    borderColor: 'rgba(75, 192, 192, 1)',
                    borderWidth: 1
                },
                {
                    label: 'Ingreso Total',
                    data: ventas,
                    backgroundColor: 'rgba(54, 162, 235, 0.3)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1,
                }
            ]
        };
        miGraficoUtilidadesProducto.update();
    }

    //Notificaciones y errores

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

    function showFieldError(fieldName, message) {
        const field = $(`#${fieldName}`);
        const errorDiv = $(`#${fieldName}-error`);

        field.addClass('is-invalid');
        errorDiv.text(message).show();
    }

    function clearFieldErrors() {
        $('.form-control').removeClass('is-invalid');
        $('.invalid-feedback').text('');
        $('.invalid-feedback').removeClass('d-block');
        $('.invalid-feedback').css('display', '');
    }

});