/**
 * Applies schema + seed to DATABASE_URL (local or Neon).
 * Usage: node scripts/setup-db.mjs
 */
import { readFileSync } from "fs";
import { join, dirname } from "path";
import { fileURLToPath } from "url";
import pg from "pg";

const root = join(dirname(fileURLToPath(import.meta.url)), "..");
const url = process.env.DATABASE_URL;
if (!url) {
  console.error("Set DATABASE_URL first");
  process.exit(1);
}

const needsSsl = !/localhost|127\.0\.0\.1/.test(url);
const client = new pg.Client({
  connectionString: url,
  ssl: needsSsl ? { rejectUnauthorized: false } : undefined,
});

await client.connect();
for (const file of ["01_schema.sql", "02_seed.sql"]) {
  const sql = readFileSync(join(root, "db", file), "utf8");
  console.log(`Running ${file}...`);
  await client.query(sql);
}
await client.end();
console.log("Database ready.");
