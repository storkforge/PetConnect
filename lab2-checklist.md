# ✅ Lab 2 Progress Checklist – PetConnect

## ✅ COMPLETED

### Non-Functional Requirements
- [x] Group of 4–6 working collaboratively
- [x] Git repository hosted at: [https://github.com/storkforge](https://github.com/storkforge)
- [x] Spring Boot 3.4.3+
- [x] CI pipeline using GitHub Actions
- [x] Pull Request workflow for merging code
- [x] Spring MVC with Thymeleaf for HTML views
- [x] Internationalization configured (EN, SV, AR, FR, ES, HI)
- [x] Spring Security:
    - [x] Password login
    - [x] Google OAuth login
    - [x] Facebook OAuth login
- [x] Role-based access control (`USER`, `PREMIUM`, `ADMIN`)
- [x] Three data formats supported:
    - [x] HTML views (Thymeleaf)
    - [x] REST API (JSON)
    - [x] GraphQL
- [x] Spring AI integrated with retry + fallback
- [x] Custom environment variable loader (`EnvInitializer`)
- [x] Global exception handling
- [x] Image upload & file validation via `FileStorageService`
- [x] Data initialization (`DataInitializer`) for test users
- [x] Pet filtering, pagination, and dynamic search via `PetFilter` & `PetSpecification`
- [x] GraphQL schema and resolvers for pets and users
- [x] Unit & integration tests (present, unreviewed)

---

### Functional Features
- [x] Register and authenticate users (OAuth & password)
- [x] Upload profile pictures for users and pets
- [x] Create and update pet profiles with validation
- [x] AI-powered recommendations for available pets (via Spring AI)
- [x] Role-based access to premium features
- [x] REST and GraphQL endpoints for all core entities
- [x] Multi-language UI with dynamic language switcher

---

## ❗ REMAINING

### Non-Functional / Technical
- [ ] Integrate Redis caching for performance optimization
- [ ] Integrate external REST APIs:
    - [ ] Animal APIs (e.g. top 20 public APIs)
    - [ ] Weather API for outdoor activity planning
- [ ] Optional: Support Spring Security “keywords” feature (VG only, unclear)

---

### Functional Features
- [ ] Allow users to plan and discover **cat meetups**
- [ ] Add feature for users to **share cat pictures and stories** in a social feed
- [ ] Provide **vet visit reminders and care tips**
- [ ] UI features for **premium-only access** (e.g. exclusive tabs, content gating)

