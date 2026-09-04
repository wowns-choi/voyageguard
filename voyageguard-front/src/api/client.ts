import type { Departure, ReservationCreateRequest } from './types'

const BASE_URL = 'http://localhost:8080/api/v1'

// 백엔드 GlobalExceptionHandler가 RFC7807 ProblemDetail로 응답 - detail(메시지) + code(비즈니스 예외일 때만)
export class ApiError extends Error {
  status: number
  code?: string

  constructor(status: number, detail: string, code?: string) {
    super(detail)
    this.status = status
    this.code = code
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })

  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new ApiError(res.status, body?.detail ?? '요청 처리 중 오류가 발생했습니다.', body?.code)
  }

  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export function listOpenDepartures(): Promise<Departure[]> {
  return request<Departure[]>('/departures')
}

export function reserve(req: ReservationCreateRequest): Promise<number> {
  return request<number>('/reservations', {
    method: 'POST',
    body: JSON.stringify(req),
  })
}
