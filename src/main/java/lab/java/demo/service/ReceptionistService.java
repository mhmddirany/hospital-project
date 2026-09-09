package lab.java.demo.service;

import lab.java.demo.Models.Receptionist;
import lab.java.demo.repository.ReceptionistRepository;
import lab.java.demo.util.ReceptionistComparators;

import java.util.List;
import java.util.Optional;

public class ReceptionistService extends BaseCrudService<Receptionist, Integer> {

    private final ReceptionistRepository receptionistRepository;

    public ReceptionistService(ReceptionistRepository receptionistRepository) {
        this.receptionistRepository = receptionistRepository;
    }

    // -------- BaseCrudService hooks --------

    @Override
    protected List<Receptionist> rawFindAll() {
        return receptionistRepository.findAll();
    }

    @Override
    protected Optional<Receptionist> rawFindById(Integer id) {
        return receptionistRepository.findById(id);
    }

    @Override
    protected Receptionist rawSave(Receptionist entity) {
        return receptionistRepository.save(entity);
    }

    @Override
    protected boolean rawDeleteById(Integer id) {
        return receptionistRepository.deleteById(id);
    }

    // -------- Receptionist-specific API --------

    public Receptionist addReceptionist(Receptionist r) {
        return save(r);
    }

    @Override
    public Optional<Receptionist> findById(Integer id) {
        return super.findById(id);
    }

    public List<Receptionist> getAllSortedByName() {
        List<Receptionist> list = findAll();
        list.sort(ReceptionistComparators.byName());
        return list;
    }
}
