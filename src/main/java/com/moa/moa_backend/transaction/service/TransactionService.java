package com.moa.moa_backend.transaction.service;

import com.moa.moa_backend.accountbook.domain.AccountBook;
import com.moa.moa_backend.accountbook.service.AccountBookService;
import com.moa.moa_backend.category.domain.Category;
import com.moa.moa_backend.category.repository.CategoryRepository;
import com.moa.moa_backend.paymentmethod.domain.PaymentMethod;
import com.moa.moa_backend.paymentmethod.repository.PaymentMethodRepository;
import com.moa.moa_backend.transaction.domain.RecurringType;
import com.moa.moa_backend.transaction.domain.Transaction;
import com.moa.moa_backend.transaction.domain.TransactionType;
import com.moa.moa_backend.transaction.dto.*;
import com.moa.moa_backend.transaction.repository.TransactionRepository;
import com.moa.moa_backend.user.domain.User;
import com.moa.moa_backend.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final AccountBookService accountBookService;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public TransactionService(TransactionRepository transactionRepository,
                             UserService userService,
                             AccountBookService accountBookService,
                             CategoryRepository categoryRepository,
                             PaymentMethodRepository paymentMethodRepository) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.accountBookService = accountBookService;
        this.categoryRepository = categoryRepository;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Transactional
    public TransactionResponseDto createTransaction(HttpSession session, Long accountBookId, CreateTransactionRequestDto request) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        Category category = categoryRepository.findByIdAndAccountBook(request.getCategoryId(), accountBook)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndAccountBook(request.getPaymentMethodId(), accountBook)
                .orElseThrow(() -> new RuntimeException("결제 수단을 찾을 수 없습니다."));

        Transaction transaction = Transaction.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .type(request.getType())
                .transactionDate(request.getTransactionDate())
                .accountBook(accountBook)
                .category(category)
                .paymentMethod(paymentMethod)
                .createdBy(currentUser)
                .memo(request.getMemo())
                .isExcludedFromBudget(request.getIsExcludedFromBudget())
                .recurringType(request.getRecurringType() != null ? request.getRecurringType() : RecurringType.NONE)
                .isActive(true)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("거래 내역 생성 완료 - 가계부 ID: {}, 사용자 ID: {}, 거래 ID: {}",
                accountBookId, currentUser.getId(), savedTransaction.getId());

        return TransactionResponseDto.from(savedTransaction);
    }

    public List<TransactionResponseDto> getTransactions(HttpSession session, Long accountBookId) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        List<Transaction> transactions = transactionRepository.findByAccountBookAndIsActiveTrueOrderByTransactionDateDesc(accountBook);

        return transactions.stream()
                .map(TransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    public Page<TransactionResponseDto> getTransactionsWithPaging(HttpSession session, Long accountBookId, Pageable pageable) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        Page<Transaction> transactions = transactionRepository.findByAccountBookAndIsActiveTrueOrderByTransactionDateDesc(accountBook, pageable);

        return transactions.map(TransactionResponseDto::from);
    }

    public TransactionResponseDto getTransaction(HttpSession session, Long accountBookId, Long id) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        Transaction transaction = transactionRepository.findByIdAndAccountBook(id, accountBook)
                .orElseThrow(() -> new RuntimeException("거래 내역을 찾을 수 없습니다."));

        if (!transaction.getIsActive()) {
            throw new RuntimeException("삭제된 거래 내역입니다.");
        }

        return TransactionResponseDto.from(transaction);
    }

    @Transactional
    public TransactionResponseDto updateTransaction(HttpSession session, Long accountBookId, Long id, UpdateTransactionRequestDto request) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        Transaction transaction = transactionRepository.findByIdAndAccountBookAndCreatedBy(id, accountBook, currentUser)
                .orElseThrow(() -> new RuntimeException("거래 내역을 찾을 수 없거나 수정 권한이 없습니다."));

        if (!transaction.getIsActive()) {
            throw new RuntimeException("삭제된 거래 내역입니다.");
        }

        Category category = categoryRepository.findByIdAndAccountBook(request.getCategoryId(), accountBook)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndAccountBook(request.getPaymentMethodId(), accountBook)
                .orElseThrow(() -> new RuntimeException("결제 수단을 찾을 수 없습니다."));

        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCategory(category);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setMemo(request.getMemo());
        transaction.setIsExcludedFromBudget(request.getIsExcludedFromBudget());
        transaction.setRecurringType(request.getRecurringType() != null ? request.getRecurringType() : RecurringType.NONE);

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("거래 내역 수정 완료 - 가계부 ID: {}, 사용자 ID: {}, 거래 ID: {}",
                accountBookId, currentUser.getId(), savedTransaction.getId());

        return TransactionResponseDto.from(savedTransaction);
    }

    @Transactional
    public void deleteTransaction(HttpSession session, Long accountBookId, Long id) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        Transaction transaction = transactionRepository.findByIdAndAccountBookAndCreatedBy(id, accountBook, currentUser)
                .orElseThrow(() -> new RuntimeException("거래 내역을 찾을 수 없거나 삭제 권한이 없습니다."));

        if (!transaction.getIsActive()) {
            throw new RuntimeException("이미 삭제된 거래 내역입니다.");
        }

        transaction.setIsActive(false);
        transactionRepository.save(transaction);

        log.info("거래 내역 삭제 완료 - 가계부 ID: {}, 사용자 ID: {}, 거래 ID: {}",
                accountBookId, currentUser.getId(), transaction.getId());
    }

    public List<TransactionResponseDto> getTransactionsByDateRange(HttpSession session, Long accountBookId, LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        List<Transaction> transactions = transactionRepository.findByAccountBookAndIsActiveTrueAndTransactionDateBetweenOrderByTransactionDateDesc(
                accountBook, startDate, endDate);

        return transactions.stream()
                .map(TransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<TransactionResponseDto> getTransactionsByMonth(HttpSession session, Long accountBookId, int year, int month) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        List<Transaction> transactions = transactionRepository.findByAccountBookAndYearAndMonth(accountBook, year, month);

        return transactions.stream()
                .map(TransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<TransactionResponseDto> getTransactionsByType(HttpSession session, Long accountBookId, TransactionType type) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        List<Transaction> transactions = transactionRepository.findByAccountBookAndTypeAndIsActiveTrueOrderByTransactionDateDesc(accountBook, type);

        return transactions.stream()
                .map(TransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    public TransactionStatsDto getTransactionStats(HttpSession session, Long accountBookId, LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        Long totalIncome = transactionRepository.getTotalIncomeByAccountBook(accountBook);
        Long totalExpense = transactionRepository.getTotalExpenseByAccountBook(accountBook);
        Long totalSavings = transactionRepository.getTotalSavingsByAccountBook(accountBook);
        Long netIncome = totalIncome - totalExpense;

        long totalCount = transactionRepository.countByAccountBookAndIsActiveTrue(accountBook);

        return TransactionStatsDto.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalSavings(totalSavings)
                .netIncome(netIncome)
                .startDate(startDate)
                .endDate(endDate)
                .totalTransactionCount(totalCount)
                .build();
    }

    /**
     * 월별 달력 데이터 조회
     */
    public CalendarResponseDto getCalendarByMonth(HttpSession session, Long accountBookId, int year, int month) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 월별 통계 조회
        Long totalIncome = transactionRepository.getTotalIncomeByMonth(accountBook, year, month);
        Long totalExpense = transactionRepository.getTotalExpenseByMonth(accountBook, year, month);
        Long totalSavings = transactionRepository.getTotalSavingsByMonth(accountBook, year, month);
        Long totalExcludedFromBudget = transactionRepository.getTotalExcludedFromBudgetByMonth(accountBook, year, month);
        Long total = totalIncome - totalExpense;

        CalendarStatsDto stats = CalendarStatsDto.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .total(total)
                .totalSavings(totalSavings)
                .totalExcludedFromBudget(totalExcludedFromBudget)
                .year(year)
                .month(month)
                .build();

        // 해당 월의 일별 통계 조회
        List<Object[]> dailyStats = transactionRepository.getDailyStatsByMonth(accountBook, year, month);
        Map<LocalDate, CalendarDayDto> dayMap = new HashMap<>();

        // 일별 통계 데이터 처리
        for (Object[] stat : dailyStats) {
            LocalDate date = (LocalDate) stat[0];
            TransactionType type = (TransactionType) stat[1];
            Long amount = (Long) stat[2];
            Long excludedAmount = (Long) stat[3];

            CalendarDayDto dayDto = dayMap.getOrDefault(date, CalendarDayDto.builder()
                    .date(date)
                    .totalIncome(0L)
                    .totalExpense(0L)
                    .totalExcludedFromBudget(0L)
                    .transactions(new ArrayList<>())
                    .build());

            if (type == TransactionType.INCOME) {
                dayDto = dayDto.toBuilder().totalIncome(dayDto.getTotalIncome() + amount).build();
            } else if (type == TransactionType.EXPENSE) {
                dayDto = dayDto.toBuilder().totalExpense(dayDto.getTotalExpense() + amount).build();
            }

            dayDto = dayDto.toBuilder().totalExcludedFromBudget(dayDto.getTotalExcludedFromBudget() + excludedAmount).build();
            dayMap.put(date, dayDto);
        }

        // 해당 월의 모든 날짜에 대해 거래 내역 조회
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (!dayMap.containsKey(date)) {
                dayMap.put(date, CalendarDayDto.builder()
                        .date(date)
                        .totalIncome(0L)
                        .totalExpense(0L)
                        .totalExcludedFromBudget(0L)
                        .transactions(new ArrayList<>())
                        .build());
            }

            // 해당 날짜의 거래 내역 조회
            List<Transaction> transactions = transactionRepository.findByAccountBookAndDateWithDetails(accountBook, date);
            List<DailyTransactionDto> dailyTransactions = transactions.stream()
                    .map(this::convertToDailyTransactionDto)
                    .collect(Collectors.toList());

            CalendarDayDto dayDto = dayMap.get(date);
            dayDto = dayDto.toBuilder().transactions(dailyTransactions).build();
            dayMap.put(date, dayDto);
        }

        List<CalendarDayDto> days = dayMap.values().stream()
                .sorted(Comparator.comparing(CalendarDayDto::getDate))
                .collect(Collectors.toList());

        return CalendarResponseDto.builder()
                .stats(stats)
                .days(days)
                .build();
    }

    /**
     * 기간별 달력 데이터 조회
     */
    public CalendarResponseDto getCalendarByDateRange(HttpSession session, Long accountBookId, LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 기간별 통계 조회
        Long totalIncome = transactionRepository.getTotalIncomeByDateRange(accountBook, startDate, endDate);
        Long totalExpense = transactionRepository.getTotalExpenseByDateRange(accountBook, startDate, endDate);
        Long totalSavings = transactionRepository.getTotalSavingsByDateRange(accountBook, startDate, endDate);
        Long totalExcludedFromBudget = transactionRepository.getTotalExcludedFromBudgetByDateRange(accountBook, startDate, endDate);
        Long total = totalIncome - totalExpense;

        CalendarStatsDto stats = CalendarStatsDto.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .total(total)
                .totalSavings(totalSavings)
                .totalExcludedFromBudget(totalExcludedFromBudget)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // 기간 내의 모든 날짜에 대해 거래 내역 조회
        Map<LocalDate, CalendarDayDto> dayMap = new HashMap<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 해당 날짜의 거래 내역 조회
            List<Transaction> transactions = transactionRepository.findByAccountBookAndDateWithDetails(accountBook, date);
            
            // 일별 통계 계산
            Long dayIncome = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .mapToLong(Transaction::getAmount)
                    .sum();
            
            Long dayExpense = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .mapToLong(Transaction::getAmount)
                    .sum();
            
            Long dayExcluded = transactions.stream()
                    .filter(Transaction::getIsExcludedFromBudget)
                    .mapToLong(Transaction::getAmount)
                    .sum();

            List<DailyTransactionDto> dailyTransactions = transactions.stream()
                    .map(this::convertToDailyTransactionDto)
                    .collect(Collectors.toList());

            CalendarDayDto dayDto = CalendarDayDto.builder()
                    .date(date)
                    .totalIncome(dayIncome)
                    .totalExpense(dayExpense)
                    .totalExcludedFromBudget(dayExcluded)
                    .transactions(dailyTransactions)
                    .build();

            dayMap.put(date, dayDto);
        }

        List<CalendarDayDto> days = dayMap.values().stream()
                .sorted(Comparator.comparing(CalendarDayDto::getDate))
                .collect(Collectors.toList());

        return CalendarResponseDto.builder()
                .stats(stats)
                .days(days)
                .build();
    }

    /**
     * 특정 날짜의 거래 내역 조회
     */
    public List<DailyTransactionDto> getDailyTransactions(HttpSession session, Long accountBookId, LocalDate date) {
        User currentUser = userService.getCurrentUser(session);
        if (currentUser == null) {
            throw new RuntimeException("로그인된 사용자 정보를 찾을 수 없습니다.");
        }

        AccountBook accountBook = accountBookService.getAccountBookByIdAndUser(accountBookId, currentUser);
        if (accountBook == null) {
            throw new RuntimeException("가계부를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        List<Transaction> transactions = transactionRepository.findByAccountBookAndDateWithDetails(accountBook, date);

        return transactions.stream()
                .map(this::convertToDailyTransactionDto)
                .collect(Collectors.toList());
    }

    /**
     * Transaction을 DailyTransactionDto로 변환
     */
    private DailyTransactionDto convertToDailyTransactionDto(Transaction transaction) {
        return DailyTransactionDto.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .transactionDate(transaction.getTransactionDate())
                .isExcludedFromBudget(transaction.getIsExcludedFromBudget())
                .createdById(transaction.getCreatedBy().getId())
                .createdByNickname(transaction.getCreatedBy().getNickname())
                .createdByProfileImageUrl(transaction.getCreatedBy().getProfileImageUrl())
                .categoryId(transaction.getCategory().getId())
                .categoryName(transaction.getCategory().getName())
                .paymentMethodId(transaction.getPaymentMethod().getId())
                .paymentMethodName(transaction.getPaymentMethod().getName())
                .memo(transaction.getMemo())
                .build();
    }
}

