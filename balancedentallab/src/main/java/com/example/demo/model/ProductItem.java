package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_item")
public class ProductItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="item_no", unique = true, nullable = false, length = 20)
    private String itemNo;

    @Column(nullable = false, length = 150)
    private String name;

	@Override
	public String toString() {
		return "ProductItem [id=" + id + ", itemNo=" + itemNo + ", name=" + name + "]";
	}
    
    
}
