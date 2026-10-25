import { Pool } from "pg";

// One shared connection pool for the whole server process.
// Neon / cloud Postgres need TLS; local Docker does not.
const connectionString = process.env.DATABASE_URL;
const needsSsl = !!connectionString && !/localhost|127\.0\.0\.1/.test(connectionString);

export const pool = new Pool({
  connectionString,
  ssl: needsSsl ? { rejectUnauthorized: false } : undefined,
});
