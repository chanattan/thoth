# Directories and classpath
SRC_DIR := thoth
BUILD_DIR := build
LIB_DIR := lib
ASSETS_DIR := assets
LIB_CP := lib/*:thoth

# JAR config
JAR_NAME := Thoth.jar
MAIN_CLASS := thoth.simulator.Thoth

# Find all Java source files
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

# Default target
all: compile

# Compile Java sources
compile:
	mkdir -p $(BUILD_DIR)
	javac -d $(BUILD_DIR) -cp "$(LIB_CP)" $(SOURCES)

# Run the program (dev mode)
run: compile
	java -cp "$(BUILD_DIR):lib/*:thoth" $(MAIN_CLASS)

# Create Fat JAR (production)
jar: compile
	@echo "Copie assets..."
	cp -r $(ASSETS_DIR) $(BUILD_DIR)/
	@echo "Extraction libraries..."
	@cd $(BUILD_DIR) && for jar in ../$(LIB_DIR)/*.jar; do \
		unzip -o -q "$$jar"; \
	done
	@echo "Nettoyage META-INF..."
	@rm -f $(BUILD_DIR)/META-INF/*.SF $(BUILD_DIR)/META-INF/*.RSA $(BUILD_DIR)/META-INF/*.DSA
	@echo "Creation MANIFEST..."
	@echo "Manifest-Version: 1.0" > MANIFEST.MF
	@echo "Main-Class: $(MAIN_CLASS)" >> MANIFEST.MF
	@echo "" >> MANIFEST.MF
	@echo "Creation JAR..."
	@jar cfm $(JAR_NAME) MANIFEST.MF -C $(BUILD_DIR) .
	@echo "JAR cree: $(JAR_NAME)"

# Test JAR
test-jar: jar
	java -jar $(JAR_NAME)

# Clean compiled files
clean:
	rm -rf $(BUILD_DIR) $(JAR_NAME) MANIFEST.MF

.PHONY: all compile run jar test-jar clean
