import { NextResponse } from "next/server";
import { pool } from "@/lib/db";

// Without a request argument Next would pre-render this at build time, when there's no database.
export const dynamic = "force-dynamic";

// Full skill catalogue, used by the app's "add skill" picker.
export async function GET() {
  const { rows } = await pool.query(`SELECT id, name FROM skills ORDER BY name`);
  return NextResponse.json({ skills: rows });
}
