# GraalVM Hot Swap Demo

This project demonstrates a simple but powerful feature using GraalVM's polyglot capabilities - the ability to hot swap JavaScript code in a running Java application. It showcases how to integrate JavaScript with Java using GraalVM, complete with promises, timeouts, and a simple GUI interface.

## Features

- Java Swing GUI application that can execute JavaScript code
- Hot swapping capability for JavaScript code without restarting the application
- Demonstration of GraalVM's polyglot API
- Promise-based JavaScript execution
- Custom console output handling
- Styled output display

## Prerequisites

To run this project on macOS, you'll need:

1. **Homebrew** - macOS package manager
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```

2. **jEnv** - Java version manager
   ```bash
   brew install jenv
   echo 'export PATH="$HOME/.jenv/bin:$PATH"' >> ~/.zshrc
   echo 'eval "$(jenv init -)"' >> ~/.zshrc
   source ~/.zshrc
   ```

3. **GraalVM JDK 17**
   ```bash
   brew install --cask graalvm/tap/graalvm-jdk-17
   # Add GraalVM to jenv
   jenv add /Library/Java/JavaVirtualMachines/graalvm-jdk-17.jdk/Contents/Home
   ```

4. **Maven** - Build tool
   ```bash
   brew install maven
   ```

## Setup

1. Clone the repository:
   ```bash
   git clone [repository-url]
   cd graal-hotswap
   ```

2. Set Java version for the project:
   ```bash
   jenv local 17
   ```

3. Build the project:
   ```bash
   mvn clean package
   ```

## Running the Application

You can run the application in two ways:

1. Using Maven:
   ```bash
   mvn exec:exec
   ```

2. Using the generated JAR:
   ```bash
   java -jar target/graalvm-hotswap-demo-1.0-SNAPSHOT.jar
   ```

## Using the Application

1. The application window will open with three buttons:
   - **Run JavaScript**: Executes the JavaScript code
   - **Hot Swap**: Reloads the JavaScript code from disk
   - **Clear**: Clears the output area

2. The JavaScript code is located in `src/main/resources/script.js`. You can modify this file while the application is running.

3. To test the hot swap feature:
   1. Run the application
   2. Click "Run JavaScript" to see the current behavior
   3. Modify the `script.js` file
   4. Click "Hot Swap" to load your changes
   5. Click "Run JavaScript" again to see your modifications in action

## Output Colors

The application uses different colors for different types of output:
- Dark green: JavaScript console output
- Dark blue: Hot swap related messages
- Dark red: Error messages

## Project Structure

- `src/main/java/com/example/HotSwapDemo.java`: Main Java application
- `src/main/resources/script.js`: JavaScript code that can be hot swapped
- `pom.xml`: Maven project configuration

## Technical Details

This demo uses:
- GraalVM SDK 23.0.1
- Java 17
- Maven for build management
- Swing for the GUI
- GraalVM's JavaScript engine for executing JavaScript code

## Troubleshooting

1. If you see "Context is already closed" messages:
   - This is normal when closing the application while JavaScript code is still executing
   - The application handles these gracefully

2. If Java version is incorrect:
   - Verify your jEnv setup: `jenv doctor`
   - Check current Java version: `java -version`
   - Ensure GraalVM is properly installed: `which java` 