import { NextResponse } from "next/server";

export function error(status: number, message: string) {
  return NextResponse.json({ error: message }, { status });
}

/** Parses a JSON body, returning null instead of throwing on bad input. */
export async function readJson<T>(req: Request): Promise<Partial<T> | null> {
  try {
    const body = await req.json();
    return body && typeof body === "object" ? body : null;
  } catch {
    return null;
  }
}

/** Route params like "/api/jobs/abc" must be positive integers. */
export function parseId(raw: string): number | null {
  const id = Number(raw);
  return Number.isInteger(id) && id > 0 ? id : null;
}
