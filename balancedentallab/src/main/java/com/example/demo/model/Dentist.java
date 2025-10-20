package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dentist")
public class Dentist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="dentist_no", unique = true, nullable = false, length = 20)
    private String dentistNo;

    @Column(name="dentist_name", nullable = false, length = 120)
    private String dentistName;

	@Override
	public String toString() {
		return "Dentist [id=" + id + ", dentistNo=" + dentistNo + ", dentistName=" + dentistName + "]";
	}
    
    
    
}
