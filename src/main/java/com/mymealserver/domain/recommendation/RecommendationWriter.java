package com.mymealserver.domain.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationWriter {

    private final RecommendationRepository recommendationRepository;

    @Transactional
    public Recommendation save(Recommendation recommendation) {
        return recommendationRepository.save(recommendation);
    }

    @Transactional
    public List<Recommendation> saveAll(List<Recommendation> recommendations) {
        return recommendationRepository.saveAll(recommendations);
    }

    @Transactional
    public void delete(Recommendation recommendation) {
        recommendation.softDelete();
        recommendationRepository.save(recommendation);
    }

    @Transactional
    public void deleteById(Long id) {
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow();
        recommendation.softDelete();
        recommendationRepository.save(recommendation);
    }

    @Transactional
    public void markAsSent(Recommendation recommendation) {
        recommendation.markAsSent();
        recommendationRepository.save(recommendation);
    }
}
