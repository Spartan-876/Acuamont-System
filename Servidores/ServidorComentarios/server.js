import "dotenv/config";
import express from "express";
import cors from "cors";
import mysql from "mysql2";
import multer from "multer";
import path from "path";
import fs from "fs";
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();
const port = process.env.PORT || 3002;

app.use(cors());
app.use(express.json());

app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

const uploadDir = path.join(__dirname, 'uploads');

if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir);
}

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/');
    },
    filename: (req, file, cb) => {
        const uniqueName = Date.now() + '-' + Math.round(Math.random() * 1E9) + path.extname(file.originalname);
        cb(null, uniqueName);
    }
});

const upload = multer({ storage: storage });

const db = mysql.createPool({
    host: 'localhost',
    user: 'root',
    password: '',
    database: 'acceso',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
}).promise();


// 1. GET: Listar comentarios
app.get("/comentarios", async (req, res) => {
    try {
        const [rows] = await db.query("SELECT * FROM comentarios ORDER BY fecha DESC");
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Error al obtener los comentarios" });
    }
});

// 2. POST: Crear comentario con imagen opcional
app.post("/comentarios", upload.single("imagen"), async (req, res) => {
    try {
        const { nombre, comentario } = req.body;
        
        if (!nombre || !comentario) {
            return res.status(400).json({ error: "Nombre y comentario son obligatorios" });
        }

        let imagenUrl = null;
        if (req.file) {
            imagenUrl = `${req.protocol}://${req.get('host')}/uploads/${req.file.filename}`;
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
        console.error(error);
        res.status(500).json({ error: "Error al guardar el comentario" });
    }
});

app.listen(port, () => {
    console.log(`Servidor corriendo en http://localhost:${port}`);
});