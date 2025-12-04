$(document).ready(function () {

    let productos = [];
    let carrito = [];

    const API_BASE = 'productos/api';
    const ENDPOINTS = {
        LISTAR: `${API_BASE}/listar`,
        get: (id) => `${API_BASE}/${id}`,
        categorias: `${API_BASE}/categorias`,
        toggleStatus: (id) => `${API_BASE}/cambiar-estado/${id}`,
    };

    //inicializar componentes
    cargarProductos();
    loadCategorias();

    $(document).on('click', '.btn-detalles', function (e) {
        e.preventDefault();
        const idProducto = $(this).data('id');
        showModalDetalles(idProducto);
    });

    $('#btnCarrito').on('click', function () {
        mostrarCarrito(carrito);
    });

    $(document).on('click', '.btn-agregar', function (e) {
        e.preventDefault();
        const idProducto = $(this).data('id');
        const cantidad = parseInt($('#cantidad-producto').val()) || 1;
        agregarProducto(idProducto, cantidad);
    });

    $(document).on('click', '#btnVaciarCarrito', function () {
        limpiarCarrito();
        mostrarCarrito(carrito);
    });


    $('#id_categoria, #orden, #nombre').on('change keyup', filtrarProductos);

    // Función para cargar los productos desde el servidor
    function cargarProductos() {
        $.get(ENDPOINTS.LISTAR, function (respuesta) {
            productos = respuesta.data;
            renderizarProductos(productos);
        });
    }


    function renderizarProductos(productos) {
        const contenedor = document.getElementById('contenedor-productos');
        contenedor.innerHTML = "";

        if (!productos.length) {
            contenedor.innerHTML = `
                <div class="col mx-auto">
                    <div class="alert alert-warning text-center ">
                        No se encontraron productos
                    </div>
                </div>`;
            return;
        }

        productos.forEach(prod => {
            const isAgotado = prod.stock === 0;
            const productoCard = `
            <div class="col-12 col-sm-6 col-lg-3">
                <div class="card h-100 card-rounded border-0 shadow overflow-hidden ${isAgotado ? 'agotado-card' : ''}">
                    ${isAgotado ? '<span class="badge bg-danger text-white position-absolute top-50 start-50 translate-middle fs-5 z-1">Agotado</span>' : ''}
                    <div class="ratio ratio-4x3">
                    <img src="/Fotos-Productos/${prod.imagen}" 
                        class="card-img-top img-fluid object-fit-cover"
                        alt="${prod.nombre}">
                    </div>
                    <div class="card-body d-flex flex-column">
                        <h5 class="card-title fw-bold">${prod.nombre}</h5>
                        <p class="card-text text-secondary flex-grow-1 w-auto fw-bold">S/ ${prod.precioVenta.toFixed(2)}</p>
                        <a href="#"
                            class="btn text-white btn-sm btn-rounded mt-auto align-self-start m-auto btn-detalles"
                            data-id="${prod.id}" style="background-color:#061748">
                            Ver Detalles
                        </a>
                    </div>
                </div>
            </div>
        `;
            contenedor.innerHTML += productoCard;
        });
    }

    function showModalDetalles(idProducto) {
        const producto = productos.find(p => p.id == idProducto);

        if (producto) {
            const isAgotado = producto.stock === 0;
            const modalHtml = `
                <div class="modal fade" id="modal-Detalles" tabindex="-1" aria-hidden="true">
                    <div class="modal-dialog modal-lg modal-dialog-centered">
                        <div class="modal-content border-0 shadow-lg">

                            <!-- Header simple -->
                            <div class="modal-header border-bottom bg-light">
                                <h5 class="modal-title fw-bold text-dark">Detalles del Producto</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                            </div>

                            <div class="modal-body">
                                <div class="row">

                                    <!-- Imagen -->
                                    <div class="col-md-6 text-center">
                                        <img src="/Fotos-Productos/${producto.imagen}"
                                            class="img-fluid rounded shadow-sm"
                                            alt="${producto.nombre}"
                                            style="max-height: 300px; object-fit: contain;">
                                    </div>

                                    <!-- Detalles -->
                                    <div class="col-md-6">
                                        <h4 class="fw-bold text-black mb-2">${producto.nombre}</h4>
                                        <h3 class="fw-bold text-success mb-3">S/ ${producto.precioVenta.toFixed(2)}</h3>

                                        <div class="mb-4">
                                            <h6 class="fw-semibold text-muted mb-2 fw-bold">Descripción:</h6>
                                            <p class="text-dark">${producto.descripcion || "Producto de calidad disponible."}</p>
                                            <p class="text-dark">Stock: ${producto.stock}</p>

                                        </div>

                                        <!-- Cantidad -->
                                        <div class="d-flex align-items-center mb-3">
                                            <label for="cantidad-producto" class="form-label me-3 mb-0">Cantidad:</label>
                                            <input type="number" id="cantidad-producto" class="form-control w-25" value="1" min="1" max="${producto.stock}" ${isAgotado ? 'disabled' : ''}>
                                        </div>

                                        <!-- Botones -->
                                        <div class="d-grid gap-2">
                                            <button class="btn btn-primary btn-lg btn-agregar" data-id="${producto.id}" ${isAgotado ? 'disabled' : ''}>
                                                <i class="bi bi-cart-plus me-2"></i>Agregar al Carrito
                                            </button>
                                            <a id="btn-whatsapp" target="_blank" class="btn btn-success btn-lg">
                                                <i class="bi bi-whatsapp me-2"></i>Consultar por WhatsApp
                                            </a>
                                        </div>
                                    </div>

                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            `;

            // Limpiar contenedor y agregar modal
            $("#modal-container").html(modalHtml);

            // Configurar WhatsApp
            const mensaje = encodeURIComponent(`¡Hola! Estoy interesado en: ${producto.nombre} - S/ ${producto.precioVenta.toFixed(2)}`);
            $("#btn-whatsapp").attr("href", `https://wa.me/51913048853?text=${mensaje}`);

            // Mostrar modal
            const modal = new bootstrap.Modal(document.getElementById('modal-Detalles'));
            modal.show();
        }
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


    function filtrarProductos() {
        const categoriaId = $('#id_categoria').val();
        const orderable = $('#orden').val();
        const nombre = $('#nombre').val().toLowerCase();

        let productosFiltrados = productos.filter(prod => {
            const coincideCategoria = categoriaId ? prod.categoria.id == categoriaId : true;
            const coincideNombre = nombre ? prod.nombre.toLowerCase().includes(nombre) : true;
            return coincideCategoria && coincideNombre;
        });

        // Ordenar según selección
        if (orderable) {
            if (orderable === "precioASC") {
                productosFiltrados.sort((a, b) => a.precioVenta - b.precioVenta);
            } else if (orderable === "precioDESC") {
                productosFiltrados.sort((a, b) => b.precioVenta - a.precioVenta);
            } else if (orderable === "nombre-AZ") {
                productosFiltrados.sort((a, b) => a.nombre.localeCompare(b.nombre));
            } else if (orderable === "nombre-ZA") {
                productosFiltrados.sort((a, b) => b.nombre.localeCompare(a.nombre));
            }
        }

        renderizarProductos(productosFiltrados);
    }

    //Funciones para el carrito
    function agregarProducto(idProducto, cantidad = 1) {
        const prod = productos.find(p => p.id === idProducto);
        if (!prod) return; // No debería pasar si la UI está correcta

        const existente = carrito.find(item => item.id === idProducto);
        const cantidadEnCarrito = existente ? existente.cantidad : 0;
        const nuevaCantidadTotal = cantidadEnCarrito + cantidad;

        if (prod.stock === 0) {
            showNotification(`${prod.nombre} está agotado y no se puede agregar al carrito.`, 'danger');
            return;
        }

        if (nuevaCantidadTotal > prod.stock) {
            showNotification(`Stock insuficiente. Solo quedan ${prod.stock} unidades de ${prod.nombre}.`, 'danger');
            return;
        }

        if (existente) {
            existente.cantidad += cantidad;
        } else {
            carrito.push({ ...prod, cantidad: cantidad });
        }

        showNotification(`${prod.nombre} agregado al carrito`, 'success');
        actualizarContadorCarrito();
    }


    function actualizarContadorCarrito() {
        const total = carrito.reduce((acc, p) => acc + p.cantidad, 0);
        $('#cartCount').text(total);
    }

    function eliminarProducto(idProducto) {
        carrito = carrito.filter(item => item.id !== idProducto);

        showNotification("Producto eliminado", "danger");
        actualizarContadorCarrito();
        mostrarCarrito(carrito);
    }


    let carritoModal = null;

    function mostrarCarrito(carrito) {
        const tbody = $("#carrito-body");
        tbody.empty();

        if (carrito.length === 0) {
            tbody.append(`<tr><td colspan="5" class="text-center">Tu carrito está vacío</td></tr>`);
            $("#carrito-total").text("Total: S/ 0.00");
        } else {
            carrito.forEach(item => {
                let subtotal = item.precioVenta * item.cantidad;
                tbody.append(`
                    <tr>
                        <td>${item.nombre}</td>
                        <td>S/ ${item.precioVenta.toFixed(2)}</td>
                        <td><input type="number" class="form-control cantidad-carrito" data-id="${item.id}" value="${item.cantidad}" min="1" max="${item.stock}"></td>
                        <td>S/ ${subtotal.toFixed(2)}</td>
                        <td><button class="btn btn-sm btn-danger btn-eliminar" data-id="${item.id}">
                            <i class="bi bi-trash"></i> Eliminar
                        </button></td>
                    </tr>
                `);
            });

            let total = carrito.reduce((acc, item) => acc + (item.precioVenta * item.cantidad), 0);
            $("#carrito-total").text(`Total: S/ ${total.toFixed(2)}`);
        }

        if (!carritoModal) {
            carritoModal = new bootstrap.Modal(document.getElementById('carritoModal'));
        }
        carritoModal.show();

        $(document).off('click', '.btn-eliminar').on('click', '.btn-eliminar', function () {
            const idProducto = $(this).data('id');
            eliminarProducto(idProducto);
        });

        $(document).off('change', '.cantidad-carrito').on('change', '.cantidad-carrito', function () {
            const idProducto = $(this).data('id');
            const nuevaCantidad = parseInt($(this).val());
            actualizarCantidad(idProducto, nuevaCantidad);
        });
    }

    function actualizarCantidad(idProducto, cantidad) {
        const item = carrito.find(p => p.id === idProducto);
        if (item) {
            // El 'item' en el carrito ya tiene la propiedad 'stock' gracias al spread operator
            if (cantidad > item.stock) {
                showNotification(`Stock insuficiente. Solo quedan ${item.stock} unidades.`, 'danger');
                // No se actualiza la cantidad, y al llamar a mostrarCarrito se revierte el valor en el input.
            } else if (cantidad < 1) {
                item.cantidad = 1; // Forzar a 1 si el valor es inválido (menor a 1)
            } else {
                item.cantidad = cantidad;
            }
        }
        actualizarContadorCarrito();
        mostrarCarrito(carrito); // Refresca la vista del carrito para mostrar el valor correcto
    }

    function limpiarCarrito() {
        carrito = [];
        showNotification(`Carrito Limpiado`, 'success');
        actualizarContadorCarrito();
    }

    function finalizarCompra() {
        let mensaje = '¡Hola! Quisiera hacer el siguiente pedido:\n\n';
        let total = 0;
        carrito.forEach(item => {
            let subtotal = item.precioVenta * item.cantidad;
            mensaje += `${item.cantidad} x ${item.nombre} - S/ ${subtotal.toFixed(2)}\n`;
            total += subtotal;
        });
        mensaje += `\nTotal: S/ ${total.toFixed(2)}`;

        const link = `https://wa.me/51913048853?text=${encodeURIComponent(mensaje)}`;
        window.open(link, '_blank');
    }

    $(document).on('click', '#btnFinalizarCompra', finalizarCompra);

    function showNotification(message, type = 'success') {
        const toastClass = type === 'success' ? 'text-bg-success' : 'text-bg-danger';

        const notification = $(
            `<div class="toast align-items-center ${toastClass} border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">
                        ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>`
        );

        $('#notification-container').append(notification);

        const toast = new bootstrap.Toast(notification, {
            delay: 5000
        });
        toast.show();
    }

});