# Лабораторная работа 3

ФИО: Иванов Олег Петрович

Группа: Б9124-02.03.01мо

HTTP-сервис проката велосипедов. Java 21, Maven, Spring Boot, PostgreSQL 18.

Данные проката хранятся в PostgreSQL. Flyway создаёт таблицы при запуске.

Запуск базы в Docker — `docker compose up -d --wait db`

Запуск — `mvn spring-boot:run`

Проверка API — `bash requests.sh` в Git Bash с пустой базой.

Запуск тестов — `mvn test`

Тесты с PostgreSQL — `mvn -Dtest=PostgresRentalIT test`. В `TEST_DB_URL` 
