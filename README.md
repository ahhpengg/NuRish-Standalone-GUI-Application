# NuRish – Standalone Nutrition GUI Application

NuRish is a standalone GUI-based nutrition and wellness application built using **Scala 3**, **Java 21**, and **ScalaFX 21**. Inspired by **United Nations Sustainable Development Goal (SDG) 2: Zero Hunger**, the system promotes healthy eating, nutrition awareness, and community involvement through an intuitive, modern interface.

---

## ✨ Key Features

### **1. Personalized Meal Planner**
Generate meal plans based on:
- Dietary preferences (Vegan, Keto, Mediterranean, etc.)
- Calorie and macronutrient targets
- Number of meals  
NuRish filters foods from the database to create meal plans that meet the user’s specified requirements.

### **2. Food Search**
Search for foods by name or category and view:
- Calories
- Macronutrients (carbs, protein, fats)
- Serving units  
Includes real-time suggestions and nutrition pie charts.

### **3. Nutrition Calculator**
Build and analyze meals by adding ingredients with adjustable serving sizes.  
The system:
- Calculates total nutrition
- Shows a pie chart breakdown
- Allows users to save custom meals into their account

### **4. Food Aid Program**
A community feature enabling users to sign up for food aid initiatives.  
Includes:
- Poster preview  
- Form validation  
- Automatic saving to the database

### **5. User Account System**
- Login & Sign Up with validation  
- Profile page  
- Edit username, email, or password  
- Manage saved recipes  

---

## 🧩 System Architecture

NuRish follows a modular object-oriented design using:
- **Models** extending a shared `Database` trait  
- **Controllers** for each interface component  
- **FXML-based UI** created with SceneBuilder  
- **CSS styling** for modern UI design  
- **SQLite database** managed via DataGrip  

Database tables include:
- `users`
- `foods`
- `diets`
- `food_diets`
- `food_aid_applications`

---

## 🛠 Technologies Used

- **Scala 3**
- **Java 21**
- **ScalaFX 21**
- **FXML + SceneBuilder**
- **SQLite**
- **IntelliJ IDEA**
- **SBT**

---

## 🎥 Demo Video
Watch the system demonstration:  
https://youtu.be/BMYnYf-_zGA

---

## 📘 Documentation
The full report is included in the repository, detailing:
- UML Class Diagram  
- System features  
- GUI design  
- Database setup  
- OOP concepts used  
- Individual reflection  

---

## 👤 Author
**Lee Peng Haw (23098387)**  
Repository: https://github.com/ahhpengg

---

## 📄 License
This project is for academic purposes and part of the course **PRG2104: Object-Oriented Programming**.
