package com.voyageguard.planning.api;

import com.voyageguard.planning.api.dto.DepartureCreateRequest;
import com.voyageguard.planning.api.dto.DepartureResponse;
import com.voyageguard.planning.application.DepartureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Departure", description = "회차 상태 전이 API (OPEN -> CLOSED/CANCELLED)")
@RestController
@RequestMapping("/api/v1/departures")
@RequiredArgsConstructor
public class DepartureController {

    private final DepartureService departureService;

    @Operation(summary = "회차 등록", description = "판매종료/폐기 상태가 아닌 상품에 회차를 등록한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 상품")
    @ApiResponse(responseCode = "409", description = "상품이 판매종료 또는 폐기 상태여서 회차 등록 불가")
    @PostMapping
    public Long create(@RequestBody DepartureCreateRequest request) {
        return departureService.create(request.productId(), request.departureDate(), request.minParticipants(),
                request.capacity(), request.itinerary(), request.saleStartDate(), request.saleEndDate(), request.salePrice());
    }

    @Operation(summary = "모집중 회차 목록", description = "OPEN 상태의 회차 목록을 잔여 좌석과 함께 조회한다.")
    @GetMapping
    public List<DepartureResponse> listOpen() {
        return departureService.listOpen();
    }

    @Operation(summary = "회차 상세", description = "회차 하나를 잔여 좌석과 함께 조회한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 회차")
    @GetMapping("/{id}")
    public DepartureResponse get(@PathVariable Long id) {
        return departureService.get(id);
    }

    @Operation(summary = "회차 마감", description = "모집중 상태의 회차를 마감으로 전환한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 회차")
    @ApiResponse(responseCode = "409", description = "모집중 상태가 아니어서 마감 불가")
    @PostMapping("/{id}/close")
    public void close(@PathVariable Long id) {
        departureService.close(id);
    }

    @Operation(summary = "회차 취소", description = "모집중 또는 마감 상태의 회차를 취소로 전환한다.")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 회차")
    @ApiResponse(responseCode = "409", description = "모집중 또는 마감 상태가 아니어서 취소 불가")
    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) {
        departureService.cancel(id);
    }
}
