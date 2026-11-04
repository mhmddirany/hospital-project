package lab.java.demo.service;

import lab.java.demo.Models.Nurse;
import lab.java.demo.exception.NurseNotFoundException;
import lab.java.demo.repository.NurseRepository;
import lab.java.demo.util.NurseComparators;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NurseService extends BaseCrudService<Nurse, Integer> {

    private final NurseRepository nurseRepository;

    public NurseService(NurseRepository nurseRepository) {
        this.nurseRepository = nurseRepository;
    }

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
        // JpaRepository.deleteById returns nothing, so check first to keep
        // BaseCrudService's "did anything get deleted?" contract.
        if (!nurseRepository.existsById(id)) {
            return false;
        }
        nurseRepository.deleteById(id);
        return true;
    }

    public Nurse addNurse(Nurse nurse) {
        return save(nurse);
    }

    @Override
    public Optional<Nurse> findById(Integer id) {
        return super.findById(id);
    }

    public Nurse getByIdOrThrow(int id) {
        return findById(id).orElseThrow(() -> new NurseNotFoundException(id));
    }

    public List<Nurse> findByDepartment(String department) {
        return nurseRepository.findByDepartmentIgnoreCase(department);
    }

    public List<Nurse> getAllSortedByName() {
        List<Nurse> list = findAll();
        list.sort(NurseComparators.byName());
        return list;
    }
}
