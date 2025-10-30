/**
 * Script para la gestión de usuarios con Bootstrap 5
 * Archivo: src/main/resources/static/js/usuarios.js
 */

$(document).ready(function () {
    // Variables globales
    let dataTable;
    let isEditing = false;
    let usuarioModal;
    let modal2FA;
    let usuario;


    // Configuración inicial
    const API_BASE = '/usuarios/api';
    const ENDPOINTS = {
        usuarioLogin: `${API_BASE}/usuarioLogueado`,
        usuario: (id) => `${API_BASE}/${id}`,
        list: `${API_BASE}/listar`,
        save: `${API_BASE}/guardar`,
        get: (id) => `${API_BASE}/${id}`,
        delete: (id) => `${API_BASE}/eliminar/${id}`,
        profiles: `${API_BASE}/perfiles`,
        toggleStatus: (id) => `${API_BASE}/cambiar-estado/${id}`,
        generate2fa: (id) => `${API_BASE}/generar-2fa/${id}`,
        verify2fa: `${API_BASE}/verificar-2fa`,
    };

    cargarDatosUsuarioLogueado();

    // Inicializar DataTable
    initializeDataTable();

    // Inicializar Modal de Bootstrap
    usuarioModal = new bootstrap.Modal(document.getElementById('usuarioModal'));
    modal2FA = new bootstrap.Modal(document.getElementById('modal2FA'));

    // Cargar perfiles para el select
    loadProfiles();

    // Event Listeners
    setupEventListeners();

    /**
     * Inicializa DataTable con configuración completa
     */
    function initializeDataTable() {
        dataTable = $('#tablaUsuarios').DataTable({
            responsive: true,
            processing: true,
            ajax: {
                url: ENDPOINTS.list,
                dataSrc: 'data' // La propiedad en la respuesta JSON que contiene el array de usuarios
            },
            columns: [
                { data: 'id' },
                { data: 'nombre' },
                { data: 'usuario' },
                { data: 'perfil.nombre' }, // Nueva columna para el perfil
                { data: 'correo' },
                {
                    data: 'estado',
                    render: function (data, type, row) {
                        return data === 1
                            ? '<span class="badge text-bg-success">Activo</span>' // estado 1
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
                { responsivePriority: 1, targets: 1 }, // Nombre
                { responsivePriority: 2, targets: 6 }, // Acciones
            ],
            language: {
                url: 'https://cdn.datatables.net/plug-ins/1.13.4/i18n/es-ES.json',
            },
            pageLength: 10
        });
    }

    /**
     * Crea los botones de acción para cada fila de la tabla
     */
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
                    <button data-id="${row.id}" class="btn btn-sm ${row.usa2FA ? 'btn-info' : 'btn-secondary'} action-2fa" title="Configurar 2FA">
                        <i class="bi bi-shield-lock-fill"></i>
                    </button>
                </div>
            `;
    }

    /**
     * Configura todos los event listeners
     */
    function setupEventListeners() {
        // Botón nuevo registro
        $('#btnNuevoRegistro').on('click', openModalForNew);

        // No es necesario un listener para cerrar el modal, Bootstrap lo maneja con data-bs-dismiss

        // Submit form
        $('#formUsuario').on('submit', function (e) {
            e.preventDefault();
            saveUsuario();
        });

        // Eventos de la tabla (delegados)
        $('#tablaUsuarios tbody').on('click', '.action-edit', handleEdit);
        $('#tablaUsuarios tbody').on('click', '.action-status', handleToggleStatus);
        $('#tablaUsuarios tbody').on('click', '.action-delete', handleDelete);
        $('#tablaUsuarios tbody').on('click', '.action-2fa', handleSetup2FA);

        $('#formVerificar2FA').on('submit', function (e) {
            e.preventDefault();
            verifyAndEnable2FA();
        });
    }

    /**
     * Carga la lista de usuarios desde el backend y redibuja la tabla
     */
    function loadUsuarios() {
        // DataTables se encarga de la carga y el indicador de "processing"
        // Simplemente recargamos los datos desde la fuente AJAX
        dataTable.ajax.reload();
    }

    /**
     * Carga los perfiles en el select del modal
     */
    function loadProfiles() {
        fetch(ENDPOINTS.profiles)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const select = $('#id_perfil');
                    select.empty().append('<option value="">Seleccione un perfil...</option>');
                    data.data.forEach(profile => {
                        select.append(`<option value="${profile.id}">${profile.nombre}</option>`);
                    });
                } else {
                    showNotification('Error al cargar perfiles', 'error');
                }
            }).catch(error => {
                console.error('Error cargando perfiles:', error);
            });
    }

    /**
     * Guarda un usuario (crear o actualizar)
     */
    function saveUsuario() {
        clearFieldErrors();

        const formData = {
            id: $('#id').val() || null,
            nombre: $('#nombre').val().trim(),
            usuario: $('#usuario').val().trim(),
            clave: $('#clave').val(),
            correo: $('#correo').val().trim(),
            perfil: {
                id: $('#id_perfil').val()
            }
        };

        // Validación básica del lado cliente
        if (!validateForm(formData)) {
            return;
        }

        // Si es edición y la clave está vacía, no enviarla
        if (isEditing && !formData.clave) {
            delete formData.clave;
        }

        showLoading(true);

        fetch(ENDPOINTS.save, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    hideModal();
                    showNotification(data.message, 'success');
                    loadUsuarios(); // Recargar la tabla
                } else {
                    if (data.errors) {
                        // Mostrar errores de validación del servidor
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
                showNotification('Error de conexión al guardar usuario', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    /**
     * Maneja la edición de un usuario
     */
    function handleEdit(e) {
        e.preventDefault();
        const id = $(this).data('id');

        showLoading(true);

        fetch(ENDPOINTS.get(id))
            .then(response => {
                if (!response.ok) {
                    throw new Error('Usuario no encontrado');
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    openModalForEdit(data.data);
                } else {
                    showNotification('Error al cargar usuario: ' + data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showNotification('Error al cargar los datos del usuario', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    /**
     * Maneja el cambio de estado de un usuario
     */
    function handleToggleStatus(e) {
        e.preventDefault();
        const id = $(this).data('id');

        if (!usuario || !usuario.id) {
            showNotification('No se pudo verificar el usuario logueado. Intente recargar la página.', 'error');
            return;
        }

        if (usuario.id === id) {
            Swal.fire({
                title: 'Acción Inválida',
                text: 'No puede inactivar su propio usuario.',
                icon: 'error',
                confirmButtonText: 'Aceptar',
                confirmButtonColor: '#dc3545'
            });
            return;
        }

        showLoading(true);

        fetch(ENDPOINTS.toggleStatus(id), {
            method: 'POST'
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showNotification(data.message, 'success');
                    loadUsuarios(); // Recargar la tabla
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

    /**
     * Maneja la eliminación de un usuario
     */
    function handleDelete(e) {
        e.preventDefault();

        const id = $(this).data('id');

        if (!usuario || !usuario.id) {
            showNotification('No se pudo verificar el usuario logueado. Intente recargar la página.', 'error');
            return;
        }

        if (usuario.id === id) {
            Swal.fire({
                title: 'Acción Inválida',
                text: 'No puede eliminar su propio usuario.',
                icon: 'error',
                confirmButtonText: 'Aceptar',
                confirmButtonColor: '#dc3545'
            });
            return;
        }

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
                            loadUsuarios(); // Recargar la tabla
                        } else {
                            showNotification('Error: ' + data.message, 'error');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        showNotification('Error de conexión al eliminar usuario', 'error');
                    })
                    .finally(() => {
                        showLoading(false);
                    });
            }
        });
    }

    /**
     * Maneja la configuración de 2FA
     */
    function handleSetup2FA(e) {
        e.preventDefault();
        const id = $(this).data('id');
        $('#idUsuario2FA').val(id);

        showLoading(true);
        fetch(ENDPOINTS.generate2fa(id))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    $('#qrCode').attr('src', data.qrCodeUri);
                    // Se añade el estilo CSS para que la clave no se desborde de su contenedor.
                    $('#secretKey')
                        .text(data.secreto)
                        .css('word-break', 'break-all');

                    $('#codigo2FA').val('');
                    modal2FA.show();
                } else {
                    showNotification(data.message, 'error');
                }
            })
            .catch(error => {
                console.error('Error generando QR:', error);
                showNotification('Error al iniciar la configuración de 2FA.', 'error');
            })
            .finally(() => {
                showLoading(false);
            });
    }

    /**
     * Verifica el código y activa 2FA
     */
    function verifyAndEnable2FA() {
        const payload = {
            id: $('#idUsuario2FA').val(),
            codigo: $('#codigo2FA').val(),
            secreto: $('#secretKey').text()
        };

        showLoading(true);
        fetch(ENDPOINTS.verify2fa, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                modal2FA.hide();
                showNotification(data.message, 'success');
                loadUsuarios(); // Recargar para mostrar el botón actualizado
            } else {
                showNotification(data.message, 'error');
            }
        })
        .catch(error => {
            console.error('Error verificando código:', error);
            showNotification('Error de conexión al verificar el código.', 'error');
        })
        .finally(() => showLoading(false));
    }

    /**
     * Abre el modal para crear nuevo usuario
     */
    function openModalForNew() {
        isEditing = false;
        clearForm();
        $('#modalTitle').text('Agregar Usuario');
        $('#clave').prop('required', true).attr('placeholder', '');
        showModal();
    }

    /**
     * Abre el modal para editar usuario
     */
    function openModalForEdit(usuario) {
        isEditing = true;
        clearForm();
        $('#modalTitle').text('Editar Usuario');

        $('#id').val(usuario.id);
        $('#nombre').val(usuario.nombre);
        $('#usuario').val(usuario.usuario);
        $('#correo').val(usuario.correo);
        $('#id_perfil').val(usuario.perfil ? usuario.perfil.id : '');
        $('#clave').val('').prop('required', false).attr('placeholder', 'Dejar en blanco para no cambiar');

        showModal();
    }

    /**
     * Muestra el modal
     */
    function showModal() {
        usuarioModal.show();
    }

    /**
     * Oculta el modal
     */
    function hideModal() {
        usuarioModal.hide();
        clearForm();
    }

    /**
     * Limpia el formulario y resetea el estado
     */
    function clearForm() {
        $('#formUsuario')[0].reset();
        $('#formUsuario .form-control').removeClass('is-invalid');
        $('.invalid-feedback').text('');
        isEditing = false;
    }

    /**
     * Valida el formulario del lado cliente
     */
    function validateForm(formData) {
        let hasErrors = false;
        clearFieldErrors();

        if (!formData.nombre) {
            showFieldError('nombre', 'El nombre es obligatorio');
            hasErrors = true;
        } else if (formData.nombre.length < 2) {
            showFieldError('nombre', 'El nombre debe tener al menos 2 caracteres');
            hasErrors = true;
        }

        if (!formData.usuario) {
            showFieldError('usuario', 'El usuario es obligatorio');
            hasErrors = true;
        } else if (formData.usuario.length < 3) {
            showFieldError('usuario', 'El usuario debe tener al menos 3 caracteres');
            hasErrors = true;
        }

        if (!formData.perfil.id) {
            showFieldError('id_perfil', 'Debe seleccionar un perfil');
            hasErrors = true;
        }

        if (!isEditing && !formData.clave) {
            showFieldError('clave', 'La contraseña es obligatoria');
            hasErrors = true;
        } else if (formData.clave && formData.clave.length < 6) {
            showFieldError('clave', 'La contraseña debe tener al menos 6 caracteres');
            hasErrors = true;
        }

        if (!formData.correo) {
            showFieldError('correo', 'El correo es obligatorio');
            hasErrors = true;
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.correo)) {
            showFieldError('correo', 'El formato del correo no es válido');
            hasErrors = true;
        }

        return !hasErrors;
    }

    /**
     * Muestra error en un campo específico
     */
    function showFieldError(fieldName, message) {
        const field = $(`#${fieldName}`);
        const errorDiv = $(`#${fieldName}-error`);

        field.addClass('is-invalid');
        errorDiv.text(message);
    }

    /**
     * Limpia todos los errores de campo
     */
    function clearFieldErrors() {
        $('.invalid-feedback').text('');
        $('#formUsuario .form-control').removeClass('is-invalid');
    }

    /**
     * Muestra notificaciones toast
     */
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

    /**
     * Muestra/oculta indicador de carga
     */
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

    function cargarDatosUsuarioLogueado() {
        fetch(ENDPOINTS.usuarioLogin)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al obtener ID de usuario.');
                }
                return response.json();
            })
            .then(loginData => {
                if (loginData.success && loginData.usuarioActual) {
                    return fetch(ENDPOINTS.get(loginData.usuarioActual));
                } else {
                    throw new Error('No se encontró el ID del usuario logueado en la respuesta.');
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al obtener los datos del usuario.');
                }
                return response.json();
            })
            .then(userData => {
                if (userData.success) {
                    usuario = userData.data;
                } else {
                    throw new Error('La respuesta para obtener datos de usuario no fue exitosa.');
                }
            })
            .catch(error => {
                console.error('Error al cargar la información del usuario logueado:', error);
                usuario = null;
                showNotification('No se pudo cargar la información del usuario. Algunas funciones podrían no operar correctamente.', 'error');
            });
    }

});