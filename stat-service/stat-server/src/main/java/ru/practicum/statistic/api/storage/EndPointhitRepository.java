package ru.practicum.statistic.api.storage;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.statistic.api.exceptions.NotValidException;
import ru.practicum.statistic.api.tool.Union;
import ru.practicum.statistic.dto.EndpointHit;
import ru.practicum.statistic.dto.ViewStats;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EndPointhitRepository {
	private final JdbcTemplate jdbc;

	private static final String INSERT_HIT_SQL =
			"INSERT INTO endpoint_hit(app_id, uri, ip, ts) VALUES (?, ?, ?, ?)";

	private static final String VIEWS_STATS_SQL =
			"SELECT a.name app, h.uri, #1# hits \n" +
					"FROM endpoint_hit as h \n" +
					"join apps as a on a.id = h.app_id \n" +
					"where h.ts >= ? and h.ts <= ? #2# \n" +
					"group by a.name, h.uri \n" +
					"order by hits desc";

	@Transactional
	public EndpointHit saveHit(EndpointHit hit) {
		var application = hit.getApp();
		var appIdOpt = getAppId(application);

		if (appIdOpt.hasValue1() == false) {
			jdbc.update("INSERT INTO apps(name) VALUES(?)", application);
			appIdOpt = getAppId(application);
		}

		if (appIdOpt.hasValue2()) {
			var exception = appIdOpt.getValue2().get();

			log.debug("No record found for application [" + application + "]", exception);

			throw new NotValidException(exception.getMessage());
		}

		if (appIdOpt.hasValue1() == false) {
			throw new NotValidException("Don't found application [" + application + "]");
		}

		var appId = appIdOpt.getValue1().get();

		jdbc.update(INSERT_HIT_SQL, appId, hit.getUri(), hit.getIp(), hit.getTimestamp());

		log.trace("Save hit " + hit);

		return hit;
	}

	public List<ViewStats> getIntervalStats(
			Timestamp start,
			Timestamp end,
			List<String> uris,
			Integer limit,
			Boolean unique) {

		var result = jdbc.query(
				getViewsStatsSql(uris, limit, unique),
				BeanPropertyRowMapper.newInstance(ViewStats.class),
				start,
				end);

		log.trace("Selected " + result.size() + " items of ViewStats");
		log.trace(result.toString());

		return result;
	}

	private Union<Integer, EmptyResultDataAccessException> getAppId(String application) {
		try {
			var appId = jdbc.queryForObject("SELECT id FROM apps where name like ?", Integer.class, application);

			return Union.ofValue1(appId);
		} catch (EmptyResultDataAccessException e) {
			//log.debug("No record found for application [" + application + "]", e);
			return Union.ofValue2(e);
		}
	}

	private String getViewsStatsSql(
			List<String> uris,
			Integer limit,
			Boolean isUnique) {

		String sql;
		if (isUnique) {
			sql = VIEWS_STATS_SQL.replace("#1#", "count(distinct h.ip)");
			//sql = VIEWS_STATS_SQL.replace("#1#", "1");
		} else {
			sql = VIEWS_STATS_SQL.replace("#1#", "count(*)");
		}

		if (!uris.isEmpty()) {
			var uriQuery = uris.stream()
					.map(path -> "'" + path + "'")
					.collect(Collectors.joining(","));
			sql = sql.replace("#2#", " and h.uri in (" + uriQuery + ")");
		} else {
			sql = sql.replace("#2#", "");
		}

		return (limit != null && limit != 0) ? sql + " limit " + limit : sql;
	}
}
