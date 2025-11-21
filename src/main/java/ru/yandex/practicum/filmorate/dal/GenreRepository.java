package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveFilmGenres(int filmId, Set<Genre> genres) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        if (genres != null && !genres.isEmpty()) {
            String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

            List<Object[]> batchArgs = new ArrayList<>();
            for (Genre genre : genres) {
                batchArgs.add(new Object[]{filmId, genre.getId()});
            }

            jdbcTemplate.batchUpdate(insertSql, batchArgs);
        }
    }

    public void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (" + placeholders + ")";

        Map<Integer, Set<Genre>> filmGenresMap = jdbcTemplate.query(sql, filmIds.toArray(), rs -> {
            Map<Integer, Set<Genre>> result = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
                result.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            }
            return result;
        });

        for (Film film : films) {
            Set<Genre> genres = filmGenresMap.getOrDefault(film.getId(), Collections.emptySet());
            film.setGenres(genres);
        }
    }

    public void loadGenresForFilm(Film film) {
        loadGenresForFilms(List.of(film));
    }

    public boolean validateGenresExist(Set<Integer> genreIds) {
        if (genreIds.isEmpty()) {
            return true;
        }

        String placeholders = String.join(",", Collections.nCopies(genreIds.size(), "?"));
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id IN (" + placeholders + ")";
        Integer count = jdbcTemplate.queryForObject(sql, genreIds.toArray(), Integer.class);

        return count != null && count == genreIds.size();
    }
}