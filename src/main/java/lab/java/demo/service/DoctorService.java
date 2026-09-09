package lab.java.demo.service;

import lab.java.demo.Models.Doctor;
import lab.java.demo.repository.DoctorRepository;
import lab.java.demo.util.DoctorComparators;

import java.util.List;
import java.util.Optional;

public class DoctorService extends BaseCrudService<Doctor, Integer> {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    // -------- BaseCrudService hooks --------

    @Override
    protected List<Doctor> rawFindAll() {
        return doctorRepository.findAll();
    }

    @Override
    protected Optional<Doctor> rawFindById(Integer id) {
        return doctorRepository.findById(id);
    }

    @Override
    protected Doctor rawSave(Doctor entity) {
        return doctorRepository.save(entity);
    }

    @Override
    protected boolean rawDeleteById(Integer id) {
        return doctorRepository.deleteById(id);
    }

    // -------- Doctor-specific API --------

    public Doctor addDoctor(Doctor doctor) {
        return save(doctor);
    }

    @Override
    public Optional<Doctor> findById(Integer id) {
        return super.findById(id);
    }

    public List<Doctor> findBySpecialty(String specialty) {
        return doctorRepository.findBySpecialty(specialty);
    }

    public List<Doctor> getAllSortedByName() {
        List<Doctor> list = findAll();
        list.sort(DoctorComparators.byName());
        return list;
    }

    public List<Doctor> getAllSortedBySpecialty() {
        List<Doctor> list = findAll();
        list.sort(DoctorComparators.bySpecialtyThenName());
        return list;
    }
}
