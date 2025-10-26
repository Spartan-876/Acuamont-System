$(document).ready(function () {

    // Variables
    let dataTable;
    let isEditing = false;
    let ventaModal;
    let cuotasModal;
    let pagosModal;
    let registrarPagoModal;
    let clienteSeleccionadoId = null;
    let productosDisponibles = [];
    let productosSeleccionados = [];
    let usuarioLogueadoId = null;

    // Configuración de las rutas de la API
    const API_CLIENTES = '/clientes/api';
    const API_USUARIOS = '/usuarios/api';
    const API_PRODUCTOS = '/productos/api';
    const API_PAGOS = '/pagos/api';
    const API_BASE = '/ventas/api';
    const ENDPOINTS = {
        usuario: `${API_USUARIOS}/usuarioLogueado`,
        list: `${API_BASE}/listar`,
        save: `${API_BASE}/guardar`,
        select_venta: (id) => `${API_BASE}/ventas_id/${id}`,
        actualizar: (id) => `${API_BASE}/actualizar/${id}`,
        delete: (id) => `${API_BASE}/eliminar/${id}`,
        serie_comprobantes: `${API_BASE}/serieComprobante`,
        formas_pago: `${API_BASE}/formaPago`,
        lista_Pagos: (id) => `${API_BASE}/pagos/${id}`,
        lista_Cuotas: (id) => `${API_BASE}/cuotas/${id}`,
        productos: `${API_PRODUCTOS}/listar`,
        guardar_cliente: `${API_CLIENTES}/guardar`,
        buscar_cliente: (documento) => `${API_CLIENTES}/buscar-cliente-documento/${documento}`,
        buscar_documento_externo: (documento) => `${API_CLIENTES}/buscar-documento/${documento}`,
        guardar_pago: `${API_PAGOS}/registrarPago`,
    };

    // Inicializar Componentes
    initializeDataTable();
    ventaModal = new bootstrap.Modal(document.getElementById('ventaModal'));
    cuotasModal = new bootstrap.Modal(document.getElementById('cuotasModal'));
    pagosModal = new bootstrap.Modal(document.getElementById('pagosModal'));
    registrarPagoModal = new bootstrap.Modal(document.getElementById('registrarPagoModal'));

    cargarUsuarioLogueado();
    cargarProductosDisponibles();
    loadSelects();
    setupEventListeners();

    // Funciones
    function initializeDataTable() {
        dataTable = $('#tablaVentas').DataTable({
            responsive: true,
            processing: true,
            serverSide: false,
            ajax: {
                url: ENDPOINTS.list,
                type: 'GET',
                dataSrc: 'data'
            },
            columns: [
                { data: 'id' },
                {
                    data: null,
                    render: (data, type, row) => `${row.serieComprobante.serie}-${row.correlativo}`
                },
                { data: 'cliente.nombre' },
                { data: 'usuario.nombre' },
                {
                    data: 'fecha',
                    render: (data) => new Date(data).toLocaleString('es-PE')
                },
                {
                    data: 'total',
                    render: (data) => `S/ ${(data ?? 0).toFixed(2)}`
                },
                {
                    data: 'formaPago.nombre',
                    render: function (data) {
                        if (data === 'Contado') return '<span class="badge bg-info">Contado</span>';
                        if (data === 'Credito') return '<span class="badge bg-warning">Crédito</span>';
                        return '';
                    }
                },
                {
                    data: 'deuda',
                    render: (data) => `S/ ${(data ?? 0).toFixed(2)}`
                },
                {
                    data: 'estado',
                    render: function (data) {
                        if (data === 0) return '<span class="badge bg-warning">Pendiente</span>';
                        if (data === 1) return '<span class="badge bg-success">Pagado</span>';
                        if (data === 2) return '<span class="badge bg-danger">Anulado</span>';
                        return '';
                    }
                },
                {
                    data: null,
                    orderable: false,
                    searchable: false,
                    render: (data, type, row) => createActionButtons(row)
                }
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
                    text: '<i class="bi bi-file-earmark-excel mt-1"></i> Exportar a Excel',
                    title: 'Listado de Ventas',
                    className: 'btn btn-success',
                    exportOptions: {
                        columns: [0, 1, 2, 3, 4, 5, 6, 7, 8],
                        modifier: {
                            page: 'all'
                        }
                    }
                },
                {
                    extend: 'pdfHtml5',
                    text: '<i class="bi bi-file-earmark-pdf"></i> Exportar a PDF',
                    title: 'Listado de Ventas',
                    className: 'btn btn-danger',
                    orientation: 'landscape',
                    pageSize: 'A4',
                    exportOptions: {
                        columns: [0, 1, 2, 3, 4, 5, 6, 7, 8],
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
                        columns: [0, 1, 2, 3, 4, 5, 6, 7, 8],
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
        let buttons = `
            <div class="d-flex gap-1">
                <button data-id="${row.id}" class="btn btn-sm btn-primary action-editarVenta" title="Editar Venta">
                    <i class="bi bi-pencil-square"></i>
                </button>
                <button data-id="${row.id}" class="btn btn-sm btn-danger action-eliminarVenta" title="Eliminar Venta">
                    <i class="bi bi-trash"></i>
                </button>
        `;

        if (row.formaPago.nombre.toLowerCase() === 'credito') {
            buttons += `
                <button data-id="${row.id}" class="btn btn-sm btn-success action-verCuotas" title="Ver Cuotas">
                    <i class="bi bi-calendar-week"></i>
                </button>
                <button data-id="${row.id}" class="btn btn-sm btn-info action-verPagos" title="Ver Pagos">
                    <i class="bi bi-cash"></i>
                </button>
            `;
        }

        buttons += `</div>`;
        return buttons;
    }

    function setupEventListeners() {
        // Abrir modal para nueva venta
        $('#btnNuevoRegistro').on('click', openModalForNew);

        $('#formVenta').on('submit', function (e) {
            e.preventDefault();
            saveVenta();
        });

        // Eventos de las tablas
        $('#tablaVentas tbody').on('click', '.action-eliminarVenta', eliminarVenta);
        $('#tablaVentas tbody').on('click', '.action-editarVenta', editarVenta);
        $('#tablaVentas tbody').on('click', '.action-verPagos', verPagos);
        $('#tablaVentas tbody').on('click', '.action-verCuotas', verCuotas);

        // Evento para cambio de forma de pago
        $('#forma_pago').on('change', function () {
            if (this.value === '2') {
                showContentCredito();
            } else {
                hideContentCredito();
            }
        });

        $(document).on('change', '#numero_cuotas, #fecha_inicio_credito, #intervalo_pago', generarInputsFechasCuotas);

        $(document).on('change', '.fecha-cuota', function () {
            const index = parseInt($(this).data('cuota-index'));
            const currentValue = $(this).val();
            const nextInput = $(`#fecha_cuota_${index + 1}`);

            if (nextInput.length) {
                let minDate = new Date(currentValue + 'T00:00:00');
                minDate.setDate(minDate.getDate() + 1);
                const minDateISO = minDate.toISOString().split('T')[0];
                nextInput.attr('min', minDateISO);

                if (nextInput.val() < minDateISO) {
                    nextInput.val(minDateISO);
                }
            }
        });

        // Evento para búsqueda de cliente por documento
        $('#btnBuscarCliente').on('click', buscarClientePorDocumento);

        // Evento para guardar pago
        $('#btnGuardarPago').on('click', guardarPago);

        $('#buscarProducto').on('input', function () {
            const termino = $(this).val().trim().toLowerCase();
            mostrarSugerencias(termino);
        });

        // Ocultar sugerencias al hacer clic fuera
        $(document).on('click', function (e) {
            if (!$(e.target).closest('#buscarProducto, #sugerenciasProductos').length) {
                $('#sugerenciasProductos').addClass('d-none');
            }
        });

        // Prevenir el envío del formulario al presionar Enter en el buscador
        $('#buscarProducto').on('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const primeraSugerencia = $('#sugerenciasProductos .sugerencia-item:first');
                if (primeraSugerencia.length) {
                    primeraSugerencia.click();
                }
            }
        });

    }

    function cargarUsuarioLogueado() {
        fetch(ENDPOINTS.usuario)
            .then(response => response.json())
            .then(data => {
                if (data.success && data.usuarioActual) {
                    usuarioLogueadoId = data.usuarioActual;
                } else {
                    usuarioLogueadoId = 1; // Fallback
                }
            })
            .catch(error => {
                console.error('Error al cargar usuario logueado:', error);
                usuarioLogueadoId = 1; // Fallback
            });
    }

    async function buscarClientePorDocumento() {
        const documento = $('#documento').val().trim();
        if (documento.length < 8) {
            showFieldError('documento', 'El documento debe tener al menos 8 digitos');
            return;
        } else {
            $('#documento').removeClass('is-invalid');
            $('#documento-error').text('');
        }

        showLoading(true);

        try {
            let localResp = await fetch(ENDPOINTS.buscar_cliente(documento));
            let localData = await localResp.json();

            if (localResp.ok && localData.success && localData.data) {
                $('#nombreCliente').val(localData.data.nombre);
                clienteSeleccionadoId = localData.data.id;
            } else {
                let externalResp = await fetch(ENDPOINTS.buscar_documento_externo(documento));
                let externalData = await externalResp.json();

                if (externalResp.ok && externalData.success && externalData.datos) {
                    const persona = externalData.datos;
                    const nombreCompleto = [
                        persona.nombres,
                        persona.ape_paterno,
                        persona.ape_materno
                    ].filter(Boolean).join(' ');

                    let createResp = await fetch(ENDPOINTS.guardar_cliente, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            nombre: nombreCompleto,
                            documento: documento,
                            telefono: '',
                            correo: ''
                        })
                    });
                    let nuevoCliente = await createResp.json();

                    if (!createResp.ok || !nuevoCliente.cliente?.id) {
                        throw new Error('No se pudo crear el nuevo cliente');
                    }

                    $('#nombreCliente').val(nombreCompleto);
                    clienteSeleccionadoId = nuevoCliente.cliente.id;
                    showNotification('Cliente encontrado en API externa y creado localmente', 'success');
                } else {
                    $('#nombreCliente').val('');
                    clienteSeleccionadoId = null;
                    showNotification('Cliente no encontrado', 'error');
                }
            }
        } catch (error) {
            console.error('Error al buscar cliente:', error);
            $('#nombreCliente').val('');
            clienteSeleccionadoId = null;
            showNotification('Error al buscar cliente: ' + error.message, 'error');
        } finally {
            showLoading(false);
        }
    }

    function loadSelects() {
        // Cargar Serie Comprobantes
        fetch(ENDPOINTS.serie_comprobantes)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const select = $('#serie_comprobante');
                    select.empty().append(new Option('Seleccione una serie', ''));
                    data.data.forEach(item => {
                        select.append(new Option(`${item.nombre}(${item.serie})`, item.id));
                    });

                    // Evento para cuando se selecciona una serie
                    select.on('change', function () {
                        const serieId = $(this).val();
                        if (serieId) {
                            cargarCorrelativoActual(serieId);
                        } else {
                            $('#correlativo_venta').val('');
                        }
                    });
                } else {
                    showNotification('Error al cargar series de comprobantes', 'error');
                }
            }).catch(error => {
                console.error('Error cargar series de comprobantes:', error);
            });

        // Cargar Formas de Pago
        fetch(ENDPOINTS.formas_pago)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const select = $('#forma_pago');
                    select.empty().append(new Option('Seleccione una forma de pago', ''));
                    data.data.forEach(item => {
                        select.append(new Option(item.nombre, item.id));
                    });
                } else {
                    showNotification('Error al cargar formas de pago', 'error');
                }
            }).catch(error => {
                console.error('Error cargar formas de pago:', error);
            });
    }

    async function saveVenta() {
        const formData = prepareFormData();

        if (!clienteSeleccionadoId) {
            showNotification('Debe seleccionar un cliente válido antes de guardar la venta.', 'error');
            return;
        }

        formData.clienteId = clienteSeleccionadoId;

        if (!validateForm(formData)) return;

        showLoading(true);

        try {
            let ventaResp;

            if (isEditing) {
                const ventaId = $('#id').val();
                ventaResp = await fetch(ENDPOINTS.actualizar(ventaId), {
                    method: "PUT",
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(formData)
                });
            } else {
                ventaResp = await fetch(ENDPOINTS.save, {
                    method: "POST",
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(formData)
                });
            }

            const ventaGuardada = await ventaResp.json();

            if (!ventaResp.ok || !ventaGuardada.success) {
                throw new Error(ventaGuardada.message || 'Error al guardar la venta');
            }


            hideModal();
            showNotification('Venta guardada exitosamente', 'success');
            dataTable.ajax.reload();
            clearForm();
            clienteSeleccionadoId = null; // Resetear para la siguiente venta
            isEditing = false; // Resetear para la siguiente venta
        } catch (error) {
            console.error('Error al guardar la venta:', error);
            showNotification('Error al guardar la venta: ' + error.message, 'error');
        } finally {
            showLoading(false);
        }
    }

    function prepareFormData() {
        const total = productosSeleccionados.reduce((sum, producto) => sum + producto.subtotal, 0);
        const formData = {
            clienteId: clienteSeleccionadoId,
            usuarioId: usuarioLogueadoId || 1,
            serieComprobanteId: $('#serie_comprobante').val(),
            formaPagoId: $('#forma_pago').val(),
            detalles: productosSeleccionados.map(producto => ({
                productoId: producto.id,
                cantidad: producto.cantidad,
                precio: producto.precio
            })),
            total: total
        };

        // Si es crédito, agregar campos adicionales  
        if ($('#forma_pago').val() === '2') {
            const montoInicial = parseFloat($('#monto_inicial').val()) || 0;
            formData.montoInicial = montoInicial;
            formData.planDeCuotas = generarPlanDeCuotas(total - montoInicial);
        }
        return formData;
    }

    function generarPlanDeCuotas(montoDeuda) {
        const numeroCuotas = parseInt($('#numero_cuotas').val()) || 0;
        const cuotas = [];

        if (numeroCuotas <= 0) {
            return [];
        }

        const montoCuota = montoDeuda / numeroCuotas;

        for (let i = 1; i <= numeroCuotas; i++) {
            const fechaVencimiento = $(`#fecha_cuota_${i}`).val();
            cuotas.push({
                monto: parseFloat(montoCuota.toFixed(2)),
                fechaVencimiento: fechaVencimiento
            });
        }

        return cuotas;
    }

    function generarInputsFechasCuotas() {
        const container = $('#cuotas-fechas-container');
        container.empty();

        const numeroCuotas = parseInt($('#numero_cuotas').val()) || 0;
        const fechaInicioCreditoStr = $('#fecha_inicio_credito').val();
        const intervalo = parseInt($('#intervalo_pago').val()) || 30;

        if (numeroCuotas <= 0 || !fechaInicioCreditoStr) {
            return;
        }

        let fechaCalculada = new Date(fechaInicioCreditoStr + 'T00:00:00');
        let minDate = fechaInicioCreditoStr;

        for (let i = 1; i <= numeroCuotas; i++) {
            fechaCalculada.setDate(fechaCalculada.getDate() + intervalo);
            const fechaISO = fechaCalculada.toISOString().split('T')[0];

            let d = new Date(minDate + 'T00:00:00');
            d.setDate(d.getDate() + 1);
            let minDateISO = d.toISOString().split('T')[0];

            const inputHtml = `
                <div class="col-md-4">
                    <label for="fecha_cuota_${i}" class="form-label">Fecha Cuota ${i}:</label>
                    <input type="date" id="fecha_cuota_${i}" name="fecha_cuota_${i}" class="form-control fecha-cuota" data-cuota-index="${i}" value="${fechaISO}" min="${minDateISO}">
                    <div id="fecha_cuota_${i}-error" class="invalid-feedback d-block"></div>
                </div>
            `;
            container.append(inputHtml);
            minDate = fechaISO;
        }
    }

    function validateForm(formData) {
        let hasErrors = false;
        clearFieldErrors();

        if (!formData.clienteId) {
            showFieldError('documento', 'Ingrese un Dni valido')
        }

        if (!formData.serieComprobanteId) {
            showFieldError('serie_comprobante', 'La serie del comprobante es obligatoria.');
            hasErrors = true;
        }

        const serieText = $('#serie_comprobante option:selected').text().toLowerCase();
        const documento = $('#documento').val().trim();

        if (serieText.includes('factura') && documento.length !== 11) {
            showFieldError('documento', 'Para Factura, el Documento debe tener 11 dígitos.');
            hasErrors = true;
        }

        if (!formData.formaPagoId) {
            showFieldError('forma_pago', 'La forma de pago es obligatoria.');
            hasErrors = true;
        }

        if (formData.formaPagoId === '2') {
            const montoInicial = parseFloat($('#monto_inicial').val()) || 0;
            const numeroCuotas = parseInt($('#numero_cuotas').val()) || 0;
            const fechaInicioCredito = $('#fecha_inicio_credito').val();
            const totalVenta = productosSeleccionados.reduce((sum, producto) => sum + producto.subtotal, 0);

            if (montoInicial < 0) {
                showFieldError('monto_inicial', 'El monto inicial no puede ser negativo.');
                hasErrors = true;
            }

            if (montoInicial >= totalVenta) {
                showFieldError('monto_inicial', 'El monto inicial no puede ser mayor o igual al total de la venta.');
                hasErrors = true;
            }

            if (numeroCuotas <= 0) {
                showFieldError('numero_cuotas', 'El número de cuotas debe ser mayor a 0.');
                hasErrors = true;
            }

            if (!fechaInicioCredito) {
                showFieldError('fecha_inicio_credito', 'La fecha de inicio del crédito es obligatoria.');
                hasErrors = true;
            }

            const hoy = new Date();
            hoy.setHours(0, 0, 0, 0);

            // Validar consistencia de montos y fechas de cuotas
            if (formData.planDeCuotas && formData.planDeCuotas.length > 0) {
                let hasDateErrors = false;
                let lastDate = new Date(fechaInicioCredito + 'T00:00:00');

                formData.planDeCuotas.forEach((cuota, index) => {
                    const fieldName = `fecha_cuota_${index + 1}`;
                    const fechaVencimiento = new Date(cuota.fechaVencimiento + 'T00:00:00');

                    if (!cuota.fechaVencimiento) {
                        showFieldError(fieldName, 'La fecha es obligatoria.');
                        hasDateErrors = true;
                    } else if (fechaVencimiento <= lastDate) {
                        showFieldError(fieldName, `La fecha debe ser posterior a la fecha anterior.`);
                        hasDateErrors = true;
                    }
                    lastDate = fechaVencimiento;
                });

                if (hasDateErrors) {
                    hasErrors = true;
                }

                const sumaCuotas = formData.planDeCuotas.reduce((sum, cuota) => sum + cuota.monto, 0);
                const totalPagado = montoInicial + sumaCuotas;

                if (Math.abs(totalVenta - totalPagado) > 0.01) {
                    showNotification(
                        `Error de consistencia: El total de la venta (S/ ${totalVenta.toFixed(2)}) no coincide con la suma del monto inicial y las cuotas (S/ ${totalPagado.toFixed(2)}).`,
                        'error'
                    );
                    hasErrors = true;
                }
            } else if (numeroCuotas > 0) {
                showNotification('No se generaron las fechas de las cuotas. Verifique los datos de crédito.', 'error');
                hasErrors = true;
            }
        }

        if (productosSeleccionados.length === 0) {
            showNotification('Debe agregar al menos un producto a la venta.', 'error');
            hasErrors = true;
        }

        return !hasErrors;
    }

    function editarVenta(e) {
        e.preventDefault();
        const ventaId = $(this).data('id');

        showLoading(true);

        fetch(ENDPOINTS.select_venta(ventaId))
            .then(response => {
                if (!response.ok) {
                    throw new Error('Venta no encontrada');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    const venta = data.data;
                    if (venta) {
                        openModalForEdit(venta);
                    } else {
                        throw new Error('Venta no encontrada en la lista');
                    }
                } else {
                    throw new Error('Error en la respuesta del servidor');
                }
            })
            .catch(error => {
                console.error('Error al cargar la venta:', error);
                showNotification('Error al cargar la venta.', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    function openModalForEdit(venta) {
        isEditing = true;
        clearForm();
        $('#modalTitle').text(`Editar Venta ${venta.id}`);

        $('#id').val(venta.id);
        $('#documento').val(venta.cliente.documento);
        $('#nombreCliente').val(venta.cliente.nombre);
        clienteSeleccionadoId = venta.cliente.id;
        $('#serie_comprobante').val(venta.serieComprobante.id);

        productosSeleccionados = venta.detalleVentas.map(detalle => ({
            id: detalle.producto.id,
            nombre: detalle.producto.nombre,
            cantidad: detalle.cantidad,
            precio: detalle.precioUnitario ? parseFloat(detalle.precioUnitario.toString()) : parseFloat(detalle.producto.precioVenta.toString()),
            subtotal: detalle.cantidad * (detalle.precioUnitario ? parseFloat(detalle.precioUnitario.toString()) : parseFloat(detalle.producto.precioVenta.toString())),
            stockDisponible: (detalle.producto.stock + detalle.cantidad)
        }));

        $('#forma_pago').val(venta.formaPago.id);
        $('#forma_pago').trigger('change');

        if (String(venta.formaPago.id) === '2') {
            $('#monto_inicial').val(venta.montoInicial || 0);
            if (venta.cuotas && venta.cuotas.length > 0) {
                $('#numero_cuotas').val(venta.cuotas.length);

                const firstDueDate = new Date(venta.cuotas[0].fechaVencimiento + 'T00:00:00');
                const intervalo = 30; 
                const startDate = new Date(firstDueDate.setDate(firstDueDate.getDate() - intervalo));
                $('#fecha_inicio_credito').val(startDate.toISOString().split('T')[0]);

                generarInputsFechasCuotas();

                venta.cuotas.forEach((cuota, index) => {
                    const cuotaIndex = index + 1;
                    const dueDate = new Date(cuota.fechaVencimiento + 'T00:00:00');
                    $(`#fecha_cuota_${cuotaIndex}`).val(dueDate.toISOString().split('T')[0]);
                });
            } else {
                $('#numero_cuotas').val(1);
            }
        }

        actualizarTablaProductosSeleccionados();
        actualizarTotalVenta();
        showModal();
    }

    function eliminarVenta(e) {
        e.preventDefault();
        const ventaId = $(this).data('id');

        Swal.fire({
            title: '¿Estas seguro?',
            text: "¡No podras revertir esta accion!",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Si, ¡eliminar!',
            cancelButtonText: 'Cancelar'
        })
            .then((result) => {
                if (result.isConfirmed) {
                    showLoading(true);
                    fetch(ENDPOINTS.delete(ventaId), {
                        method: 'DELETE'
                    })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error('Error al eliminar venta');
                            }
                            return response.json();
                        })
                        .then(ventaEliminada => {
                            showNotification('Venta eliminada exitosamente', 'success');
                            dataTable.ajax.reload();
                        })
                        .catch(error => {
                            console.error('Error al eliminar venta:', error);
                            showNotification('Error al eliminar la venta: ' + error.message, 'error');
                        })
                        .finally(() => {
                            showLoading(false);
                        });
                }
            });
    }

    function cargarCorrelativoActual(serieId) {
        fetch(ENDPOINTS.serie_comprobantes)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const serie = data.data.find(item => item.id == serieId);
                    if (serie) {
                        // Sumar 1 al correlativo actual para mostrar el próximo número
                        const siguienteCorrelativo = serie.correlativo_actual + 1;
                        $('#correlativo_venta').val(siguienteCorrelativo);
                    }
                } else {
                    showNotification('Error al obtener el correlativo', 'error');
                }
            })
            .catch(error => {
                console.error('Error al cargar correlativo:', error);
                showNotification('Error al cargar el correlativo', 'error');
            });
    }

    function verPagos(e) {
        e.preventDefault();
        const ventaId = $(this).data('id');
        ventaActualId = ventaId;

        showLoading(true);

        // Primero obtener los datos de la venta, luego los pagos
        obtenerDatosVenta(ventaId)
            .then(venta => {
                return fetch(ENDPOINTS.lista_Pagos(ventaId))
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Error al cargar pagos');
                        }
                        return response.json();
                    })
                    .then(pagos => {
                        mostrarPagosConDatos(venta, pagos);
                    });
            })
            .catch(error => {
                console.error('Error al cargar pagos:', error);
                showNotification('Error al cargar los pagos: ' + error.message, 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    function verCuotas(e) {
        e.preventDefault();
        const ventaId = $(this).data('id');
        ventaActualId = ventaId;

        showLoading(true);

        // Primero obtener los datos de la venta, luego las cuotas
        obtenerDatosVenta(ventaId)
            .then(venta => {
                return fetch(ENDPOINTS.lista_Cuotas(ventaId))
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Error al cargar cuotas');
                        }
                        return response.json();
                    })
                    .then(cuotas => {
                        mostrarCuotasConDatos(venta, cuotas);
                    });
            })
            .catch(error => {
                console.error('Error al cargar cuotas:', error);
                showNotification('Error al cargar las cuotas: ' + error.message, 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    function obtenerDatosVenta(ventaId) {
        return fetch(ENDPOINTS.list)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al cargar ventas');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    const venta = data.data.find(v => v.id == ventaId);
                    if (venta) {
                        return venta;
                    } else {
                        throw new Error('Venta no encontrada');
                    }
                } else {
                    throw new Error('Error en la respuesta del servidor');
                }
            });
    }

    function mostrarPagosConDatos(venta, pagos) {
        $('#pagoVentaId').text(venta.id || 'N/A');
        $('#pagoClienteNombre').text(venta.cliente?.nombre || 'No disponible');

        const tbody = $('#pagosTableBody');
        tbody.empty();

        if (!pagos || pagos.length === 0) {
            tbody.append(`
                <tr>
                    <td colspan="5" class="text-center text-muted py-4">
                        <i class="bi bi-info-circle me-2"></i>
                        No se encontraron pagos para esta venta
                    </td>
                </tr>
            `);
        } else {
            pagos.forEach(pago => {
                const estado = '<span class="badge bg-success">Completado</span>';
                const numeroCuota = pago.cuota ? (pago.cuota.numeroCuota || 'N/A') : 'N/A';
                const fechaVencimiento = pago.cuota && pago.cuota.fechaVencimiento ?
                    new Date(pago.cuota.fechaVencimiento).toLocaleDateString('es-PE') : 'N/A';

                const row = `
                    <tr>
                        <td>${numeroCuota}</td>
                        <td>${fechaVencimiento}</td>
                        <td>S/ ${pago.montoPagado?.toFixed(2) || '0.00'}</td>
                        <td>${estado}</td>
                        <td>${pago.metodoPago || 'N/A'}</td>
                    </tr>
                `;
                tbody.append(row);
            });
        }

        pagosModal.show();
    }

    function mostrarCuotasConDatos(venta, cuotas) {
        $('#cuotaVentaId').text(venta.id || 'N/A');
        $('#cuotaClienteNombre').text(venta.cliente?.nombre || 'No disponible');

        const tbody = $('#cuotasTableBody');
        tbody.empty();

        if (!cuotas || cuotas.length === 0) {
            tbody.append(`
                <tr>
                    <td colspan="5" class="text-center text-muted py-4">
                        <i class="bi bi-info-circle me-2"></i>
                        No se encontraron cuotas para esta venta
                    </td>
                </tr>
            `);
        } else {
            cuotas.forEach((cuota, index) => {
                const estado = getEstadoCuota(cuota);
                const numeroCuota = index + 1;

                const row = `
                    <tr class="${cuota.estado === 1 ? 'table-success' : getEstadoCuota(cuota).includes('Vencido') ? 'table-danger' : ''}">
                        <td>${numeroCuota}</td>
                        <td>${cuota.fechaVencimiento ? new Date(cuota.fechaVencimiento).toLocaleDateString('es-PE') : 'N/A'}</td>
                        <td>S/ ${cuota.monto?.toFixed(2) || '0.00'}</td>
                        <td>${estado}</td>
                        <td>
                            ${cuota.estado !== 1 ?
                        `<button class="btn btn-sm btn-success btn-pagar-cuota" 
                                        data-cuota-id="${cuota.id}" 
                                        data-monto="${cuota.saldo || cuota.monto || 0}"
                                        data-numero="${numeroCuota}">
                                    <i class="bi bi-cash"></i> Pagar
                                </button>` :
                        '<span class="badge bg-success">Pagado</span>'
                    }
                        </td>
                    </tr>
                `;
                tbody.append(row);
            });

            // Event listeners para botones de pagar
            $('.btn-pagar-cuota').on('click', function () {
                const cuotaId = $(this).data('cuota-id');
                const monto = $(this).data('monto');
                const numeroCuota = $(this).data('numero');
                abrirModalRegistrarPago(cuotaId, monto, numeroCuota);
            });
        }

        cuotasModal.show();
    }

    function abrirModalRegistrarPago(cuotaId, monto, numeroCuota) {
        $('#pago_cuota_id').val(cuotaId);
        $('#info_cuota').text(`Cuota ${numeroCuota}`);
        $('#info_monto').text(`S/ ${monto.toFixed(2)}`);
        $('#metodo_pago').val('');

        registrarPagoModal.show();
    }

    function guardarPago() {
        const formData = {
            cuotaId: $('#pago_cuota_id').val(),
            montoPagado: parseFloat($('#info_monto').text().replace('S/ ', '')),
            metodoPago: $('#metodo_pago').val(),
            comentario: $('#comentario').val()
        };

        if (!formData.metodoPago) {
            showFieldError('metodo_pago', 'Seleccione un método de pago.');
            return;
        }

        showLoading(true);

        fetch(ENDPOINTS.guardar_pago, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en el registro del pago');
                }
                return response.json();
            })
            .then(ventaActualizada => {
                showNotification('Pago registrado exitosamente', 'success');
                registrarPagoModal.hide();
                cuotasModal.hide();

                dataTable.ajax.reload();
            })
            .catch(error => {
                console.error('Error al registrar pago:', error);
                showNotification('Error al registrar el pago: ' + error.message, 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    function getEstadoCuota(cuota) {
        if (!cuota) return '<span class="badge bg-secondary">Desconocido</span>';

        const hoy = new Date();
        const vencimiento = cuota.fechaVencimiento ? new Date(cuota.fechaVencimiento) : null;

        if (cuota.estado === 1) {
            return '<span class="badge bg-success">Pagado</span>';
        } else if (vencimiento && vencimiento < hoy) {
            return '<span class="badge bg-danger">Vencido</span>';
        } else {
            return '<span class="badge bg-warning">Pendiente</span>';
        }
    }

    function actualizarTablaProductosSeleccionados() {
        const tbody = $('#tablaProductosSeleccionados');
        tbody.empty();

        productosSeleccionados.forEach((producto, index) => {
            const row = `
                <tr>
                    <td>${producto.nombre}</td>
                    <td>
                        <div class="input-group input-group-sm">
                            <button class="btn btn-outline-secondary btn-restar" type="button" data-index="${index}">-</button>
                            <input type="number" class="form-control text-center input-cantidad" 
                                value="${producto.cantidad}" min="1" max="${producto.stockDisponible}" 
                                data-index="${index}">
                            <button class="btn btn-outline-secondary btn-sumar" type="button" data-index="${index}">+</button>
                        </div>
                    </td>
                    <td>S/ ${producto.precio.toFixed(2)}</td>
                    <td>S/ ${producto.subtotal.toFixed(2)}</td>
                    <td>
                        <button class="btn btn-sm btn-danger btn-eliminar-producto" data-index="${index}">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
            tbody.append(row);
        });

        $('.btn-restar').on('click', function () {
            const index = $(this).data('index');
            cambiarCantidadProducto(index, -1);
        });

        $('.btn-sumar').on('click', function () {
            const index = $(this).data('index');
            cambiarCantidadProducto(index, 1);
        });

        $('.input-cantidad').on('change', function () {
            const index = $(this).data('index');
            const nuevaCantidad = parseInt($(this).val());
            actualizarCantidadProducto(index, nuevaCantidad);
        });

        $('.btn-eliminar-producto').on('click', function () {
            const index = $(this).data('index');
            eliminarProducto(index);
        });
    }

    function agregarProductoAVenta(producto) {
        const productoExistente = productosSeleccionados.find(p => p.id === producto.id);

        if (productoExistente) {
            if (productoExistente.cantidad < producto.stock) {
                productoExistente.cantidad += 1;
                productoExistente.subtotal = productoExistente.cantidad * productoExistente.precio;
            } else {
                showNotification(`No hay suficiente stock de ${producto.nombre}. Stock disponible: ${producto.stock}`, 'error');
                return;
            }
        } else {
            productosSeleccionados.push({
                id: producto.id,
                nombre: producto.nombre,
                cantidad: 1,
                precio: producto.precioVenta,
                subtotal: producto.precioVenta,
                stockDisponible: producto.stock
            });
        }

        actualizarTablaProductosSeleccionados();
        actualizarTotalVenta();
    }

    function cambiarCantidadProducto(index, cambio) {
        const producto = productosSeleccionados[index];
        const nuevaCantidad = producto.cantidad + cambio;

        if (nuevaCantidad >= 1 && nuevaCantidad <= producto.stockDisponible) {
            producto.cantidad = nuevaCantidad;
            producto.subtotal = producto.cantidad * producto.precio;

            actualizarTablaProductosSeleccionados();
            actualizarTotalVenta();
        } else if (nuevaCantidad < 1) {
            eliminarProducto(index);
        } else {
            showNotification(`No hay suficiente stock. Stock disponible: ${producto.stockDisponible}`, 'error');
        }
    }

    function actualizarCantidadProducto(index, nuevaCantidad) {
        if (nuevaCantidad < 1) {
            eliminarProducto(index);
            return;
        }

        const producto = productosSeleccionados[index];

        if (nuevaCantidad > producto.stockDisponible) {
            showNotification(`No hay suficiente stock de ${producto.nombre}. Stock disponible: ${producto.stockDisponible}`, 'error');
            return;
        }

        producto.cantidad = nuevaCantidad;
        producto.subtotal = producto.cantidad * producto.precio;

        actualizarTablaProductosSeleccionados();
        actualizarTotalVenta();
    }

    function cargarProductosDisponibles() {
        fetch(ENDPOINTS.productos)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    productosDisponibles = data.data;
                    actualizarDatalistProductos();
                } else {
                    showNotification('Error al cargar productos', 'error');
                }
            })
            .catch(error => {
                console.error('Error al cargar productos:', error);
            });
    }

    function actualizarDatalistProductos() {
        const datalist = $('#listaProductos');
        datalist.empty();

        productosDisponibles.forEach(producto => {
            if (producto.stock > 0) {
                datalist.append(new Option(
                    `${producto.nombre} - Stock: ${producto.stock} - S/ ${producto.precioVenta.toFixed(2)}`,
                    producto.nombre
                ));
            }
        });
    }

    function mostrarSugerencias(termino) {
        const contenedor = $('#sugerenciasProductos');
        contenedor.empty();

        if (termino.length < 2) {
            contenedor.addClass('d-none');
            return;
        }

        const productosFiltrados = productosDisponibles.filter(producto =>
            producto.nombre.toLowerCase().includes(termino) && producto.stock > 0
        );

        if (productosFiltrados.length === 0) {
            contenedor.append(`
                <div class="p-3 text-muted text-center">
                    <i class="bi bi-search"></i> No se encontraron productos
                </div>
            `);
        } else {
            productosFiltrados.forEach(producto => {
                const item = $(`
                    <div class="sugerencia-item p-3 border-bottom" 
                        style="cursor: pointer;" 
                        data-producto-id="${producto.id}">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <strong>${producto.nombre}</strong>
                                <div class="text-muted small">
                                    Stock: ${producto.stock} • S/ ${producto.precioVenta.toFixed(2)}
                                </div>
                            </div>
                            <i class="bi bi-plus-circle text-primary"></i>
                        </div>
                    </div>
                `);

                item.on('click', function () {
                    agregarProductoAVenta(producto);
                    $('#buscarProducto').val('');
                    contenedor.addClass('d-none');
                });

                item.on('mouseenter', function () {
                    $(this).css('background-color', '#f8f9fa');
                });

                item.on('mouseleave', function () {
                    $(this).css('background-color', '');
                });

                contenedor.append(item);
            });
        }

        contenedor.removeClass('d-none');
    }

    function eliminarProducto(index) {
        productosSeleccionados.splice(index, 1);
        actualizarTablaProductosSeleccionados();
        actualizarTotalVenta();
    }

    function actualizarTotalVenta() {
        const total = productosSeleccionados.reduce((sum, producto) => sum + producto.subtotal, 0);
        $('#totalVenta').text(`S/ ${total.toFixed(2)}`);
        $('#total').val(total.toFixed(2));
    }

    function showFieldError(fieldName, message) {
        const field = $(`#${fieldName}`);
        const errorDiv = $(`#${fieldName}-error`);

        field.addClass('is-invalid');
        errorDiv.text(message).show();
    }

    function clearFieldErrors() {
        $('.form-control').removeClass('is-invalid');
        $('.invalid-feedback').hide();
    }

    function openModalForNew() {
        isEditing = false;
        clearForm();
        $('#modalTitle').text('Nueva Venta');
        showModal();
    }

    function clearForm() {
        $('#formVenta')[0].reset();
        clearFieldErrors();
        productosSeleccionados = [];
        clienteSeleccionadoId = null;
        hideContentCredito();
        actualizarTablaProductosSeleccionados();
        actualizarTotalVenta();
        $('#correlativo_venta').val('');
    }

    function showContentCredito() {
        const contenedorCredito = $('#conteiner-credito');
        const contenido = `
            <div class="row g-3">
                <div class="col-md-4">
                    <label for="monto_inicial" class="form-label">Pago inicial:</label>
                    <input type="number" id="monto_inicial" name="monto_inicial" class="form-control" min="0" step="0.01">
                    <div id="monto_inicial-error" class="invalid-feedback d-block"></div>
                </div>
                <div class="col-md-4">
                    <label for="numero_cuotas" class="form-label">N° Cuotas:</label>
                    <input type="number" id="numero_cuotas" name="numero_cuotas" class="form-control" min="1" value="1">
                    <div id="numero_cuotas-error" class="invalid-feedback d-block"></div>
                </div>
                <div class="col-md-4">
                    <label for="intervalo_pago" class="form-label">Intervalo de pago:</label>
                    <select name="intervalo_pago" class="form-select" id="intervalo_pago">
                        <option value="7">Semanal (7 días)</option>
                        <option value="15">Quincenal (15 días)</option>
                        <option value="30" selected>Mensual (30 días)</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label for="fecha_inicio_credito" class="form-label">Fecha Inicio Crédito:</label>
                    <input type="date" id="fecha_inicio_credito" name="fecha_inicio_credito" class="form-control">
                    <div id="fecha_inicio_credito-error" class="invalid-feedback d-block"></div>
                </div>
            </div>
            <div id="cuotas-fechas-container" class="row g-3 mt-2"></div>
        `;
        contenedorCredito.html(contenido);

        const hoy = new Date();
        const anio = hoy.getFullYear();
        const mes = String(hoy.getMonth() + 1).padStart(2, '0');
        const dia = String(hoy.getDate()).padStart(2, '0');

        const fechaLocalDeHoy = `${anio}-${mes}-${dia}`;

        $('#fecha_inicio_credito').val(fechaLocalDeHoy);

        generarInputsFechasCuotas();
    }

    function hideContentCredito() {
        $('#conteiner-credito').empty();
    }

    function showModal() {
        ventaModal.show();
    }

    function hideModal() {
        ventaModal.hide();
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

});