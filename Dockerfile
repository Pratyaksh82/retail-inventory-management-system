# ==============================================================================
# Multi-Stage Dockerfile for Retail & Library Inventory Management System
# ==============================================================================

# Stage 1: Build & Verification Environment
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy source code and libraries
COPY . .

# Compile all classes
RUN mkdir -p bin dist && \
    find src -name "*.java" > dist/sources.txt && \
    javac -cp "lib/*:bin" -d bin @dist/sources.txt && \
    rm -f dist/sources.txt

# Run automated tests to gate build
RUN java -cp bin com.inventory.library.LibraryTestRunner

# Package Executable JAR
RUN echo "Manifest-Version: 1.0" > dist/MANIFEST.MF && \
    echo "Main-Class: com.inventory.UnifiedInventorySystem" >> dist/MANIFEST.MF && \
    jar cvfm dist/retail-inventory-system-1.0.jar dist/MANIFEST.MF -C bin com

# Stage 2: Minimal Production JRE Runtime
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built JAR from builder stage
COPY --from=builder /workspace/dist/retail-inventory-system-1.0.jar /app/app.jar

# Environment defaults
ENV JAVA_OPTS="-Xms64m -Xmx256m -XX:+UseG1GC"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD java -version || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
