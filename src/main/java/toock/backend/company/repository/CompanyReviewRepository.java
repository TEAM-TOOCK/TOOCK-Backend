package toock.backend.company.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import toock.backend.company.domain.CompanyReview;


import java.util.List;

public interface CompanyReviewRepository extends JpaRepository<CompanyReview, Long> {
    List<CompanyReview> findByCompany_Name(String companyName);
    List<CompanyReview> findByCompany_NameAndField(String companyName, String field);

    @Query(value = """
        SELECT cr.* 
        FROM company_review cr
        JOIN company c ON c.id = cr.company_id
        WHERE c.name = :companyName
          AND cr.field = :field
        ORDER BY RAND()
        """,
            nativeQuery = true)
    List<CompanyReview> findRandomByCompanyAndField(
            @Param("companyName") String companyName,
            @Param("field") String field,
            Pageable pageable
    );
}