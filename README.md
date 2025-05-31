# Simple API Caller Tool

A simple template-based API calling tool designed mainly for testers who need to perform API calls 
without coding knowledge.

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Basic Usage (Template-Based)](#basic-usage-template-based)
- [Best Practices](#tips)
- [Troubleshooting](#troubleshooting-common-issues)

## Overview

This tool provides a template-based approach to API testing, allowing users to:
- Copy and paste template classes for quick API calls
- Customize requests without deep coding knowledge
- Handle different HTTP methods (GET, POST, PUT, PATCH, DELETE, etc.)

## Project Structure

```
src/
├── main/
│   └── java/
│       └── core/       --> Core API calling functionality, for advanced usage only
└── requests/
    ├── collections/    --> Main folder to work with, place your requests here
    ├── samples/        --> ready-made examples you can try
    │   ├── GetSample
    │   └── PostSample
    └── templates/      --> tamplates for new requests
        ├── DeleteTemplate
        ├── GetTemplate
        ├── HeadTemplate
        ├── OptionsTemplate
        ├── PatchTemplate
        ├── PostTemplate
        └── PutTemplate
```

## Getting Started

### Prerequisites

Simple API Caller - a small Java Maven project located in GitHub, so basically you need java, git and maven installed, 
and any IDE of your choice to work with it. 
If you do not know those things [This Guide](ide_setup.md#api-caller-tool-prerequisites) is for you, 
otherwise you are free to go on your own. [This Guide](ide_setup.md#api-caller-tool-prerequisites) 
will help you set up everything needed to run API Caller Tool with minimal steps.

## Basic Usage (Template-Based)

The simplest way to use this tool is by copying and customizing template classes.

### Step 1: Choose the Right Template

Based on your HTTP method you need (GET, POST, PUT etc.), copy the appropriate template 
from `templates` package to `collections`:

1. Navigate to `src/requests/templates/`
2. Copy the desired template (e.g., `GetTemplate`)
3. Paste it in appropriate package (e.g., `src/requests/collections/`)
4. Rename the file and class to match your test case, e.g. GetAllUsersRequest

#### Note: Name of the file and class name inside this file must be the same!

### Step 2: Customize the Template

Edit the copied template to match your API requirements:

```java
public class GetAllUsersRequest {
    public static void main(String[] args) {
        GET("your-api-endpoint")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .queryParam("param", "value")
            .execute();
    }
}
```

### Step 3: Run the request

1. **Run the Application**
    - On the left of editor window click the green play button ▶ next to the `main` method
    - In popup click the green play button ▶ next to the `GetAllUsersRequest.main()` method

2. **View Output**
    - The application output will appear in the "Run" tool window at the bottom of IntelliJ


## Tips

### 1. Naming Conventions
- Use descriptive class names: `GetUserProfile`, `CreateNewUser`, `UpdateOrderStatus`
- Use camelCase for method names: `authenticateUser()`, `fetchOrderDetails()`

### 2. Organization
- term `package` - means file folder, if you want to create sub-folder - create sub-package, 
right-click on package, in context menu choose `New -> package`
- Use the `collections` package as root for organized test suites in sub-packages
- Group related tests in the same package, e.g. `collections/users`, `collections/orders`

### 3. Error Handling
- Always include appropriate headers
- Validate required parameters before making calls


## Troubleshooting Common Issues

#### 1. Method Not Found
- **Problem**: `GET`, `POST` methods not recognized
- **Solution**: Check if static import is correct: `import static core.ApiCall.*;`

#### 2. Compilation Errors
- **Problem**: Syntax errors in template usage
- **Solution**: Ensure proper syntax or copy new template
```java
// Correct
GET("https://api.example.com")
    .header("Content-Type", "application/json")
    .execute();

// Incorrect - missing dots before method calls
GET("https://api.example.com")
    header("Content-Type", "application/json")
    execute();

// Incorrect - missing semicolon at the end of expression
GET("https://api.example.com")
    .header("Content-Type", "application/json")
    .execute()

// Incorrect - missing comma between method arguments
GET("https://api.example.com")
    .header("Content-Type" "application/json")
    .execute();
```

#### 3. Runtime Errors
- **Problem**: NullPointerException or connection errors
- **Solution**: 
  - Verify URL is accessible
  - Check authentication credentials
  - Ensure proper headers are set
  - Use online validators to ensure your request body is valid format

---

## Quick Reference

### Available Templates
| HTTP Method | Template | Use Case |
|-------------|----------|----------|
| GET | GetTemplate | Retrieve data |
| POST | PostTemplate | Create new resources |
| PUT | PutTemplate | Update entire resource |
| PATCH | PatchTemplate | Partial resource update |
| DELETE | DeleteTemplate | Remove resources |
| HEAD | HeadTemplate | Get headers only |
| OPTIONS | OptionsTemplate | Check allowed methods |


### URL Structure
```
https://api.example.com/v1/users/123?include=profile&limit=10
│      │               │  │     │   │                    │
│      │               │  │     │   │                    └─ Query parameters
│      │               │  │     │   └─ Resource ID
│      │               │  │     └─ Resource type
│      │               │  └─ API version
│      │               └─ Base path
│      └─ Domain
└─ Protocol
```

This tool simplifies API testing by providing ready-to-use templates that can be easily customized for specific testing needs. 
Start with the basic templates and gradually move to more complex scenarios as you become comfortable with the tool.