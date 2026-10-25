import { NextResponse } from "next/server";
import { getUserId } from "@/lib/auth";
import { error, parseId } from "@/lib/http";
import { getJob } from "@/lib/jobs";

export async function GET(req: Request, { params }: { params: Promise<{ id: string }> }) {
  const userId = await getUserId(req);
  if (!userId) return error(401, "Please log in");

  const jobId = parseId((await params).id);
  if (!jobId) return error(400, "Invalid job id");

  const job = await getJob(userId, jobId);
  return job ? NextResponse.json({ job }) : error(404, "Job not found");
}
