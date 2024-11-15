# Marvel API Aggregator Service

A REST API built with Finagle and Scala that aggregates data from the Marvel API. It includes caching with Redis, dependency injection via Google Guice, and configuration management with environment variable overrides.

## Features

- **Marvel Character and Comics Aggregation**: Fetches data about Marvel characters and their comics from the Marvel API.
- **Caching**: Uses Redis OR Scaffeine as a caching mechanism to reduce redundant API calls and improve performance.
- **Dependency Injection**: Utilizes Google Guice for modular and testable service configuration.
- **Configuration Management**: Configuration via `.conf` file with environment variable fallback.

## Prerequisites

- **Scala**: Ensure Scala is installed on your machine.
- **Docker**: For running Redis and Postgres containers.

## Configuration

```
export MARVEL_API_KEY=your_api_key
export MARVEL_PRIVATE_KEY=your_private_key
```

### To use InMemory Cache set
```
export CACHE_USE_IN_MEMORY=true
```

## Running the application locally

### Start the redis service locally located at `/dev/docker-compose.yml`
```
docker-compose up -d
```

### Run the application
```
sbt run
```

### Test the API
```
GET http://localhost:8080/marvel-characters?name=Hulk&comicYear=2020&orderBy=title
```

## Testing
`sbt test`

