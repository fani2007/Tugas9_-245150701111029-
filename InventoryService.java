import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator; 
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class InventoryService {

    /** Buat folder data jika belum ada */
    public void initDataDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectory(dir);
        }
    }

    /** Load daftar produk dari CSV */
    public List<Product> loadProducts(Path csvPath) throws IOException {
        List<Product> products = new ArrayList<>();
        if (Files.exists(csvPath)) {
            List<String> lines = Files.readAllLines(csvPath);
            for (int i = 1; i < lines.size(); i++) {
                products.add(Product.fromCsvLine(lines.get(i)));
            }
        }
        return products;
    }

    /** Simpan daftar produk ke CSV */
    public void saveProducts(List<Product> products, Path csvPath) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("id,name,category,price,quantity");
        for (Product p : products) {
            lines.add(p.toCsvLine());
        }
        Files.write(csvPath, lines);
    }

    /** Pencarian nama produk (substring match) */
    public List<Product> searchProducts(List<Product> products, String keyword) {
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /** Sortir produk berdasarkan "price" atau "quantity" */
    public void sortProducts(List<Product> products, String criteria) {
        if (criteria.equalsIgnoreCase("price")) {
            products.sort(Comparator.comparingDouble(Product::getPrice));
        } else if (criteria.equalsIgnoreCase("quantity")) {
            products.sort(Comparator.comparingInt(Product::getQuantity));
        }
    }

    /** Filter produk berdasarkan rentang harga [min..max] */
    public List<Product> filterByPrice(List<Product> products, double min, double max) {
        return products.stream()
                .filter(p -> p.getPrice() >= min && p.getPrice() <= max)
                .collect(Collectors.toList());
    }

    /** Menu interaktif */
    public void runMenu(List<Product> products) throws IOException {
        Scanner sc = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n=== INVENTORY MANAGER ===");
            System.out.println("1. Lihat semua");
            System.out.println("2. Tambah produk");
            System.out.println("3. Update stok");
            System.out.println("4. Hapus produk");
            System.out.println("5. Cari produk");
            System.out.println("6. Sort produk");
            System.out.println("7. Filter harga");
            System.out.println("8. Simpan & Keluar");
            System.out.print("Pilih: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    viewAll(products);
                    break;
                case "2":
                    addProduct(products, sc);
                    break;
                case "3":
                    updateQuantity(products, sc);
                    break;
                case "4":
                    deleteProduct(products, sc);
                    break;
                case "5":
                    System.out.print("Masukkan kata kunci: ");
                    String keyword = sc.nextLine();
                    List<Product> found = searchProducts(products, keyword);
                    viewAll(found);
                    break;
                case "6":
                    System.out.print("Sort by (price/quantity): ");
                    String criteria = sc.nextLine();
                    sortProducts(products, criteria);
                    viewAll(products);
                    break;
                case "7":
                    System.out.print("Harga minimum: ");
                    double min = Double.parseDouble(sc.nextLine());
                    System.out.print("Harga maksimum: ");
                    double max = Double.parseDouble(sc.nextLine());
                    List<Product> filtered = filterByPrice(products, min, max);
                    viewAll(filtered);
                    break;
                case "8":
                    running = false;
                    break;
                default:
                    System.out.println("Pilihan tidak valid.");
            }
        }
    }

    private void viewAll(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("Tidak ada produk.");
        } else {
            for (Product p : products) {
                System.out.printf("ID: %d | %s | %s | Rp%.2f | Stok: %d\n",
                        p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getQuantity());
            }
        }
    }

    private void addProduct(List<Product> products, Scanner sc) {
        System.out.print("ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("Nama: ");
        String name = sc.nextLine();
        System.out.print("Kategori: ");
        String category = sc.nextLine();
        System.out.print("Harga: ");
        double price = Double.parseDouble(sc.nextLine());
        System.out.print("Kuantitas: ");
        int quantity = Integer.parseInt(sc.nextLine());

        products.add(new Product(id, name, category, price, quantity));
        System.out.println("Produk berhasil ditambahkan.");
    }

    private void updateQuantity(List<Product> products, Scanner sc) {
        System.out.print("ID produk: ");
        int id = Integer.parseInt(sc.nextLine());
        for (Product p : products) {
            if (p.getId() == id) {
                System.out.print("Stok baru: ");
                int qty = Integer.parseInt(sc.nextLine());
                p.setQuantity(qty);
                System.out.println("Stok diperbarui.");
                return;
            }
        }
        System.out.println("Produk tidak ditemukan.");
    }

    private void deleteProduct(List<Product> products, Scanner sc) {
        System.out.print("ID produk: ");
        int id = Integer.parseInt(sc.nextLine());
        Iterator<Product> iterator = products.iterator(); 
        while (iterator.hasNext()) {
            Product p = iterator.next();
            if (p.getId() == id) {
                iterator.remove(); 
                System.out.println("Produk dihapus.");
                return;
            }
        }
        System.out.println("Produk tidak ditemukan.");
    }
}
