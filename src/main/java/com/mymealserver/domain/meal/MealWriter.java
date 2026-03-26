package com.mymealserver.domain.meal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MealWriter {

  private final MealRepository mealRepository;

  @Transactional
  public Meal save(Meal meal) {
    return mealRepository.save(meal);
  }

  @Transactional
  public void delete(Meal meal) {
    meal.softDelete();
    mealRepository.save(meal);
  }
}
