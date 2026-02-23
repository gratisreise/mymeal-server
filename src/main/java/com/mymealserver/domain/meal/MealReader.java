package com.mymealserver.domain.meal;

import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.enums.MealType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MealReader {

    private final MealRepository mealRepository;

    public Meal findById(Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_NOT_FOUND));
    }

    public Optional<Meal> findByIdOptional(Long id) {
        return mealRepository.findById(id);
    }

    public Page<Meal> findByMemberId(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            MealType mealType,
            Pageable pageable
    ) {
        Specification<Meal> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("memberId"), memberId)
        );

        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("mealTime"), startDateTime)
            );
        }

        if (endDate != null) {
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("mealTime"), endDateTime)
            );
        }

        if (mealType != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("mealType"), mealType)
            );
        }

        return mealRepository.findAll(spec, pageable);
    }

    public boolean existsByMemberId(Long memberId) {
        return mealRepository.existsByMemberId(memberId);
    }

    public List<Meal> findByMemberIdAndDateRange(
            Long memberId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        return mealRepository.findAllByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
                memberId,
                startDateTime,
                endDateTime
        );
    }
}
