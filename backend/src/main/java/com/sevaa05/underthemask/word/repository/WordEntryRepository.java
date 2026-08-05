package com.sevaa05.underthemask.word.repository;

import com.sevaa05.underthemask.word.entity.WordEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordEntryRepository extends JpaRepository<WordEntry, Long> {

    List<WordEntry> findByCategoryIdAndActiveTrue(Long categoryId);

    List<WordEntry> findByActiveTrue();
}
