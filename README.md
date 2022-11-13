# java-filmorate
## Database

### Diagram
![Untitled (9)](https://user-images.githubusercontent.com/6263385/201519938-58d0a64e-53d5-4bc2-ba56-6e4da4fd0bfc.png)

### Directories
**sp_film_genre** - Справочник жанров фильмов
1. Comedy
2. Drama
3. Cartoon
4. Thriller
5. Documentary
6. Action movie

**sp_film_rating** - Справочник рейтингов фильмов
1. G
2. PG
3. PG-13
4. R
5. NC-17

**sp_friendship_status** - Справочник статусов дружбы
1. Accepted
2. Pending
### Queries
**Получить информацию о фильме по ID фильма**
```
SELECT
  f.*,
  sr.name
FROM film f
LEFT JOIN sp_film_rating sfr ON f.rating_id = sfr.rating_id
WHERE f.film_id = %идентификатор_фильма%
```
**Получить информацию о жанрах фильма по ID фильма**
```
SELECT
  f.name,
  sfg.name
FROM film f
INNER JOIN film_genre fg ON f.film_id = fg.film_id
INNER JOIN sp_film_genre sfg ON fg.genre_id = sfg.genre_id
```
**Получить информацию о пользователе по ID**
```
SELECT
  *
FROM user u
WHERE u.user_id = %идентификатор_пользователя%
```
**Получить информацию о друзьях пользователя**
```
SELECT
  u.name user_name,
  uf.name friend_name,
  f.status
FROM user u
LEFT JOIN friends f ON u.user_id = f.user_id
INNER JOIN user uf ON f.friend_id = uf.user_id
INNER JOIN sp_friendship_status sfs ON f.status_id = sfs.status_id
```
