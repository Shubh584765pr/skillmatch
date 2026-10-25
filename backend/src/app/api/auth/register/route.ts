import { NextResponse } from "next/server";
import { pool } from "@/lib/db";
import { signToken } from "@/lib/auth";
import { error, readJson } from "@/lib/http";
import { getProfile } from "@/lib/users";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export async function POST(req: Request) {
  const body = await readJson<{ name: string; email: string; password: string }>(req);
  const name = typeof body?.name === "string" ? body.name.trim() : "";
  const email = typeof body?.email === "string" ? body.email.trim().toLowerCase() : "";
  const password = typeof body?.password === "string" ? body.password : "";

  if (!name) return error(400, "Name is required");
  if (!EMAIL_PATTERN.test(email)) return error(400, "Enter a valid email");
  if (password.length < 6) return error(400, "Password must be at least 6 characters");

  const { rows } = await pool.query(
    `INSERT INTO users (name, email, password_hash)
     VALUES ($1, $2, crypt($3, gen_salt('bf')))
     ON CONFLICT (email) DO NOTHING
     RETURNING id`,
    [name, email, password],
  );
  if (!rows[0]) return error(409, "An account with this email already exists");

  const userId: number = rows[0].id;
  return NextResponse.json(
    { token: await signToken(userId), user: await getProfile(userId) },
    { status: 201 },
  );
}
