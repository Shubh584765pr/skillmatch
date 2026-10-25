import { pool } from "./db";

export type Tab = "recommended" | "all" | "saved" | "applied";
export type Fit = "excellent" | "good" | "stretch";

// A job is "recommended" when the user already has at least half of its skills.
const RECOMMENDED_MIN_SCORE = 0.5;

/** Match score = share of the job's skills the user has. */
export function fitFor(score: number): Fit {
  if (score >= 0.8) return "excellent";
  if (score >= RECOMMENDED_MIN_SCORE) return "good";
  return "stretch";
}

interface JobRow {
  id: number;
  title: string;
  company: string;
  location: string;
  country: string;
  employment_type: string;
  work_mode: string;
  experience_min: number;
  experience_max: number;
  salary_min_lpa: string | null; // pg returns NUMERIC as string
  salary_max_lpa: string | null;
  description: string;
  posted_at: Date;
  applied_at: Date | null;
  matched: string[];
  gaps: string[];
  saved: boolean;
  applied: boolean;
}

// Each job with its skills split into the ones the user has (matched) and doesn't (gaps).
const JOB_SELECT = `
  SELECT j.*,
    COALESCE(array_agg(s.name ORDER BY s.name) FILTER (WHERE us.skill_id IS NOT NULL), '{}') AS matched,
    COALESCE(array_agg(s.name ORDER BY s.name) FILTER (WHERE us.skill_id IS NULL), '{}') AS gaps,
    EXISTS (SELECT 1 FROM saved_jobs sj WHERE sj.job_id = j.id AND sj.user_id = $1) AS saved,
    EXISTS (SELECT 1 FROM applied_jobs aj WHERE aj.job_id = j.id AND aj.user_id = $1) AS applied,
    (SELECT aj.applied_at FROM applied_jobs aj WHERE aj.job_id = j.id AND aj.user_id = $1) AS applied_at
  FROM jobs j
  LEFT JOIN job_skills js ON js.job_id = j.id
  LEFT JOIN skills s ON s.id = js.skill_id
  LEFT JOIN user_skills us ON us.skill_id = js.skill_id AND us.user_id = $1
`;

function toJob(row: JobRow, withDescription: boolean) {
  const total = row.matched.length + row.gaps.length;
  const matchScore = total === 0 ? 0 : row.matched.length / total;
  return {
    id: row.id,
    title: row.title,
    company: row.company,
    location: row.location,
    country: row.country,
    employmentType: row.employment_type,
    workMode: row.work_mode,
    experienceMin: row.experience_min,
    experienceMax: row.experience_max,
    salaryMinLpa: row.salary_min_lpa === null ? null : Number(row.salary_min_lpa),
    salaryMaxLpa: row.salary_max_lpa === null ? null : Number(row.salary_max_lpa),
    postedAt: row.posted_at.toISOString(),
    appliedAt: row.applied_at ? row.applied_at.toISOString() : null,
    matched: row.matched,
    gaps: row.gaps,
    matchScore: Math.round(matchScore * 100),
    fit: fitFor(matchScore),
    saved: row.saved,
    applied: row.applied,
    ...(withDescription ? { description: row.description } : {}),
  };
}

export async function listJobs(userId: number, tab: Tab, query: string) {
  const params: unknown[] = [userId];
  const where: string[] = [];

  const q = query.trim();
  if (q) {
    // Search title, company, location, or any of the job's skills.
    // Escape LIKE wildcards so a search for "50%" means the literal text.
    params.push(`%${q.replace(/[\\%_]/g, "\\$&")}%`);
    where.push(`(
      j.title ILIKE $2 OR j.company ILIKE $2 OR j.location ILIKE $2
      OR EXISTS (SELECT 1 FROM job_skills js2 JOIN skills s2 ON s2.id = js2.skill_id
                 WHERE js2.job_id = j.id AND s2.name ILIKE $2)
    )`);
  }
  if (tab === "saved") {
    where.push(`EXISTS (SELECT 1 FROM saved_jobs sj WHERE sj.job_id = j.id AND sj.user_id = $1)`);
  }
  if (tab === "applied") {
    where.push(`EXISTS (SELECT 1 FROM applied_jobs aj WHERE aj.job_id = j.id AND aj.user_id = $1)`);
  }

  const orderBy =
    tab === "applied"
      ? `ORDER BY (SELECT aj.applied_at FROM applied_jobs aj WHERE aj.job_id = j.id AND aj.user_id = $1) DESC NULLS LAST`
      : `ORDER BY j.posted_at DESC`;

  const sql = `${JOB_SELECT}
    ${where.length ? `WHERE ${where.join(" AND ")}` : ""}
    GROUP BY j.id
    ${orderBy}`;

  const { rows } = await pool.query<JobRow>(sql, params);
  const jobs = rows.map((r) => toJob(r, false));

  if (tab === "recommended") {
    return jobs
      .filter((j) => j.matchScore >= RECOMMENDED_MIN_SCORE * 100)
      .sort((a, b) => b.matchScore - a.matchScore);
  }
  return jobs;
}

export async function getJob(userId: number, jobId: number) {
  const { rows } = await pool.query<JobRow>(`${JOB_SELECT} WHERE j.id = $2 GROUP BY j.id`, [userId, jobId]);
  return rows[0] ? toJob(rows[0], true) : null;
}
