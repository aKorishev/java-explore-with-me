package ru.practicum.statistic.api.storage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.statistic.api.exceptions.NotValidException;
import ru.practicum.statistic.dto.StatisticInfo;
import ru.practicum.statistic.dto.vlidators.TimeFormatValidator;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StatisticStorage {
    private final StatisticRepository statisticRepository;

    @PersistenceContext
    EntityManager entityManager;

    public StatisticEntity postStaticEntity(StatisticEntity statisticEntity) {
        return statisticRepository.saveAndFlush(statisticEntity);
    }

    public List<StatisticInfo> getStatistics(
            String start,
            String end,
            String[] uris,
            Boolean unique) {

        var criteriaBuilder = entityManager.getCriteriaBuilder();

        var criteriaQuery = criteriaBuilder.createQuery(StatisticInfo.class);

        var root = criteriaQuery.from(StatisticEntity.class);

        var predicate = getPredicate(start, end, uris, criteriaBuilder, root);

        Expression<Long> expression = unique ? criteriaBuilder.literal(1L) : criteriaBuilder.count(root);

        var query = criteriaQuery;

        if (predicate.isPresent())
            query = query.where(predicate.get());

        query = query
                .multiselect(root.get("app"), root.get("uri"), expression)
                .groupBy(root.get("app"), root.get("uri"))
                .orderBy(criteriaBuilder.desc(expression));

        var result = entityManager.createQuery(query)
                .getResultList();

        return result;
    }

    public <T> Optional<Predicate> getPredicate(String start,
                                                      String end,
                                                      String[] uris,
                                                      CriteriaBuilder criteriaBuilder,
                                                      Root<T> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (!start.isBlank()) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), parseTimestamp(start)));
        }

        if (!end.isBlank()) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), parseTimestamp(end)));
        }

        if (uris.length == 1) {
            predicates.add(criteriaBuilder.equal(root.get("uri"), uris[0]));
        } else if (uris.length > 1) {
            CriteriaBuilder.In<String> inValue = criteriaBuilder.in(root.get("uri"));

            for (var uri: uris) {
                inValue.value(uri);
            }

            predicates.add(inValue);
        }

        if (predicates.isEmpty())
            return Optional.empty();

        var predicateIndex = predicates.size();
        var predicateArray = new Predicate[predicateIndex];

        for (int i = 0; i < predicateIndex; i++)
            predicateArray[i] = predicates.get(i);

        return Optional.of(criteriaBuilder.and(predicateArray));
    }

    public Timestamp parseTimestamp(String value) {
        try {
            var date = new SimpleDateFormat(TimeFormatValidator.PATTERN).parse(value);
            return Timestamp.from(date.toInstant());
        } catch (ParseException e) {
            throw new NotValidException(e.getMessage());
        }
    }
}
