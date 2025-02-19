# FXInject - Lightweight Dependency Injection for JavaFX

## 🚀 Overview
FXInject is a simple, lightweight dependency injection library designed specifically for JavaFX applications. It provides an easy-to-use mechanism for managing dependencies and improving the modularity of your JavaFX projects.

## ✨ Features
- Simple annotation-based dependency injection
- Seamless integration with JavaFX
- Minimal configuration required
- Supports field-level injection
- Lightweight and easy to use

## 📦 Installation
### Maven Dependency
Add the following to your `pom.xml`:
```xml
<dependency>
    <groupId>com.fxinject</groupId>
    <artifactId>fxinject</artifactId>
    <version>1.0.0</version>
</dependency>

<repositories>
    <repository>
        <id>github</id>
        <name>GitHub KIBUTI-BOT Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/KIBUTI-BOT/FXInject</url>
    </repository>
</repositories>
```

## 🎯 Quick Start

### 1. Mark Your Components
Use `@Component` to mark your classes for dependency injection:

```java
@Component
public class UserService {
    // Your service implementation
}

@Component
public class MainController {
    @Inject
    private UserService userService;
    
    @FXML
    private void initialize() {
        // Your controller initialization
    }
}
```

### 2. Setup in Main Application
Initialize FXInject in your JavaFX application:

```java
public class MainApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Create and initialize container
        FXInjectContainer container = FXInject.createContainer();
        container.scan(); // Scans current package for components
        
        // Create FXML loader with DI support
        FXMLDILoader loader = FXInject.createFXMLLoader(container);
        
        // Load your FXML
        Parent root = loader.load(getClass().getResource("/fxml/main.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
```

## 💡 Key Concepts

### Component Registration
Mark your classes with `@Component`:
```java
@Component
public class DataService {
    // Your implementation
}
```

### Dependency Injection
Use `@Inject` or `@Autowired` for injection:
```java
@Component
public class MainController {
    @Inject
    private DataService dataService;
    
    @Autowired // Alternative annotation
    private UserService userService;
}
```

### FXML Integration
Your FXML files work seamlessly with FXInject:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.layout.VBox?>
<VBox fx:controller="com.example.MainController"
      xmlns:fx="http://javafx.com/fxml">
    <!-- Your FXML content -->
</VBox>
```

## 🔧 Additional Features

### Package Scanning
Scan specific packages:
```java
container.scan("com.example.app");
```

### Manual Component Retrieval
Get components directly from container:
```java
UserService service = container.getComponent(UserService.class);
```

## ⚠️ Error Handling

FXInject provides a dedicated exception class `FXInjectException` that wraps all library-related errors:

```java
try {
    container.scan();
} catch (FXInjectException e) {
    logger.error("Dependency injection failed", e);
    // Handle error appropriately
}
```

Common scenarios that throw `FXInjectException`:
- Component instantiation failures
- Missing required dependencies
- Invalid package scanning
- FXML loading errors
- Circular dependencies
- Invalid component configuration

Example error handling with specific cases:
```java
try {
    // Create and initialize container
    FXInjectContainer container = FXInject.createContainer();
    container.scan("com.example.app");
    
    // Load FXML with dependency injection
    FXMLDILoader loader = FXInject.createFXMLLoader(container);
    Parent root = loader.load(getClass().getResource("/fxml/main.fxml"));
} catch (FXInjectException e) {
    if (e.getMessage().contains("No component found")) {
        logger.error("Missing required dependency", e);
        // Handle missing dependency
    } else if (e.getMessage().contains("Failed to scan package")) {
        logger.error("Package scanning failed", e);
        // Handle scanning error
    } else {
        logger.error("Unexpected injection error", e);
        // Handle other errors
    }
}
```
## 📝 Example Project Structure


src/
├── main/
│   ├── java/
│   │   └── com/example/
│   │       ├── MainApplication.java
│   │       ├── controllers/
│   │       │   └── MainController.java
│   │       └── services/
│   │           └── UserService.java
│   └── resources/
│       └── fxml/
│           └── main.fxml



## 🤝 Contributing
Contributions are welcome! Feel free to:
- Submit issues
- Fork the repository
- Submit pull requests

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

## 🔗 Links
- [GitHub Repository](https://github.com/KIBUTI-BOT/FXInject)
- [Issue Tracker](https://github.com/KIBUTI-BOT/FXInject/issues)
- [Documentation](https://github.com/KIBUTI-BOT/FXInject/wiki)
