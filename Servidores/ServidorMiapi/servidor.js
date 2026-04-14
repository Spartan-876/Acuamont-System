import "dotenv/config";
import express from "express";
import cors from "cors";
import axios from "axios";

const TOKEN_CODE = process.env.TOKEN_CODE;
const URL_DNI = process.env.URL_DNI;
const URL_RUC = process.env.URL_RUC;

const app = express();
const port = 3001;

// Define quiénes pueden entrar
const whitelist = [
    "http://localhost:8080",
    "https://acuamont-system-ek2h.onrender.com"
];

app.use(cors({
    origin: function (origin, callback) {
        if (!origin) return callback(null, true);
        if (whitelist.indexOf(origin) !== -1) {
            callback(null, true);
        } else {
            console.log("Bloqueado por CORS:", origin);
            callback(new Error('Not allowed by CORS'));
        }
    }
}));

app.use(express.json());

app.get('/clientes/api/buscar-documento/:documento', async (req, res) => {
    const { documento } = req.params;
    let url;

    if (documento.length === 8) {
        url = `${URL_DNI}${documento}`;
    } else if (documento.length === 11) {
        url = `${URL_RUC}${documento}`;
    } else {
        return res.status(400).json({
            success: false,
            message: "Documento inválido"
        });
    }

    try {
        const config = {
            headers: {
                'Authorization': `Bearer ${TOKEN_CODE}`
            }
        };
        const response = await axios.get(url, config);
        const body = response.data;
        if (!body || body.success !== true) {
            return res.status(404).json({
                success: false,
                message: "No se encontró información para el documento ingresado"
            });
        }

        return res.status(200).json(body);

    } catch (error) {
        console.error(error);
        return res.status(500).json({
            success: false,
            message: "Error al consultar el documento: " + error.message
        });
    }
})

app.listen(port, () => {
    console.log(`Servidor inciado en http://localhost:${port}`);
})