package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clinic")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="clinic_no", unique = true, nullable = false, length = 20)
    private String clinicNo;

    @Column(name="clinic_name", nullable = false, length = 150)
    private String clinicName;

    @Column(length = 50)
    private String phone;

    @Column(length = 200)
    private String address;

	@Override
	public String toString() {
		return "Clinic [id=" + id + ", clinicNo=" + clinicNo + ", clinicName=" + clinicName + ", phone=" + phone
				+ ", address=" + address + "]";
	}
    
    
}
