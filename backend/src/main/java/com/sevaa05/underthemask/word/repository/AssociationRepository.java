package com.sevaa05.underthemask.word.repository;

import com.sevaa05.underthemask.word.entity.Association;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssociationRepository extends JpaRepository<Association, Long> {

    List<Association> findByWordEntryId(Long wordEntryId);
}
