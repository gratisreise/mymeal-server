package com.mymealserver.domain.meallog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MealLogWriter {

    private final MealLogRepository mealLogRepository;

    @Transactional
    public MealLog save(MealLog mealLog) {
        return mealLogRepository.save(mealLog);
    }

    @Transactional
    public void delete(MealLog mealLog) {
        mealLog.softDelete();
        mealLogRepository.save(mealLog);
    }
}
