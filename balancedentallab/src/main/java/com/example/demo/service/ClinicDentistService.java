package com.example.demo.service;

import java.util.*;
import java.util.stream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Dentist;
import com.example.demo.model.ClinicDentist;
import com.example.demo.repository.ClinicDentistRepository;

@Service
@Transactional
public class ClinicDentistService {

	@Autowired
	private ClinicDentistRepository clinicDentistRepo;

	// 取得全部
	public List<ClinicDentist> getAllClinicDentist() {
		return clinicDentistRepo.findAll();
	}

	// 依ID取得
	public ClinicDentist getClinicDentistById(Long id) {
		return clinicDentistRepo.findById(id).orElseThrow(() -> new RuntimeException("項目不存在: " + id));
	}

	// 建立
	public ClinicDentist createClinicDentist(ClinicDentist request) {
		// 檢查是否重複（避免同一診所與牙醫重複綁定）
		String clinicNo = request.getClinic().getClinicNo();
		String dentistNo = request.getDentist().getDentistNo();

		boolean exists = clinicDentistRepo.existsByClinic_ClinicNoAndDentist_DentistNo(clinicNo, dentistNo);
		if (exists) {
			throw new RuntimeException("診所 " + clinicNo + " 與牙醫 " + dentistNo + " 的關聯已存在！");
		}

		ClinicDentist clinicDentist = new ClinicDentist();
		clinicDentist.setClinic(request.getClinic());
		clinicDentist.setDentist(request.getDentist());
		return clinicDentistRepo.save(clinicDentist);
	}

	// 修改
	public ClinicDentist updateClinicDentist(Long id, ClinicDentist request) {
		ClinicDentist clinicDentist = clinicDentistRepo.findById(id)
				.orElseThrow(() -> new RuntimeException("項目不存在: " + id));

		clinicDentist.setClinic(request.getClinic());
		clinicDentist.setDentist(request.getDentist());

		return clinicDentistRepo.save(clinicDentist);
	}

	// 刪除
	public void deleteClinicDentist(Long id) {
		if (!clinicDentistRepo.existsById(id)) {
			throw new RuntimeException("項目不存在: " + id);
		}
		clinicDentistRepo.deleteById(id);
	}

	// 🔹 依診所查牙醫
	public List<ClinicDentist> getDentistsByClinicNo(String clinicNo) {
		return clinicDentistRepo.findByClinic_ClinicNo(clinicNo);
	}

	// 🔹 依牙醫查診所
	public List<ClinicDentist> getClinicsByDentistNo(String dentistNo) {
		return clinicDentistRepo.findByDentist_DentistNo(dentistNo);
	}

	// 🔹 檢查診所與牙醫的關聯是否已存在
	public boolean checkRelationExists(String clinicNo, String dentistNo) {
		return clinicDentistRepo.existsByClinic_ClinicNoAndDentist_DentistNo(clinicNo, dentistNo);
	}

	public List<Dentist> findDentistsByClinicId(Long clinicId) {
		List<ClinicDentist> links = clinicDentistRepo.findByClinicId(clinicId);
		if (links == null || links.isEmpty())
			return Collections.emptyList();
		return links.stream().map(ClinicDentist::getDentist).filter(Objects::nonNull).collect(Collectors.toList());
	}
}