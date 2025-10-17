package com.hanainplan.domain.user.service;

import com.hanainplan.domain.user.dto.RiskProfileRequest;
import com.hanainplan.domain.user.dto.RiskProfileResponse;
import com.hanainplan.domain.user.entity.Customer;
import com.hanainplan.domain.user.entity.RiskProfileAnswer;
import com.hanainplan.domain.user.repository.CustomerRepository;
import com.hanainplan.domain.user.repository.RiskProfileAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RiskProfileService {

    private final CustomerRepository customerRepository;
    private final RiskProfileAnswerRepository riskProfileAnswerRepository;

    // 질문 텍스트 정의
    private static final String[] QUESTIONS = {
        "귀하의 투자 경험은 어느 정도입니까?", // 1
        "투자금의 일부 손실 가능성에 대해 어떻게 생각하십니까?", // 2
        "IRP 계좌의 운용 기간(인출 시점까지)은 어느 정도입니까?", // 3
        "투자 성과를 평가할 때 가장 중요하게 생각하는 것은?", // 4
        "최근 1년간 금융상품 투자 시, 변동성에 따른 대응 방식은?", // 5
        "현재의 전체 자산 대비 투자 가능 비율은 어느 정도입니까?", // 6
        "IRP를 통해 얻고 싶은 주요 목표는 무엇입니까?" // 7
    };

    // 답변 텍스트 정의
    private static final String[][] ANSWER_OPTIONS = {
        {"전혀 없음", "예·적금만 있음", "펀드 등 간접투자 경험 있음", "직접투자(주식 등) 경험 있음", "3년 이상 투자활동 지속"},
        {"절대 손실 원하지 않음", "약간의 손실은 감수 가능", "수익을 위해 손실도 감수 가능", "손실을 감수하더라도 고수익 추구", "큰 손실 가능해도 고수익 기대"},
        {"3년 미만", "3~5년", "5~10년", "10~20년", "20년 이상"},
        {"원금 보전", "안정적인 이자 수익", "예금보다 높은 안정적 수익", "시장 평균 이상의 수익", "높은 기대수익률"},
        {"손실 시 즉시 해지", "일부 축소", "유지", "저가 매수", "추가 매수"},
        {"10% 이하", "20% 이하", "30% 이하", "50% 이하", "50% 이상"},
        {"안정적 퇴직금 운용", "물가 상승 방어", "중위험 중수익", "장기 성장성 확보", "공격적 수익 추구"}
    };

    public RiskProfileResponse evaluateRiskProfile(RiskProfileRequest request) {
        log.info("리스크 프로파일 평가 시작 - 고객 ID: {}", request.getCustomerId());

        // 고객 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));

        // 세션 ID 생성 (요청에 없으면 새로 생성)
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "session_" + System.currentTimeMillis();
        }
        final String finalSessionId = sessionId;

        // 기존 답변 삭제 (같은 세션)
        if (riskProfileAnswerRepository.existsByCustomerIdAndSessionId(request.getCustomerId(), sessionId)) {
            riskProfileAnswerRepository.deleteByCustomerIdAndSessionId(request.getCustomerId(), sessionId);
        }

        // 답변 저장
        List<RiskProfileAnswer> answers = request.getAnswers().stream()
                .map(answerDto -> {
                    validateAnswer(answerDto);
                    return RiskProfileAnswer.create(
                            request.getCustomerId(),
                            answerDto.getQuestionNumber(),
                            answerDto.getAnswerScore(),
                            answerDto.getQuestionText(),
                            answerDto.getAnswerText(),
                            finalSessionId
                    );
                })
                .toList();

        riskProfileAnswerRepository.saveAll(answers);

        // 평균 점수 계산
        BigDecimal averageScore = calculateAverageScore(answers);
        
        // 리스크 프로파일 타입 결정
        Customer.RiskProfileType riskType = Customer.RiskProfileType.fromScore(averageScore);

        // 고객 정보 업데이트
        customer.setRiskProfileScore(averageScore);
        customer.setRiskProfileType(riskType);
        customer.setRiskProfileUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        log.info("리스크 프로파일 평가 완료 - 고객 ID: {}, 점수: {}, 타입: {}", 
                request.getCustomerId(), averageScore, riskType);

        // 응답 생성
        return RiskProfileResponse.from(customer, answers);
    }

    @Transactional(readOnly = true)
    public RiskProfileResponse getRiskProfile(Long customerId) {
        log.info("리스크 프로파일 조회 - 고객 ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));

        List<RiskProfileAnswer> latestAnswers = riskProfileAnswerRepository.findLatestAnswersByCustomer(customerId);

        return RiskProfileResponse.from(customer, latestAnswers);
    }

    @Transactional(readOnly = true)
    public List<RiskProfileAnswer> getRiskProfileAnswers(Long customerId) {
        return riskProfileAnswerRepository.findByCustomerIdOrderByAnsweredAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<String> getQuestionTexts() {
        return List.of(QUESTIONS);
    }

    @Transactional(readOnly = true)
    public List<String> getAnswerOptions(Integer questionNumber) {
        if (questionNumber < 1 || questionNumber > 7) {
            throw new IllegalArgumentException("질문 번호는 1~7 사이여야 합니다: " + questionNumber);
        }
        return List.of(ANSWER_OPTIONS[questionNumber - 1]);
    }

    private void validateAnswer(RiskProfileRequest.QuestionAnswer answer) {
        if (answer.getQuestionNumber() == null || answer.getQuestionNumber() < 1 || answer.getQuestionNumber() > 7) {
            throw new IllegalArgumentException("질문 번호는 1~7 사이여야 합니다: " + answer.getQuestionNumber());
        }
        if (answer.getAnswerScore() == null || answer.getAnswerScore() < 1 || answer.getAnswerScore() > 5) {
            throw new IllegalArgumentException("답변 점수는 1~5 사이여야 합니다: " + answer.getAnswerScore());
        }
    }

    private BigDecimal calculateAverageScore(List<RiskProfileAnswer> answers) {
        if (answers.isEmpty()) {
            throw new IllegalArgumentException("답변이 없습니다.");
        }

        double sum = answers.stream()
                .mapToInt(RiskProfileAnswer::getAnswerScore)
                .sum();

        double average = sum / answers.size();
        return BigDecimal.valueOf(average).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public RiskProfileRequest.QuestionAnswer createQuestionAnswer(
            Integer questionNumber, Integer answerScore) {
        
        if (questionNumber < 1 || questionNumber > 7) {
            throw new IllegalArgumentException("질문 번호는 1~7 사이여야 합니다: " + questionNumber);
        }
        if (answerScore < 1 || answerScore > 5) {
            throw new IllegalArgumentException("답변 점수는 1~5 사이여야 합니다: " + answerScore);
        }

        return RiskProfileRequest.QuestionAnswer.builder()
                .questionNumber(questionNumber)
                .answerScore(answerScore)
                .questionText(QUESTIONS[questionNumber - 1])
                .answerText(ANSWER_OPTIONS[questionNumber - 1][answerScore - 1])
                .build();
    }
}
