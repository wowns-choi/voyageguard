export type DepartureStatus = 'OPEN' | 'CLOSED' | 'CANCELLED'

export interface Departure {
  id: number
  productId: number
  productTitle: string
  departureDate: string
  minParticipants: number
  capacity: number
  remainingCount: number
  itinerary: string
  saleStartDate: string
  saleEndDate: string
  salePrice: number
  status: DepartureStatus
}

export interface ReservationCreateRequest {
  departureId: number
  headcount: number
  travelerName: string
}
