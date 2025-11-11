-- Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    birthday DATE NOT NULL
);

-- Создание таблицы рейтингов MPA
CREATE TABLE IF NOT EXISTS mpa_ratings (
    mpa_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Создание таблицы фильмов
CREATE TABLE IF NOT EXISTS films (
    film_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    mpa_id INT NOT NULL,
    CONSTRAINT fk_films_mpa FOREIGN KEY (mpa_id) REFERENCES mpa_ratings(mpa_id)
);

-- Создание таблицы жанров
CREATE TABLE IF NOT EXISTS genres (
    genre_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Создание таблицы связи фильмов и жанров
CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    CONSTRAINT fk_film_genres_film FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
    CONSTRAINT fk_film_genres_genre FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);

-- Создание таблицы лайков фильмов
CREATE TABLE IF NOT EXISTS film_likes (
    film_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (film_id, user_id),
    CONSTRAINT fk_film_likes_film FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
    CONSTRAINT fk_film_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Создание таблицы статусов дружбы
CREATE TABLE IF NOT EXISTS friendship_status (
    status_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Создание таблицы дружбы
CREATE TABLE IF NOT EXISTS friendships (
    user_id INT NOT NULL,
    friend_id INT NOT NULL,
    status_id INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT fk_friendships_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_status FOREIGN KEY (status_id) REFERENCES friendship_status(status_id),
    CONSTRAINT chk_no_self_friendship CHECK (user_id != friend_id)
);

-- Создание индексов для улучшения производительности
CREATE INDEX IF NOT EXISTS idx_film_likes_film ON film_likes(film_id);
CREATE INDEX IF NOT EXISTS idx_film_likes_user ON film_likes(user_id);
CREATE INDEX IF NOT EXISTS idx_friendships_user ON friendships(user_id, status_id);
CREATE INDEX IF NOT EXISTS idx_friendships_friend ON friendships(friend_id, status_id);
CREATE INDEX IF NOT EXISTS idx_films_release_date ON films(release_date);

-- Создание таблицы лайков (если еще нет)
CREATE TABLE IF NOT EXISTS film_likes (
    film_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (film_id, user_id),
    CONSTRAINT fk_film_likes_film FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
    CONSTRAINT fk_film_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);