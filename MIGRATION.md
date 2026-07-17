# Gradle Multi-Module to Maven Migration

## Removed architecture

The old codebase used four Gradle modules:

```text
Domain
Repository
Service
Web
```

The modules were physically separate but still leaked MongoDB, Spring Web and
Spring Data types across their boundaries.

## New architecture

The converted project is one Maven application with package-level separation:

```text
controller -> service -> repository -> model
                |
                +-> GridFS
                +-> SMTP
```

DTOs keep the HTTP contract separate from MongoDB documents.

## Compatibility decisions

The following old paths remain available:

```text
GET  /api/portfolio/user/details
POST /api/portfolio/user/submit
GET  /api/portfolio/retrieve/{fileId}
POST /api/portfolio/user/save
POST /api/portfolio/user/bsonfile
```

The two write/upload legacy routes now require an `X-Admin-Key` header.

## Before replacing the old repository

1. Rotate all previously committed credentials.
2. Back up the MongoDB database and GridFS data.
3. Build with `mvn clean verify`.
4. Deploy to a temporary Render service.
5. Verify health, profile retrieval, media retrieval and contact delivery.
6. Update the frontend API URL.
7. Only then replace the old production deployment.

## Git cleanup

Deleting a secret from `application.properties` does not remove it from Git
history. Use a history-rewriting tool such as `git filter-repo`, then force
push only after coordinating with every clone and deployment.

Credential rotation must happen before history cleanup.
