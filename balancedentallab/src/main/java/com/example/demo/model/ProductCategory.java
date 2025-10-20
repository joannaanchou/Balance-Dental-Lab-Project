package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_category")
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="category_no", unique = true, nullable = false, length = 20)
    private String categoryNo;

    @Column(nullable = false, length = 120)
    private String name;

	@Override
	public String toString() {
		return "ProductCategory [id=" + id + ", categoryNo=" + categoryNo + ", name=" + name + "]";
	}
    
    
}
