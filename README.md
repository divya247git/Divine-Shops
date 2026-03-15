# 🛍️ Divine Shops — Java Desktop Shopping Application

> A fully functional desktop shopping application built with Java Swing and MySQL,
> inspired by platforms like Flipkart, Myntra and Blinkit.

---

## 📱 About The Project

**Divine Shops** is a Java-based desktop shopping application developed as part of a
university project at **Mody University of Science and Technology**.
The idea was to build a real-world style e-commerce application that runs on the desktop,
connects to a live MySQL database, and lets users browse products, manage a cart,
and place orders — all from a clean and simple graphical interface.

The application is built entirely using core Java technologies —
**Java Swing** for the GUI and **JDBC** for database connectivity —
with no external UI frameworks, making it lightweight and easy to run on any system
that has Java and MySQL installed.

---

## ✨ Features

- 🛍️ **Browse Products** — View all available products in a clean table with name, category, price and stock
- 🔍 **Search** — Search products by name using a keyword search box
- 🗂️ **Filter by Category** — Filter products by Electronics, Fashion, Sports, Beauty or Grocery
- 🛒 **Shopping Cart** — Add products to cart with custom quantity, update or remove items
- 💰 **Smart Delivery Fee** — Delivery is FREE for orders above Rs.499, otherwise Rs.49
- ✅ **Place Orders** — Choose payment method (UPI / Card / Cash on Delivery) and enter delivery address
- 📦 **Order History** — View all past orders with items, totals and dates loaded from database
- 🗄️ **MySQL Integration** — All products, orders and order items are stored and retrieved from a live MySQL database
- 📊 **Live Stock Updates** — Stock count reduces in the database every time an order is placed
- 🟢 **Connection Status** — Status bar at the bottom shows if MySQL is connected or not

---

## 🖥️ Screenshots

```
Tab 1 — Products             Tab 2 — Cart              Tab 3 — Orders
┌─────────────────────┐      ┌─────────────────────┐   ┌─────────────────────┐
│ Search | Category   │      │ Product | Qty | Total│   │ ORDER #1            │
│─────────────────────│      │─────────────────────│   │ Payment: UPI        │
│ Headphones  2999    │      │ Headphones  1  2999  │   │ Total: Rs.2999      │
│ Smart Watch 4999    │      │ Yoga Mat    2  1398  │   │ Status: Confirmed   │
│ Running Shoes 3499  │      │─────────────────────│   │─────────────────────│
│ Yoga Mat     699    │      │ Total: Rs.4397       │   │ ORDER #2            │
│─────────────────────│      │ Delivery: FREE       │   │ ...                 │
│  [ Add to Cart ]    │      │ [ Place Order ]      │   │                     │
└─────────────────────┘      └─────────────────────┘   └─────────────────────┘
```

---

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java (JDK 23) | Core programming language |
| Java Swing | Desktop GUI framework |
| JDBC | Java Database Connectivity |
| MySQL 8.0 | Backend relational database |
| Apache NetBeans IDE 29 | Development environment |
| Apache Maven | Build and dependency management |
| Ubuntu Linux 24.04 | Operating system |
| mysql-connector-j 8.0.33 | MySQL JDBC driver |

---

## 🗄️ Database Schema

The application uses a MySQL database called `DIVINE_SHOPPING` with 3 tables:

```sql
-- Stores all products
CREATE TABLE products (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    name     VARCHAR(100),
    category VARCHAR(50),
    price    INT,
    stock    INT
);

-- Stores placed orders
CREATE TABLE orders (
    id             INT PRIMARY KEY AUTO_INCREMENT,
    payment_method VARCHAR(50),
    address        VARCHAR(255),
    total          INT,
    status         VARCHAR(50) DEFAULT 'Confirmed',
    order_date     DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Stores items in each order
CREATE TABLE order_items (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    order_id   INT,
    product_id INT,
    name       VARCHAR(100),
    price      INT,
    quantity   INT
);
```

