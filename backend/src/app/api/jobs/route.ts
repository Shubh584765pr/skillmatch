import { NextResponse } from "next/server";
import { getUserId } from "@/lib/auth";
import { error } from "@/lib/http";
import { listJobs, type Tab } from "@/lib/jobs";

const TABS: Tab[] = ["recommended", "all", "saved", "applied"];

// GET /api/jobs?tab=recommended|all|saved|applied&q=kotlin
export async function GET(req: Request) {
  const userId = await getUserId(req);
  if (!userId) return error(401, "Please log in");

  const params = new URL(req.url).searchParams;
  const tab = (params.get("tab") ?? "all") as Tab;
  if (!TABS.includes(tab)) return error(400, `tab must be one of: ${TABS.join(", ")}`);

  const jobs = await listJobs(userId, tab, params.get("q") ?? "");
  return NextResponse.json({ jobs });
}
