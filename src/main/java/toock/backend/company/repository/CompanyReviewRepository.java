package toock.backend.company.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toock.backend.company.domain.CompanyReview;


import java.util.List;

public interface CompanyReviewRepository extends JpaRepository<CompanyReview, Long> {
    List<CompanyReview> findByCompany_Name(String companyName);
}