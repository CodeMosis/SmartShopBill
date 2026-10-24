import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
 
class Product {
    private final String name, type;
    private final double price, tax, discount;
    private final int stock;
 
    public Product(String name, String type, double price, double tax, double discount, int stock) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.tax = tax;
        this.discount = discount;
        this.stock = stock;
    }
 
    public String getName(){
        return name; 
    }

    public String getType(){
        return type; 
    }

    public double getPrice(){ 
        return price; 
    }
    
    public double getTax(){ 
        return tax; 
    }

    public double getDiscount(){ 
        return discount; 
    }

    public int getStock(){
        return stock; 
    }
}
 
class CartItem {
    private final Product product;
    private int quantity;
 
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
 
    public Product getProduct(){
        return product; 
    }

    public int getQuantity(){
        return quantity; 
    }

    public void setQuantity(int quantity){ 
        this.quantity = quantity; 
    }

    public double getSubtotal(){
        return product.getPrice() * quantity; 
    }

    public double getTaxAmount(){
        return getSubtotal() * (product.getTax() / 100.0);
    }

    public double getDiscountAmount(){
        return getSubtotal() * (product.getDiscount() / 100.0); 
    }

    public double getTotal(){ 
        return getSubtotal() + getTaxAmount() - getDiscountAmount(); 
    }

}
 
public class ProductDemo extends JFrame implements ActionListener {
 
    JLabel l1, l2, l3, result;
    JComboBox<String> type, product;
    JTextField t3;
    JButton add, clear, confirm, edit_qty, remove, checkstock;
    JPanel displayPanel;
    JTable cartTable;
    DefaultTableModel cartModel;
    ArrayList<CartItem> cart = new ArrayList<>();
    JScrollPane billScroll;          
    boolean billGenerated = false;   
 
    private final String url = ("DB_URL");
    private final String user = ("DB_USER");
    private final String pass = ("DB_PASSWORD");
    
