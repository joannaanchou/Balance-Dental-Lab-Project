package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clinic_dentist")
public class ClinicDentist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="clinic_no", referencedColumnName = "clinic_no")
    private Clinic clinic;

    @ManyToOne(optional = false)
    @JoinColumn(name="dentist_no", referencedColumnName = "dentist_no")
    private Dentist dentist;

	@Override
	public String toString() {
		return "ClinicDentist [id=" + id + ", clinic=" + clinic + ", dentist=" + dentist + "]";
	}
    
    
}
