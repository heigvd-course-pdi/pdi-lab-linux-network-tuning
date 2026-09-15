const express = require('express');
const { Pool } = require('pg');

const app = express();
const PORT = 8080;

const pool = new Pool({
  host: process.env.POSTGRES_HOST,
  port: process.env.POSTGRES_PORT,
  database: process.env.POSTGRES_DB,
  user: process.env.POSTGRES_USER,
  password: process.env.POSTGRES_PASSWORD
});

app.get('/', async (req, res) => {
  try {
    const result = await queryDb();
    res.json(result);
  } catch (error) {
    console.error('Error querying database:', error);
    res.status(500).json({ error: 'Error generating random numbers: ' + error.message });
  }
});

async function queryDb() {
  const client = await pool.connect();
  try {
    const result = await client.query('SELECT (RANDOM() * 1000000)::INTEGER as random_num');
    const random_num = result.rows[0].random_num;
    return new Array(10000).fill(random_num);
  } finally {
    client.release();
  }
}

app.listen(PORT, () => {
  console.log(`Server started on port ${PORT}`);
});

process.on('SIGINT', async () => {
  console.log('Shutting down...');
  await pool.end();
  process.exit(0);
});
