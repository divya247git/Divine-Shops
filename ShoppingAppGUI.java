```
package com.mycompany.divine_shops;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class ShoppingAppGUI extends JFrame {

    // ═══════════════════════════════════════════════
    // CONNECT MYSQL TO THE SHOPPING APPLICATION
    // ═══════════════════════════════════════════════
    static final String URL  = "jdbc:mysql://localhost:3306/DIVINE_SHOPPING?useSSL=false&allowPublicKeyRetrieval=true";
    static final String USER = "root";
    static final String PASS = "";   // ← put your MySQL password here

    static Connection getConn() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }


    // ── Data Classes ─────────────────────────────────────────
    static class Product {
        int id; String name, category; double price; int stock;
        Product(int id, String name, String category, double price, int stock) {
            this.id = id; this.name = name; this.category = category;
            this.price = price; this.stock = stock;
        }
    }

    static class CartItem {
        Product product; int quantity;
        CartItem(Product p, int q) { product = p; quantity = q; }
    }

    // ── App Data ─────────────────────────────────────────────
    ArrayList<Product>  catalog = new ArrayList<>();
    ArrayList<CartItem> cart    = new ArrayList<>();
    ArrayList<String>   orders  = new ArrayList<>();

    // ── UI Components ────────────────────────────────────────
    JTabbedPane tabs;

    // Products tab
    JTable      productTable;
    DefaultTableModel productModel;
    JTextField  searchField;
    JComboBox<String> categoryFilter;

    // Cart tab
    JTable      cartTable;
    DefaultTableModel cartModel;
    JLabel      totalLabel;

    // Orders tab
    JTextArea   ordersArea;

    // Colors & Fonts
    Color ORANGE  = new Color(247, 99, 27);
    Color DARK    = new Color(15, 14, 13);
    Color CREAM   = new Color(250, 247, 242);
    Color GREEN   = new Color(29, 184, 142);
    Font  TITLE   = new Font("Arial", Font.BOLD, 22);
    Font  BOLD    = new Font("Arial", Font.BOLD, 13);
    Font  NORMAL  = new Font("Arial", Font.PLAIN, 13);

    // ── Constructor ──────────────────────────────────────────
    public ShoppingAppGUI() {
        loadProducts();
        setupFrame();
        buildUI();
        refreshProductTable(catalog);
        setVisible(true);
    }

    // ── Frame Setup ──────────────────────────────────────────
    void setupFrame() {
        setTitle("DIVINE — Online Shopping");
        setSize(950, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CREAM);
    }

    // ── Build Full UI ────────────────────────────────────────
    void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(BOLD);
        tabs.setBackground(Color.WHITE);
        tabs.addTab("🛍️  Products",  buildProductsTab());
        tabs.addTab("🛒  Cart",       buildCartTab());
        tabs.addTab("📦  My Orders",  buildOrdersTab());
        add(tabs, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ───────────────────────────────────────────────
    JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK);
        header.setPreferredSize(new Dimension(0, 65));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel logo = new JLabel("DIVINE SHOPS");
        logo.setFont(new Font("Arial", Font.BOLD, 28));
        logo.setForeground(ORANGE);

        JLabel tagline = new JLabel("Money can't buy happiness,but it can buy clothes and that's pretty much the same");
        tagline.setFont(new Font("Arial", Font.ITALIC, 12));
        tagline.setForeground(new Color(180, 180, 180));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setBackground(DARK);
        left.add(logo);
        left.add(tagline);

        JLabel cartCount = new JLabel("Shopping is cheaper than Tharepy");
        cartCount.setForeground(ORANGE);
        cartCount.setFont(BOLD);

        header.add(left, BorderLayout.WEST);
        header.add(cartCount, BorderLayout.EAST);
        return header;
    }

    // ── Products Tab ─────────────────────────────────────────
    JPanel buildProductsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ── Top: Search + Filter ──
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBar.setBackground(CREAM);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(BOLD);

        searchField = new JTextField(18);
        searchField.setFont(NORMAL);
        searchField.setPreferredSize(new Dimension(180, 32));

        JButton searchBtn = makeButton("🔍 Search", ORANGE);
        searchBtn.addActionListener(e -> doSearch());

        JLabel catLbl = new JLabel("  Category:");
        catLbl.setFont(BOLD);

        categoryFilter = new JComboBox<>(new String[]{
            "All", "Electronics", "Fashion", "Sports", "Beauty", "Grocery"
        });
        categoryFilter.setFont(NORMAL);
        categoryFilter.setPreferredSize(new Dimension(130, 32));
        categoryFilter.addActionListener(e -> doFilter());

        JButton clearBtn = makeButton("Clear", Color.GRAY);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            refreshProductTable(catalog);
        });

        topBar.add(searchLbl); topBar.add(searchField);
        topBar.add(searchBtn); topBar.add(catLbl);
        topBar.add(categoryFilter); topBar.add(clearBtn);

        // ── Product Table ──
        String[] cols = {"ID", "Product Name", "Category", "Price (Rs.)", "Stock"};
        productModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        productTable = new JTable(productModel);
        styleTable(productTable);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(70);

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 215, 205)));

        // ── Bottom Buttons ──
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnBar.setBackground(CREAM);

        JButton addCartBtn = makeButton("🛒 Add to Cart", ORANGE);
        addCartBtn.addActionListener(e -> addSelectedToCart());

        JButton wishBtn = makeButton("❤️ Wishlist", new Color(220, 60, 90));
        wishBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row < 0) { showMsg("Select a product first."); return; }
            String name = (String) productModel.getValueAt(row, 1);
            showMsg("❤️ \"" + name + "\" added to wishlist!");
        });

        btnBar.add(addCartBtn);
        btnBar.add(wishBtn);

        panel.add(topBar,  BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        panel.add(btnBar,  BorderLayout.SOUTH);
        return panel;
    }

    // ── Cart Tab ─────────────────────────────────────────────
    JPanel buildCartTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Cart table
        String[] cols = {"Product Name", "Price (Rs.)", "Qty", "Line Total (Rs.)"};
        cartModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        styleTable(cartTable);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(260);

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 215, 205)));

        // Total panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(CREAM);
        totalLabel = new JLabel("Total: Rs.0   |   Delivery: Rs.49");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 15));
        totalLabel.setForeground(DARK);
        totalPanel.add(totalLabel);

        // Buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnBar.setBackground(CREAM);

        JButton removeBtn = makeButton("🗑️ Remove Item", new Color(200, 60, 60));
        removeBtn.addActionListener(e -> removeFromCart());

        JButton clearBtn = makeButton("Clear Cart", Color.GRAY);
        clearBtn.addActionListener(e -> {
            cart.clear();
            refreshCartTable();
            showMsg("Cart cleared.");
        });

        JButton orderBtn = makeButton("✅ Place Order", GREEN);
        orderBtn.setFont(new Font("Arial", Font.BOLD, 14));
        orderBtn.addActionListener(e -> showPlaceOrderDialog());

        btnBar.add(removeBtn);
        btnBar.add(clearBtn);
        btnBar.add(Box.createHorizontalStrut(200));
        btnBar.add(orderBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(CREAM);
        south.add(totalPanel, BorderLayout.NORTH);
        south.add(btnBar,     BorderLayout.SOUTH);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(south,  BorderLayout.SOUTH);
        return panel;
    }

    // ── Orders Tab ───────────────────────────────────────────
    JPanel buildOrdersTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CREAM);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        ordersArea = new JTextArea();
        ordersArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ordersArea.setEditable(false);
        ordersArea.setBackground(new Color(245, 243, 238));
        ordersArea.setText("No orders yet. Place an order to see it here.");

        JScrollPane scroll = new JScrollPane(ordersArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 215, 205)));

        panel.add(new JLabel("  📦 Your Order History", JLabel.LEFT), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Footer ───────────────────────────────────────────────
    JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(DARK);
        JLabel lbl = new JLabel("© 2026 BAZAAR  |  Free delivery above Rs.499  |  Easy returns");
        lbl.setForeground(new Color(150, 150, 150));
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        footer.add(lbl);
        return footer;
    }

    // ── Actions ──────────────────────────────────────────────

    void doSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        ArrayList<Product> result = new ArrayList<>();
        for (Product p : catalog)
            if (p.name.toLowerCase().contains(kw)) result.add(p);
        refreshProductTable(result);
    }

    void doFilter() {
        String cat = (String) categoryFilter.getSelectedItem();
        if ("All".equals(cat)) { refreshProductTable(catalog); return; }
        ArrayList<Product> result = new ArrayList<>();
        for (Product p : catalog)
            if (p.category.equalsIgnoreCase(cat)) result.add(p);
        refreshProductTable(result);
    }

    void addSelectedToCart() {
        int row = productTable.getSelectedRow();
        if (row < 0) { showMsg("Please select a product from the table."); return; }

        int    id    = (int)    productModel.getValueAt(row, 0);
        int    stock = (int)    productModel.getValueAt(row, 4);
        if (stock == 0) { showMsg("This product is out of stock!"); return; }

        String qtyStr = JOptionPane.showInputDialog(this,
            "Enter quantity (max " + stock + "):", "Add to Cart",
            JOptionPane.QUESTION_MESSAGE);
        if (qtyStr == null) return;

        int qty;
        try { qty = Integer.parseInt(qtyStr.trim()); }
        catch (Exception ex) { showMsg("Invalid quantity."); return; }

        if (qty < 1 || qty > stock) {
            showMsg("Quantity must be between 1 and " + stock); return;
        }

        Product product = findProduct(id);
        for (CartItem item : cart) {
            if (item.product.id == id) {
                item.quantity += qty;
                refreshCartTable();
                tabs.setSelectedIndex(1);
                showMsg("✓ Quantity updated in cart!");
                return;
            }
        }
        cart.add(new CartItem(product, qty));
        refreshCartTable();
        tabs.setSelectedIndex(1);
        showMsg("✓ Added to cart!");
    }

    void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { showMsg("Select an item to remove."); return; }
        cart.remove(row);
        refreshCartTable();
    }

    void showPlaceOrderDialog() {
        if (cart.isEmpty()) { showMsg("Your cart is empty!"); return; }

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Calculate totals
        double subtotal = 0;
        for (CartItem i : cart) subtotal += i.product.price * i.quantity;
        double delivery = subtotal >= 499 ? 0 : 49;
        double total    = subtotal + delivery;

        form.add(new JLabel("Subtotal:"));
        form.add(new JLabel("Rs." + (int) subtotal));
        form.add(new JLabel("Delivery:"));
        form.add(new JLabel(delivery == 0 ? "FREE" : "Rs.49"));
        form.add(new JLabel("Grand Total:"));
        JLabel totalLbl = new JLabel("Rs." + (int) total);
        totalLbl.setFont(BOLD); totalLbl.setForeground(ORANGE);
        form.add(totalLbl);

        form.add(new JLabel("Payment Method:"));
        JComboBox<String> payBox = new JComboBox<>(
            new String[]{"UPI", "Credit/Debit Card", "Cash on Delivery"});
        form.add(payBox);

        form.add(new JLabel("Delivery Address:"));
        JTextField addrField = new JTextField("Enter your address");
        form.add(addrField);

        int result = JOptionPane.showConfirmDialog(this, form,
            "Place Order", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String addr = addrField.getText().trim();
            if (addr.isEmpty() || addr.equals("Enter your address")) {
                showMsg("Please enter a delivery address."); return;
            }
            confirmOrder((String) payBox.getSelectedItem(), addr, total);
        }
    }

    void confirmOrder(String payment, String address, double total) {
        StringBuilder sb = new StringBuilder();
        sb.append("ORDER #").append(orders.size() + 1).append("\n");
        sb.append("─────────────────────────────\n");
        sb.append("Payment : ").append(payment).append("\n");
        sb.append("Address : ").append(address).append("\n");
        sb.append("Items   :\n");
        for (CartItem i : cart) {
            sb.append("  • ").append(i.product.name)
              .append(" x").append(i.quantity)
              .append("  = Rs.").append((int)(i.product.price * i.quantity)).append("\n");
            i.product.stock -= i.quantity;  // deduct stock
        }
        sb.append("Total   : Rs.").append((int) total).append("\n");
        sb.append("Status  : ✅ Confirmed\n");
        sb.append("ETA     : 2–5 business days\n");

        orders.add(sb.toString());
        cart.clear();
        refreshCartTable();
        refreshOrdersTab();
        tabs.setSelectedIndex(2);

        JOptionPane.showMessageDialog(this,
            "✅ Order placed!\nAmount: Rs." + (int) total +
            "\nEstimated delivery: 2–5 days",
            "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Refresh Helpers ──────────────────────────────────────

    void refreshProductTable(ArrayList<Product> list) {
        productModel.setRowCount(0);
        for (Product p : list)
            productModel.addRow(new Object[]{
                p.id, p.name, p.category, (int) p.price, p.stock
            });
    }

    void refreshCartTable() {
        cartModel.setRowCount(0);
        double subtotal = 0;
        for (CartItem i : cart) {
            double line = i.product.price * i.quantity;
            subtotal += line;
            cartModel.addRow(new Object[]{
                i.product.name, (int) i.product.price, i.quantity, (int) line
            });
        }
        double delivery = (subtotal > 0 && subtotal < 499) ? 49 : 0;
        totalLabel.setText(String.format(
            "Subtotal: Rs.%d   |   Delivery: %s   |   Total: Rs.%d",
            (int) subtotal,
            delivery == 0 ? "FREE" : "Rs.49",
            (int)(subtotal + delivery)
        ));
    }

    void refreshOrdersTab() {
        if (orders.isEmpty()) {
            ordersArea.setText("No orders yet.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = orders.size() - 1; i >= 0; i--)
            sb.append(orders.get(i)).append("\n");
        ordersArea.setText(sb.toString());
    }

    // ── Utility ──────────────────────────────────────────────

    JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 34));
        return btn;
    }

    void styleTable(JTable table) {
        table.setFont(NORMAL);
        table.setRowHeight(28);
        table.setSelectionBackground(new Color(255, 220, 190));
        table.setSelectionForeground(DARK);
        table.setGridColor(new Color(230, 225, 215));
        table.setShowGrid(true);
        table.getTableHeader().setFont(BOLD);
        table.getTableHeader().setBackground(DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    Product findProduct(int id) {
        for (Product p : catalog) if (p.id == id) return p;
        return null;
    }

    // ── Sample Products ──────────────────────────────────────
    void loadProducts() {
        catalog.add(new Product(1,  "Wireless Headphones", "Electronics", 2999, 50));
        catalog.add(new Product(2,  "Smart Watch",         "Electronics", 4999, 25));
        catalog.add(new Product(3,  "Running Shoes",       "Sports",      3499, 30));
        catalog.add(new Product(4,  "Yoga Mat",            "Sports",       699, 100));
        catalog.add(new Product(5,  "Linen Shirt",         "Fashion",     1299, 80));
        catalog.add(new Product(6,  "Denim Jacket",        "Fashion",     1999, 40));
        catalog.add(new Product(7,  "Vitamin C Serum",     "Beauty",       599, 200));
        catalog.add(new Product(8,  "Matte Lipstick",      "Beauty",       349, 150));
        catalog.add(new Product(9,  "Basmati Rice 5kg",    "Grocery",      649, 500));
        catalog.add(new Product(10, "Organic Almonds",     "Grocery",      399, 300));
    }

    // ── Entry Point ──────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ShoppingAppGUI());
    }
}

```
