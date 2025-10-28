# Bank REST API

Надёжный RESTful‑бэкенд банковской системы, созданный на **Spring Boot 3.5**, **Spring Security JWT**, **PostgreSQL** и **Liquibase**.  
Реализует управление пользователями и картами, переводы, а также административные функции в соответствии с техническим заданием.

---

## Основные возможности

| Категория | Описание |
|------------|-----------|
| **Безопасность** | JWT‑аутентификация · Ролевая модель (USER / ADMIN) · Шифрование и маскирование номеров карт (PAN) |
| **Бизнес‑логика** | CRUD‑операции с картами · Переводы между своими картами · Блокировка / активация · Фильтрация и пагинация |
| **Администрирование** | Управление пользователями и картами · Полный доступ к записям |
| **Хранилище данных** | PostgreSQL + миграции Liquibase |
| **Документация** | Swagger UI / OpenAPI 3 с кнопкой авторизации JWT |
| **Валидация** | Bean Validation + централизованная обработка ошибок |
| **Развёртывание** | Docker Compose для локальной среды с PostgreSQL |

---

## Запуск проекта

### №1 Необходимое ПО
- JDK 17+  
- Docker Desktop или Docker Compose v2  
- Maven 3.9+

### №2 Клонирование и переменные окружения
```bash
git clone https://github.com/yourusername/Bank-REST-Api.git
cd Bank-REST-Api
```

Проект использует файл `.env` для подключения к базе (пример):

```env
DB_URL=jdbc:postgresql://localhost:5432/bank_db
DB_USER=postgres
DB_PASS=yourpassword
JWT_SECRET_B64=qD7ziVx+/xeythX9dM8OHGZnxI86t2Rq6Hvs6+w2N3c=
ATTRIBUTE_AES_KEY_B64=h8q7jZfF3e5eR3KM4gQ2rQ==
SERVER_PORT=8080
```

### №3 Запуск PostgreSQL через Docker
```bash
docker compose up -d
```
Запускает контейнер `postgres:18` с базой `bank_db`.

### №4 Запуск приложения
```bash
mvn spring-boot:run
```

Приложение доступно по адресу **http://localhost:8080**.

---

## Документация API

Swagger UI создаётся автоматически библиотекой **Springdoc 2.8.13**.

| Ресурс | URL |
|---------|-----|
| Swagger UI | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| OpenAPI JSON | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| OpenAPI YAML | [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml) |

Если в `application.yml` задан собственный путь:
```yaml
springdoc:
  swagger-ui:
    path: /docs
```
то используйте [http://localhost:8080/docs/index.html](http://localhost:8080/docs/index.html).

### Авторизация JWT
1. Получите токен через `POST /auth/login`.  
2. Нажмите **Authorize** в Swagger UI.  
3. Вставьте токен (без `Bearer`) и подтвердите.

---

## Миграции базы данных (Liquibase)

Все миграции находятся в  
`src/main/resources/db/changelog/`.

Liquibase выполняется автоматически при старте.  
Для ручного запуска:
```bash
mvn liquibase:update
```

---

## Генерация файла OpenAPI

Для формирования `docs/openapi.yaml`:
```bash
# Приложение должно быть запущено
mvn -DskipTests=false verify
```
Плагин **springdoc-openapi-maven-plugin** обращается к `/v3/api-docs`  
и сохраняет спецификацию YAML в `docs/openapi.yaml` (нужно закоммитить).

---

## Структура проекта

```
src/
 ├─ main/java/com/example/bankcards/
 │   ├─ config/           # Безопасность, JWT, OpenAPI
 │   ├─ controller/       # Контроллеры Auth, User, Card
 │   ├─ dto/              # DTO‑объекты запросов и ответов
 │   ├─ entity/           # JPA‑сущности
 │   ├─ repository/       # Репозитории Spring Data
 │   ├─ service/          # Сервисный слой
 │   └─ exception/        # Глобальная обработка ошибок
 ├─ main/resources/
 │   ├─ db/changelog/     # Миграции Liquibase
 │   └─ application.yml   # Конфигурация (БД, JWT, Swagger)
 └─ test/java/...         # Юнит‑тесты (по необходимости)
```

---

## Основные эндпоинты

| Роль | Endpoint | Назначение |
|------|-----------|------------|
| Public | `POST /auth/register` | Регистрация пользователя |
| Public | `POST /auth/login` | Аутентификация и получение JWT |
| USER | `GET /cards/me` | Список собственных карт (с пагинацией) |
| USER | `POST /cards/me/create` | Создать карту |
| USER | `POST /cards/me/transfers` | Перевод между своими картами |
| USER | `PATCH /cards/me/{id}/request-block` | Запросить блокировку карты |
| ADMIN | `GET /cards` | Просмотр всех карт |
| ADMIN | `POST /cards/users/{userId}/create` | Создать карту пользователю |
| ADMIN | `PATCH /cards/{id}/block` | Заблокировать карту |
| ADMIN | `PATCH /cards/{id}/activate` | Активировать карту |
| ADMIN | `DELETE /cards/{id}` | Удалить карту |
| ADMIN | `GET /users` | Список пользователей |
| ADMIN | `PUT /users/{id}` | Обновить пользователя |
| ADMIN | `DELETE /users/{id}` | Удалить пользователя |

---

## Конфигурация безопасности

```java
.requestMatchers(
  "/auth/**",
  "/v3/api-docs/**",
  "/v3/api-docs.yaml",
  "/swagger-ui.html",
  "/swagger-ui/**",
  "/docs/**"
).permitAll()
.anyRequest().authenticated();
```
Фильтр `JwtAuthenticationFilter` пропускает эти пути,  
все остальные требуют заголовок `Authorization: Bearer <token>`.

---

## Docker Compose (локальная БД)

```yaml
version: '3.9'
services:
  db:
    image: postgres:18
    container_name: bank_db
    restart: always
    environment:
      POSTGRES_DB: bank_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: t4040657
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:
```

---

## Решение типовых ошибок

| Проблема | Причина / решение |
|-----------|------------------|
| `{"reason":"No static resource docs"}` | Отсутствует зависимость `springdoc-openapi-starter-webmvc-ui` или неверный путь → установить **2.8.13** и разрешить `/docs/**` |
| `/v3/api-docs` → 500 `ControllerAdviceBean.<init>` | Несовместимость Spring Boot 3.5 и старой springdoc 2.6.x → обновить до **2.8.13** |
| Swagger UI показывает «Failed to load API definition» | Проверить, что `/v3/api-docs` отдаёт 200 и не блокируется JWT‑фильтром |
| UI 403 / 401 | Добавить `/docs/**` или `/swagger-ui/**` в permitAll() и исключить из JWT‑фильтра |

---

## Автор

**Ожогин Тихон Сергеевич**  
_Тестовое задание – Bank REST API_

---

### Статус
**Рабочая сборка** · Swagger UI и генерация OpenAPI проверены 
