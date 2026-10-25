import { NextResponse } from "next/server";
import { pool } from "@/lib/db";
import { getUserId } from "@/lib/auth";
import { error, parseId } from "@/lib/http";

type Ctx = { params: Promise<{ id: string }> };

async function resolve(req: Request, ctx: Ctx) {
  const userId = await getUserId(req);
  const jobId = parseId((await ctx.params).id);
  return { userId, jobId };
}

// POST saves (bookmarks) the job; saving twice is a no-op.
export async function POST(req: Request, ctx: Ctx) {
  const { userId, jobId } = await resolve(req, ctx);
  if (!userId) return error(401, "Please log in");
  if (!jobId) return error(400, "Invalid job id");

  const { rowCount } = await pool.query(
    `INSERT INTO saved_jobs (user_id, job_id)
     SELECT $1, id FROM jobs WHERE id = $2
     ON CONFLICT DO NOTHING`,
    [userId, jobId],
  );
  const exists = rowCount || (await pool.query(`SELECT 1 FROM jobs WHERE id = $1`, [jobId])).rowCount;
  return exists ? NextResponse.json({ saved: true }) : error(404, "Job not found");
}

// DELETE removes the bookmark.
export async function DELETE(req: Request, ctx: Ctx) {
  const { userId, jobId } = await resolve(req, ctx);
  if (!userId) return error(401, "Please log in");
  if (!jobId) return error(400, "Invalid job id");

  await pool.query(`DELETE FROM saved_jobs WHERE user_id = $1 AND job_id = $2`, [userId, jobId]);
  return NextResponse.json({ saved: false });
}
