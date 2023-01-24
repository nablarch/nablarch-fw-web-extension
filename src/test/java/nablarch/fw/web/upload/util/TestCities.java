package nablarch.fw.web.upload.util;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * コード名称
 * 
 */
@Entity
@Table(name = "TEST_CITIES")
public class TestCities {
   
    public TestCities() {
    };
    
    public TestCities(Long id, String city) {
        this.id = id;
        this.city = city;
    }

    @Id
    @Column(name = "ID", length = 1, nullable = false)
    public Long id;
    
    @Id
    @Column(name = "CITY", length = 9, nullable = false)
    public String city;
}