    ProductDemo() {

        setTitle("Smart Shopping Cart Billing System");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));
 
        result = new JLabel("");
 
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createTitledBorder(" Add Item to Cart "));
 
        l1 = new JLabel("Type:");
        type = new JComboBox<>(new String[]{"Grocery", "Electronics"});
 
        l2 = new JLabel("Product:");
        product = new JComboBox<>();
 
        loadProducts();
        type.addActionListener(this);
 
        l3 = new JLabel("Enter Quantity:");
        t3 = new JTextField();
 
        type.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        product.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        t3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
 
        add = new JButton("Add to Cart");
        clear = new JButton("Clear");
 
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        formPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        formPanel.add(l1);
        formPanel.add(type);
        formPanel.add(l2);
        formPanel.add(product);
        formPanel.add(l3);
        formPanel.add(t3);
 
        leftPanel.add(formPanel);
        leftPanel.add(Box.createVerticalStrut(20));
 
        JPanel leftButtonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        leftButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        leftButtonPanel.add(add);
        leftButtonPanel.add(clear);
        leftPanel.add(leftButtonPanel);
 
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder(" Cart Summary "));
 
        displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
 
        String[] columns = {"Product", "Quantity", "Price", "Tax", "Discount", "Total"};
        cartModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        cartTable = new JTable(cartModel);
        displayPanel.add(new JScrollPane(cartTable));
 
        rightPanel.add(displayPanel, BorderLayout.CENTER);
 
        confirm = new JButton("Confirm");
        edit_qty = new JButton("Edit Quantity");
        remove = new JButton("Remove an Item");
        checkstock = new JButton("Check Stock");
 
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(checkstock);
        buttonPanel.add(edit_qty);
        buttonPanel.add(remove);
        buttonPanel.add(confirm);
 
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);
 
        result.setBorder(new EmptyBorder(5, 0, 0, 0));
 
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.3);
 
        add(splitPane, BorderLayout.CENTER);
        add(result, BorderLayout.SOUTH);
 
        add.addActionListener(this);
        clear.addActionListener(this);
        confirm.addActionListener(this);
        edit_qty.addActionListener(this);
        remove.addActionListener(this);
        checkstock.addActionListener(this);
 
        setVisible(true);
    }
 
    private Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, pass);
    }
 
    private void loadProducts() {
        product.removeAllItems();
        String query = "SELECT prod_name FROM Product WHERE prod_type = ?";
 
        try{
            Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query);
 
            stmt.setString(1, (String) type.getSelectedItem());
 
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    product.addItem(rs.getString("prod_name"));
                }
            }
        }
        
        catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            result.setText(e.getMessage());
        }
    }
 
    private void showStockWindow() {
        JFrame stockFrame = new JFrame("Available Stock");
        stockFrame.setSize(550, 400);
        stockFrame.setLocationRelativeTo(this);
 
        String[] columns = {"Product ID", "Product Name", "Available Stock"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable stockTable = new JTable(model);
 
        String sql = "SELECT prod_id, prod_name, prod_stock FROM Product";
 
        try{
            Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
 
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("prod_id"),
                    rs.getString("prod_name"),
                    rs.getInt("prod_stock")
                });
            }
        }
        catch (ClassNotFoundException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Unable to load stock: " + ex.getMessage());
            return;
        }
 
        stockFrame.add(new JScrollPane(stockTable));
        stockFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        stockFrame.setVisible(true);
    }
 
    private void refreshRow(int idx) {
        CartItem item = cart.get(idx);
        cartModel.setValueAt(item.getQuantity(), idx, 1);
        cartModel.setValueAt(String.format("%.2f", item.getTaxAmount()), idx, 3);
        cartModel.setValueAt(String.format("%.2f", item.getDiscountAmount()), idx, 4);
        cartModel.setValueAt(String.format("%.2f", item.getTotal()), idx, 5);
    }
 
    private int findInCart(String name) {
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProduct().getName().equals(name)) return i;
        }
        return -1;
    }
 
    private boolean checkBillLocked() {
        if (billGenerated) {
            result.setText("Bill already generated. Press Clear to start a new bill.");
            return true;
        }
        return false;
    }
 
    private boolean deductStock() {
        String sql = "UPDATE Product SET prod_stock = prod_stock - ? " +
                    "WHERE prod_name = ? AND prod_stock >= ?";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (CartItem item : cart) {
                    ps.setInt(1, item.getQuantity());
                    ps.setString(2, item.getProduct().getName());
                    ps.setInt(3, item.getQuantity());
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        result.setText("Insufficient stock for " + item.getProduct().getName() + "!");
                        return false;
                    }
                }
                con.commit();
                return true;
            }
            
            catch (SQLException ex) {
                con.rollback();
                throw ex;
            }

        }
        
        catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            result.setText(ex.getMessage());
            return false;
        }
    }
 
    @Override
    public void actionPerformed(ActionEvent e) {
 
        if (e.getSource() == type) {
            loadProducts();
        }

        else if (e.getSource() == add) {
            if (checkBillLocked()) return;
 
            String query = "SELECT prod_name, prod_type, prod_price, prod_tax, prod_discount, prod_stock " +
                           "FROM Product WHERE prod_type = ? AND prod_name = ?";
 
            try {
                int quantity = Integer.parseInt(t3.getText().trim());
                if (quantity <= 0) {
                    result.setText("Enter a valid quantity!");
                    return;
                }
 
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement(query)) {
 
                    stmt.setString(1, (String) type.getSelectedItem());
                    stmt.setString(2, (String) product.getSelectedItem());
 
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            result.setText("Product not found!");
                            return;
                        }
 
                        int stock = rs.getInt("prod_stock");
                        String name = rs.getString("prod_name");
 
                        int existing = findInCart(name);
                        int alreadyInCart = existing >= 0 ? cart.get(existing).getQuantity() : 0;
 
                        if (alreadyInCart + quantity > stock) {
                            result.setText("Insufficient stock! Available: " + stock);
                            return;
                        }
 
                        if (existing >= 0) {
                            cart.get(existing).setQuantity(alreadyInCart + quantity);
                            refreshRow(existing);
                        }

                        else {
                            Product p = new Product(
                                name,
                                rs.getString("prod_type"),
                                rs.getDouble("prod_price"),
                                rs.getDouble("prod_tax"),
                                rs.getDouble("prod_discount"),
                                stock);
                            CartItem item = new CartItem(p, quantity);
                            cart.add(item);
 
                            cartModel.addRow(new Object[]{
                                p.getName(),
                                item.getQuantity(),
                                String.format("%.2f", p.getPrice()),
                                String.format("%.2f", item.getTaxAmount()),
                                String.format("%.2f", item.getDiscountAmount()),
                                String.format("%.2f", item.getTotal())
                            });
                        }
                        result.setText("Item added to cart!");
                        t3.setText("");
                    }
                }
            }
            catch (ClassNotFoundException | SQLException ex) {
                ex.printStackTrace();
                result.setText(ex.getMessage());
            }
            catch (NumberFormatException ex) {
                result.setText("Enter a valid quantity!");
            }
        }

        else if (e.getSource() == edit_qty) {
            if (checkBillLocked()) return;
 
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow == -1) {
                result.setText("Please select an item from the cart to edit!");
                return;
            }
 
            String input = JOptionPane.showInputDialog(this, "Enter new quantity:");
 
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int newQty = Integer.parseInt(input.trim());
 
                    if (newQty <= 0) {
                        result.setText("Quantity must be greater than 0!");
                        return;
                    }
 
                    CartItem item = cart.get(selectedRow);
                    if (newQty > item.getProduct().getStock()) {
                        result.setText("Insufficient stock! Available: " + item.getProduct().getStock());
                        return;
                    }
 
                    item.setQuantity(newQty);
                    refreshRow(selectedRow);
                    result.setText("Quantity updated successfully!");
                }
                catch (NumberFormatException ex) {
                    result.setText("Please enter a valid number!");
                }
            }
        }

        else if (e.getSource() == remove) {
            if (checkBillLocked()) return;
 
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow != -1) {
                cart.remove(selectedRow);        
                cartModel.removeRow(selectedRow);
                result.setText("Item removed successfully!");
            }
            else {
                result.setText("Please select an item to remove!");
            }
        }
        else if (e.getSource() == confirm) {
            if (billGenerated) {
                result.setText("Bill already generated. Press Clear to start a new bill.");
                return;
            }
            if (cart.isEmpty()) {
                result.setText("Cart is empty!");
                return;
            }
            if (!deductStock()) {
                return;
            }
            billGenerated = true;
 
            JTextArea billArea = new JTextArea();
            billArea.setEditable(false);
            billArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            billArea.setMargin(new Insets(15, 15, 15, 15));
 
            StringBuilder bill = new StringBuilder();
            bill.append("          SMART SHOP\n");
            bill.append("--------------------------------\n");
            bill.append("Product          Qty       Total\n");
            bill.append("--------------------------------\n");
 
            double grandTotal = 0;
            for (CartItem item : cart) {
                bill.append(String.format("%-16s %3d %10.2f%n",
                        item.getProduct().getName(), item.getQuantity(), item.getTotal()));
                grandTotal += item.getTotal();
            }
 
            bill.append("--------------------------------\n");
            bill.append(String.format("Grand Total: Rs. %.2f%n", grandTotal));
            bill.append("\n       Thank you for shopping!");
 
            billArea.setText(bill.toString());
 
            billScroll = new JScrollPane(billArea);
            billScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            billScroll.setPreferredSize(new Dimension(500, 220));
            billScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
 
            displayPanel.add(billScroll);
            displayPanel.revalidate();
            displayPanel.repaint();
 
            result.setText("Bill generated successfully!");
        }

        else if (e.getSource() == checkstock) {
            showStockWindow();
        }

        else if (e.getSource() == clear) {
            t3.setText("");
            result.setText("");
            cart.clear();
            cartModel.setRowCount(0);
            billGenerated = false;
 
            if (billScroll != null) {
                displayPanel.remove(billScroll);
                billScroll = null;
            }
            displayPanel.revalidate();
            displayPanel.repaint();
        }
    }
 
    public static void main(String[] args) {
        new ProductDemo();
    }
}
