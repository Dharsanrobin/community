# Community API — Spring Boot migration starter

This is the **starter foundation**, not a complete conversion of the uploaded Node.js backend yet.

## Stack
- Java 21
- Spring Boot 3.5
- Spring Web, Spring Data JPA, Validation, Spring Security
- MySQL Connector/J
- JWT library and Springdoc OpenAPI dependencies

## Run
1. Install JDK 21 and Maven.
2. Create or identify the existing MySQL database. Do not run schema-changing scripts until the migration has been reviewed.
3. Copy `.env.example` values into your IDE run configuration or environment variables.
4. Set `DB_URL`, `DB_USER`, `DB_PASSWORD`, and a strong `JWT_SECRET`.
5. Start with `mvn spring-boot:run`.
6. Test `GET http://localhost:5050/api/v1/health`.

## Critical migration notes
- The uploaded Node project has many modules (auth, users, polls, votes, challenges, events, news, notifications, media, admin, etc.). They have **not all been ported** in this starter.
- Existing database tables and API response shapes should be preserved deliberately.
- `spring.jpa.hibernate.ddl-auto=validate` is used to avoid accidental schema changes.
- The security configuration currently permits requests so the health endpoint can be tested. **Do not deploy this starter publicly** until JWT authentication, role/permission checks, CORS origins, rate limiting, and endpoint authorization have been ported.
- The next migration phase should port the database schema/entities, then auth/session/token handling, then feature modules and tests.
