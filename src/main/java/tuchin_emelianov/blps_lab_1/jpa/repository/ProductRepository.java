package tuchin_emelianov.blps_lab_1.jpa.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tuchin_emelianov.blps_lab_1.jpa.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findProductByName(String name);
    Product findProductById(Long id);

    Page<Product> findAll(Pageable pageable);
}
