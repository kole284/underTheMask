package com.sevaa05.underthemask.word.service;

import com.sevaa05.underthemask.word.entity.Association;
import com.sevaa05.underthemask.word.entity.Category;
import com.sevaa05.underthemask.word.entity.WordEntry;
import com.sevaa05.underthemask.word.repository.AssociationRepository;
import com.sevaa05.underthemask.word.repository.CategoryRepository;
import com.sevaa05.underthemask.word.repository.WordEntryRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
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

    public Optional<WordSelection> findRandomPlayableWord(boolean associationRequired) {
        List<WordEntry> words = wordEntryRepository.findByActiveTrue();
        if (words.isEmpty()) {
            return Optional.empty();
        }

        int startIndex = ThreadLocalRandom.current().nextInt(words.size());
        for (int offset = 0; offset < words.size(); offset++) {
            WordEntry word = words.get((startIndex + offset) % words.size());
            List<Association> associations = associationRepository.findByWordEntryId(word.getId());
            if (associationRequired && associations.isEmpty()) {
                continue;
            }

            String association = associations.isEmpty()
                    ? word.getCategory().getName()
                    : associations.get(ThreadLocalRandom.current().nextInt(associations.size())).getValue();
            return Optional.of(new WordSelection(
                    word.getValue(),
                    word.getCategory().getName(),
                    association
            ));
        }

        return Optional.empty();
    }

    public record WordSelection(String word, String category, String association) {
    }
}
