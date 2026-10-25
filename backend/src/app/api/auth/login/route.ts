import { NextResponse } from "next/server";
import { pool } from "@/lib/db";
import { signToken } from "@/lib/auth";
import { error, readJson } from "@/lib/http";
import { getProfile } from "@/lib/users";

export async function POST(req: Request) {
  const body = await readJson<{ email: string; password: string }>(req);
  const email = typeof body?.email === "string" ? body.email.trim().toLowerCase() : "";
  const password = typeof body?.password === "string" ? body.password : "";
  if (!email || !password) return error(400, "Email and password are required");

  // pgcrypto re-hashes the given password with the stored salt and compares.
  const { rows } = await pool.query(
    `SELECT id FROM users WHERE email = $1 AND password_hash = crypt($2, password_hash)`,
    [email, password],
  );
  if (!rows[0]) return error(401, "Invalid email or password");

  const userId: number = rows[0].id;
  return NextResponse.json({ token: await signToken(userId), user: await getProfile(userId) });
}
