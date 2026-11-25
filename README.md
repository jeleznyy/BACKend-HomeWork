# Лабораторная работа 3

ФИО: Иванов Олег Петрович

Группа: Б9124-02.03.01мо

HTTP-сервис проката велосипедов. Java 21, Maven, Spring Boot, PostgreSQL 18.

Данные проката хранятся в PostgreSQL. Flyway создаёт и обновляет таблицы.

`POST /customers/with-rental` создаёт клиента и сразу выдаёт ему велосипед

`GET /bikes/with-customers` показывает велосипеды, клиентов и время выдачи.

Запуск базы в Docker — `docker compose up -d --wait db`

Запуск — `mvn spring-boot:run`

Проверка API — `bash requests.sh` в Git Bash с пустой базой.

Запуск тестов — `mvn test`

Тесты с PostgreSQL — `mvn -Dtest=PostgresRentalIT test` с адресом тестовой базы в `TEST_DB_URL`.
