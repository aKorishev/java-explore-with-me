# Explore with me - микросервисное приложение-афиша мероприятий
Java, Spring (Boot, Data, Security, MVC), PostgreSQL, REST API, Docker, Mockito, JUnit, Postman, MapStruct, Lombok, Thymeleaf, JavaScript, CSS, HTML

## О проекте
Приложение для размещения мероприятий и поиска компаний для участия в них

<p align="center">
  <img src="https://pictures.s3.yandex.net/resources/S19_09-2_1674558748.png" />
</p>

Приложение содержит два микросервиса: 
- main-service для бизнес-логики
- stats-service для сбора статистики просмотра событий по ip, который состоит из трех модулей

<p align="center">
  <img src="https://pictures.s3.yandex.net/resources/S18_01_1674662618.png" />
</p>

У каждого микросервиса есть своя база данных.  
Микросервисы и базы данных запускаются в собственных Docker контейнерах (4 шт).

## Основная функциональность: 

### Неавторизованные пользователи 
- просматривать все события, в том числе по категориям
- видеть детали отдельных событий
- видеть закрепленные подборки событий

### Авторизованные пользователи 
- добавление в приложение новые мероприятия, редактировать их и просматривать после добавления
- подача заявок на участие в интересующих мероприятиях
- создатель мероприятия может подтверждать заявки, которые отправили другие пользователи сервиса

### Администраторы
- добавление, изменение и удаление категорий для событий
- добавление, удаление и закрепление на главной странице подборки мероприятий
- модерация событий, размещённых пользователями, — публикация или отклонение
- управление пользователями — добавление, активация, просмотр и удаление

## Эндпоинты

### main-service 

- POST /admin/categories - добавить новую категорию
- GET /admin/categories/{catId} - получить категорию событий
- DELETE /admin/categories/{catId} - удалить категорию
- DELETE /admin/comments/{commentId} - удалить комментарий
- POST /admin/compilations - добавить подборку событий
- DELETE /admin/compilations/{compId} - удалить подборку событий
- PATCH /admin/compilations/{compId} - обновить подборку событий
- GET /admin/events - получить события по любым параметрам:
  - users - список id пользователей
  - states - список статусов события (PENDING, PUBLISHED, CANCELED)
  - categories - список id категорий событий
  - rangeStart - начало временного отрезка в формате yyyy-MM-dd HH:mm:ss
  - rangeEnd - конец временного отрезка в формате yyyy-MM-dd HH:mm:ss
  - from - параметр для пагинации
  - size - параметр для пагинации
- PATCH /admin/events/{eventId} - изменить событие
- POST /admin/users - добавить пользователя
- GET /admin/users - получить всех пользователей
- DELETE /admin/users/{userId} - удалить пользователя
<br>


- GET /categories - получить все категории
- GET /categories/{catId} - получить категорию
  <br>
- 

- GET /compilations - получить все подборки событий
- GET /compilations/{compId} - получить подборку событий
  <br>
- 

- POST /events/{eventId}/comments - получить события по любым параметрам:
  - userid - идентификатор пользователя
- GET /events/{eventId}/comments - получить список комментариев к событию
- GET /events - получить события по любым параметрам:
  - text - текст для поиска в названии и описании событий
  - categories - список id категорий событий
  - paid - только платные события (true/false)
  - rangeStart - начало временного отрезка в формате yyyy-MM-dd HH:mm:ss
  - rangeEnd - конец временного отрезка в формате yyyy-MM-dd HH:mm:ss
  - onlyAvailable - только доступные события, т.е. у которых еще не исчерпан лимит участников (true/false)
  - sort - способ сортировки событий (EVENT_DATE, VIEWS)
  - from - параметр для пагинации
  - size - параметр для пагинации
- GET /events/{id} - получить событие
  <br>



- GET /users/{userId}/events - получить события пользователя
- GET /users/{userId}/events/{eventId} - получить событие
- POST /users/{userId}/events - добавить новое событие
- PATCH /users/{userId}/events/{eventId} - изменить событие
- GET /users/{userId}/events/{eventId}/requests - получить запросы пользователя на участие в событии
- PATCH /users/{userId}/events/{eventId}/requests - изменить статус (подтверждение, отмена) заявок на участие пользователя в событии
- DELETE /users/{userId}/comments/{commentId} - удалить комментарий от имени пользователя
- PATCH /users/{userId}/comments/{commentId} - обновить комментарий от имени пользователя
- GET /users/{userId}/requests - получить запросы пользователя на участие в событиях
- POST /users/{userId}/requests - добавить запрос на участие в событии
- PATCH /users/{userId}/requests/{requestId}/cancel - отменить запрос на участие в событии
<br>


### stats-service 

- GET /stats - Получение статистики по посещениям
- POST /hit - Сохранение информации о том, что на uri конкретного сервиса был отправлен запрос пользователем

## Как использовать:
Ознакомиться с примерами использования можно в [этой коллекции тестов Postman](https://github.com/yandex-praktikum/java-explore-with-me/tree/main_svc/postman)

## last PR
https://github.com/aKorishev/java-explore-with-me/pull/4


