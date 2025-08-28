package toock.backend.company.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toock.backend.company.domain.CompanyReview;

public interface CompanyReviewRepository extends JpaRepository<CompanyReview, Long> {
}