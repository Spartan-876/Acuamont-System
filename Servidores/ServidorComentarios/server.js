require("dotenv").config();
const express = require("express");
const cors = require("cors");
const mysql = require("mysql2");
const multer = require("multer");
const path = require("path");

const cloudinary = require("cloudinary");
const multerStorageCloudinary = require("multer-storage-cloudinary");

const CloudinaryStorage = multerStorageCloudinary.CloudinaryStorage ||
    multerStorageCloudinary.default?.CloudinaryStorage ||
    multerStorageCloudinary;

const app = express();
const port = process.env.PORT || 3002;

app.use(cors());
app.use(express.json());

// --- CONFIGURACIÓN CLOUDINARY ---
cloudinary.config({
    cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
    api_key: process.env.CLOUDINARY_API_KEY,
    api_secret: process.env.CLOUDINARY_API_SECRET
});

// --- CONFIGURACIÓN MULTER ---
const storage = new CloudinaryStorage({
    cloudinary: cloudinary,
    params: {
        folder: 'comentarios_acuamont',
        allowed_formats: ['jpg', 'png', 'jpeg', 'webp'],
    },
});

const upload = multer({ storage: storage });

// --- BASE DE DATOS ---
const db = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'acceso',
    port: process.env.DB_PORT || 3306,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
    ssl: {
        rejectUnauthorized: false
    }
}).promise();

// RUTAS
app.get("/comentarios", async (req, res) => {
    try {
        const [rows] = await db.query("SELECT * FROM comentarios ORDER BY fecha DESC");
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Error al obtener los comentarios" });
    }
});

app.post("/comentarios", upload.single("imagen"), async (req, res) => {
    try {
        const { nombre, comentario } = req.body;
        
        if (!nombre || !comentario) {
            return res.status(400).json({ error: "Nombre y comentario son obligatorios" });
        }

        let imagenUrl = null;
        
        if (req.file) {
            imagenUrl = req.file.path || req.file.secure_url; 
        }

        const [result] = await db.query(
            "INSERT INTO comentarios (nombre, comentario, imagen_url) VALUES (?, ?, ?)",
            [nombre, comentario, imagenUrl]
        );

        res.status(201).json({
            id: result.insertId,
            nombre,
            comentario,
            imagenUrl,
            message: "Comentario agregado exitosamente"
        });

    } catch (error) {
        console.error("Error en POST:", error);
        res.status(500).json({ error: "Error al guardar el comentario" });
    }
});

app.listen(port, () => {
    console.log(`Servidor corriendo en http://localhost:${port}`);
});