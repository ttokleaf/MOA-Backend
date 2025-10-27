package com.moa.moa_backend.budget.service;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.accountbook.service.AccountBookService;
import com.moa.moa_backend.budget.domain.Budget;
import com.moa.moa_backend.budget.dto.*;
import com.moa.moa_backend.budget.repository.BudgetRepository;
import com.moa.moa_backend.transaction.repository.TransactionRepository;
import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final AccountBookService accountBookService;

    /**
     * 예산 생성 (현재 년월로 자동 설정)
     */
    @Transactional
    public BudgetResponseDto createBudget(HttpSession session, Long accountBookId, CreateBudgetRequestDto request) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 현재 년월로 자동 설정
        LocalDate currentDate = LocalDate.now();
        Integer year = currentDate.getYear();
        Integer month = currentDate.getMonthValue();

        // 해당 년월에 이미 예산이 설정되어 있는지 확인
        if (budgetRepository.existsByAccountBookAndYearAndMonthAndIsActiveTrue(accountBook, year, month)) {
            throw new RuntimeException("이번 달에 이미 예산이 설정되어 있습니다.");
        }

        Budget budget = Budget.builder()
                .accountBook(accountBook)
                .year(year)
                .month(month)
                .amount(request.getAmount())
                .createdBy(currentUser)
                .isActive(true)
                .build();

        Budget savedBudget = budgetRepository.save(budget);

        log.info("예산 생성 완료 - 가계부 ID: {}, 사용자 ID: {}, 예산 ID: {}, 금액: {}원, 년월: {}-{}",
                accountBookId, currentUser.getId(), savedBudget.getId(), request.getAmount(), year, month);

        return BudgetResponseDto.from(savedBudget);
    }

    /**
     * 현재 년월 예산 수정
     */
    @Transactional
    public BudgetResponseDto updateCurrentBudget(HttpSession session, Long accountBookId, UpdateBudgetRequestDto request) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 현재 년월로 예산 조회
        LocalDate currentDate = LocalDate.now();
        Integer year = currentDate.getYear();
        Integer month = currentDate.getMonthValue();

        Budget budget = budgetRepository.findByAccountBookAndYearAndMonthAndIsActiveTrue(accountBook, year, month)
                .orElseThrow(() -> new RuntimeException("이번 달 예산을 찾을 수 없습니다."));

        budget.setAmount(request.getAmount());
        Budget savedBudget = budgetRepository.save(budget);

        log.info("현재 년월 예산 수정 완료 - 가계부 ID: {}, 사용자 ID: {}, 예산 ID: {}, 금액: {}원, 년월: {}-{}",
                accountBookId, currentUser.getId(), savedBudget.getId(), request.getAmount(), year, month);

        return BudgetResponseDto.from(savedBudget);
    }

    /**
     * 현재 년월 예산 조회
     */
    public BudgetResponseDto getCurrentBudget(HttpSession session, Long accountBookId) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        LocalDate currentDate = LocalDate.now();
        Integer year = currentDate.getYear();
        Integer month = currentDate.getMonthValue();

        Budget budget = budgetRepository.findByAccountBookAndYearAndMonthAndIsActiveTrue(accountBook, year, month)
                .orElseThrow(() -> new RuntimeException("이번 달 예산을 찾을 수 없습니다."));

        return BudgetResponseDto.from(budget);
    }

    /**
     * 예산 조회 (특정 년월)
     */
    public BudgetResponseDto getBudget(HttpSession session, Long accountBookId, Integer year, Integer month) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        Budget budget = budgetRepository.findByAccountBookAndYearAndMonthAndIsActiveTrue(accountBook, year, month)
                .orElseThrow(() -> new RuntimeException("해당 년월의 예산을 찾을 수 없습니다."));

        return BudgetResponseDto.from(budget);
    }

    /**
     * 예산 목록 조회
     */
    public List<BudgetResponseDto> getBudgets(HttpSession session, Long accountBookId) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        List<Budget> budgets = budgetRepository.findByAccountBookAndIsActiveTrueOrderByYearDescMonthDesc(accountBook);

        return budgets.stream()
                .map(BudgetResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 현재 년월 예산 분석
     */
    public BudgetAnalysisDto getCurrentBudgetAnalysis(HttpSession session, Long accountBookId) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        LocalDate currentDate = LocalDate.now();
        Integer year = currentDate.getYear();
        Integer month = currentDate.getMonthValue();

        // 해당 년월의 예산 조회
        Budget budget = budgetRepository.findByAccountBookAndYearAndMonthAndIsActiveTrue(accountBook, year, month)
                .orElseThrow(() -> new RuntimeException("이번 달 예산을 찾을 수 없습니다."));

        // 이번달 지출 총 합 계산
        Long currentMonthExpense = calculateMonthlyExpense(accountBook, year, month);

        // 지난달 지출 총 합 계산
        LocalDate lastMonth = LocalDate.of(year, month, 1).minusMonths(1);
        Long lastMonthExpense = calculateMonthlyExpense(accountBook, lastMonth.getYear(), lastMonth.getMonthValue());

        // 최근 3개월 평균 지출 계산
        Long averageExpense = calculateAverageExpense(accountBook, year, month);

        // 예산 분석 계산
        return calculateBudgetAnalysis(budget, currentMonthExpense, lastMonthExpense, averageExpense);
    }

    /**
     * 예산 분석 (특정 년월)
     */
    public BudgetAnalysisDto getBudgetAnalysis(HttpSession session, Long accountBookId, Integer year, Integer month) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 해당 년월의 예산 조회
        Budget budget = budgetRepository.findByAccountBookAndYearAndMonthAndIsActiveTrue(accountBook, year, month)
                .orElseThrow(() -> new RuntimeException("해당 년월의 예산을 찾을 수 없습니다."));

        // 이번달 지출 총 합 계산
        Long currentMonthExpense = calculateMonthlyExpense(accountBook, year, month);

        // 지난달 지출 총 합 계산
        LocalDate lastMonth = LocalDate.of(year, month, 1).minusMonths(1);
        Long lastMonthExpense = calculateMonthlyExpense(accountBook, lastMonth.getYear(), lastMonth.getMonthValue());

        // 최근 3개월 평균 지출 계산
        Long averageExpense = calculateAverageExpense(accountBook, year, month);

        // 예산 분석 계산
        return calculateBudgetAnalysis(budget, currentMonthExpense, lastMonthExpense, averageExpense);
    }

    /**
     * 예산 삭제
     */
    @Transactional
    public void deleteBudget(HttpSession session, Long accountBookId, Long budgetId) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("예산을 찾을 수 없습니다."));

        if (!budget.getAccountBook().getId().equals(accountBookId)) {
            throw new RuntimeException("해당 가계부의 예산이 아닙니다.");
        }

        budget.setIsActive(false);
        budgetRepository.save(budget);

        log.info("예산 삭제 완료 - 가계부 ID: {}, 사용자 ID: {}, 예산 ID: {}",
                accountBookId, currentUser.getId(), budgetId);
    }

    /**
     * 특정 년월의 지출 총 합 계산
     */
    private Long calculateMonthlyExpense(AccountBook accountBook, Integer year, Integer month) {
        return transactionRepository.getTotalExpenseByMonth(accountBook, year, month);
    }

    /**
     * 최근 3개월 평균 지출 계산
     */
    private Long calculateAverageExpense(AccountBook accountBook, Integer year, Integer month) {
        LocalDate currentDate = LocalDate.of(year, month, 1);
        LocalDate startDate = currentDate.minusMonths(2); // 3개월 전
        
        Long totalExpense = 0L;
        int monthCount = 0;
        
        for (int i = 0; i < 3; i++) {
            LocalDate targetDate = startDate.plusMonths(i);
            Long monthlyExpense = calculateMonthlyExpense(accountBook, targetDate.getYear(), targetDate.getMonthValue());
            totalExpense += monthlyExpense;
            monthCount++;
        }
        
        return monthCount > 0 ? totalExpense / monthCount : 0L;
    }

    /**
     * 예산 분석 계산
     */
    private BudgetAnalysisDto calculateBudgetAnalysis(Budget budget, Long currentMonthExpense, 
                                                     Long lastMonthExpense, Long averageExpense) {
        Long budgetAmount = budget.getAmount();
        Long remainingAmount = budgetAmount - currentMonthExpense;
        
        BudgetAnalysisDto.BudgetStatus status;
        Long overAmount = null;
        Double usageRate;
        
        if (remainingAmount >= 0) {
            // 예산 내
            status = BudgetAnalysisDto.BudgetStatus.UNDER_BUDGET;
            usageRate = budgetAmount > 0 ? (double) currentMonthExpense / budgetAmount * 100 : 0.0;
        } else {
            // 예산 초과
            status = BudgetAnalysisDto.BudgetStatus.OVER_BUDGET;
            overAmount = Math.abs(remainingAmount);
            usageRate = 100.0;
        }
        
        return BudgetAnalysisDto.builder()
                .budgetAmount(budgetAmount)
                .currentMonthExpense(currentMonthExpense)
                .remainingAmount(Math.max(0, remainingAmount))
                .overAmount(overAmount)
                .usageRate(Math.round(usageRate * 10.0) / 10.0) // 소수점 첫째자리까지
                .status(status)
                .lastMonthExpense(lastMonthExpense)
                .averageExpense(averageExpense)
                .year(budget.getYear())
                .month(budget.getMonth())
                .build();
    }
}