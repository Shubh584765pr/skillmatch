import { NextResponse } from "next/server";
import { pool } from "@/lib/db";
import { getUserId } from "@/lib/auth";
import { error, readJson } from "@/lib/http";
import { EXPERIENCE_LEVELS, LANGUAGES, getProfile } from "@/lib/users";

export async function GET(req: Request) {
  const userId = await getUserId(req);
  if (!userId) return error(401, "Please log in");

  const user = await getProfile(userId);
  return user ? NextResponse.json({ user }) : error(404, "User not found");
}

interface ProfileUpdate {
  name: string;
  location: string | null;
  experienceLevel: (typeof EXPERIENCE_LEVELS)[number];
  language: (typeof LANGUAGES)[number];
  jobAlerts: boolean;
  skills: { id: number; level: number }[];
}

// PUT /api/me — every field is optional; only the ones sent are changed.
// "skills", when sent, replaces the user's whole skill list.
export async function PUT(req: Request) {
  const userId = await getUserId(req);
  if (!userId) return error(401, "Please log in");

  const body = await readJson<ProfileUpdate>(req);
  if (!body) return error(400, "Body must be a JSON object");

  const sets: string[] = [];
  const values: unknown[] = [];
  const set = (column: string, value: unknown) => {
    values.push(value);
    sets.push(`${column} = $${values.length}`);
  };

  if (body.name !== undefined) {
    if (typeof body.name !== "string" || !body.name.trim()) return error(400, "Name cannot be empty");
    set("name", body.name.trim());
  }
  if (body.location !== undefined) {
    if (body.location !== null && typeof body.location !== "string") return error(400, "Invalid location");
    set("location", body.location?.trim() || null);
  }
  if (body.experienceLevel !== undefined) {
    if (!EXPERIENCE_LEVELS.includes(body.experienceLevel)) return error(400, "Invalid experience level");
    set("experience_level", body.experienceLevel);
  }
  if (body.language !== undefined) {
    if (!LANGUAGES.includes(body.language)) return error(400, "Language must be en or hi");
    set("language", body.language);
  }
  if (body.jobAlerts !== undefined) {
    if (typeof body.jobAlerts !== "boolean") return error(400, "jobAlerts must be true or false");
    set("job_alerts", body.jobAlerts);
  }

  const skills = body.skills;
  if (skills !== undefined) {
    const valid =
      Array.isArray(skills) &&
      skills.every((s) => Number.isInteger(s?.id) && Number.isInteger(s?.level) && s.level >= 1 && s.level <= 4);
    if (!valid) return error(400, "skills must be a list of { id, level } with level 1-4");
  }

  const client = await pool.connect();
  try {
    await client.query("BEGIN");
    if (sets.length) {
      values.push(userId);
      await client.query(`UPDATE users SET ${sets.join(", ")} WHERE id = $${values.length}`, values);
    }
    if (skills) {
      await client.query(`DELETE FROM user_skills WHERE user_id = $1`, [userId]);
      for (const s of skills) {
        await client.query(
          `INSERT INTO user_skills (user_id, skill_id, level) VALUES ($1, $2, $3)
           ON CONFLICT (user_id, skill_id) DO UPDATE SET level = EXCLUDED.level`,
          [userId, s.id, s.level],
        );
      }
    }
    await client.query("COMMIT");
  } catch (e) {
    await client.query("ROLLBACK");
    // 23503 = foreign key violation, i.e. an unknown skill id.
    if ((e as { code?: string }).code === "23503") return error(400, "Unknown skill id");
    throw e;
  } finally {
    client.release();
  }

  return NextResponse.json({ user: await getProfile(userId) });
}
