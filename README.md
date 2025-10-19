# Multi-Threaded HTTP Web Server

A high-performance HTTP/1.1 web server built from scratch in Java using raw sockets and thread pooling.

## Features

- Multi-threaded architecture with thread pool (10 workers)
- HTTP/1.1 protocol implementation from scratch
- Static file serving (HTML, CSS, JS, images)
- Handles 50+ concurrent connections
- 100% success rate, 188+ requests/second
- Complete error handling (404, 400, 403, 405)

## Performance

- **Throughput**: 188.68 requests/second
- **Success Rate**: 100% (50/50 requests)
- **Average Response**: 5ms per request
- **Total Time**: 265ms for 50 concurrent requests

## Quick Start

### Compile
```bash
javac SimpleWebServer.java RequestHandler.java
```

### Run
```bash
java SimpleWebServer
```

### Test
Visit: `http://localhost:8080`

### Load Test
```bash
javac LoadTest.java
java LoadTest
```

## Technical Implementation

- **Socket Programming**: Raw TCP/IP sockets
- **Multi-threading**: ExecutorService with fixed thread pool
- **HTTP Parsing**: Manual request/response handling
- **Concurrency**: Thread-safe request processing
- **Security**: Directory traversal protection

## Author

Jayanth Sadurla
- Email: sadurlajayanth@gmail.com
- Portfolio: sadurlajayanth.me
