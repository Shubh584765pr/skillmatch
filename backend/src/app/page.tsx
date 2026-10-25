const endpoints = [
  "POST /api/auth/register",
  "POST /api/auth/login",
  "GET  /api/jobs?tab=recommended|all|saved&q=",
  "GET  /api/jobs/:id",
  "POST /api/jobs/:id/save",
  "DELETE /api/jobs/:id/save",
  "GET  /api/me",
  "PUT  /api/me",
  "GET  /api/skills",
  "GET  /api/health",
];

export default function Home() {
  return (
    <main>
      <h1>SkillMatch API</h1>
      <p>Backend for the SkillMatch Android app.</p>
      <pre>{endpoints.join("\n")}</pre>
    </main>
  );
}
