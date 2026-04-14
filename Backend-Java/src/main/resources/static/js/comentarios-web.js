document.addEventListener('DOMContentLoaded', function () {
    const API_URL = 'https://acuamont-system.onrender.com/comentarios';
    const form = document.getElementById('comment-form');
    const listaComentarios = document.getElementById('lista-comentarios');

    // Cargar comentarios al iniciar
    loadComments();

    // Manejar el envío del formulario
    form.addEventListener('submit', function (e) {
        e.preventDefault();
        clearFieldErrors();
        
        const nombreInput = document.getElementById('nombre');
        const mensajeInput = document.getElementById('mensaje');
        const imagenInput = document.getElementById('imagen');

        // Validación simple
        if (!nombreInput.value.trim()) {
            showFieldError('nombre','El nombre es obligatorio')
            return;
        }
        if (!mensajeInput.value.trim()){
            showFieldError('mensaje','El mensaje es obligatorio')
            return;
        }

        // Usamos FormData para poder enviar archivos
        const formData = new FormData();
        formData.append('nombre', nombreInput.value);
        formData.append('comentario', mensajeInput.value);
        
        if (imagenInput.files.length > 0) {
            formData.append('imagen', imagenInput.files[0]);
        }

        // Enviar al servidor
        fetch(API_URL, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al enviar el comentario.');
            }
            return response.json();
        })
        .then(data => {
            console.log('Comentario enviado:', data);
            form.reset();
            loadComments();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Hubo un problema al enviar tu comentario. Inténtalo de nuevo.');
        });
    });

    function loadComments() {
        fetch(API_URL)
            .then(response => {
                if (!response.ok) {
                    throw new Error('No se pudieron cargar los comentarios.');
                }
                return response.json();
            })
            .then(comments => {
                renderComments(comments);
            })
            .catch(error => {
                console.error('Error:', error);
                listaComentarios.innerHTML = '<p class="text-danger">No se pudieron cargar los comentarios en este momento.</p>';
            });
    }

    function getInitials(name) {
        const words = name.split(' ');
        let initials = '';
        if (words.length > 0) {
            initials += words[0][0];
            if (words.length > 1) {
                initials += words[words.length - 1][0];
            }
        }
        return initials.toUpperCase();
    }

    function renderComments(comments) {
        listaComentarios.innerHTML = '';

        if (comments.length === 0) {
            listaComentarios.innerHTML = '<div class="col-12"><p class="text-center text-muted">Aún no hay comentarios. ¡Sé el primero en dejar uno!</p></div>';
            return;
        }

        comments.forEach(comment => {
            const initials = getInitials(comment.nombre);
            const avatarUrl = `https://placehold.co/60x60/061748/ffffff?text=${initials}`;
            
            const commentDate = new Date(comment.fecha).toLocaleDateString('es-ES', {
                year: 'numeric', month: 'long', day: 'numeric'
            });

            const userImageHTML = comment.imagen_url
                ? `<img src="${comment.imagen_url}" class="img-fluid rounded mt-3" style="max-height: 300px; width: 100%; object-fit: cover;" alt="Imagen de ${comment.nombre}">`
                : '';

            const commentElement = document.createElement('div');
            commentElement.className = 'col-lg-6 col-md-12 mb-4';
            commentElement.innerHTML = `
                <div class="card h-100 p-4 card-rounded border-1 shadow">
                    <div class="d-flex align-items-center mb-3">
                        <img src="${avatarUrl}" class="rounded-circle me-3" alt="Avatar de ${comment.nombre}">
                        <div>
                            <h6 class="mb-0 fw-bold">${comment.nombre}</h6>
                            <small class="text-secondary">${commentDate}</small>
                        </div>
                    </div>
                    <p class="text-secondary fst-italic">
                        "${comment.comentario}"
                    </p>
                    ${userImageHTML}
                </div>
            `;
            listaComentarios.appendChild(commentElement);
        });
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