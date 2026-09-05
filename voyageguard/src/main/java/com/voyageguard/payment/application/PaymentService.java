package com.voyageguard.payment.application;

import com.voyageguard.payment.api.dto.PaymentRequestResponse;
import com.voyageguard.payment.application.pg.PgApiException;
import com.voyageguard.payment.application.pg.PgCancelResult;
import com.voyageguard.payment.application.pg.PgClient;
import com.voyageguard.payment.application.pg.PgConfirmResult;
import com.voyageguard.payment.application.reservation.ReservationClient;
import com.voyageguard.payment.application.reservation.ReservationView;
import com.voyageguard.payment.domain.payment.Payment;
import com.voyageguard.payment.domain.payment.PaymentRepository;
import com.voyageguard.payment.domain.payment.PaymentStatus;
import com.voyageguard.payment.domain.payment.PaymentType;
import com.voyageguard.sales.domain.reservation.Reservation;
import com.voyageguard.sales.domain.reservation.ReservationRepository;
import com.voyageguard.sales.domain.reservation.ReservationStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationClient reservationClient;
    private final PgClient pgClient;

    // MSA 대비 1단계: 예약 상태 검증을 DB 직접 조회 대신 ReservationClient(동기 REST)로 함
    public PaymentRequestResponse request(Long reservationId, PaymentType paymentType, Integer amount) {
        ReservationView reservation = reservationClient.get(reservationId);
        if (reservation.status() != ReservationView.Status.REQUESTED) {
            throw new IllegalStateException("예약요청 상태에서만 결제를 요청할 수 있습니다. 현재 상태: " + reservation.status());
        }

        String orderId = generateOrderId(reservationId, paymentType);
        Payment payment = Payment.create(reservationId, paymentType, orderId, amount);
        paymentRepository.save(payment);

        // Toss가 발급하는 값이 아니라 우리가 만들어서 넘겨주는 값 - 정기결제 없이는 재사용할 이유가 없어 매번 새로 발급, 저장 안 함
        String customerKey = UUID.randomUUID().toString();
        return new PaymentRequestResponse(payment.getId(), orderId, customerKey, amount);
    }

    /**
     * 결제 승인 및 예약 확정.
     * 금액 위변조 검증을 PG 승인 요청보다 먼저 해서, 조작된 금액으로 실제 승인 API가 나가는 일이 없게 한다.
     */
    public void confirmSuccess(String orderId, String paymentKey, Integer amount) {
        Payment payment = getPaymentByOrderId(orderId);
        if (!payment.getAmount().equals(amount)) {
            throw new IllegalStateException(
                    "요청 금액이 결제 금액과 일치하지 않습니다. 저장된 금액: " + payment.getAmount() + ", 전달된 금액: " + amount);
        }

        // 결제 승인 요청
        // 참고) paymentKey : orderId와 달리 Toss가 발급한 값. 이후 조회/취소 시 Toss가 요구하는 식별자
        try {
            PgConfirmResult result = pgClient.confirm(paymentKey, orderId, amount); /// 결제 승인 요청
            payment.approve(result.paymentKey(), result.approvedAt());
        } catch (PgApiException e) {
            payment.fail(e.getMessage());
            throw e;
        }

        // 예약 확정
        Reservation reservation = reservationRepository.findById(payment.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. id=" + payment.getReservationId()));
        if (reservation.getStatus() == ReservationStatus.REQUESTED) {
            reservation.confirm();
        }
    }

    /** 결제 실패 */
    public void confirmFailure(String orderId, String failureReason) {
        Payment payment = getPaymentByOrderId(orderId);
        payment.fail(failureReason);
    }

    /** 환불 요청 */
    public void requestRefund(Long paymentId, String refundReason) {
        Payment payment = getPayment(paymentId);
        payment.requestRefund(refundReason);
    }

    /** 환불 승인 요청 */
    public void approveRefund(Long paymentId, Integer refundAmount) {
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.REFUND_REQUESTED) {
            throw new IllegalStateException("환불요청 상태에서만 환불을 승인할 수 있습니다. 현재 상태: " + payment.getStatus());
        }

        PgCancelResult result = pgClient.cancel(payment.getPaymentKey(), payment.getRefundReason(), refundAmount);
        payment.approveRefund(refundAmount, result.canceledAt());
    }

    /** 환불 거절 */
    public void rejectRefund(Long paymentId, String rejectReason) {
        Payment payment = getPayment(paymentId);
        payment.rejectRefund(rejectReason);
    }

    public void disputeRefundRejection(Long paymentId) {
        Payment payment = getPayment(paymentId);
        payment.disputeRefundRejection();
    }

    /**
     * reservationId를 그대로 orderId로 쓰지 않는 이유:
     * 1) 한 Reservation에 Payment가 여러 개(예약금/잔금, 재시도) 달릴 수 있는데, Toss orderId는
     *    결제 시도 1건마다 유니크해야 한다 - reservationId만 쓰면 두 번째 결제부터 충돌.
     * 2) Toss orderId는 최소 6자 이상이어야 하는데, reservationId 숫자만으로는 이 길이를 못 채우는
     *    경우가 많다(특히 초창기 낮은 id).
     */
    private String generateOrderId(Long reservationId, PaymentType paymentType) {
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);
        return "RESV" + reservationId + "-" + paymentType + "-" + shortUuid;
    }

    private Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. id=" + id));
    }

    private Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. orderId=" + orderId));
    }
}
