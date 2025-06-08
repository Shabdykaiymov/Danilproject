const express = require('express');
const cors = require('cors');
const app = express();

app.use(cors({
    origin: 'http://localhost:3000',
    credentials: true,
}));

app.get('/api', (req, res) => {
    res.json({ message: 'Hello!' });
});

app.listen(8082, () => {
    console.log('Server is running on port 8082');
});