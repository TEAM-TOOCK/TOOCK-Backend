package toock.backend.company.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toock.backend.company.domain.Company;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
}