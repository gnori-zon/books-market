JDTLS_CONFIG := .jdtls/config.json

ifneq ($(wildcard $(JDTLS_CONFIG)),)
  JAVA_HOME := $(shell grep -o '"javaHome"[[:space:]]*:[[:space:]]*"[^"]*"' $(JDTLS_CONFIG) | grep -o '"[^"]*"$$' | tr -d '"')
  export JAVA_HOME
  export PATH := $(JAVA_HOME)/bin:$(PATH)
endif

JAVA_BIN := java

.PHONY: run
run:
	./gradlew bootRun 

.PHONY: clean 
clean:
	./gradlew clean 

.PHONY: build
build:
	./gradlew build -x test  

.PHONY: dep-refresh
dep-refresh:
	./gradlew --refresh-dependencies	

.PHONY: test-unit
test-unit:
	./gradlew test-unit

.PHONY: test-e2e
test-e2e:
	./gradlew test-e2e

