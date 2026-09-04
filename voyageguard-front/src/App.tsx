import { useCallback, useEffect, useState } from 'react'
import { ApiError, listOpenDepartures } from './api/client'
import type { Departure } from './api/types'
import DepartureCard from './DepartureCard'
import './App.css'

function App() {
  const [departures, setDepartures] = useState<Departure[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchDepartures = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setDepartures(await listOpenDepartures())
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '회차 목록을 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchDepartures()
  }, [fetchDepartures])

  return (
    <div className="page">
      <header className="page__header">
        <h1>VoyageGuard</h1>
        <p>모집중인 여행 회차 목록</p>
        <button type="button" onClick={fetchDepartures} disabled={loading}>
          새로고침
        </button>
      </header>

      {loading && <p className="status">불러오는 중...</p>}
      {error && <p className="status status--error">{error}</p>}
      {!loading && !error && departures.length === 0 && (
        <p className="status">모집중인 회차가 없습니다.</p>
      )}

      <div className="departure-list">
        {departures.map((departure) => (
          <DepartureCard key={departure.id} departure={departure} onReserved={fetchDepartures} />
        ))}
      </div>
    </div>
  )
}

export default App
