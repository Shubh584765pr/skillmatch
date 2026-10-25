-- SkillMatch schema. Runs automatically the first time the Postgres container starts.
CREATE EXTENSION IF NOT EXISTS pgcrypto; -- crypt()/gen_salt() for bcrypt password hashes

CREATE TABLE users (
  id               SERIAL PRIMARY KEY,
  name             TEXT NOT NULL,
  email            TEXT NOT NULL UNIQUE,
  password_hash    TEXT NOT NULL,
  location         TEXT,
  experience_level TEXT NOT NULL DEFAULT 'mid' CHECK (experience_level IN ('fresher', 'junior', 'mid', 'senior')),
  language         TEXT NOT NULL DEFAULT 'en' CHECK (language IN ('en', 'hi')),
  job_alerts       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE skills (
  id   SERIAL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE
);

-- level: 1 = Beginner, 2 = Proficient, 3 = Advanced, 4 = Expert
CREATE TABLE user_skills (
  user_id  INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  skill_id INT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
  level    SMALLINT NOT NULL CHECK (level BETWEEN 1 AND 4),
  PRIMARY KEY (user_id, skill_id)
);

CREATE TABLE jobs (
  id              SERIAL PRIMARY KEY,
  title           TEXT NOT NULL,
  company         TEXT NOT NULL,
  location        TEXT NOT NULL,
  country         TEXT NOT NULL DEFAULT 'India',
  employment_type TEXT NOT NULL DEFAULT 'Full-time',
  work_mode       TEXT NOT NULL DEFAULT 'On-site',
  experience_min  SMALLINT NOT NULL DEFAULT 0,
  experience_max  SMALLINT NOT NULL DEFAULT 5,
  salary_min_lpa  NUMERIC(5, 1),
  salary_max_lpa  NUMERIC(5, 1),
  description     TEXT NOT NULL,
  posted_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE job_skills (
  job_id   INT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
  skill_id INT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
  PRIMARY KEY (job_id, skill_id)
);

CREATE TABLE saved_jobs (
  user_id  INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  job_id   INT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
  saved_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, job_id)
);

CREATE TABLE applied_jobs (
  user_id    INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  job_id     INT NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
  applied_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, job_id)
);

CREATE INDEX idx_jobs_posted_at ON jobs (posted_at DESC);
