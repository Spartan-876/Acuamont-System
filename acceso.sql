-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 12-10-2025 a las 04:44:10
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `acceso`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categorias`
--

CREATE TABLE `categorias` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `estado` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `categorias`
--

INSERT INTO `categorias` (`id`, `nombre`, `estado`) VALUES
(1, 'Peces', 1),
(2, 'Alimentos', 1),
(3, 'Decoraciones', 1),
(4, 'Medicamentos', 1),
(5, 'Extra1', 2),
(7, 'Categoria de prueba', 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `clientes`
--

CREATE TABLE `clientes` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `documento` varchar(255) NOT NULL,
  `telefono` varchar(9) NOT NULL,
  `estado` int(11) NOT NULL,
  `correo` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `clientes`
--

INSERT INTO `clientes` (`id`, `nombre`, `documento`, `telefono`, `estado`, `correo`) VALUES
(1, 'CLIENTES VARIOS', '00000000', '999999999', 1, 'cliente@email.com'),
(2, 'IRMA DEL CARMEN CORIA SANCHEZ', '60051938', '913048859', 2, 'prueba@email.com'),
(3, 'JOHN ANDERSON CHAPOÑAN MONTAÑO', '60051937', '913048853', 1, 'john@email.com'),
(4, 'GABRIELA ALEXANDRA TABOADA MIMBELA', '70497204', '931680722', 1, 'alexandra@email.com'),
(5, 'CORPORACION JARD S.A.C.', '20613407279', '999888777', 1, 'corporacion@email.com');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `forma_pago`
--

CREATE TABLE `forma_pago` (
  `id` bigint(20) NOT NULL,
  `estado` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `opciones`
--

CREATE TABLE `opciones` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `ruta` varchar(100) NOT NULL,
  `icono` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `opciones`
--

INSERT INTO `opciones` (`id`, `nombre`, `ruta`, `icono`) VALUES
(1, 'Dashboard', '/', 'bi-house'),
(2, 'Gestión de Usuarios', '/usuarios/listar', 'bi-person-gear'),
(3, 'Gestión de Perfiles', '/perfiles/listar', 'bi-person-badge'),
(4, 'Gestión de Categorias', '/categorias/listar', 'bi-card-checklist'),
(5, 'Gestion de Productos', '/productos/listar', 'bi-box'),
(6, 'Gestión de Imagenes', '/slides/listar', 'bi-card-image'),
(7, 'Gestión de Clientes', '/clientes/listar', 'bi-people');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `perfiles`
--

CREATE TABLE `perfiles` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `perfiles`
--

INSERT INTO `perfiles` (`id`, `nombre`, `descripcion`, `estado`) VALUES
(1, 'Administrador', 'Acceso total al sistema.', 1),
(2, 'Editor', 'Puede gestionar usuarios pero no perfiles.', 1),
(3, 'Supervisor', 'Solo puede visualizar información.', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `perfil_opcion`
--

CREATE TABLE `perfil_opcion` (
  `id_perfil` bigint(20) NOT NULL,
  `id_opcion` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `perfil_opcion`
--

INSERT INTO `perfil_opcion` (`id_perfil`, `id_opcion`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(2, 1),
(2, 2),
(3, 1),
(3, 2),
(3, 4),
(3, 5);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `productos`
--

CREATE TABLE `productos` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) NOT NULL,
  `precio_compra` double NOT NULL,
  `precio_venta` double NOT NULL,
  `stock` int(11) NOT NULL DEFAULT 0,
  `stock_seguridad` int(11) NOT NULL DEFAULT 0,
  `imagen` varchar(255) DEFAULT NULL,
  `id_categoria` bigint(20) NOT NULL,
  `estado` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `productos`
--

INSERT INTO `productos` (`id`, `nombre`, `descripcion`, `precio_compra`, `precio_venta`, `stock`, `stock_seguridad`, `imagen`, `id_categoria`, `estado`) VALUES
(1, 'Piña Bob Esponja', 'Decoración con la forma de la casa de Bob Esponja', 10, 25, 2, 1, '73cfc240-9f9e-4dd7-906c-dc8a26a8910e_piña.jpg', 3, 1),
(2, 'Casco Buzo', 'Decoración con la forma de un casco de buzo', 12, 23, 2, 1, 'e3d876c1-be75-41bf-b02e-8bb040bf7acf_buzo.jpg', 3, 1),
(3, 'Medusa Pequeña', 'Decoración con forma de medusa flotante', 7, 15, 3, 1, '6fa597f8-3469-4b79-8240-14f3a6b1527a_IMG-20250903-WA0008.jpg', 3, 1),
(4, 'Rana', 'Decoración de rana burbujeantes', 10, 20, 3, 1, '007ec4e5-ec20-42f1-a62a-bc5658025bb0_IMG-20250903-WA0009.jpg', 3, 1),
(5, 'Astronautas', 'Astronautas de pvc 3 piezas', 10, 20, 2, 1, 'e9148a02-e5f6-437d-a4e9-78ce304df5c3_IMG-20250903-WA0011.jpg', 3, 1),
(6, 'Avioneta pequeña', 'Decoración con la forma de avioneta caida', 10, 20, 2, 1, '07ce4954-83a6-4122-a599-0491137bc447_IMG-20250903-WA0012.jpg', 3, 1),
(7, 'Coral piramide', 'Decoración de coral con forma de piramide', 18, 36, 1, 1, 'e28ca75f-196f-4492-a310-5a75c60b3639_IMG-20250903-WA0010.jpg', 3, 1),
(8, 'Life Care - Porpoise', 'medicamento para cualquier tipo de enfermedad', 5.5, 12, 3, 1, 'b1846985-d492-41fd-b575-262408443aa5_IMG-20250903-WA0013.jpg', 4, 1),
(9, 'Volcan', 'decoracion de volcan', 12, 30, 2, 1, 'ff23b2a9-9be3-4c48-b968-a90dc2150d85_volcan.jpg', 3, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `series_comprobante`
--

CREATE TABLE `series_comprobante` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `serie` varchar(100) NOT NULL,
  `correlativo_actual` int(11) NOT NULL,
  `estado` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `series_comprobante`
--

INSERT INTO `series_comprobante` (`id`, `nombre`, `serie`, `correlativo_actual`, `estado`) VALUES
(1, 'Nota de Venta', 'N0001', 1, 1),
(2, 'Boleta', 'B0001', 1, 1),
(3, 'Factura', 'F0001', 1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `clave` varchar(255) DEFAULT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `estado` int(11) NOT NULL DEFAULT 1,
  `id_perfil` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `usuario`, `clave`, `correo`, `estado`, `id_perfil`) VALUES
(8, 'Daryl', 'admin', '$2a$10$OZuN1MJlw/01gIodlwqaQOKk.d5XhfbWAD8X2adyG9pkKtpDlVN1O', 'luis@ejemplo.com', 1, 1),
(10, 'María Supervisor', 'supervisor', '$2a$10$OZuN1MJlw/01gIodlwqaQOKk.d5XhfbWAD8X2adyG9pkKtpDlVN1O', 'supervisor@ejemplo.com', 1, 3),
(11, 'Carlos Analista', 'analista', '$2a$10$N9qo8uLOickgx2ZMRZoMye5aZl8ZzO8Fns2h0eCZgP2h7ZWCpU9/y', 'analista@ejemplo.com', 1, 2),
(14, 'Luis Antonio', 'luis', '$2a$10$bDRnfg7TQgcBeV.e0cd.ZuNfDUGfPRPhp62tfLVtycqwV/unM0VWm', 'luis@ejemplo.com', 1, 1),
(15, 'Blanca Rosa', 'blanca', '$2a$10$UTJNtLoen3wHnh1WMF756uBNJo9Gm4Hlmm8XuiFTOrJy5wdnt1d3C', 'blanca@ejemplo.com', 1, 2);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `categorias`
--
ALTER TABLE `categorias`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `clientes`
--
ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `documento` (`documento`);

--
-- Indices de la tabla `forma_pago`
--
ALTER TABLE `forma_pago`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `opciones`
--
ALTER TABLE `opciones`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_nombre_opcion` (`nombre`);