---

## ⚙️ How It Works

```
User opens app
      ↓
Java connects to MySQL (DIVINE_SHOPPING database)
      ↓
Products are loaded from database into the GUI table
      ↓
User searches / filters → selects product → adds to cart
      ↓
User clicks Place Order → fills payment + address
      ↓
Java inserts order into MySQL orders table
Java inserts items into MySQL order_items table
Java reduces stock count in products table
      ↓
Order confirmed! Cart cleared. Orders tab updated.
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- Java JDK 17 or higher
- Apache NetBeans IDE
- MySQL Server 8.0
- Maven (comes with NetBeans)

### Installation

**Step 1 — Clone the repository**
```bash
git clone https://github.com/yourusername/divine-shops.git
cd divine-shops
```

**Step 2 — Set up MySQL database**
```bash
sudo mysql -u root
```
```sql
CREATE DATABASE DIVINE_SHOPPING;
USE DIVINE_SHOPPING;

CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    category VARCHAR(50),
    price INT,
    stock INT
);

CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    payment_method VARCHAR(50),
    address VARCHAR(255),
    total INT,
    status VARCHAR(50) DEFAULT 'Confirmed',
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    product_id INT,
    name VARCHAR(100),
    price INT,
    quantity INT
);

INSERT INTO products (name, category, price, stock) VALUES
('Wireless Headphones','Electronics',2999,50),
('Smart Watch','Electronics',4999,25),
('Running Shoes','Sports',3499,30),
('Yoga Mat','Sports',699,100),
('Linen Shirt','Fashion',1299,80),
('Denim Jacket','Fashion',1999,40),
('Vitamin C Serum','Beauty',599,200),
('Matte Lipstick','Beauty',349,150),
('Basmati Rice 5kg','Grocery',649,500),
('Organic Almonds','Grocery',399,300);

-- Fix MySQL auth for JDBC connection
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '';
FLUSH PRIVILEGES;
EXIT;
```

**Step 3 — Open project in NetBeans**
- File → Open Project → select the `divine-shops` folder
- NetBeans will automatically detect it as a Maven project

**Step 4 — Add MySQL dependency**

Make sure `pom.xml` contains:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

**Step 5 — Update database credentials**

In `ShoppingAppGUI.java` update these lines:
```java
static final String URL  = "jdbc:mysql://localhost:3306/DIVINE_SHOPPING?useSSL=false&allowPublicKeyRetrieval=true";
static final String USER = "root";
static final String PASS = "";   // add your MySQL password if you have one
```

**Step 6 — Run the project**
- Press `F6` in NetBeans or click the Run button
- The GUI window will open
- Status bar at the bottom shows `Connected to MySQL!` if successful

---

## 📂 Project Structure

```
Divine_Shops/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── mycompany/
│                   └── divine_shops/
│                       └── ShoppingAppGUI.java   ← main file
├── pom.xml                                        ← Maven config + MySQL dependency
└── README.md
```

---

## 🎯 How To Use

1. **Search products** — type a keyword in the search box and click Search
2. **Filter by category** — select a category from the dropdown
3. **Add to cart** — click a product row to select it, then click Add to Cart
4. **Enter quantity** — type how many you want in the popup
5. **Go to Cart tab** — review your items and total
6. **Place Order** — click Place Order, choose payment method, enter address, click OK
7. **View Orders** — go to Orders tab and click Load Orders to see all past orders

---

## 🔮 Future Improvements

- [ ] User login and registration system
- [ ] Product images
- [ ] Admin panel to add/edit/delete products
- [ ] Coupon and discount system
- [ ] Order tracking with status updates
- [ ] Export orders to PDF or Excel
- [ ] Dark mode UI

---

## 👩‍💻 Developer

**Divyanshi Yadav**
Mody University of Science and Technology

---

## 📄 License

This project is open source and available under the MIT License.

---

> *— Shop Smarter, Not Harder"* 🛍️
