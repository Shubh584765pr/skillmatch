const { Client } = require('pg');
const fs = require('fs');
const path = require('path');

async function main() {
  console.log('Connecting to PostgreSQL server...');
  // Connect to default postgres DB as user postgres
  const rootClient = new Client({
    host: '127.0.0.1',
    port: 5432,
    user: 'postgres',
    password: 'password' // Windows default or empty
  });

  try {
    await rootClient.connect();
    console.log('Connected to Postgres server!');
  } catch (err) {
    console.log('Failed to connect with password "password", trying without password...', err.message);
    const rootClientNoPass = new Client({
      host: '127.0.0.1',
      port: 5432,
      user: 'postgres'
    });
    try {
      await rootClientNoPass.connect();
      console.log('Connected to Postgres server without password!');
      return await setupDb(rootClientNoPass);
    } catch (err2) {
      console.error('Could not connect to Postgres:', err2.message);
      process.exit(1);
    }
  }

  await setupDb(rootClient);
}

async function setupDb(client) {
  try {
    // Check if user skillmatch exists
    const userRes = await client.query("SELECT 1 FROM pg_roles WHERE rolname='skillmatch'");
    if (userRes.rowCount === 0) {
      console.log("Creating role 'skillmatch'...");
      await client.query("CREATE ROLE skillmatch WITH LOGIN PASSWORD 'skillmatch' SUPERUSER;");
    }

    // Check if database skillmatch exists
    const dbRes = await client.query("SELECT 1 FROM pg_database WHERE datname='skillmatch'");
    if (dbRes.rowCount === 0) {
      console.log("Creating database 'skillmatch'...");
      await client.query("CREATE DATABASE skillmatch WITH OWNER skillmatch;");
    }

    await client.end();

    // Now connect to skillmatch database as skillmatch user
    console.log("Connecting to 'skillmatch' database...");
    const dbClient = new Client({
      host: '127.0.0.1',
      port: 5432,
      user: 'skillmatch',
      password: 'skillmatch',
      database: 'skillmatch'
    });

    await dbClient.connect();
    console.log("Connected to 'skillmatch' database!");

    // Execute schema.sql
    console.log("Applying 01_schema.sql...");
    const schemaSql = fs.readFileSync(path.join(__dirname, 'db', '01_schema.sql'), 'utf8');
    await dbClient.query(schemaSql);
    console.log("Schema applied successfully!");

    // Execute seed.sql
    console.log("Applying 02_seed.sql...");
    const seedSql = fs.readFileSync(path.join(__dirname, 'db', '02_seed.sql'), 'utf8');
    await dbClient.query(seedSql);
    console.log("Seed data inserted successfully!");

    await dbClient.end();
    console.log("Database setup COMPLETE!");
  } catch (e) {
    console.error("Database setup error:", e);
    process.exit(1);
  }
}

main();
