package com.sevaa05.underthemask.word.service;

import com.sevaa05.underthemask.word.entity.Association;
import com.sevaa05.underthemask.word.entity.Category;
import com.sevaa05.underthemask.word.entity.WordEntry;
import com.sevaa05.underthemask.word.repository.AssociationRepository;
import com.sevaa05.underthemask.word.repository.CategoryRepository;
import com.sevaa05.underthemask.word.repository.WordEntryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WordContentService {

    private final CategoryRepository categoryRepository;
    private final WordEntryRepository wordEntryRepository;
    private final AssociationRepository associationRepository;

    public WordContentService(CategoryRepository categoryRepository,
                              WordEntryRepository wordEntryRepository,
                              AssociationRepository associationRepository) {
        this.categoryRepository = categoryRepository;
        this.wordEntryRepository = wordEntryRepository;
        this.associationRepository = associationRepository;
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public List<WordEntry> findActiveWordsByCategory(Long categoryId) {
        return wordEntryRepository.findByCategoryIdAndActiveTrue(categoryId);
    }

    public List<Association> findAssociationsForWord(Long wordEntryId) {
        return associationRepository.findByWordEntryId(wordEntryId);
    }
}
