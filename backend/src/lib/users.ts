import { pool } from "./db";

export const EXPERIENCE_LEVELS = ["fresher", "junior", "mid", "senior"] as const;
export const LANGUAGES = ["en", "hi"] as const;

export async function getProfile(userId: number) {
  const { rows } = await pool.query(
    `SELECT id, name, email, location, experience_level, language, job_alerts FROM users WHERE id = $1`,
    [userId],
  );
  const user = rows[0];
  if (!user) return null;

  const skills = await pool.query(
    `SELECT s.id, s.name, us.level
     FROM user_skills us JOIN skills s ON s.id = us.skill_id
     WHERE us.user_id = $1
     ORDER BY us.level DESC, s.name`,
    [userId],
  );

  return {
    id: user.id,
    name: user.name,
    email: user.email,
    location: user.location,
    experienceLevel: user.experience_level,
    language: user.language,
    jobAlerts: user.job_alerts,
    skills: skills.rows,
  };
}
