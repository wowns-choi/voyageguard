import { useState } from 'react'
import { ApiError, reserve } from './api/client'
import type { Departure } from './api/types'

interface Props {
  departure: Departure
  onReserved: () => void
}

function DepartureCard({ departure, onReserved }: Props) {
  const [headcount, setHeadcount] = useState(1)
  const [travelerName, setTravelerName] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  const soldOut = departure.remainingCount <= 0

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!travelerName.trim()) {
      setMessage({ type: 'error', text: '여행자명을 입력해주세요.' })
      return
    }

    setSubmitting(true)
    setMessage(null)
    try {
      const reservationId = await reserve({ departureId: departure.id, headcount, travelerName })
      setMessage({ type: 'success', text: `예약 완료! 예약번호 ${reservationId}` })
      setTravelerName('')
      setHeadcount(1)
      onReserved()
    } catch (err) {
      const text = err instanceof ApiError ? err.message : '예약 중 오류가 발생했습니다.'
      setMessage({ type: 'error', text })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="departure-card">
      <div className="departure-card__header">
        <h3>{departure.productTitle}</h3>
        <span className={`badge ${soldOut ? 'badge--soldout' : ''}`}>
          잔여 {departure.remainingCount} / {departure.capacity}석
        </span>
      </div>
      <p className="departure-card__itinerary">{departure.itinerary}</p>
      <dl className="departure-card__info">
        <div>
          <dt>출발일</dt>
          <dd>{departure.departureDate}</dd>
        </div>
        <div>
          <dt>판매가</dt>
          <dd>{departure.salePrice.toLocaleString()}원</dd>
        </div>
        <div>
          <dt>최소출발인원</dt>
          <dd>{departure.minParticipants}명</dd>
        </div>
      </dl>

      <form className="reserve-form" onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="여행자명"
          value={travelerName}
          onChange={(e) => setTravelerName(e.target.value)}
          disabled={soldOut || submitting}
        />
        <input
          type="number"
          min={1}
          value={headcount}
          onChange={(e) => setHeadcount(Number(e.target.value))}
          disabled={soldOut || submitting}
        />
        <button type="submit" disabled={soldOut || submitting}>
          {soldOut ? '매진' : submitting ? '예약 중...' : '예약하기'}
        </button>
      </form>

      {message && <p className={`message message--${message.type}`}>{message.text}</p>}
    </div>
  )
}

export default DepartureCard
