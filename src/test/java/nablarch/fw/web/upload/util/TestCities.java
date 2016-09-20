package nablarch.fw.web.upload.util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
