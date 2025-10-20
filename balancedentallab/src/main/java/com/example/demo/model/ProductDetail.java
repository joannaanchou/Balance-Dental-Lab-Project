package com.example.demo.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "product_detail",
    uniqueConstraints = @UniqueConstraint(name="uk_pd_cat_item", columnNames = {"category_no","item_no"})
)
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_no", unique = true, nullable = false, length = 20)
    private String productNo;

    @ManyToOne(optional = false)
    @JoinColumn(name="category_no", referencedColumnName = "category_no")
    private ProductCategory category;

    @ManyToOne(optional = false)
    @JoinColumn(name="item_no", referencedColumnName = "item_no")
    private ProductItem item;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

	@Override
	public String toString() {
		return "ProductDetail [id=" + id + ", productNo=" + productNo + ", category=" + category + ", item=" + item
				+ ", price=" + price + "]";
	}
    
    
}
