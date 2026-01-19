# Directories and classpath
SRC_DIR := thoth
BUILD_DIR := build
LIB_CP := lib/*:thoth

# Find all Java source files
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

# Default target
all: compile

# Compile Java sources
compile:
	mkdir -p $(BUILD_DIR)
	javac -d $(BUILD_DIR) -cp "$(LIB_CP)" $(SOURCES)

# Run the program
run: compile
	java -cp "$(BUILD_DIR):lib/*:thoth" thoth.simulator.Thoth

# Clean compiled files
clean:
	rm -rf $(BUILD_DIR)

.PHONY: all compile run clean
