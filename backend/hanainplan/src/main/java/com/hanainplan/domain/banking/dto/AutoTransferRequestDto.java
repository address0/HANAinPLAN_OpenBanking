package com.hanainplan.domain.banking.dto;

import com.hanainplan.domain.banking.entity.AutoTransfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoTransferRequestDto {

    @NotNull(message = "출금 계좌 ID는 필수입니다")
    private Long fromAccountId;

    @NotNull(message = "입금 계좌 ID는 필수입니다")
    private Long toAccountId;

    @NotNull(message = "자동이체명은 필수입니다")
    @Size(max = 100, message = "자동이체명은 100자를 초과할 수 없습니다")
    private String transferName;

    @NotNull(message = "이체 금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "이체 금액은 0.01원 이상이어야 합니다")
    private BigDecimal amount;

    @NotNull(message = "이체 주기는 필수입니다")
    private AutoTransfer.TransferCycle transferCycle;

    private Integer transferDay;

    private AutoTransfer.Weekday transferWeekday;

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 20, message = "상대방 계좌번호는 20자를 초과할 수 없습니다")
    private String counterpartAccountNumber;

    @Size(max = 50, message = "상대방 이름은 50자를 초과할 수 없습니다")
    private String counterpartName;

    @Size(max = 10, message = "상대방 은행 코드는 10자를 초과할 수 없습니다")
    private String counterpartBankCode;

    @Size(max = 200, message = "설명은 200자를 초과할 수 없습니다")
    private String description;

    private Integer maxTransferCount;

    @NotNull(message = "최대 실패 허용 횟수는 필수입니다")
    private Integer maxFailureCount;
}