--
-- Indices de la tabla `perfiles`
--
ALTER TABLE `perfiles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_nombre_perfil` (`nombre`);

--
-- Indices de la tabla `perfil_opcion`
--
ALTER TABLE `perfil_opcion`
  ADD PRIMARY KEY (`id_perfil`,`id_opcion`),
  ADD KEY `FKccootfr17pdgjedgifd92qao0` (`id_opcion`);

--
-- Indices de la tabla `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_producto_categoria` (`id_categoria`);

--
-- Indices de la tabla `series_comprobante`
--
ALTER TABLE `series_comprobante`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_usuario` (`usuario`),
  ADD KEY `FK_usuarios_perfiles` (`id_perfil`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `clientes`
--
ALTER TABLE `clientes`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `forma_pago`
--
ALTER TABLE `forma_pago`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `opciones`
--
ALTER TABLE `opciones`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `perfiles`
--
ALTER TABLE `perfiles`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `productos`
--
ALTER TABLE `productos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `series_comprobante`
--
ALTER TABLE `series_comprobante`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `perfil_opcion`
--
ALTER TABLE `perfil_opcion`
  ADD CONSTRAINT `FKccootfr17pdgjedgifd92qao0` FOREIGN KEY (`id_opcion`) REFERENCES `opciones` (`id`),
  ADD CONSTRAINT `FKe1pcyxsiyjjqt8g486euwsxft` FOREIGN KEY (`id_perfil`) REFERENCES `perfiles` (`id`);

--
-- Filtros para la tabla `productos`
--
ALTER TABLE `productos`
  ADD CONSTRAINT `fk_producto_categoria` FOREIGN KEY (`id_categoria`) REFERENCES `categorias` (`id`);

--
-- Filtros para la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `FK_usuarios_perfiles` FOREIGN KEY (`id_perfil`) REFERENCES `perfiles` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
