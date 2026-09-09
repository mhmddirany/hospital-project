package lab.java.demo.service;

import lab.java.demo.Models.Nurse;
import lab.java.demo.repository.NurseRepository;
import lab.java.demo.util.NurseComparators;

import java.util.List;
import java.util.Optional;

public class NurseService extends BaseCrudService<Nurse, Integer> {

    private final NurseRepository nurseRepository;

    public NurseService(NurseRepository nurseRepository) {
        this.nurseRepository = nurseRepository;
    }

    // -------- BaseCrudService hooks --------

    @Override
    protected List<Nurse> rawFindAll() {
        return nurseRepository.findAll();
    }

    @Override
    protected Optional<Nurse> rawFindById(Integer id) {
        return nurseRepository.findById(id);
    }

    @Override
    protected Nurse rawSave(Nurse entity) {
        return nurseRepository.save(entity);
    }

    @Override
    protected boolean rawDeleteById(Integer id) {
        return nurseRepository.deleteById(id);
    }

    // -------- Nurse-specific API --------

    public Nurse addNurse(Nurse nurse) {
        return save(nurse);
    }

    @Override
    public Optional<Nurse> findById(Integer id) {
        return super.findById(id);
    }

    public List<Nurse> findByDepartment(String department) {
        return nurseRepository.findByDepartment(department);
    }

    public List<Nurse> getAllSortedByName() {
        List<Nurse> list = findAll();
        list.sort(NurseComparators.byName());
        return list;
    }
}
